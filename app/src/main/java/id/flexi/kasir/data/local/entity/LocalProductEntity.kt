package id.flexi.kasir.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entitas database untuk menyimpan data produk secara lokal.
 * Mendukung skenario offline-first.
 */
@Entity(tableName = "produk")
data class LocalProductEntity(
    @PrimaryKey
    val id: String,
    val nama: String,
    val harga: Long,
    val stokTersedia: Int,
    val kodePindai: String?,
    val deskripsi: String,
    val apakahAktif: Boolean = true,
    val kategori: String = "",
    val fotoUri: String? = null,
    val favorit: Boolean = false,
    val hargaModal: Long? = null,
    val varianJson: String? = null,
    val apakahStokDiaktifkan: Boolean = false,
)
