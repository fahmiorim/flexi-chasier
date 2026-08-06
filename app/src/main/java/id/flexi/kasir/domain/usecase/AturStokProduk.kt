package id.flexi.kasir.domain.usecase

import id.flexi.kasir.domain.model.PenyesuaianStok
import id.flexi.kasir.domain.model.StokJenis
import id.flexi.kasir.domain.repository.ProductRepository
import id.flexi.kasir.domain.repository.StokRekeningRepository
import kotlinx.coroutines.flow.first
import java.util.UUID

/**
 * Mengatur ulang stok produk menjadi nilai baru dan mencatat riwayat penyesuaian.
 *
 * @param alasan Alasan penyesuaian (mis. "Opname fisik" / "Reset stok").
 */
class AturStokProduk(
    private val productRepository: ProductRepository,
    private val stokRekeningRepository: StokRekeningRepository,
) {
    suspend operator fun invoke(
        produkId: String,
        stokBaru: Int,
        alasan: String,
    ) {
        val produk = productRepository.ObserveProductById(produkId).first() ?: return
        val sebelum = produk.stokTersedia
        if (sebelum == stokBaru) return

        stokRekeningRepository.simpanPenyesuaian(
            PenyesuaianStok(
                id = UUID.randomUUID().toString(),
                jenis = StokJenis.Produk,
                entitasId = produkId,
                namaEntitas = produk.nama,
                stokSebelum = sebelum,
                stokSesudah = stokBaru,
                alasan = alasan,
            ),
        )
        productRepository.SaveProduct(produk.copy(stokTersedia = stokBaru.coerceAtLeast(0)))
    }
}
