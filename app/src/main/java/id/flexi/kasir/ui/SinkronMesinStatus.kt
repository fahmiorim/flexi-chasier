package id.flexi.kasir.ui

import androidx.compose.runtime.Immutable
import id.flexi.kasir.data.sync.SinkronStatusLokal
import id.flexi.kasir.domain.model.SyncStatus
import java.time.format.DateTimeFormatter

/**
 * Status sinkronisasi mesin yang dirender UI (beranda kasir & pengaturan).
 *
 * Dibentuk dari [SinkronStatusLokal] (outbox + metadata hasil mesin sinkronisasi).
 *
 * @property status Status sinkronisasi keseluruhan (Syncing/LocalChanges/Gagal/Never/Synced).
 * @property jumlahPerubahanLokal Banyak perubahan lokal yang belum terkirim ke server.
 * @property waktuSinkronTerakhirEpochMili Waktu siklus sinkronisasi terakhir dicoba.
 * @property apakahSedangBerjalan Menandakan mesin sedang menjalankan satu siklus.
 */
@Immutable
data class SinkronMesinStatus(
    val status: SyncStatus = SyncStatus.Never,
    val jumlahPerubahanLokal: Int = 0,
    val waktuSinkronTerakhirEpochMili: Long? = null,
    val apakahSedangBerjalan: Boolean = false,
)

/** Memetakan status lokal (data layer) ke model tampilan UI. */
fun SinkronStatusLokal.keSinkronMesinStatus(): SinkronMesinStatus = SinkronMesinStatus(
    status = keSyncStatus(),
    jumlahPerubahanLokal = jumlahPerubahanLokal,
    waktuSinkronTerakhirEpochMili = waktuSinkronTerakhirEpochMili,
    apakahSedangBerjalan = apakahSedangBerjalan,
)

/** Judul ringkas status sinkronisasi untuk bar/kartu status. */
fun labelJudulSinkron(status: SinkronMesinStatus): String = when (val s = status.status) {
    SyncStatus.Syncing -> "Menyinkronkan data..."
    SyncStatus.LocalChanges -> "Ada ${status.jumlahPerubahanLokal} perubahan lokal menunggu"
    is SyncStatus.Gagal -> "Sinkronisasi gagal"
    SyncStatus.Synced -> "Data tersinkron"
    SyncStatus.Never -> "Belum pernah disinkronkan"
}

/** Keterangan detail status sinkronisasi (waktu terakhir / pesan error). */
fun labelMetadataSinkron(status: SinkronMesinStatus): String = when (val s = status.status) {
    is SyncStatus.Gagal -> s.pesan.trim().takeIf { pesan -> pesan.isNotBlank() }
        ?: "Terjadi kendala. Coba sinkronkan lagi nanti."
    SyncStatus.LocalChanges -> status.waktuSinkronTerakhirEpochMili?.let { waktu ->
        "Terakhir sinkron ${waktu.sebagaiLabelWaktuSinkronMesin()}"
    } ?: "Perubahan belum dikirim ke server."
    SyncStatus.Synced -> status.waktuSinkronTerakhirEpochMili?.let { waktu ->
        "Terakhir sinkron ${waktu.sebagaiLabelWaktuSinkronMesin()}"
    } ?: "Semua data sudah tersinkron."
    SyncStatus.Syncing -> "Mengirim perubahan & mengambil data terbaru..."
    SyncStatus.Never -> "Sinkronkan untuk mencadangkan data ke server."
}

private fun Long.sebagaiLabelWaktuSinkronMesin(): String =
    java.time.Instant.ofEpochMilli(this)
        .atZone(java.time.ZoneId.systemDefault())
        .format(PEMBENTUK_WAKTU_SINKRON_MESIN)

private val PEMBENTUK_WAKTU_SINKRON_MESIN: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")
