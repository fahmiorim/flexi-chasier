package id.flexi.kasir.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Antrian outbox: satu baris mewakili satu entitas (produk, transaksi, meja, dsb.)
 * yang berubah di perangkat dan menunggu dikirim ke server.
 *
 * Payload disimpan sebagai JSON persis dengan bentuk kontrak push backend
 * (`POST /api/sync/<entitas>`), sehingga retry selalu mengirim data yang sama
 * (idempotent) dan `versi` digunakan untuk aturan last-write-wins di server.
 *
 * @property entitas Jenis entitas, mis. "transaksi", "meja", "shift-kas".
 * @property itemId Identitas unik entitas di server (bukan id baris outbox).
 * @property geraiId Gerai tempat perubahan terjadi.
 * @property versi Versi monotonik (epoch mili saat perubahan). Server menolak
 *                versi yang lebih tua dari data yang sudah tersimpan.
 * @property payload JSON item lengkap sesuai kontrak push.
 * @property status "Antri" | "Berhasil" | "Gagal".
 */
@Entity(
    tableName = "outbox_sinkron",
    indices = [
        Index(value = ["entitas", "itemId"], unique = true),
        Index(value = ["status"]),
    ],
)
data class OutboxSinkronEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val entitas: String,
    val itemId: String,
    val geraiId: String,
    val versi: Long,
    val payload: String,
    val status: String = "Antri",
    val jumlahPercobaan: Int = 0,
    val waktuDibuat: Long,
    val pesanError: String? = null,
)
