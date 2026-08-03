package id.flexi.kasir.domain.model

/**
 * Representasi status sinkronisasi data antara lokal dan cloud.
 */
sealed interface SyncStatus {
    /**
     * Data belum pernah disinkronkan ke cloud.
     */
    data object Never : SyncStatus

    /**
     * Data hanya tersimpan di lokal (setelah ada perubahan).
     */
    data object LocalChanges : SyncStatus

    /**
     * Proses pengiriman/pengambilan data sedang berjalan.
     */
    data object Syncing : SyncStatus

    /**
     * Data berhasil disinkronkan dan identik dengan server.
     */
    data object Synced : SyncStatus

    /**
     * Proses sinkronisasi gagal karena kendala tertentu.
     * @property pesan Pesan kesalahan yang terjadi.
     */
    data class Gagal(val pesan: String) : SyncStatus
}
