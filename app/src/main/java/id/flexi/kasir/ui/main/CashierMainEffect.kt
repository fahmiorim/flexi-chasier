package id.flexi.kasir.ui.main

/**
 * Representasi efek sekali pakai (side-effects) dari ViewModel ke antarmuka pengguna.
 *
 * Berbeda dengan "status" yang bersifat persisten, efek digunakan untuk kejadian
 * sesaat yang tidak perlu disimpan saat terjadi konfigurasi ulang (seperti rotasi layar),
 * contohnya adalah menampilkan Snackbar atau navigasi.
 */
sealed interface CashierMainEffect {

    /**
     * Menginstruksikan antarmuka untuk menampilkan pesan singkat (Snackbar).
     *
     * @property pesan Isi teks pesan yang akan ditampilkan kepada pengguna.
     */
    data class TampilkanPesanSingkat(
        val pesan: String,
    ) : CashierMainEffect
}
