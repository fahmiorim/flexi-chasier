package id.flexi.kasir.data.sync

import androidx.room.withTransaction
import id.flexi.kasir.data.local.database.FlexiCashierDatabase
import id.flexi.kasir.data.local.entity.OutboxSinkronEntity
import id.flexi.kasir.data.network.config.CashierNetworkProvider
import id.flexi.kasir.data.network.model.PushBahanRequest
import id.flexi.kasir.data.network.model.PushMejaRequest
import id.flexi.kasir.data.network.model.PushMutasiKasRequest
import id.flexi.kasir.data.network.model.PushPembelianBahanRequest
import id.flexi.kasir.data.network.model.PushPengaturanTokoRequest
import id.flexi.kasir.data.network.model.PengaturanTokoSinkron
import id.flexi.kasir.data.network.model.PushProdukRequest
import id.flexi.kasir.data.network.model.PushResepRequest
import id.flexi.kasir.data.network.model.PushResponse
import id.flexi.kasir.data.network.model.PushSetoranRequest
import id.flexi.kasir.data.network.model.PushShiftKasRequest
import id.flexi.kasir.data.network.model.PushTransaksiRequest
import id.flexi.kasir.data.network.service.SyncNetworkService
import id.flexi.kasir.domain.repository.RepositoriStoreSetting
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.decodeFromString
import retrofit2.HttpException

/**
 * Hasil satu siklus sinkronisasi untuk gerai aktif.
 */
data class HasilSinkronisasi(
    val geraiId: String?,
    val dorongDiterima: Int = 0,
    val tarikIterasi: Int = 0,
    val kodeError: Int? = null,
    val pesanError: String? = null,
) {
    val berhasil: Boolean get() = kodeError == null
}

/**
 * Mesin sinkronisasi dua arah dengan backend:
 * 1. DORONG — mengirim antrian outbox per entitas (induk didahulukan agar FK aman).
 * 2. TARIK — memuat perubahan semua entitas sejak kursor terakhir, berulang
 *    selama respons menyatakan [PerubahanResponse.terpotong], lalu menyimpan
 *    kursor baru (waktu server) untuk pull berikutnya.
 *
 * Seluruh endpoint memakai klien ber-AuthInterceptor (Bearer + refresh 401).
 */
