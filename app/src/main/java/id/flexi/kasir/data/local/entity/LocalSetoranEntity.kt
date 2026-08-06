package id.flexi.kasir.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "setoran_kas",
    indices = [
        Index(value = ["dihapus"]),
        Index(value = ["shiftId"]),
    ],
)
data class LocalSetoranEntity(
    @PrimaryKey
    val id: String,
    val shiftId: String = "",
    val nominal: Long,
    val catatan: String = "",
    val waktu: Long,
    val dihapus: Boolean = false,
)
