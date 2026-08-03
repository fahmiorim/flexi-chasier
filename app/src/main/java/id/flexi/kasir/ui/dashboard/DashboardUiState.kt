package id.flexi.kasir.ui.dashboard

data class DashboardUiState(
    val judulLayar: String = "Dashboard",
    val totalPenjualanHariIni: String = "Rp0",
    val totalPenjualanMingguIni: String = "Rp0",
    val totalPenjualanBulanIni: String = "Rp0",
    val jumlahTransactionHariIni: String = "0 Transaction",
    val jumlahTransactionMingguIni: String = "0 Transaction",
    val jumlahTransactionBulanIni: String = "0 Transaction",
    val totalProdukTerjualHariIni: Int = 0,
    val totalProdukTerjualMingguIni: Int = 0,
    val totalProdukTerjualBulanIni: Int = 0,
    val apakahSedangMemuat: Boolean = true,
    // Ringkasan metode bayar hari ini
    val totalTunaiHariIni: String = "Rp0",
    val totalQrisHariIni: String = "Rp0",
    val jumlahTransactionTunaiHariIni: Int = 0,
    val jumlahTransactionQrisHariIni: Int = 0,
    // Metrik tambahan
    val rataWaktuTungguHariIni: String = "-",
    val jumlahAntrianPending: Int = 0,
    val jumlahAntrianDiproses: Int = 0,
    val produkTerlaris: List<ProdukTerlaris> = emptyList(),
    // Tren perbandingan
    val trenHariIni: String = "0%",
    val trenMingguIni: String = "0%",
    val trenBulanIni: String = "0%",
    val apakahTrenHariIniPositif: Boolean = true,
    val apakahTrenMingguIniPositif: Boolean = true,
    val apakahTrenBulanIniPositif: Boolean = true,
    // Metrik tambahan
    val rataRataPerTransaction: String = "Rp0",
    val totalDiskonHariIni: String = "Rp0",
    // Saldo kas
    val saldoKasSaatIni: String = "-",
    val apakahKasAktif: Boolean = false,
    // Chart data — 7 hari terakhir
    val dailySalesBreakdown: List<DailySalesData> = emptyList(),
)

data class ProdukTerlaris(
    val ranking: Int,
    val nama: String,
    val jumlah: Int,
    val persentase: Float,
)

data class DailySalesData(
    val label: String,
    val amount: Long,
    val isToday: Boolean,
)
