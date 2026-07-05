package id.cassy.kasir.antarmuka.kelola

/**
 * Representasi seluruh aksi pengguna pada layar kelola produk.
 */
sealed interface AksiLayarKelolaProduk {

    /**
     * Aksi saat pengguna ingin menghapus produk.
     *
     * @property identitasProduk ID produk yang akan dihapus.
     * @property namaProduk Nama produk untuk ditampilkan di dialog konfirmasi.
     */
    data class MintaHapusProduk(
        val identitasProduk: String,
        val namaProduk: String,
    ) : AksiLayarKelolaProduk

    /**
     * Aksi konfirmasi penghapusan produk.
     */
    data object KonfirmasiHapusProduk : AksiLayarKelolaProduk

    /**
     * Aksi pembatalan penghapusan produk.
     */
    data object BatalkanHapusProduk : AksiLayarKelolaProduk

    /**
     * Aksi pencarian produk berdasarkan kata kunci.
     */
    data class PerbaruiKataKunciPencarian(val kataKunci: String) : AksiLayarKelolaProduk

    /**
     * Aksi membersihkan kata kunci pencarian.
     */
    data object ResetPencarian : AksiLayarKelolaProduk
}
