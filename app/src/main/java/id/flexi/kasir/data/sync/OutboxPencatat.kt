package id.flexi.kasir.data.sync

import id.flexi.kasir.data.local.database.FlexiKasirDatabase
import id.flexi.kasir.data.local.mapping.keDomain
import id.flexi.kasir.data.network.config.CashierNetworkProvider
import id.flexi.kasir.data.network.model.SetoranSinkron
import id.flexi.kasir.domain.model.Bahan
import id.flexi.kasir.domain.model.CashKas
import id.flexi.kasir.domain.model.CashMutation
import id.flexi.kasir.domain.model.Meja
import id.flexi.kasir.domain.model.MutasiRekening
import id.flexi.kasir.domain.model.PembelianBahan
import id.flexi.kasir.domain.model.PenyesuaianStok
import id.flexi.kasir.domain.model.Produk
import id.flexi.kasir.domain.model.Resep
import id.flexi.kasir.domain.model.Setoran
import id.flexi.kasir.domain.model.StoreSetting
import id.flexi.kasir.domain.model.Transaction
import id.flexi.kasir.domain.model.TransactionStatus
import kotlin.math.max
import kotlinx.coroutines.flow.first
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

/**
 * Mencatat perubahan lokal ke antrian outbox agar bisa di-push ke server.
 *
 * Satu baris outbox per (entitas, itemId) — menulis ulang item yang sama akan
 * mengganti payload dan menaikkan [versi] (monotonik), sesuai aturan
 * last-write-wins di server.
 *
 * Pencatatan bersifat best-effort: bila belum ada gerai aktif, tidak ada
 * pembacaan nama pendukung, atau serialisasi gagal, perubahan tidak dicatat
 * (data lokal tetap tersimpan normal — hanya tidak tersinkron).
 */
