package id.cassy.kasir.data.lokal.entitas

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entitas database untuk menyimpan item produk di dalam sebuah transaksi.
 * Menggunakan Foreign Key yang terhubung ke [EntitasTransaksiLokal].
 *
 * @property idLokal ID auto-increment untuk kebutuhan database lokal.
 * @property transaksiId ID transaksi induk.
 * @property produkId ID produk asli dari katalog.
 * @property namaProduk Nama produk saat transaksi terjadi (snapshot).
 * @property hargaProduk Harga produk saat transaksi terjadi (snapshot).
 * @property jumlah Kuantitas produk yang dibeli.
 * @property catatanItem Catatan khusus untuk item ini (misal: "tanpa gula").
 * @property kodePindai Kode barcode/QR produk (snapshot).
 * @property deskripsiProduk Deskripsi produk (snapshot).
 */
@Entity(
    tableName = "item_transaksi_lokal",
    foreignKeys = [
        ForeignKey(
            entity = EntitasTransaksiLokal::class,
            parentColumns = ["id"],
            childColumns = ["transaksiId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["transaksiId"]),
        Index(value = ["produkId"]),
    ],
)
data class EntitasItemTransaksiLokal(
    @PrimaryKey(autoGenerate = true)
    val idLokal: Long = 0,
    val transaksiId: String,
    val produkId: String,
    val namaProduk: String,
    val hargaProduk: Long,
    val jumlah: Int,
    val catatanItem: String?,
    val kodePindai: String?,
    val deskripsiProduk: String,
)
