package id.cassy.kasir.data.lokal.entitas

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entitas database untuk menyimpan data produk secara lokal.
 * Mendukung skenario offline-first.
 */
@Entity(tableName = "produk")
data class EntitasProdukLokal(
    @PrimaryKey
    val id: String,
    val nama: String,
    val harga: Long,
    val stokTersedia: Int,
    val kodePindai: String?,
    val deskripsi: String,
    val apakahAktif: Boolean = true,
    val kategori: String = "",
)