class OutboxPencatat(
    private val basisData: FlexiKasirDatabase,
    private val sumberGeraiAktifId: suspend () -> String?,
) {

    private val outboxDao = basisData.OutboxDao()
    private val bahanDao = basisData.BahanDao()
    private val produkDao = basisData.LocalProductDao()
    private val kasDao = basisData.LocalCashDao()
    private val json = CashierNetworkProvider.jsonUtama

    suspend fun catatProduk(produk: Produk, dihapus: Boolean = false) {
        catat(ENTITAS_PRODUK, produk.id) { versi ->
            json.encodeToString(PayloadSinkron.produk(produk, versi, dihapus))
        }
    }

    suspend fun catatTransaksi(transaction: Transaction, dihapus: Boolean = false) {
        // Transaksi Pending (draft/keranjang) belum final — jangan didorong ke
        // server. Saat status berubah ke lunas/processing atau dibatalkan,
        // catatTransaksi dipanggil lagi sehingga versi final tetap ter-push.
        if (transaction.status == TransactionStatus.Pending && !transaction.dibatalkan) return
        // Versi entity dipakai sebagai batas bawah versi outbox: versi entity
        // sudah pasti lebih besar dari versi server terakhir yang diketahui
        // (dinaikkan saat simpan lokal), sehingga push selalu dianggap lebih
        // baru oleh server — bahkan saat jam perangkat tidak sinkron.
        catat(ENTITAS_TRANSAKSI, transaction.id, versiMinimal = transaction.versi) { versi ->
            json.encodeToString(PayloadSinkron.transaksi(transaction, versi, dihapus))
        }
    }

    suspend fun catatMeja(meja: Meja, dihapus: Boolean = false) {
        catat(ENTITAS_MEJA, meja.id) { versi ->
            json.encodeToString(PayloadSinkron.meja(meja, versi, dihapus))
        }
    }

    suspend fun catatShiftKas(kas: CashKas, dihapus: Boolean = false) {
        catat(ENTITAS_SHIFT_KAS, kas.id) { versi ->
            json.encodeToString(PayloadSinkron.shiftKas(kas, versi, dihapus))
        }
    }

    suspend fun catatMutasiKas(mutasi: CashMutation, dihapus: Boolean = false) {
        catat(ENTITAS_MUTASI_KAS, mutasi.id) { versi ->
            json.encodeToString(PayloadSinkron.mutasiKas(mutasi, versi, dihapus))
        }
    }

    /**
     * Setoran butuh shift sebagai induk. Prioritaskan shift aktif; bila shift
     * sudah ditutup, pakai shiftId dari entri outbox lama (agar edit/hapus
     * setoran tetap tersinkron). Tanpa keduanya, tidak dicatat.
     */
    suspend fun catatSetoran(setoran: Setoran) {
        val shiftId = kasDao.amatiKasAktif().first()?.id
            ?: ambilShiftIdDariOutbox(setoran.id)
            ?: return
        catat(ENTITAS_SETORAN, setoran.id) { versi ->
            json.encodeToString(PayloadSinkron.setoran(setoran, shiftId, versi))
        }
    }

    private suspend fun ambilShiftIdDariOutbox(itemId: String): String? {
        val payload = outboxDao.ambilPayload(ENTITAS_SETORAN, itemId) ?: return null
        return runCatching {
            json.decodeFromString<SetoranSinkron>(payload).shiftId
        }.getOrNull()
    }

    suspend fun catatBahan(bahan: Bahan, dihapus: Boolean = false) {
        catat(ENTITAS_BAHAN, bahan.id) { versi ->
            json.encodeToString(PayloadSinkron.bahan(bahan, versi, dihapus))
        }
    }

    suspend fun catatPembelianBahan(pembelian: PembelianBahan, dihapus: Boolean = false) {
        val namaBahan = bahanDao.ambilBahanBerdasarkanId(pembelian.bahanId)?.nama ?: return
        catat(ENTITAS_PEMBELIAN_BAHAN, pembelian.id) { versi ->
            json.encodeToString(PayloadSinkron.pembelianBahan(pembelian, namaBahan, versi, dihapus))
        }
    }

    /**
     * Pengaturan toko bersifat satu baris per gerai: id deterministik dari
     * gerai aktif agar stabil lintas perangkat dan unik antar gerai
     * (server memakai id sebagai primary key).
     */
    suspend fun catatPengaturanToko(pengaturan: StoreSetting) {
        val geraiId = sumberGeraiAktifId() ?: return
        val idPengaturan = idPengaturanToko(geraiId)
        catat(ENTITAS_PENGATURAN_TOKO, idPengaturan) { versi ->
            json.encodeToString(PayloadSinkron.pengaturanToko(pengaturan, idPengaturan, versi))
        }
    }

    suspend fun catatResep(resep: Resep, dihapus: Boolean = false) {
        val namaProduk = produkDao
            .ambilProdukBerdasarkanDaftarIdentitas(listOf(resep.produkId))
            .firstOrNull()
            ?.keDomain()
            ?.nama ?: "Produk"
        val namaBahan: Map<String, String> = resep.daftarBahan.mapNotNull { bahanResep ->
            bahanDao.ambilBahanBerdasarkanId(bahanResep.bahanId)?.let { it.id to it.nama }
        }.toMap()

        catat(ENTITAS_RESEP, resep.id) { versi ->
            json.encodeToString(PayloadSinkron.resep(resep, namaProduk, namaBahan, versi, dihapus))
        }
    }

    suspend fun catatPenyesuaianStok(penyesuaian: PenyesuaianStok, dihapus: Boolean = false) {
        catat(ENTITAS_PENYESUAIAN_STOK, penyesuaian.id) { versi ->
            json.encodeToString(PayloadSinkron.penyesuaianStok(penyesuaian, versi, dihapus))
        }
    }

    suspend fun catatMutasiRekening(mutasi: MutasiRekening, dihapus: Boolean = false) {
        catat(ENTITAS_MUTASI_REKENING, mutasi.id) { versi ->
            json.encodeToString(PayloadSinkron.mutasiRekening(mutasi, versi, dihapus))
        }
    }

    /** Menulis satu baris outbox (insert/timpa) dengan versi monotonik. */
    private suspend fun catat(
        entitas: String,
        itemId: String,
        versiMinimal: Long = 0L,
        buatPayload: (versi: Long) -> String,
    ) {
        val geraiId = sumberGeraiAktifId() ?: return
        val versiLama = outboxDao.ambilVersi(entitas, itemId)
        val versi = max(
            PayloadSinkron.hitungVersiBaru(versiLama, System.currentTimeMillis()),
            versiMinimal,
        )
        val payload = try {
            buatPayload(versi)
        } catch (_: Exception) {
            return
        }
        outboxDao.tulis(entitas, itemId, geraiId, versi, payload, System.currentTimeMillis())
    }

    companion object {
        const val ENTITAS_PRODUK = "produk"
        const val ENTITAS_TRANSAKSI = "transaksi"
        const val ENTITAS_MEJA = "meja"
        const val ENTITAS_SHIFT_KAS = "shift-kas"
        const val ENTITAS_MUTASI_KAS = "mutasi-kas"
        const val ENTITAS_SETORAN = "setoran"
        const val ENTITAS_BAHAN = "bahan"
        const val ENTITAS_PEMBELIAN_BAHAN = "pembelian-bahan"
        const val ENTITAS_RESEP = "resep"
        const val ENTITAS_PENGATURAN_TOKO = "pengaturan-toko"
        const val ENTITAS_PENYESUAIAN_STOK = "penyesuaian-stok"
        const val ENTITAS_MUTASI_REKENING = "mutasi-rekening"

        /** ID deterministik pengaturan toko per gerai (satu baris per gerai). */
        fun idPengaturanToko(geraiId: String): String = "pengaturan-toko-$geraiId"
    }
}
