package id.flexi.kasir.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Metadata sinkronisasi berbentuk key-value.
 *
 * Kunci yang dipakai:
 * - `pull_terakhir:<geraiId>` → kursor epoch mili (waktu server) terakhir yang
 *   berhasil ditarik, agar pull berikutnya hanya mengambil perubahan baru.
 */
@Entity(tableName = "meta_sinkron")
data class SinkronMetaEntity(
    @PrimaryKey
    val kunci: String,
    val nilai: String,
)
