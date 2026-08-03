package id.flexi.kasir.ui.report

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.flexi.kasir.domain.model.PaymentMethod
import id.flexi.kasir.domain.model.Transaction
import id.flexi.kasir.domain.usecase.ObserveTransactionHistory
import id.flexi.kasir.domain.util.hitungTotalAkhirTransaction
import id.flexi.kasir.domain.util.sebagaiRupiah
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SalesReportViewModel(
    private val observeTransactionHistory: ObserveTransactionHistory,
) : ViewModel() {

    private val _periode = MutableStateFlow(ReportPeriod.HariIni)
    private val _tanggalMulaiKustom = MutableStateFlow<Long?>(null)
    private val _tanggalSelesaiKustom = MutableStateFlow<Long?>(null)

    // stateIn Eagerly agar data siap sebelum combine
    private val transactionHistory = observeTransactionHistory()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _state = MutableStateFlow(SalesReportUiState())
    val state: StateFlow<SalesReportUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                transactionHistory,
                _periode,
                _tanggalMulaiKustom,
                _tanggalSelesaiKustom,
            ) { daftarTransaksi, periode, tglMulai, tglSelesai ->
                val transaksiLunas = daftarTransaksi.filter { it.paymentStatus == id.flexi.kasir.domain.model.PaymentStatus.SudahDibayar && !it.dibatalkan }
                val (mulai, selesai) = hitungBatasWaktu(periode, tglMulai, tglSelesai)

                val transaksiTerfilter = if (mulai != null && selesai != null) {
                    transaksiLunas.filter { t ->
                        t.waktuTransactionEpochMili in mulai..selesai
                    }
                } else {
                    transaksiLunas
                }

                val grafik = hitungDataGrafik7Hari(transaksiLunas)

                bentukState(transaksiTerfilter, periode, tglMulai, tglSelesai, grafik)
            }.collect { state ->
                _state.value = state
            }
        }
    }

    fun perbaruiPeriode(periode: ReportPeriod) {
        _periode.value = periode
        if (periode != ReportPeriod.Kustom) {
            _tanggalMulaiKustom.value = null
            _tanggalSelesaiKustom.value = null
        }
    }

    fun perbaruiTanggalKustom(mulai: Long, selesai: Long) {
        _tanggalMulaiKustom.value = mulai
        _tanggalSelesaiKustom.value = selesai
        _periode.value = ReportPeriod.Kustom
    }

    fun exportCsv(context: Context, uri: Uri, namaUsaha: String = "") {
        val state = _state.value
        _state.update { it.copy(apakahSedangExport = true) }

        viewModelScope.launch {
            try {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    OutputStreamWriter(outputStream, "UTF-8").use { writer ->
                        val namaToko = namaUsaha.ifBlank { "Flexi Cashier" }
                        // Header CSV
                        writer.write("Laporan Penjualan $namaToko\n")
                        writer.write("Periode: ${state.labelRentangKustom.ifBlank { periodeLabel(state.periode) }}\n")
                        writer.write("Dibuat: ${SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID")).format(Date())}\n")
                        writer.write("\n")

                        // Ringkasan
                        writer.write("RINGKASAN\n")
                        writer.write("Total Penjualan,${state.totalPenjualan}\n")
                        writer.write("Jumlah Transaksi,${state.jumlahTransaksi}\n")
                        writer.write("Rata-rata per Transaksi,${state.rataRataPerTransaksi}\n")
                        writer.write("Total Item Terjual,${state.totalItemTerjual}\n")
                        writer.write("Total Diskon,${state.totalDiskon}\n")
                        writer.write("\n")

                        // Metode bayar
                        writer.write("METODE PEMBAYARAN\n")
                        writer.write("Tunai,${state.totalTunai},${state.jumlahTransaksiTunai} transaksi\n")
                        writer.write("QRIS,${state.totalQris},${state.jumlahTransaksiQris} transaksi\n")
                        writer.write("\n")

                        // Per kategori
                        writer.write("PENJUALAN PER KATEGORI\n")
                        writer.write("Kategori,Total,Jumlah Terjual,Persentase\n")
                        state.penjualanPerKategori.forEach { kat ->
                            writer.write("${kat.nama},${kat.total},${kat.jumlah},${String.format("%.0f", kat.persentase * 100)}%\n")
                        }
                        writer.write("\n")

                        writer.write("-- Akhir Laporan --\n")
                        writer.flush()
                    }
                }
                _state.update { it.copy(apakahSedangExport = false) }
            } catch (e: Exception) {
                _state.update { it.copy(apakahSedangExport = false, pesanError = "Gagal export: ${e.message}") }
            }
        }
    }

    private fun bentukState(
        transaksi: List<Transaction>,
        periode: ReportPeriod,
        tglMulai: Long?,
        tglSelesai: Long?,
        grafik: List<DataPointGrafik> = emptyList(),
    ): SalesReportUiState {
        val totalPenjualan = transaksi.sumOf { it.hitungTotalAkhirTransaction() }
        val jumlahTransaksi = transaksi.size
        val totalItem = transaksi.sumOf { t -> t.daftarCartItem.sumOf { it.jumlah } }
        val totalDiskon = transaksi.sumOf { it.potongan.nilaiRupiah }
        val rataRata = if (jumlahTransaksi > 0) totalPenjualan / jumlahTransaksi else 0L

        // Payment method breakdown
        val tunai = transaksi.filter { it.paymentMethod == PaymentMethod.Cash }
        val qris = transaksi.filter { it.paymentMethod == PaymentMethod.Qris }

        // Category breakdown
        val penjualanKategori = mutableMapOf<String, Pair<Long, Int>>()
        transaksi.forEach { t ->
            t.daftarCartItem.forEach { item ->
                val kategori = item.produk.kategori.ifBlank { "Lainnya" }
                val existing = penjualanKategori.getOrDefault(kategori, Pair(0L, 0))
                penjualanKategori[kategori] = Pair(
                    existing.first + (item.jumlah * item.produk.harga),
                    existing.second + item.jumlah,
                )
            }
        }
        val maxKategoriJumlah = penjualanKategori.values.maxOfOrNull { it.second } ?: 0
        val daftarKategori = penjualanKategori.entries
            .sortedByDescending { it.value.second }
            .map { (nama, data) ->
                PenjualanKategori(
                    nama = nama,
                    total = data.first.sebagaiRupiah(),
                    jumlah = data.second,
                    persentase = if (maxKategoriJumlah > 0) data.second.toFloat() / maxKategoriJumlah else 0f,
                )
            }

        val labelRentang = bentukLabelRentang(periode, tglMulai, tglSelesai)

        return SalesReportUiState(
            apakahSedangMemuat = false,
            periode = periode,
            tanggalMulaiKustom = tglMulai,
            tanggalSelesaiKustom = tglSelesai,
            labelRentangKustom = labelRentang,
            totalPenjualan = totalPenjualan.sebagaiRupiah(),
            jumlahTransaksi = jumlahTransaksi,
            rataRataPerTransaksi = rataRata.sebagaiRupiah(),
            totalItemTerjual = totalItem,
            totalDiskon = totalDiskon.sebagaiRupiah(),
            totalTunai = tunai.sumOf { it.hitungTotalAkhirTransaction() }.sebagaiRupiah(),
            jumlahTransaksiTunai = tunai.size,
            totalQris = qris.sumOf { it.hitungTotalAkhirTransaction() }.sebagaiRupiah(),
            jumlahTransaksiQris = qris.size,
            penjualanPerKategori = daftarKategori,
            grafik7Hari = grafik,
        )
    }

    private fun hitungDataGrafik7Hari(transaksiLunas: List<Transaction>): List<DataPointGrafik> {
        val fmtHari = SimpleDateFormat("EEE", Locale("id", "ID"))
        val fmtTgl = SimpleDateFormat("dd/MM", Locale("id", "ID"))
        val kal = Calendar.getInstance()

        val dataPerHari = mutableListOf<DataPointGrafik>()
        var maxTotal = 0L
        val totalsPerHari = mutableMapOf<Long, Long>()

        // Inisialisasi 7 hari terakhir
        for (i in 6 downTo 0) {
            kal.timeInMillis = System.currentTimeMillis()
            kal.add(Calendar.DAY_OF_YEAR, -i)
            kal.set(Calendar.HOUR_OF_DAY, 0)
            kal.set(Calendar.MINUTE, 0)
            kal.set(Calendar.SECOND, 0)
            kal.set(Calendar.MILLISECOND, 0)
            val awalHari = kal.timeInMillis
            val akhirHari = awalHari + 24 * 60 * 60 * 1000 - 1

            val total = transaksiLunas
                .filter { t -> t.waktuTransactionEpochMili in awalHari..akhirHari }
                .sumOf { it.hitungTotalAkhirTransaction() }

            totalsPerHari[awalHari] = total
            if (total > maxTotal) maxTotal = total
        }

        // Format data untuk grafik
        var index = 0
        for (i in 6 downTo 0) {
            kal.timeInMillis = System.currentTimeMillis()
            kal.add(Calendar.DAY_OF_YEAR, -i)
            kal.set(Calendar.HOUR_OF_DAY, 0)
            kal.set(Calendar.MINUTE, 0)
            kal.set(Calendar.SECOND, 0)
            kal.set(Calendar.MILLISECOND, 0)
            val awalHari = kal.timeInMillis
            val total = totalsPerHari[awalHari] ?: 0L
            val label = if (index == 6) "Hari ini" else if (index == 5) "Kemarin" else fmtHari.format(kal.time)
            val labelTgl = fmtTgl.format(kal.time)

            dataPerHari.add(
                DataPointGrafik(
                    label = label,
                    total = total,
                    totalFormatted = total.sebagaiRupiah(),
                    persentase = if (maxTotal > 0) total.toFloat() / maxTotal else 0f,
                )
            )
            index++
        }

        return dataPerHari
    }

    private fun hitungBatasWaktu(
        periode: ReportPeriod,
        tglMulai: Long?,
        tglSelesai: Long?,
    ): Pair<Long?, Long?> {
        val kal = Calendar.getInstance()
        when (periode) {
            ReportPeriod.HariIni -> {
                kal.set(Calendar.HOUR_OF_DAY, 0); kal.set(Calendar.MINUTE, 0)
                kal.set(Calendar.SECOND, 0); kal.set(Calendar.MILLISECOND, 0)
                val awal = kal.timeInMillis
                val akhir = awal + 24 * 60 * 60 * 1000 - 1
                return Pair(awal, akhir)
            }
            ReportPeriod.MingguIni -> {
                kal.firstDayOfWeek = Calendar.MONDAY
                kal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                kal.set(Calendar.HOUR_OF_DAY, 0); kal.set(Calendar.MINUTE, 0)
                kal.set(Calendar.SECOND, 0); kal.set(Calendar.MILLISECOND, 0)
                val awal = kal.timeInMillis
                kal.add(Calendar.DAY_OF_WEEK, 6)
                kal.set(Calendar.HOUR_OF_DAY, 23); kal.set(Calendar.MINUTE, 59)
                kal.set(Calendar.SECOND, 59); kal.set(Calendar.MILLISECOND, 999)
                return Pair(awal, kal.timeInMillis)
            }
            ReportPeriod.BulanIni -> {
                kal.set(Calendar.DAY_OF_MONTH, 1)
                kal.set(Calendar.HOUR_OF_DAY, 0); kal.set(Calendar.MINUTE, 0)
                kal.set(Calendar.SECOND, 0); kal.set(Calendar.MILLISECOND, 0)
                val awal = kal.timeInMillis
                kal.set(Calendar.DAY_OF_MONTH, kal.getActualMaximum(Calendar.DAY_OF_MONTH))
                kal.set(Calendar.HOUR_OF_DAY, 23); kal.set(Calendar.MINUTE, 59)
                kal.set(Calendar.SECOND, 59); kal.set(Calendar.MILLISECOND, 999)
                return Pair(awal, kal.timeInMillis)
            }
            ReportPeriod.Kustom -> {
                if (tglMulai == null || tglSelesai == null) return Pair(null, null)
                return Pair(tglMulai, tglSelesai + 24 * 60 * 60 * 1000 - 1)
            }
        }
    }

    private fun bentukLabelRentang(periode: ReportPeriod, mulai: Long?, selesai: Long?): String {
        val fmt = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
        return when (periode) {
            ReportPeriod.HariIni -> "Hari Ini"
            ReportPeriod.MingguIni -> {
                val kal = Calendar.getInstance()
                kal.firstDayOfWeek = Calendar.MONDAY
                kal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                val awalMinggu = fmt.format(Date(kal.timeInMillis))
                kal.add(Calendar.DAY_OF_WEEK, 6)
                val akhirMinggu = fmt.format(Date(kal.timeInMillis))
                "$awalMinggu - $akhirMinggu"
            }
            ReportPeriod.BulanIni -> {
                val fmtBulan = SimpleDateFormat("MMMM yyyy", Locale("id", "ID"))
                fmtBulan.format(Date())
            }
            ReportPeriod.Kustom -> {
                if (mulai != null && selesai != null) {
                    "${fmt.format(Date(mulai))} - ${fmt.format(Date(selesai))}"
                } else ""
            }
        }
    }

    companion object {
        fun periodeLabel(periode: ReportPeriod): String = when (periode) {
            ReportPeriod.HariIni -> "Hari Ini"
            ReportPeriod.MingguIni -> "Minggu Ini"
            ReportPeriod.BulanIni -> "Bulan Ini"
            ReportPeriod.Kustom -> "Kustom"
        }
    }
}
