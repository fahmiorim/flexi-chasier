package id.flexi.kasir.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entitas database untuk menyimpan resep produk.
 *
 * @property id Identitas unik resep.
 * @property produkId Identitas produk yang menggunakan resep ini.
 * @property varianNama Nama varian produk (null jika tanpa varian).
 * @property createdAt Waktu (epoch millis) resep dibuat.
 */
@Entity(
    tableName = "resep",
    foreignKeys = [
        ForeignKey(
            entity = LocalProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["produkId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["produkId"]),
    ],
)
data class LocalResepEntity(
    @PrimaryKey
    val id: String,
    val produkId: String,
    val varianNama: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
)
