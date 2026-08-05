package id.flexi.kasir.data.sync

import id.flexi.kasir.data.local.database.FlexiCashierDatabase
import id.flexi.kasir.data.local.mapping.keDomain
import id.flexi.kasir.data.network.config.CashierNetworkProvider
import id.flexi.kasir.data.network.model.SetoranSinkron
import id.flexi.kasir.domain.model.Bahan
import id.flexi.kasir.domain.model.CashKas
import id.flexi.kasir.domain.model.CashMutation
import id.flexi.kasir.domain.model.Meja
import id.flexi.kasir.domain.model.PembelianBahan
import id.flexi.kasir.domain.model.Produk
import id.flexi.kasir.domain.model.Resep
import id.flexi.kasir.domain.model.Setoran
import id.flexi.kasir.domain.model.Transaction
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
    private val basisData: FlexiCashierDatabase,
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
        catat(ENTITAS_TRANSAKSI, transaction.id) { versi ->
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

    /** Menulis satu baris outbox (insert/timpa) dengan versi monotonik. */
    private suspend fun catat(
        entitas: String,
        itemId: String,
        buatPayload: (versi: Long) -> String,
    ) {
        val geraiId = sumberGeraiAktifId() ?: return
        val versiLama = outboxDao.ambilVersi(entitas, itemId)
        val versi = PayloadSinkron.hitungVersiBaru(versiLama, System.currentTimeMillis())
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
    }
}
