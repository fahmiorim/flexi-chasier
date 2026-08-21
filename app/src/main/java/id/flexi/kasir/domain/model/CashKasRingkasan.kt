package id.flexi.kasir.domain.model

/**
 * Ringkasan keuangan per shift kas.
 *
 * Digunakan untuk menampilkan info lengkap di riwayat kas tanpa perlu
 * query ulang saat user men-scroll daftar shift.
 *
 * @property kas Data dasar shift kas.
 * @property penjualanTunai Total penjualan tunai dalam shift.
 * @property penjualanQRIS Total penjualan QRIS dalam shift.
 * @property totalPemasukan Total pemasukan (mutasi masuk).
 * @property totalPengeluaran Total pengeluaran (mutasi keluar).
 * @property totalSetoran Total setoran kas ke rekening.
 * @property jumlahTransaksi Jumlah transaksi yang tercatat dalam shift.
 * @property selisih Selisih saldo fisik vs expected (null jika shift masih aktif).
 */
data class CashKasRingkasan(
    val kas: CashKas,
    val penjualanTunai: Long = 0L,
    val penjualanQRIS: Long = 0L,
    val totalPemasukan: Long = 0L,
    val totalPengeluaran: Long = 0L,
    val totalSetoran: Long = 0L,
    val jumlahTransaksi: Int = 0,
    val selisih: Long? = null,
)
