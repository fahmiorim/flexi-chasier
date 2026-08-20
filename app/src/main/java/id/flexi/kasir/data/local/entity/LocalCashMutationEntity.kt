package id.flexi.kasir.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entitas database untuk menyimpan data mutasi kas.
 *
 * @property id Identitas unik mutasi.
 * @property shiftId ID shift induk tempat mutasi terjadi.
 * @property tipe Tipe mutasi ("Pemasukan" atau "Pengeluaran").
 * @property kategori Kategori pengeluaran (hanya relevan untuk Pengeluaran).
 * @property nominal Jumlah uang yang dimutasikan (dalam Rupiah).
 * @property catatan Deskripsi mutasi.
 * @property waktu Epoch millis saat mutasi dicatat.
 */
@Entity(
    tableName = "mutasi_kas",
    foreignKeys = [
        ForeignKey(
            entity = LocalCashKasEntity::class,
            parentColumns = ["id"],
            childColumns = ["shiftId"],
            onDelete = ForeignKey.NO_ACTION,
        ),
    ],
    indices = [
        Index(value = ["shiftId"]),
        Index(value = ["tipe"]),
        Index(value = ["shiftId", "tipe"]),
    ],
)
data class LocalCashMutationEntity(
    @PrimaryKey
    val id: String,
    val shiftId: String,
    val tipe: String,
    val kategori: String = "Lainnya",
    val nominal: Long,
    val catatan: String = "",
    val waktu: Long,
)
