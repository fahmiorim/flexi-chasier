package id.flexi.kasir.domain.model

enum class TableStatus {
    Available,
    Occupied,
}

data class Meja(
    val id: String,
    val nomor: String,
    val aktif: Boolean = true,
    val tableStatus: TableStatus = TableStatus.Available,
    val TransactionId: String? = null,
    val waktuDudukEpochMili: Long? = null,
)
