package id.flexi.kasir.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entitas database untuk menyimpan item produk di dalam sebuah Transaction.
 * Menggunakan Foreign Key yang terhubung ke [LocalTransactionEntity].
 *
 * @property idLokal ID auto-increment untuk kebutuhan database lokal.
 * @property TransactionId ID Transaction induk.
 * @property produkId ID produk asli dari katalog.
 * @property namaProduk Nama produk saat Transaction terjadi (snapshot).
 * @property hargaProduk Harga produk saat Transaction terjadi (snapshot).
 * @property jumlah Kuantitas produk yang dibeli.
 * @property catatanItem Catatan khusus untuk item ini (misal: "tanpa gula").
 * @property kodePindai Kode barcode/QR produk (snapshot).
 * @property deskripsiProduk Deskripsi produk (snapshot).
 */
@Entity(
    tableName = "item_Transaction_lokal",
    foreignKeys = [
        ForeignKey(
            entity = LocalTransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["TransactionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["TransactionId"]),
        Index(value = ["produkId"]),
    ],
)
data class LocalTransactionItemEntity(
    @PrimaryKey(autoGenerate = true)
    val idLokal: Long = 0,
    val TransactionId: String,
    val produkId: String,
    val namaProduk: String,
    val hargaProduk: Long,
    val jumlah: Int,
    val catatanItem: String?,
    val kodePindai: String?,
    val deskripsiProduk: String,
    val varianNama: String? = null,
    val apakahSelesai: Boolean = false,
)
