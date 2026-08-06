package id.flexi.kasir.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entitas database untuk riwayat penyesuaian/reset stok (bahan maupun produk).
 *
 * Catatan disimpan dengan stok sebelum/sesudah + selisih agar laporan konsisten
 * lintas perangkat (server memakai field yang sama). Mendukung offline-first:
 * baris lokal yang dibuat di luar jaringan tetap tersimpan.
 */
@Entity(tableName = "penyesuaian_stok")
data class LocalPenyesuaianStokEntity(
    @PrimaryKey
    val id: String,
    val jenis: String, // "Bahan" | "Produk"
    val entitasId: String,
    val namaEntitas: String = "",
    val stokSebelum: Int,
    val stokSesudah: Int,
    val selisih: Int,
    val alasan: String = "",
    val waktu: Long,
)
