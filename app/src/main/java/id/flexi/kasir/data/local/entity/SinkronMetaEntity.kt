package id.flexi.kasir.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Metadata sinkronisasi berbasis key-value.
 *
 * Kunci yang dipakai:
 * - `pull_terakhir:<geraiId>:<entitas>` → kursor keyset (`<epochMili>:<id>`)
 *   per entitas, agar pull berikutnya hanya mengambil perubahan baru tanpa
 *   melewatkan data saat batch terpotong.
 */
@Entity(tableName = "meta_sinkron")
data class SinkronMetaEntity(
    @PrimaryKey
    val kunci: String,
    val nilai: String,
)
