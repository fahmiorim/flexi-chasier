package id.flexi.kasir.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entitas database untuk satu baris bahan dalam resep.
 *
 * @property id Identitas unik baris (auto-increment).
 * @property resepId Identitas resep induk.
 * @property bahanId Identitas bahan yang digunakan.
 * @property jumlah Jumlah bahan yang diperlukan.
 * @property satuan Satuan jumlah.
 */
@Entity(
    tableName = "bahan_resep",
    foreignKeys = [
        ForeignKey(
            entity = LocalResepEntity::class,
            parentColumns = ["id"],
            childColumns = ["resepId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = LocalBahanEntity::class,
            parentColumns = ["id"],
            childColumns = ["bahanId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["resepId"]),
        Index(value = ["bahanId"]),
    ],
)
data class LocalBahanResepEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val resepId: String,
    val bahanId: String,
    val jumlah: Double,
    val satuan: String = "gram",
)
