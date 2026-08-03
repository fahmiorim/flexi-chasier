package id.flexi.kasir.ui.report

import androidx.compose.runtime.Immutable

/**
 * Periode laporan yang bisa dipilih.
 */
enum class ReportPeriod {
    HariIni,
    MingguIni,
    BulanIni,
    Kustom,
}

/**
 * Status tampilan layar laporan penjualan.
 */
@Immutable
data class SalesReportUiState(
    val apakahSedangMemuat: Boolean = true,
    val periode: ReportPeriod = ReportPeriod.HariIni,
    val tanggalMulaiKustom: Long? = null,
    val tanggalSelesaiKustom: Long? = null,
    val labelRentangKustom: String = "",
    // Ringkasan
    val totalPenjualan: String = "Rp0",
    val jumlahTransaksi: Int = 0,
    val rataRataPerTransaksi: String = "Rp0",
    val totalItemTerjual: Int = 0,
    val totalDiskon: String = "Rp0",
    // Breakdown metode bayar
    val totalTunai: String = "Rp0",
    val jumlahTransaksiTunai: Int = 0,
    val totalQris: String = "Rp0",
    val jumlahTransaksiQris: Int = 0,
    // Breakdown per kategori
    val penjualanPerKategori: List<PenjualanKategori> = emptyList(),
    // Grafik 7 hari
    val grafik7Hari: List<DataPointGrafik> = emptyList(),
    // Status export
    val apakahSedangExport: Boolean = false,
    val pesanError: String? = null,
)

@Immutable
data class DataPointGrafik(
    val label: String,
    val total: Long,
    val totalFormatted: String,
    val persentase: Float,
)

@Immutable
data class PenjualanKategori(
    val nama: String,
    val total: String,
    val jumlah: Int,
    val persentase: Float,
)
