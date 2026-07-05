package id.cassy.kasir.ranah.model

/**
 * Wrapper hasil operasi jaringan untuk menangani berbagai skenario kegagalan secara eksplisit.
 */
sealed interface HasilOperasiJaringan<out T> {
    /**
     * Operasi berhasil dan mengembalikan data.
     */
    data class Berhasil<T>(val data: T) : HasilOperasiJaringan<T>

    /**
     * Gagal karena masalah koneksi internet.
     */
    data class GagalJaringan(val pesan: String) : HasilOperasiJaringan<Nothing>

    /**
     * Gagal karena respon error dari server (misal: 404, 500).
     */
    data class GagalServer(val kode: Int, val pesan: String) : HasilOperasiJaringan<Nothing>

    /**
     * Gagal namun aplikasi berhasil menggunakan data cadangan dari lokal.
     */
    data class FallbackLokal<T>(val data: T, val alasanGagal: String) : HasilOperasiJaringan<T>
}
