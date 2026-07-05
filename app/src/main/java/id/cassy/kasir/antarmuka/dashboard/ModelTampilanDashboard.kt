package id.cassy.kasir.antarmuka.dashboard

data class ModelTampilanDashboard(
    val judulLayar: String = "Dashboard",
    val totalPenjualanHariIni: String = "Rp0",
    val totalPenjualanMingguIni: String = "Rp0",
    val totalPenjualanBulanIni: String = "Rp0",
    val jumlahTransaksiHariIni: String = "0 transaksi",
    val jumlahTransaksiMingguIni: String = "0 transaksi",
    val jumlahTransaksiBulanIni: String = "0 transaksi",
    val totalProdukTerjualHariIni: Int = 0,
    val totalProdukTerjualMingguIni: Int = 0,
    val totalProdukTerjualBulanIni: Int = 0,
    val apakahSedangMemuat: Boolean = true,
)
