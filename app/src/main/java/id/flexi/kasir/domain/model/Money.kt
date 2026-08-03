package id.flexi.kasir.domain.model

/**
 * Value object untuk merepresentasikan uang dalam Flexi Cashier.
 *
 * Nilai uang disimpan sebagai Long dalam satuan Rupiah penuh.
 * Flexi Cashier tidak memakai Double untuk uang karena Double berisiko
 * menghasilkan pembulatan desimal yang tidak cocok untuk Transaction kasir.
 *
 * @property nilaiRupiah Nominal uang dalam Rupiah dan tidak boleh negatif.
 */
@JvmInline
value class Uang(
    val nilaiRupiah: Long,
) {
    init {
        require(nilaiRupiah >= 0L) {
            "Nilai uang tidak boleh negatif."
        }
    }

    /**
     * Menambahkan uang lain ke nilai saat ini.
     *
     * @param uangLain Nominal tambahan.
     * @return Nominal baru setelah penambahan.
     */
    fun tambah(
        uangLain: Uang,
    ): Uang {
        return Uang(
            nilaiRupiah = nilaiRupiah + uangLain.nilaiRupiah,
        )
    }

    /**
     * Mengurangi nilai saat ini dengan uang lain.
     *
     * Hasil minimum adalah nol agar total Transaction tidak pernah negatif.
     *
     * @param uangLain Nominal pengurang.
     * @return Nominal baru setelah pengurangan.
     */
    fun kurangi(
        uangLain: Uang,
    ): Uang {
        val hasil = nilaiRupiah - uangLain.nilaiRupiah
        require(hasil >= 0L) {
            "Hasil pengurangan uang tidak boleh negatif: $nilaiRupiah - ${uangLain.nilaiRupiah}"
        }
        return Uang(nilaiRupiah = hasil)
    }

    companion object {
        /**
         * Nilai uang nol yang dipakai sebagai default aman.
         */
        val Nol: Uang = Uang(0L)

        /**
         * Membentuk objek [Uang] dari nilai Rupiah mentah.
         *
         * @param nilaiRupiah Nominal dalam Rupiah.
         * @return Objek uang yang sudah divalidasi.
         */
        fun dariRupiah(
            nilaiRupiah: Long,
        ): Uang {
            return Uang(nilaiRupiah)
        }
    }
}
