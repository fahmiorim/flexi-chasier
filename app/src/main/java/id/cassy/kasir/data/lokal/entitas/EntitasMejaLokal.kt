package id.cassy.kasir.data.lokal.entitas

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meja_lokal")
data class EntitasMejaLokal(
    @PrimaryKey
    val id: String,
    val nomor: String,
    val aktif: Boolean = true,
)
