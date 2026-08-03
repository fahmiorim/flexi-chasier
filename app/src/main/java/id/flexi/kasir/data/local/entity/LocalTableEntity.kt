package id.flexi.kasir.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meja_lokal")
data class LocalTableEntity(
    @PrimaryKey
    val id: String,
    val nomor: String,
    val aktif: Boolean = true,
    val tableStatus: String = "Tersedia",
    val TransactionId: String? = null,
    val waktuDudukEpochMili: Long? = null,
)
