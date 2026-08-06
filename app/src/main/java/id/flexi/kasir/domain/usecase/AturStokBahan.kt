package id.flexi.kasir.domain.usecase

import id.flexi.kasir.domain.model.PenyesuaianStok
import id.flexi.kasir.domain.model.StokJenis
import id.flexi.kasir.domain.repository.BahanRepository
import id.flexi.kasir.domain.repository.StokRekeningRepository
import java.util.UUID
import kotlin.math.roundToInt

/**
 * Mengatur ulang stok bahan baku menjadi nilai baru dan mencatat riwayat penyesuaian.
 *
 * @param alasan Alasan penyesuaian (mis. "Opname fisik" / "Reset stok").
 */
class AturStokBahan(
    private val bahanRepository: BahanRepository,
    private val stokRekeningRepository: StokRekeningRepository,
) {
    suspend operator fun invoke(
        bahanId: String,
        stokBaru: Int,
        alasan: String,
    ) {
        val bahan = bahanRepository.ambilBahanById(bahanId) ?: return
        val sebelum = bahan.stokTersedia.roundToInt()
        if (sebelum == stokBaru) return

        stokRekeningRepository.simpanPenyesuaian(
            PenyesuaianStok(
                id = UUID.randomUUID().toString(),
                jenis = StokJenis.Bahan,
                entitasId = bahanId,
                namaEntitas = bahan.nama,
                stokSebelum = sebelum,
                stokSesudah = stokBaru,
                alasan = alasan,
            ),
        )
        bahanRepository.saveBahan(bahan.copy(stokTersedia = stokBaru.coerceAtLeast(0).toDouble()))
    }
}
