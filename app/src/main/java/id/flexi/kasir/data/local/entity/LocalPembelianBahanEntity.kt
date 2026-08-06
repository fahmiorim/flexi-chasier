package id.flexi.kasir.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entitas database untuk menycatat pembelian bahan baku.
 *
 * @property id Identitas unik pembelian.
 * @property bahanId Identitas bahan yang dibeli.
 * @property jumlah Jumlah yang dibeli.
 * @property satuanBeli Satuan saat membeli.
 * @property totalHarga Total harga pembelian.
 * @property tanggalBeli Waktu pembelian.
 * @property catatan Catatan opsional.
 */
@Entity(
    tableName = "pembelian_bahan",
    foreignKeys = [
        ForeignKey(
            entity = LocalBahanEntity::class,
            parentColumns = ["id"],
            childColumns = ["bahanId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["bahanId"]),
    ],
)
data class LocalPembelianBahanEntity(
    @PrimaryKey
    val id: String,
    val bahanId: String,
    val jumlah: Double,
    val satuanBeli: String = "pcs",
    val totalHarga: Long,
    val tanggalBeli: Long = System.currentTimeMillis(),
    val catatan: String? = null,
    /** ID mutasi kas BelanjaBahan terkait (dibatalkan saat pembelian dihapus). */
    val mutasiKasId: String? = null,
)