class MesinSinkronisasi(
    private val basisData: FlexiCashierDatabase,
    private val layanan: SyncNetworkService,
    private val sumberGeraiAktifId: suspend () -> String?,
    private val repositoriStoreSetting: RepositoriStoreSetting,
) {

    private val outboxDao = basisData.OutboxDao()
    private val metaDao = basisData.SinkronMetaDao()
    private val json = CashierNetworkProvider.jsonUtama

    private val produkDao = basisData.LocalProductDao()
    private val transaksiDao = basisData.LocalTransactionDao()
    private val mejaDao = basisData.LocalTableDao()
    private val kasDao = basisData.LocalCashDao()
    private val bahanDao = basisData.BahanDao()

    /**
     * Kunci bersama agar hanya SATU siklus sinkronisasi berjalan pada satu waktu
     * (worker periodik + tombol manual bisa terpicu bersamaan).
     */
    private val mutex = Mutex()

    /** Menjalankan sinkronisasi penuh untuk gerai aktif (serial, tidak dobel). */
    suspend fun sinkronkanGeraiAktif(): HasilSinkronisasi = mutex.withLock {
        sinkronkanGeraiAktifDilindungi()
    }

    private suspend fun sinkronkanGeraiAktifDilindungi(): HasilSinkronisasi {
        val geraiId = sumberGeraiAktifId()
        if (geraiId.isNullOrBlank()) {
            return HasilSinkronisasi(geraiId = null)
        }
        // Tandai sedang berjalan dengan WAKTU MULAI. UI menganggap "berjalan"
        // hanya bila waktu tersebut masih segar — jika proses mati di tengah
        // siklus, status tidak akan macet "Menyinkronkan..." selamanya.
        metaDao.simpan(KUNCI_SEDANG_BERJALAN, System.currentTimeMillis().toString())
        return try {
            val dorong = dorongSemua(geraiId)
            val tarik = tarikSemua(geraiId)
            // Bersihkan entri yang sudah Gagal agar tabel outbox tidak membengkak.
            outboxDao.bersihkanGagal()
            val hasil = HasilSinkronisasi(geraiId = geraiId, dorongDiterima = dorong, tarikIterasi = tarik)
            catatHasil(hasil)
            hasil
        } catch (kesalahan: HttpException) {
            val hasil = HasilSinkronisasi(
                geraiId = geraiId,
                kodeError = kesalahan.code(),
                pesanError = "Sinkronisasi gagal (HTTP ${kesalahan.code()}).",
            )
            catatHasil(hasil)
            hasil
        } catch (kesalahan: Exception) {
            val hasil = HasilSinkronisasi(
                geraiId = geraiId,
                pesanError = kesalahan.message ?: "Sinkronisasi gagal.",
            )
            catatHasil(hasil)
            hasil
        } finally {
            metaDao.simpan(KUNCI_SEDANG_BERJALAN, "")
        }
    }

    /**
     * Menyimpan hasil siklus sinkronisasi ke [SinkronMetaDao] agar bisa
     * diamati UI (waktu terakhir, berhasil/gagal, pesan error).
     */
    private suspend fun catatHasil(hasil: HasilSinkronisasi) {
        metaDao.simpan(KUNCI_WAKTU_TERAKHIR, System.currentTimeMillis().toString())
        metaDao.simpan(KUNCI_BERHASIL_TERAKHIR, if (hasil.berhasil) "1" else "0")
        metaDao.simpan(KUNCI_PESAN_TERAKHIR, hasil.pesanError ?: "")
    }

    /** Jumlah perubahan lokal yang masih menunggu dikirim. */
    suspend fun hitungAntrian(): Int = outboxDao.hitungAntri()

    // ═══════════════════════════════════════
    // DORONG
    // ═══════════════════════════════════════

    private suspend fun dorongSemua(geraiId: String): Int {
        // Urutan induk dulu agar FK di server aman:
        // shift sebelum mutasi/setoran, bahan sebelum pembelian/bahanResep,
        // produk sebelum resep.
        var diterima = 0
        diterima += dorongSatu(geraiId, OutboxPencatat.ENTITAS_PRODUK) { g, items ->
            layanan.dorongProduk(PushProdukRequest(g, items))
        }
        diterima += dorongSatu(geraiId, OutboxPencatat.ENTITAS_BAHAN) { g, items ->
            layanan.dorongBahan(PushBahanRequest(g, items))
        }
        diterima += dorongSatu(geraiId, OutboxPencatat.ENTITAS_MEJA) { g, items ->
            layanan.dorongMeja(PushMejaRequest(g, items))
        }
        diterima += dorongSatu(geraiId, OutboxPencatat.ENTITAS_SHIFT_KAS) { g, items ->
            layanan.dorongShiftKas(PushShiftKasRequest(g, items))
        }
        diterima += dorongSatu(geraiId, OutboxPencatat.ENTITAS_TRANSAKSI) { g, items ->
            layanan.dorongTransaksi(PushTransaksiRequest(g, items))
        }
        diterima += dorongSatu(geraiId, OutboxPencatat.ENTITAS_RESEP) { g, items ->
            layanan.dorongResep(PushResepRequest(g, items))
        }
        diterima += dorongSatu(geraiId, OutboxPencatat.ENTITAS_PEMBELIAN_BAHAN) { g, items ->
            layanan.dorongPembelianBahan(PushPembelianBahanRequest(g, items))
        }
        diterima += dorongSatu(geraiId, OutboxPencatat.ENTITAS_SETORAN) { g, items ->
            layanan.dorongSetoran(PushSetoranRequest(g, items))
        }
        diterima += dorongSatu(geraiId, OutboxPencatat.ENTITAS_MUTASI_KAS) { g, items ->
            layanan.dorongMutasiKas(PushMutasiKasRequest(g, items))
        }
        diterima += dorongSatu(geraiId, OutboxPencatat.ENTITAS_PENGATURAN_TOKO) { g, items ->
            layanan.dorongPengaturanToko(PushPengaturanTokoRequest(g, items))
        }
        return diterima
    }

    /**
     * Mengirim satu batch antrian (maks 100) untuk satu jenis entitas.
     *
     * - Payload tak terbaca → baris ditandai Gagal.
     * - HTTP 2xx dengan `diterima >= total` → semua baris selesai.
     * - HTTP 2xx dengan `diterima < total` → ada item ditolak LWW/induk belum
     *   ada: baris tetap Antri dan percobaan ditambah (retry batch berikutnya).
     * - HTTP 401/403 → hentikan seluruh sinkronisasi (sesi/akses bermasalah).
     * - Gagal lain → baris tetap Antri (retry saat WorkManager berikutnya).
     */
    private suspend inline fun <reified T> dorongSatu(
        geraiId: String,
        entitas: String,
        kirim: suspend (geraiId: String, items: List<T>) -> PushResponse,
    ): Int {
        val daftar = outboxDao.ambilAntriPerEntitas(entitas, BATCH_MAKS)
        if (daftar.isEmpty()) return 0

        val siapKirim = mutableListOf<Pair<OutboxSinkronEntity, T>>()
        for (baris in daftar) {
            try {
                siapKirim += baris to json.decodeFromString<T>(baris.payload)
            } catch (_: Exception) {
                outboxDao.tandaiGagal(baris.id, "Payload tidak dapat dibaca.")
            }
        }
        if (siapKirim.isEmpty()) return 0

        val hasil = try {
            kirim(geraiId, siapKirim.map { it.second })
        } catch (kesalahan: HttpException) {
            val kode = kesalahan.code()
            if (kode == 401 || kode == 403) throw kesalahan
            siapKirim.forEach { outboxDao.tambahPercobaan(it.first.id) }
            throw kesalahan
        } catch (kesalahan: Exception) {
            siapKirim.forEach { outboxDao.tambahPercobaan(it.first.id) }
            throw kesalahan
        }

        return if (hasil.total > 0 && hasil.diterima >= hasil.total) {
            siapKirim.forEach { outboxDao.tandaiBerhasil(it.first.id) }
            hasil.diterima
        } else {
            // Ditolak sebagian: mungkin versi lebih tua (aman diabaikan) atau
            // induk belum ter-push — coba lagi pada siklus berikutnya.
            siapKirim.forEach { (baris, _) ->
                outboxDao.tambahPercobaan(baris.id)
                if (baris.jumlahPercobaan + 1 >= MAKS_PERCOBAAN) {
                    outboxDao.tandaiGagal(baris.id, "Ditolak server (versi lebih tua / induk belum tersedia).")
                }
            }
            hasil.diterima
        }
    }

    // ═══════════════════════════════════════
    // TARIK
    // ═══════════════════════════════════════

    private suspend fun tarikSemua(geraiId: String): Int {
        // Satu kursor keyset ("<epochMili>:<id>") per entitas di meta_sinkron.
        var kursor = ENTITAS_PULL.associateWith { entitas ->
            metaDao.ambil("$PREFIX_KURSOR:$geraiId:$entitas") ?: KURSOR_AWAL
        }
        var iterasi = 0
        var pengaturanTokoTerbaik: PengaturanTokoSinkron? = null

        while (iterasi < MAKS_ITERASI_PULL) {
            val respons = layanan.ambilPerubahan(
                geraiId = geraiId,
                kursor = kursor,
                batas = BATCH_PULL,
            )
            terapkan(respons.kePerubahanLokal(geraiId))
            iterasi++

            // Kursor per entitas maju HANYA sejauh baris yang benar-benar ditarik
            // (gap-free). Entitas yang terpotong tidak melompati datanya, jadi
            // batch berikutnya melanjutkan dari baris terakhir yang diterima.
            respons.kursorBaru.forEach { (entitas, kursorBaru) ->
                if (kursorBaru.isNotBlank()) {
                    kursor = kursor + (entitas to kursorBaru)
                }
            }

            // storeSettings sekarang dikirim incremental (hanya yang berubah sejak
            // kursor). Terapkan baris ber-`versi` tertinggi SEKALI setelah seluruh
            // batch selesai (last-write-wins).
            respons.storeSettings.forEach { s ->
                if (pengaturanTokoTerbaik == null || s.versi > pengaturanTokoTerbaik!!.versi) {
                    pengaturanTokoTerbaik = s
                }
            }

            if (!respons.terpotong) break
        }

        // Pengaturan toko (DataStore, penyimpanan terpisah) — sekali per siklus.
        pengaturanTokoTerbaik?.let { terapkanPengaturanToko(it) }

        kursor.forEach { (entitas, kursorBaru) ->
            metaDao.simpan("$PREFIX_KURSOR:$geraiId:$entitas", kursorBaru)
        }
        return iterasi
    }

    /**
     * Menerapkan perubahan hasil pull:
     * 1. Entitas Room dalam SATU transaksi (induk dulu, pembersihan anak dulu).
     * 2. Pengaturan toko ke DataStore (penyimpanan terpisah dari Room, jadi
     *    diterapkan DI LUAR transaksi Room).
     */
    private suspend fun terapkan(perubahan: PerubahanLokal) {
        basisData.withTransaction {
            // ── 1. Induk ──
            if (perubahan.produk.isNotEmpty()) {
                // Pertahankan favorit lokal: simpanBanyakProduk memakai REPLACE
                // yang akan menimpa favorit dengan nilai dari server.
                val idFavoritLokal = produkDao
                    .ambilProdukBerdasarkanDaftarIdentitas(perubahan.produk.map { it.id })
                    .filter { it.favorit }
                    .map { it.id }

                produkDao.simpanBanyakProduk(perubahan.produk)

                if (idFavoritLokal.isNotEmpty()) {
                    produkDao.tandaiSebagaiFavorit(idFavoritLokal)
                }
            }
            perubahan.bahan.forEach { bahanDao.simpanBahan(it) }
            perubahan.meja.forEach { mejaDao.SaveTable(it) }
            perubahan.shift.forEach { kasDao.simpanKas(it) }

            // ── 2. Transaksi + item (item diganti total per transaksi) ──
            if (perubahan.transaksi.isNotEmpty()) {
                perubahan.transaksi.forEach { transaksi ->
                    // Server TIDAK menyimpan field khusus-lokal (potongan, pajak,
                    // biaya layanan, status, tipe pesanan, meja, nomor antrian, dll).
                    // Saat pull menimpa transaksi yang SUDAH ada secara lokal, field
                    // itu dipertahankan agar tidak ter-reset ke nilai default kosong.
                    val adaLokal = transaksiDao
                        .ambilTransactionBerdasarkanId(transaksi.id)
                        ?.Transaction
                    val hasilGabung = if (adaLokal == null) {
                        transaksi
                    } else {
                        transaksi.copy(
                            potongan = adaLokal.potongan,
                            biayaLayanan = adaLokal.biayaLayanan,
                            pajak = adaLokal.pajak,
                            catatan = adaLokal.catatan,
                            status = adaLokal.status,
                            OrderType = adaLokal.OrderType,
                            nomorAntrian = adaLokal.nomorAntrian,
                            mejaId = adaLokal.mejaId,
                            waktuDiprosesEpochMili = adaLokal.waktuDiprosesEpochMili,
                            waktuSelesaiEpochMili = adaLokal.waktuSelesaiEpochMili,
                            alasanPembatalan = adaLokal.alasanPembatalan,
                        )
                    }
                    transaksiDao.simpanTransactionDenganItem(
                        Transaction = hasilGabung,
                        daftarItem = perubahan.itemTransaksi.filter { it.TransactionId == transaksi.id },
                    )
                }
            }

            // ── 3. Resep + bahan resep (filter induk & bahan) ──
            if (perubahan.resep.isNotEmpty()) {
                val idProdukAda = produkDao
                    .ambilProdukBerdasarkanDaftarIdentitas(perubahan.resep.map { it.produkId })
                    .map { it.id }
                    .toSet()
                // Hanya resep yang induk (produk)nya benar-benar ada yang di-insert,
                // dan idResepAda diambil DARI resep yang berhasil di-insert —
                // bila tidak, bahan_resep bisa mereferensikan resep yang tidak ada
                // dan memicu kegagalan FK yang mengulang (macet) setiap pull.
                val resepTervalidasi = perubahan.resep.filter { it.produkId in idProdukAda }
                resepTervalidasi.forEach { resep -> bahanDao.simpanResep(resep) }

                val idResepAda = resepTervalidasi.map { it.id }.toSet()
                val idBahanAda = bahanDao
                    .ambilIdBahanBerdasarkanDaftarId(perubahan.resepBahan.map { it.bahanId })
                    .toSet()
                perubahan.resepBahan
                    .filter { it.resepId in idResepAda && it.bahanId in idBahanAda }
                    .forEach { bahanDao.simpanBahanResep(it) }
            }

            // ── 4. Pembelian (filter induk bahan) ──
            if (perubahan.pembelian.isNotEmpty()) {
                val idBahanAda = bahanDao
                    .ambilIdBahanBerdasarkanDaftarId(perubahan.pembelian.map { it.bahanId })
                    .toSet()
                perubahan.pembelian.filter { it.bahanId in idBahanAda }.forEach { pembelian ->
                    bahanDao.simpanPembelian(pembelian)
                }
            }

            // ── 5. Setoran & mutasi (setoran tanpa induk; mutasi filter shift) ──
            perubahan.setoran.forEach { kasDao.simpanSetoran(it) }

            if (perubahan.mutasi.isNotEmpty()) {
                val idShiftAda = perubahan.mutasi
                    .map { it.shiftId }
                    .distinct()
                    .filter { id -> kasDao.ambilKasBerdasarkanId(id) != null }
                    .toSet()
                perubahan.mutasi.filter { it.shiftId in idShiftAda }.forEach { mutasi ->
                    kasDao.simpanMutasi(mutasi)
                }
            }

            // ── 6. Pembersihan item yang dihapus server (anak dulu) ──
            perubahan.resepDihapus.forEach { bahanDao.hapusResep(it) } // cascade bahan_resep
            perubahan.transaksiDihapus.forEach { transaksiDao.hapusTransactionBerdasarkanId(it) } // cascade item
            perubahan.pembelianDihapus.forEach { bahanDao.hapusPembelian(it) }
            perubahan.mutasiDihapus.forEach { kasDao.hapusMutasi(it) }
            perubahan.setoranDihapus.forEach { kasDao.hapusSetoran(it) } // soft delete lokal
            perubahan.shiftDihapus.forEach { kasDao.hapusKas(it) } // cascade mutasi
            perubahan.bahanDihapus.forEach { bahanDao.hapusBahan(it) } // cascade pembelian & bahan_resep
            perubahan.mejaDihapus.forEach { mejaDao.DeleteTable(it) }
            perubahan.produkDihapus.forEach { produkDao.DeleteProduct(it) }
        }
    }

    /**
     * Menimpa field toko (nama usaha/alamat/tagline/logo) dari server ke
     * DataStore, mempertahankan pengaturan khusus perangkat (printer, struk, dll).
     * Gagal menulis DataStore tidak boleh menggagalkan seluruh pull.
     */
    private suspend fun terapkanPengaturanToko(dariServer: PengaturanTokoSinkron) {
        try {
            val saatIni = repositoriStoreSetting.ambilPengaturan().first()
            repositoriStoreSetting.simpanPengaturan(dariServer.terapkanKe(saatIni))
        } catch (kesalahan: kotlinx.coroutines.CancellationException) {
            throw kesalahan
        } catch (_: Exception) {
            // Abaikan — pull data lain tetap berhasil.
        }
    }

    companion object {
        private const val BATCH_MAKS = 100
        private const val BATCH_PULL = 500
        private const val MAKS_ITERASI_PULL = 30
        private const val MAKS_PERCOBAAN = 3

        /** Awalan kunci meta kursor pull per entitas. */
        private const val PREFIX_KURSOR = "pull_terakhir"

        /** Format kursor awal: dari waktu 0 (tarik semua data). */
        private const val KURSOR_AWAL = "0:"

        /** Entitas yang ditarik, masing-masing dengan kursor keyset sendiri. */
        private val ENTITAS_PULL = listOf(
            "produk", "transaksi", "meja", "shiftKas", "mutasiKas",
            "setoran", "bahan", "pembelianBahan", "resep", "pengaturanToko",
        )

        /** Kunci meta: epoch mili saat siklus dimulai (kosong bila tidak berjalan). */
        const val KUNCI_SEDANG_BERJALAN = "sinkron_sedang_berjalan"

        /** Kunci meta: epoch mili sinkronisasi terakhir yang dicoba. */
        const val KUNCI_WAKTU_TERAKHIR = "sinkron_terakhir_waktu"

        /** Kunci meta: "1" jika siklus terakhir berhasil, "0" jika gagal. */
        const val KUNCI_BERHASIL_TERAKHIR = "sinkron_terakhir_berhasil"

        /** Kunci meta: pesan error siklus terakhir (kosong jika berhasil). */
        const val KUNCI_PESAN_TERAKHIR = "sinkron_terakhir_pesan"
    }
}
