package id.flexi.kasir.domain.model

data class Setoran(
    val id: String,
    val shiftId: String = "",
    val nominal: Uang,
    val catatan: String = "",
    val waktu: Long,
    val dihapus: Boolean = false,
)
