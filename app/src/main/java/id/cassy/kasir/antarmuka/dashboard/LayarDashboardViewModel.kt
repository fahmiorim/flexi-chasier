package id.cassy.kasir.antarmuka.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.cassy.kasir.ranah.kasuspenggunaan.AmatiRiwayatTransaksi
import id.cassy.kasir.ranah.fungsi.sebagaiRupiah
import id.cassy.kasir.ranah.model.StatusTransaksi
import id.cassy.kasir.ranah.model.Transaksi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class LayarDashboardViewModel(
    private val amatiRiwayatTransaksi: AmatiRiwayatTransaksi,
) : ViewModel() {

    private val _modelTampilan = MutableStateFlow(ModelTampilanDashboard())
    val modelTampilan: StateFlow<ModelTampilanDashboard> = _modelTampilan

    init {
        viewModelScope.launch {
            amatiRiwayatTransaksi().collect { daftarTransaksi ->
                _modelTampilan.value = bentukModelTampilan(daftarTransaksi)
            }
        }
    }

    private fun bentukModelTampilan(
        daftarTransaksi: List<Transaksi>,
    ): ModelTampilanDashboard {
        val transaksiLunas = daftarTransaksi.filter { it.apakahLunas() }

        val awalHariIni = awalHariIniEpochMili()
        val akhirHariIni = akhirHariIniEpochMili()
        val awalMingguIni = awalMingguIniEpochMili()
        val akhirMingguIni = akhirMingguIniEpochMili()
        val awalBulanIni = awalBulanIniEpochMili()
        val akhirBulanIni = akhirBulanIniEpochMili()

        val transaksiHariIni = transaksiLunas.filter { transaksi ->
            transaksi.waktuTransaksiEpochMili in awalHariIni..akhirHariIni
        }
        val transaksiMingguIni = transaksiLunas.filter { transaksi ->
            transaksi.waktuTransaksiEpochMili in awalMingguIni..akhirMingguIni
        }
        val transaksiBulanIni = transaksiLunas.filter { transaksi ->
            transaksi.waktuTransaksiEpochMili in awalBulanIni..akhirBulanIni
        }

        return ModelTampilanDashboard(
            judulLayar = "Dashboard",
            totalPenjualanHariIni = transaksiHariIni.sumOf { it.uangDibayar.nilaiRupiah }.sebagaiRupiah(),
            totalPenjualanMingguIni = transaksiMingguIni.sumOf { it.uangDibayar.nilaiRupiah }.sebagaiRupiah(),
            totalPenjualanBulanIni = transaksiBulanIni.sumOf { it.uangDibayar.nilaiRupiah }.sebagaiRupiah(),
            jumlahTransaksiHariIni = "${transaksiHariIni.size} transaksi",
            jumlahTransaksiMingguIni = "${transaksiMingguIni.size} transaksi",
            jumlahTransaksiBulanIni = "${transaksiBulanIni.size} transaksi",
            totalProdukTerjualHariIni = transaksiHariIni.sumOf { t ->
                t.daftarItemKeranjang.sumOf { it.jumlah }
            },
            totalProdukTerjualMingguIni = transaksiMingguIni.sumOf { t ->
                t.daftarItemKeranjang.sumOf { it.jumlah }
            },
            totalProdukTerjualBulanIni = transaksiBulanIni.sumOf { t ->
                t.daftarItemKeranjang.sumOf { it.jumlah }
            },
            apakahSedangMemuat = false,
        )
    }
}

private fun Transaksi.apakahLunas(): Boolean = status == StatusTransaksi.Lunas

private fun awalHariIniEpochMili(): Long {
    val kalender = Calendar.getInstance()
    kalender.set(Calendar.HOUR_OF_DAY, 0)
    kalender.set(Calendar.MINUTE, 0)
    kalender.set(Calendar.SECOND, 0)
    kalender.set(Calendar.MILLISECOND, 0)
    return kalender.timeInMillis
}

private fun akhirHariIniEpochMili(): Long {
    val kalender = Calendar.getInstance()
    kalender.set(Calendar.HOUR_OF_DAY, 23)
    kalender.set(Calendar.MINUTE, 59)
    kalender.set(Calendar.SECOND, 59)
    kalender.set(Calendar.MILLISECOND, 999)
    return kalender.timeInMillis
}

private fun awalMingguIniEpochMili(): Long {
    val kalender = Calendar.getInstance()
    kalender.firstDayOfWeek = Calendar.MONDAY
    kalender.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    kalender.set(Calendar.HOUR_OF_DAY, 0)
    kalender.set(Calendar.MINUTE, 0)
    kalender.set(Calendar.SECOND, 0)
    kalender.set(Calendar.MILLISECOND, 0)
    return kalender.timeInMillis
}

private fun akhirMingguIniEpochMili(): Long {
    val kalender = Calendar.getInstance()
    kalender.firstDayOfWeek = Calendar.MONDAY
    kalender.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
    kalender.set(Calendar.HOUR_OF_DAY, 23)
    kalender.set(Calendar.MINUTE, 59)
    kalender.set(Calendar.SECOND, 59)
    kalender.set(Calendar.MILLISECOND, 999)
    return kalender.timeInMillis
}

private fun awalBulanIniEpochMili(): Long {
    val kalender = Calendar.getInstance()
    kalender.set(Calendar.DAY_OF_MONTH, 1)
    kalender.set(Calendar.HOUR_OF_DAY, 0)
    kalender.set(Calendar.MINUTE, 0)
    kalender.set(Calendar.SECOND, 0)
    kalender.set(Calendar.MILLISECOND, 0)
    return kalender.timeInMillis
}

private fun akhirBulanIniEpochMili(): Long {
    val kalender = Calendar.getInstance()
    kalender.set(Calendar.DAY_OF_MONTH, kalender.getActualMaximum(Calendar.DAY_OF_MONTH))
    kalender.set(Calendar.HOUR_OF_DAY, 23)
    kalender.set(Calendar.MINUTE, 59)
    kalender.set(Calendar.SECOND, 59)
    kalender.set(Calendar.MILLISECOND, 999)
    return kalender.timeInMillis
}
