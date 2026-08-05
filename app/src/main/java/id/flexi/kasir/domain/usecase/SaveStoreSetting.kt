package id.flexi.kasir.domain.usecase

import id.flexi.kasir.data.sync.OutboxPencatat
import id.flexi.kasir.domain.model.StoreSetting
import id.flexi.kasir.domain.repository.RepositoriStoreSetting
import kotlinx.coroutines.flow.first

class SimpanStoreSetting(
    private val repositoriStoreSetting: RepositoriStoreSetting,
    private val pencatatOutbox: OutboxPencatat? = null,
) {
    suspend operator fun invoke(pengaturan: StoreSetting) {
        repositoriStoreSetting.simpanPengaturan(pengaturan)
        // Catat ke antrian outbox hanya bila ada field yang dibagikan lintas
        // perangkat (nama usaha/alamat/tagline/logo) yang benar-benar berubah.
        // Toggle tampilan katalog (field perangkat) tidak memicu push no-op
        // sekaligus tidak menaikkan versi LWW tanpa alasan.
        // Best-effort: gagal mencatat tidak menggagalkan penyimpanan lokal.
        // Catatan: jalur pull menulis LANGSUNG ke repositori (bukan lewat use
        // case ini), jadi perubahan dari server tidak pernah memicu pencatatan
        // ulang — tanpa risiko loop sinkronisasi.
        runCatching {
            val sebelumnya = repositoriStoreSetting.ambilPengaturan().first()
            val adaPerubahanBersama = pengaturan.namaUsaha != sebelumnya.namaUsaha ||
                pengaturan.alamat != sebelumnya.alamat ||
                pengaturan.tagline != sebelumnya.tagline ||
                pengaturan.logoUri != sebelumnya.logoUri
            if (adaPerubahanBersama) pencatatOutbox?.catatPengaturanToko(pengaturan)
        }
    }
}
