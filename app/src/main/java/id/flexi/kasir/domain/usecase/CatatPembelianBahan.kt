package id.flexi.kasir.domain.usecase

import id.flexi.kasir.domain.model.PembelianBahan
import id.flexi.kasir.domain.repository.BahanRepository
import java.util.UUID
import kotlin.math.roundToLong

class CatatPembelianBahan(
    private val BahanRepository: BahanRepository,
) {
    /**
     * Mencatat pembelian bahan, menambah stok, dan menghitung ulang harga per satuan.
     *
     * @param bahanId ID bahan yang dibeli.
     * @param jumlah Jumlah yang dibeli (dalam satuan beli).
     * @param satuanBeli Satuan saat membeli.
     * @param totalHarga Total harga pembelian.
     * @param catatan Catatan opsional.
     */
    suspend operator fun invoke(
        bahanId: String,
        jumlah: Double,
        satuanBeli: String,
        totalHarga: Long,
        catatan: String? = null,
    ) {
        if (jumlah <= 0 || totalHarga <= 0) return

        val pembelian = PembelianBahan(
            id = UUID.randomUUID().toString(),
            bahanId = bahanId,
            jumlah = jumlah,
            satuanBeli = satuanBeli,
            totalHarga = totalHarga,
            tanggalBeli = System.currentTimeMillis(),
            catatan = catatan,
        )

        // Simpan pembelian
        BahanRepository.savePembelian(pembelian)

        // Tambah stok bahan
        BahanRepository.perbaruiStokBahan(bahanId, jumlah)

        // Hitung harga per satuan dari pembelian terakhir
        val hargaPerSatuan = (totalHarga.toDouble() / jumlah).roundToLong()
        BahanRepository.perbaruiHargaSatuanBahan(bahanId, hargaPerSatuan)
    }
}
