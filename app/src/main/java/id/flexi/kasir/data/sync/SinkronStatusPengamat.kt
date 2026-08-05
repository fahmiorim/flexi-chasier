package id.flexi.kasir.data.sync

import id.flexi.kasir.data.local.database.FlexiCashierDatabase
import id.flexi.kasir.domain.model.SyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Status sinkronisasi lokal yang siap dirender UI.
 *
 * Digabung dari dua sumber:
 * 1. Jumlah antrian outbox (perubahan lokal yang belum terkirim).
 * 2. Metadata hasil siklus terakhir yang ditulis [MesinSinkronisasi]
 *    (sedang berjalan, waktu, berhasil/gagal, pesan error).
 */
data class SinkronStatusLokal(
    val apakahSedangBerjalan: Boolean = false,
    val jumlahPerubahanLokal: Int = 0,
    val waktuSinkronTerakhirEpochMili: Long? = null,
    val apakahSinkronTerakhirBerhasil: Boolean = false,
    val pesanErrorTerakhir: String? = null,
) {
    /**
     * Menyimpulkan [SyncStatus] untuk UI dengan prioritas:
     * Sedang berjalan > ada perubahan lokal > gagal terakhir > belum pernah > tersinkron.
     */
    fun keSyncStatus(): SyncStatus = when {
        apakahSedangBerjalan -> SyncStatus.Syncing
        jumlahPerubahanLokal > 0 -> SyncStatus.LocalChanges
        waktuSinkronTerakhirEpochMili == null -> SyncStatus.Never
        !apakahSinkronTerakhirBerhasil -> SyncStatus.Gagal(
            pesan = pesanErrorTerakhir ?: "Sinkronisasi gagal.",
        )
        else -> SyncStatus.Synced
    }
}

/**
 * Mengamati status sinkronisasi mesin ([MesinSinkronisasi]) secara reaktif.
 *
 * - [status] mengalirkan [SinkronStatusLokal] yang terbarui setiap ada
 *   perubahan antrian outbox atau metadata hasil sinkronisasi.
 * - [sinkronkanSekarang] menjalankan satu siklus sinkronisasi penuh;
 *   hasilnya otomatis terlihat lewat [status] (mesin menulis metadata).
 */
class SinkronStatusPengamat(
    basisData: FlexiCashierDatabase,
    private val mesin: MesinSinkronisasi,
) {

    private val outboxDao = basisData.OutboxDao()
    private val metaDao = basisData.SinkronMetaDao()

    val status: Flow<SinkronStatusLokal> = combine(
        outboxDao.observasiHitungAntri(),
        metaDao.observasi(MesinSinkronisasi.KUNCI_SEDANG_BERJALAN),
        metaDao.observasi(MesinSinkronisasi.KUNCI_WAKTU_TERAKHIR),
        metaDao.observasi(MesinSinkronisasi.KUNCI_BERHASIL_TERAKHIR),
        metaDao.observasi(MesinSinkronisasi.KUNCI_PESAN_TERAKHIR),
    ) { antri, mulaiBerjalan, waktuTerakhir, berhasilTerakhir, pesanTerakhir ->
        SinkronStatusLokal(
            // Hanya dianggap berjalan bila waktu mulai masih segar — bila proses
            // mati di tengah siklus, flag tidak akan macet "Menyinkronkan...".
            apakahSedangBerjalan = apakahPenandaBerjalanMasihSegar(mulaiBerjalan),
            jumlahPerubahanLokal = antri,
            waktuSinkronTerakhirEpochMili = waktuTerakhir?.toLongOrNull(),
            apakahSinkronTerakhirBerhasil = berhasilTerakhir == "1",
            pesanErrorTerakhir = pesanTerakhir?.takeIf { pesan -> pesan.isNotBlank() },
        )
    }

    /** Menjalankan satu siklus sinkronisasi penuh untuk gerai aktif. */
    suspend fun sinkronkanSekarang(): HasilSinkronisasi = mesin.sinkronkanGeraiAktif()

}

/** Batas usia penanda "sedang berjalan" sebelum dianggap basi (5 menit). */
internal const val AMBANG_BERJALAN_MILI = 5 * 60 * 1_000L

/**
 * Menentukan apakah penanda "sedang berjalan" (epoch mili saat siklus dimulai)
 * masih segar. Nilai null/kosong berarti tidak berjalan; nilai basi (mis. proses
 * mati di tengah siklus) juga dianggap tidak berjalan.
 */
internal fun apakahPenandaBerjalanMasihSegar(
    mulaiEpochMili: String?,
    sekarangEpochMili: Long = System.currentTimeMillis(),
): Boolean {
    val mulai = mulaiEpochMili?.toLongOrNull() ?: return false
    return sekarangEpochMili - mulai < AMBANG_BERJALAN_MILI
}
