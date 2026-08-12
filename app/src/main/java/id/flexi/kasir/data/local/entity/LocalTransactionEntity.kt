package id.flexi.kasir.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entitas database untuk menyimpan data utama sebuah Transaction.
 *
 * Indeks pada [waktuTransactionEpochMili] memastikan query riwayat berdasarkan
 * urutan waktu tetap performan seiring pertumbuhan data.
 *
 * @property id Identitas unik Transaction dalam bentuk UUID string dari
 * [id.flexi.kasir.domain.identity.TransactionIdGenerator].
 * @property uangDibayar Jumlah uang tunai yang diterima dari pelanggan.
 * @property potongan Total diskon atau potongan harga.
 * @property biayaLayanan Biaya tambahan layanan.
 * @property pajak Total pajak Transaction.
 * @property waktuTransactionEpochMili Stempel waktu saat Transaction terjadi (Unix Epoch).
 * @property catatan Catatan tambahan untuk Transaction ini.
 * @property status Status Payment Transaction (Pending/Lunas).
 * @property versi Versi monotonik untuk last-write-wins lintas perangkat
 * (disinkronkan dengan `versi` server; naik setiap perubahan lokal).
 */
@Entity(
    tableName = "Transaction_lokal",
    indices = [
        Index(value = ["waktuTransactionEpochMili"]),
        Index(value = ["status"]),
        Index(value = ["PaymentMethod"]),
        Index(value = ["dibatalkan"]),
        Index(value = ["waktuDibayarEpochMili"]),
        Index(value = ["status", "PaymentMethod", "dibatalkan"]),
    ],
)
data class LocalTransactionEntity(
    @PrimaryKey
    val id: String,
    val uangDibayar: Long,
    val potongan: Long,
    val biayaLayanan: Long,
    val pajak: Long,
    val waktuTransactionEpochMili: Long,
    val catatan: String?,
    val status: String = "Lunas",
    val PaymentMethod: String = "Tunai",
    val OrderType: String = "DineIn",
    val nomorAntrian: Int? = null,
    val mejaId: String? = null,
    val waktuDiprosesEpochMili: Long? = null,
    val waktuSelesaiEpochMili: Long? = null,
    val waktuDibayarEpochMili: Long? = null,
    val dibatalkan: Boolean = false,
    val alasanPembatalan: String? = null,
    val versi: Long = 0L,
)
