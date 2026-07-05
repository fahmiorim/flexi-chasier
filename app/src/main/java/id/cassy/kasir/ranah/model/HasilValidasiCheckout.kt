package id.cassy.kasir.ranah.model

/**
 * Hasil validasi checkout sebelum transaksi disimpan.
 *
 * Model ini mengganti pola boolean mentah agar alasan kegagalan checkout
 * bisa diketahui dengan jelas oleh use case dan ViewModel.
 */
sealed interface HasilValidasiCheckout {

    /**
     * Menandakan apakah checkout sah secara aturan domain.
     */
    val apakahSah: Boolean

    /**
     * Checkout sah dan boleh dilanjutkan.
     */
    data object Sah : HasilValidasiCheckout {
        override val apakahSah: Boolean = true
    }

    /**
     * Checkout tidak sah dan membawa alasan kegagalan.
     *
     * @property alasan Alasan domain kenapa checkout tidak boleh dilanjutkan.
     */
    data class TidakSah(
        val alasan: AlasanValidasiCheckout,
    ) : HasilValidasiCheckout {
        override val apakahSah: Boolean = false

        /**
         * Pesan ringkas yang aman ditampilkan atau diteruskan ke lapisan presentasi.
         */
        val pesan: String
            get() = alasan.pesan
    }
}

/**
 * Alasan domain kenapa checkout tidak boleh dilanjutkan.
 */
sealed interface AlasanValidasiCheckout {

    /**
     * Pesan ringkas dari alasan validasi.
     */
    val pesan: String

    /**
     * Keranjang belum memiliki item.
     */
    data object KeranjangKosong : AlasanValidasiCheckout {
        override val pesan: String = "Keranjang masih kosong."
    }

    /**
     * Jumlah item bernilai nol atau negatif.
     *
     * @property namaProduk Nama produk yang bermasalah.
     * @property jumlah Jumlah yang tidak valid.
     */
    data class JumlahItemTidakValid(
        val namaProduk: String,
        val jumlah: Int,
    ) : AlasanValidasiCheckout {
        override val pesan: String =
            "Jumlah produk $namaProduk tidak valid."
    }

    /**
     * Produk sudah tidak aktif sehingga tidak boleh dijual.
     *
     * @property namaProduk Nama produk yang bermasalah.
     */
    data class ProdukTidakAktif(
        val namaProduk: String,
    ) : AlasanValidasiCheckout {
        override val pesan: String =
            "Produk $namaProduk sedang tidak aktif."
    }

    /**
     * Jumlah yang diminta melebihi stok tersedia.
     *
     * @property namaProduk Nama produk yang bermasalah.
     * @property jumlahDiminta Jumlah yang diminta kasir.
     * @property stokTersedia Stok yang tersedia saat checkout.
     */
    data class StokTidakCukup(
        val namaProduk: String,
        val jumlahDiminta: Int,
        val stokTersedia: Int,
    ) : AlasanValidasiCheckout {
        override val pesan: String =
            "Stok $namaProduk tidak cukup."
    }

    /**
     * Potongan lebih besar daripada subtotal transaksi.
     */
    data object PotonganMelebihiSubtotal : AlasanValidasiCheckout {
        override val pesan: String =
            "Potongan tidak boleh melebihi subtotal."
    }

    /**
     * Uang pelanggan kurang dari total akhir.
     *
     * @property totalAkhir Total yang harus dibayar.
     * @property uangDibayar Uang yang diterima.
     */
    data class UangDibayarKurang(
        val totalAkhir: Uang,
        val uangDibayar: Uang,
    ) : AlasanValidasiCheckout {
        override val pesan: String =
            "Uang dibayar belum mencukupi total transaksi."
    }
}
