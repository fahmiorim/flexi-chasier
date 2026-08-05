package id.flexi.kasir.data.sync

import id.flexi.kasir.domain.model.SyncStatus
import id.flexi.kasir.ui.SinkronMesinStatus
import id.flexi.kasir.ui.keSinkronMesinStatus
import id.flexi.kasir.ui.labelJudulSinkron
import id.flexi.kasir.ui.labelMetadataSinkron
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Uji logika murni status sinkronisasi mesin:
 * prioritas status, pemetaan ke model UI, dan label yang dirender.
 */
class SinkronStatusPengamatTest {

    @Test
    fun `sedang berjalan selalu berstatus Syncing walau ada antrian`() {
        val status = SinkronStatusLokal(
            apakahSedangBerjalan = true,
            jumlahPerubahanLokal = 5,
            waktuSinkronTerakhirEpochMili = 1_000L,
            apakahSinkronTerakhirBerhasil = true,
        ).keSyncStatus()

        assertEquals(SyncStatus.Syncing, status)
    }

    @Test
    fun `ada antrian lokal jadi LocalChanges walau sinkron terakhir gagal`() {
        val status = SinkronStatusLokal(
            jumlahPerubahanLokal = 3,
            waktuSinkronTerakhirEpochMili = 1_000L,
            apakahSinkronTerakhirBerhasil = false,
            pesanErrorTerakhir = "Jaringan terputus.",
        ).keSyncStatus()

        assertEquals(SyncStatus.LocalChanges, status)
    }

    @Test
    fun `belum pernah sinkron dan tidak ada perubahan jadi Never`() {
        val status = SinkronStatusLokal().keSyncStatus()

        assertEquals(SyncStatus.Never, status)
    }

    @Test
    fun `sinkron terakhir gagal tanpa antrian jadi Gagal dengan pesan`() {
        val status = SinkronStatusLokal(
            waktuSinkronTerakhirEpochMili = 2_000L,
            apakahSinkronTerakhirBerhasil = false,
            pesanErrorTerakhir = "HTTP 500.",
        ).keSyncStatus()

        assertTrue(status is SyncStatus.Gagal)
        assertEquals("HTTP 500.", (status as SyncStatus.Gagal).pesan)
    }

    @Test
    fun `berhasil terakhir dan antrian kosong jadi Synced`() {
        val status = SinkronStatusLokal(
            waktuSinkronTerakhirEpochMili = 3_000L,
            apakahSinkronTerakhirBerhasil = true,
        ).keSyncStatus()

        assertEquals(SyncStatus.Synced, status)
    }

    @Test
    fun `pemetaan ke model UI membawa semua field`() {
        val model = SinkronStatusLokal(
            apakahSedangBerjalan = true,
            jumlahPerubahanLokal = 7,
            waktuSinkronTerakhirEpochMili = 4_000L,
            apakahSinkronTerakhirBerhasil = true,
        ).keSinkronMesinStatus()

        assertEquals(SyncStatus.Syncing, model.status)
        assertEquals(7, model.jumlahPerubahanLokal)
        assertEquals(4_000L, model.waktuSinkronTerakhirEpochMili)
        assertTrue(model.apakahSedangBerjalan)
    }

    @Test
    fun `label judul mengikuti status`() {
        assertEquals(
            "Data tersinkron",
            labelJudulSinkron(SinkronMesinStatus(status = SyncStatus.Synced)),
        )
        assertEquals(
            "Ada 2 perubahan lokal menunggu",
            labelJudulSinkron(
                SinkronMesinStatus(status = SyncStatus.LocalChanges, jumlahPerubahanLokal = 2),
            ),
        )
        assertEquals(
            "Menyinkronkan data...",
            labelJudulSinkron(SinkronMesinStatus(status = SyncStatus.Syncing)),
        )
    }

    @Test
    fun `label metadata gagal memakai pesan error`() {
        val label = labelMetadataSinkron(
            SinkronMesinStatus(status = SyncStatus.Gagal("Token kedaluwarsa.")),
        )
        assertEquals("Token kedaluwarsa.", label)
    }

    @Test
    fun `label metadata tidak pernah sinkron memberi ajakan`() {
        val label = labelMetadataSinkron(SinkronMesinStatus(status = SyncStatus.Never))
        assertTrue(label.isNotBlank())
    }

    @Test
    fun `penanda berjalan masih segar bila usia di bawah ambang`() {
        val sekarang = 10_000L
        val mulai = sekarang - 4 * 60 * 1_000L // 4 menit lalu

        assertTrue(apakahPenandaBerjalanMasihSegar(mulai.toString(), sekarang))
    }

    @Test
    fun `penanda berjalan basi bila usia melewati ambang`() {
        val sekarang = 10_000L
        val mulai = sekarang - 6 * 60 * 1_000L // 6 menit lalu

        assertEquals(false, apakahPenandaBerjalanMasihSegar(mulai.toString(), sekarang))
    }

    @Test
    fun `penanda berjalan null atau kosong berarti tidak berjalan`() {
        assertEquals(false, apakahPenandaBerjalanMasihSegar(null, 10_000L))
        assertEquals(false, apakahPenandaBerjalanMasihSegar("", 10_000L))
    }
}
