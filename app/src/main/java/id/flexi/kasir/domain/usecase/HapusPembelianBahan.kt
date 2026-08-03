package id.flexi.kasir.domain.usecase

import id.flexi.kasir.domain.repository.BahanRepository

class HapusPembelianBahan(
    private val BahanRepository: BahanRepository,
) {
    /**
     * Menghapus pembelian bahan, mengurangi stok, dan menghitung ulang harga per satuan
     * dari pembelian terakhir yang tersisa.
     *
     * @param id ID pembelian yang akan dihapus.
     * @param bahanId ID bahan terkait (untuk update stok).
     * @param jumlah Jumlah yang dibeli (untuk reverse stok).
     */
    suspend operator fun invoke(
        id: String,
        bahanId: String,
        jumlah: Double,
    ) {
        // Cegah stok jadi negatif: kurangi maksimal stok yang tersedia
        val bahan = BahanRepository.ambilBahanById(bahanId)
        if (bahan != null) {
            val stokAman = minOf(jumlah, bahan.stokTersedia)
            BahanRepository.perbaruiStokBahan(bahanId, -stokAman)
        } else {
            BahanRepository.perbaruiStokBahan(bahanId, -jumlah)
        }

        // Hapus pembelian
        BahanRepository.deletePembelian(id)

        // Hitung ulang harga per satuan dari pembelian terakhir yang masih ada
        val pembelianTerakhir = BahanRepository.ambilPembelianTerakhir(bahanId)
        if (pembelianTerakhir != null && pembelianTerakhir.jumlah > 0) {
            val hargaPerSatuan = (pembelianTerakhir.totalHarga.toDouble() / pembelianTerakhir.jumlah).toLong()
            BahanRepository.perbaruiHargaSatuanBahan(bahanId, hargaPerSatuan)
        } else {
            BahanRepository.perbaruiHargaSatuanBahan(bahanId, 0L)
        }
    }
}
