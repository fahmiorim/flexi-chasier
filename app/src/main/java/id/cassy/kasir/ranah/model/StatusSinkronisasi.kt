package id.cassy.kasir.ranah.model

/**
 * Representasi status sinkronisasi data antara lokal dan cloud.
 */
sealed interface StatusSinkronisasi {
    /**
     * Data belum pernah disinkronkan ke cloud.
     */
    data object BelumPernah : StatusSinkronisasi

    /**
     * Data hanya tersimpan di lokal (setelah ada perubahan).
     */
    data object SinkronLokal : StatusSinkronisasi

    /**
     * Proses pengiriman/pengambilan data sedang berjalan.
     */
    data object SedangSinkron : StatusSinkronisasi

    /**
     * Data berhasil disinkronkan dan identik dengan server.
     */
    data object Berhasil : StatusSinkronisasi

    /**
     * Proses sinkronisasi gagal karena kendala tertentu.
     * @property pesan Pesan kesalahan yang terjadi.
     */
    data class Gagal(val pesan: String) : StatusSinkronisasi
}
