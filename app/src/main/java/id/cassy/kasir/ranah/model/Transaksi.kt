package id.cassy.kasir.ranah.model

enum class StatusTransaksi {
    Pending,
    Lunas,
}

enum class MetodeBayar(val label: String) {
    Tunai(label = "Tunai"),
    Qris(label = "QRIS"),
}

enum class TipeOrder(val label: String) {
    DineIn(label = "Dine In"),
    TakeAway(label = "Take Away"),
}

/**
 * Representasi dokumen transaksi final setelah pembayaran.
 *
 * @property id Nomor referensi unik transaksi (misal: "TRX-20241020-001").
 * @property daftarItemKeranjang List [ItemKeranjang] yang dibeli.
 * @property uangDibayar Nominal uang yang diterima dari pelanggan.
 * @property potongan Nilai diskon yang dikurangkan dari total.
 * @property biayaLayanan Biaya tambahan (service charge).
 * @property pajak Nilai pajak (PPN/PB1).
 * @property waktuTransaksiEpochMili Timestamp saat transaksi dicatat dalam milidetik.
 * @property catatan Pesan tambahan untuk transaksi secara keseluruhan.
 * @property status Status pembayaran transaksi (Pending/Lunas).
 * @property tipeOrder Jenis pesanan (Dine In / Take Away).
 */
data class Transaksi(
    val id: String,
    val daftarItemKeranjang: List<ItemKeranjang>,
    val uangDibayar: Uang = Uang.Nol,
    val potongan: Uang = Uang.Nol,
    val biayaLayanan: Uang = Uang.Nol,
    val pajak: Uang = Uang.Nol,
    val waktuTransaksiEpochMili: Long,
    val catatan: String? = null,
    val status: StatusTransaksi = StatusTransaksi.Lunas,
    val metodeBayar: MetodeBayar = MetodeBayar.Tunai,
    val tipeOrder: TipeOrder = TipeOrder.DineIn,
)
