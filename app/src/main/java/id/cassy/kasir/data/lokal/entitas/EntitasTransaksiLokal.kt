package id.cassy.kasir.data.lokal.entitas

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entitas database untuk menyimpan data utama sebuah transaksi.
 *
 * Indeks pada [waktuTransaksiEpochMili] memastikan query riwayat berdasarkan
 * urutan waktu tetap performan seiring pertumbuhan data.
 *
 * @property id Identitas unik transaksi dalam bentuk UUID string dari
 * [id.cassy.kasir.ranah.identitas.PembangkitIdentitasTransaksi].
 * @property uangDibayar Jumlah uang tunai yang diterima dari pelanggan.
 * @property potongan Total diskon atau potongan harga.
 * @property biayaLayanan Biaya tambahan layanan.
 * @property pajak Total pajak transaksi.
 * @property waktuTransaksiEpochMili Stempel waktu saat transaksi terjadi (Unix Epoch).
 * @property catatan Catatan tambahan untuk transaksi ini.
 * @property status Status pembayaran transaksi (Pending/Lunas).
 */
@Entity(
    tableName = "transaksi_lokal",
    indices = [
        Index(value = ["waktuTransaksiEpochMili"]),
    ],
)
data class EntitasTransaksiLokal(
    @PrimaryKey
    val id: String,
    val uangDibayar: Long,
    val potongan: Long,
    val biayaLayanan: Long,
    val pajak: Long,
    val waktuTransaksiEpochMili: Long,
    val catatan: String?,
    val status: String = "Lunas",
    val metodeBayar: String = "Tunai",
    val tipeOrder: String = "DineIn",
)
