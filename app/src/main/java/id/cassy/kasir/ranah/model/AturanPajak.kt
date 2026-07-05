package id.cassy.kasir.ranah.model

/**
 * Aturan pajak untuk transaksi POS.
 *
 * Persentase disimpan sebagai basis poin agar tetap berbasis bilangan bulat.
 * Contoh:
 * - 100 basis poin = 1 persen
 * - 1_100 basis poin = 11 persen
 *
 * @property nama Nama pajak, misalnya "PPN" atau "PB1".
 * @property basisPoin Nilai pajak dalam basis poin.
 * @property aktif Menentukan apakah aturan pajak sedang diterapkan.
 */
data class AturanPajak(
    val nama: String,
    val basisPoin: Int,
    val aktif: Boolean = true,
) {
    init {
        require(nama.isNotBlank()) {
            "Nama pajak wajib diisi."
        }

        require(basisPoin >= 0) {
            "Basis poin pajak tidak boleh negatif."
        }
    }

    /**
     * Menghitung nominal pajak dari subtotal.
     *
     * @param subtotal Nilai barang sebelum pajak.
     * @return Nominal pajak dalam Rupiah.
     */
    fun hitungDariSubtotal(
        subtotal: Uang,
    ): Uang {
        if (!aktif) {
            return Uang.Nol
        }

        return Uang.dariRupiah(
            nilaiRupiah = subtotal.nilaiRupiah * basisPoin / 10_000L,
        )
    }

    companion object {
        /**
         * Aturan tanpa pajak. Dipakai sebagai default aman untuk UMKM yang belum
         * menerapkan pajak pada transaksi.
         */
        val TanpaPajak: AturanPajak = AturanPajak(
            nama = "Tanpa pajak",
            basisPoin = 0,
            aktif = false,
        )
    }
}
