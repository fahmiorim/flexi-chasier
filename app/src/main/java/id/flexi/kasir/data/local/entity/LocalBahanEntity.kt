package id.flexi.kasir.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entitas database untuk menyimpan data bahan baku.
 *
 * @property id Identitas unik bahan.
 * @property nama Nama bahan baku.
 * @property satuan Satuan ukuran (e.g., "gram", "ml", "pcs").
 * @property stokTersedia Jumlah stok tersedia.
 * @property hargaPerSatuan Harga per satuan dalam Rupiah.
 * @property createdAt Waktu (epoch millis) bahan dibuat.
 */
@Entity(tableName = "bahan")
data class LocalBahanEntity(
    @PrimaryKey
    val id: String,
    val nama: String,
    val satuan: String = "pcs",
    val stokTersedia: Double = 0.0,
    val hargaPerSatuan: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
)
