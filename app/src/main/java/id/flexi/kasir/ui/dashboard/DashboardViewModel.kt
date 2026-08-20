package id.flexi.kasir.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.flexi.kasir.domain.model.CashMutation
import id.flexi.kasir.domain.model.CashMutationType
import id.flexi.kasir.domain.model.CashKas
import id.flexi.kasir.domain.model.PaymentMethod
import id.flexi.kasir.domain.model.PaymentStatus
import id.flexi.kasir.domain.repository.TransactionRepository
import id.flexi.kasir.domain.model.Setoran
import id.flexi.kasir.domain.usecase.AmatiMutasiKas
import id.flexi.kasir.domain.usecase.AmatiKasAktif
import id.flexi.kasir.domain.usecase.AmatiSetoran
import id.flexi.kasir.domain.usecase.ObservePendingOrders
import id.flexi.kasir.domain.usecase.ObserveProcessingOrders
import id.flexi.kasir.domain.usecase.LoadProductCatalog
import id.flexi.kasir.domain.util.sebagaiRupiah
import id.flexi.kasir.domain.model.Transaction
import id.flexi.kasir.domain.model.Produk
import id.flexi.kasir.domain.util.hitungTotalAkhirTransaction
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class DashboardViewModel(
    private val transactionRepository: TransactionRepository,
    private val ObservePendingOrders: ObservePendingOrders,
    private val ObserveProcessingOrders: ObserveProcessingOrders,
    private val LoadProductCatalog: LoadProductCatalog,
    private val amatiKasAktif: AmatiKasAktif,
    private val amatiMutasiKas: AmatiMutasiKas,
    private val amatiSetoran: AmatiSetoran,
) : ViewModel() {

    /** Transaksi lunas 60 hari terakhir — cukup untuk semua periode dashboard */
    private var daftarTransaction60: List<Transaction> = emptyList()
    private var daftarPending = emptyList<Transaction>()
    private var daftarDiproses = emptyList<Transaction>()
    private var daftarProduk = emptyList<Produk>()
    private var shiftKas: CashKas? = null
    private var daftarMutasiKas = emptyList<CashMutation>()
    private var observasiMutasiJob: Job? = null
    private var shiftIdTerakhir: String? = null
    private var totalSetoranShiftAktif: Long = 0L

    private val _modelTampilan = MutableStateFlow(DashboardUiState())
    val modelTampilan: StateFlow<DashboardUiState> = _modelTampilan

    init {
        // 1) Muat 60 hari transaksi lunas → chart, produk, waktu tunggu, DAN period totals
        viewModelScope.launch {
            val sejak60Hari = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -60) }.timeInMillis
            transactionRepository.amatiTransactionSejak(sejak60Hari).collect { list ->
                daftarTransaction60 = list.filter {
                    it.paymentStatus == PaymentStatus.SudahDibayar && !it.dibatalkan
                }
                bentukUlang()
            }
        }
        // 2) Antrian pending
        viewModelScope.launch {
            ObservePendingOrders().collect { list ->
                daftarPending = list
                bentukUlang()
            }
        }
        // 3) Antrian diproses
        viewModelScope.launch {
            ObserveProcessingOrders().collect { list ->
                daftarDiproses = list
                bentukUlang()
            }
        }
        // 4) Katalog produk → untuk produk terlaris
        viewModelScope.launch {
            LoadProductCatalog.eksekusi().collect { list ->
                daftarProduk = list
                bentukUlang()
            }
        }
        // 5) Shift kas aktif + mutasi → saldo kas
        viewModelScope.launch {
            amatiKasAktif().collect { shift ->
                shiftKas = shift
                if (shift != null && shift.id != shiftIdTerakhir) {
                    shiftIdTerakhir = shift.id
                    observasiMutasiJob?.cancel()
                    observasiMutasiJob = viewModelScope.launch {
                        amatiMutasiKas(shift.id).collect { mutations ->
                            daftarMutasiKas = mutations
                            bentukUlang()
                        }
                    }
                } else if (shift == null) {
                    daftarMutasiKas = emptyList()
                    totalSetoranShiftAktif = 0L
                    shiftIdTerakhir = null
                    observasiMutasiJob?.cancel()
                    observasiMutasiJob = null
                }
                bentukUlang()
            }
        }
        // 6) Setoran per shift aktif → kurangi saldo kas
        viewModelScope.launch {
            amatiSetoran().collect { setoranList ->
                val shiftId = shiftKas?.id
                totalSetoranShiftAktif = if (shiftId != null) {
                    setoranList.filter { it.shiftId == shiftId && !it.dihapus }
                        .sumOf { it.nominal.nilaiRupiah }
                } else 0L
                bentukUlang()
            }
        }
    }

    /**
     * Segala perhitungan dari data in-memory:
     * - Period totals dari 60-hari list (filter by epoch, hitungTotalAkhirTransaction)
     * - Chart, produk terlaris, waktu tunggu
     * - Saldo kas (shift aktif + mutasi)
     * - Tren perbandingan dengan periode sebelumnya
     */
    private fun bentukUlang() {
        val sekarang = Calendar.getInstance()

        val txHariIni = daftarTransaction60.filterTgl(sekarang, 0, 0)
        val txKemarin = daftarTransaction60.filterTgl(sekarang, -1, -1)
        val txMingguIni = daftarTransaction60.filterMinggu(sekarang, 0)
        val txMingguLalu = daftarTransaction60.filterMinggu(sekarang, -1)
        val txBulanIni = daftarTransaction60.filterBulan(sekarang, 0)
        val txBulanLalu = daftarTransaction60.filterBulan(sekarang, -1)

        val txTunaiHariIni = txHariIni.filter { it.paymentMethod == PaymentMethod.Cash }
        val txQrisHariIni = txHariIni.filter { it.paymentMethod == PaymentMethod.Qris }

        // ── Hitung totals ──
        val totalHariIni = txHariIni.sumOf { it.hitungTotalAkhirTransaction() }
        val totalKemarin = txKemarin.sumOf { it.hitungTotalAkhirTransaction() }
        val totalMingguIni = txMingguIni.sumOf { it.hitungTotalAkhirTransaction() }
        val totalMingguLalu = txMingguLalu.sumOf { it.hitungTotalAkhirTransaction() }
        val totalBulanIni = txBulanIni.sumOf { it.hitungTotalAkhirTransaction() }
        val totalBulanLalu = txBulanLalu.sumOf { it.hitungTotalAkhirTransaction() }

        // ── Tren ──
        val (trenHari, trenHariPos) = hitungTren(totalHariIni, totalKemarin)
        val (trenMinggu, trenMingguPos) = hitungTren(totalMingguIni, totalMingguLalu)
        val (trenBulan, trenBulanPos) = hitungTren(totalBulanIni, totalBulanLalu)

        // ── Analytics ──
        val rataWaktu = hitungRataWaktuTunggu(txHariIni)
        val produkTerlaris = hitungProdukTerlaris(txHariIni, daftarProduk, 5)
        val dailySales = hitungDailySales(daftarTransaction60, sekarang)
        val totalDiskon = txHariIni.sumOf { it.potongan.nilaiRupiah }
        val rataRata = if (txHariIni.isNotEmpty()) totalHariIni / txHariIni.size else 0L

        // ── Saldo kas ──
        val saldoKas = if (shiftKas != null) {
            val tunaiHariCount = txTunaiHariIni.sumOf { it.hitungTotalAkhirTransaction() }
            val pemasukan = daftarMutasiKas.filter { it.tipe == CashMutationType.Pemasukan }
                .sumOf { it.nominal.nilaiRupiah }
            val pengeluaran = daftarMutasiKas.filter { it.tipe == CashMutationType.Pengeluaran }
                .sumOf { it.nominal.nilaiRupiah }
            (shiftKas!!.saldoAwal.nilaiRupiah + tunaiHariCount + pemasukan - pengeluaran - totalSetoranShiftAktif).sebagaiRupiah()
        } else "-"

        // ── Jumlah item ──
        val produkHari = txHariIni.sumOf { t -> t.daftarCartItem.sumOf { it.jumlah } }
        val produkMinggu = txMingguIni.sumOf { t -> t.daftarCartItem.sumOf { it.jumlah } }
        val produkBulan = txBulanIni.sumOf { t -> t.daftarCartItem.sumOf { it.jumlah } }

        _modelTampilan.value = DashboardUiState(
            saldoKasSaatIni = saldoKas,
            apakahKasAktif = shiftKas != null,
            apakahSedangMemuat = false,
            totalPenjualanHariIni = totalHariIni.sebagaiRupiah(),
            totalPenjualanMingguIni = totalMingguIni.sebagaiRupiah(),
            totalPenjualanBulanIni = totalBulanIni.sebagaiRupiah(),
            jumlahTransactionHariIni = "${txHariIni.size} Transaction",
            jumlahTransactionMingguIni = "${txMingguIni.size} Transaction",
            jumlahTransactionBulanIni = "${txBulanIni.size} Transaction",
            totalProdukTerjualHariIni = produkHari,
            totalProdukTerjualMingguIni = produkMinggu,
            totalProdukTerjualBulanIni = produkBulan,
            totalTunaiHariIni = txTunaiHariIni.sumOf { it.hitungTotalAkhirTransaction() }.sebagaiRupiah(),
            totalQrisHariIni = txQrisHariIni.sumOf { it.hitungTotalAkhirTransaction() }.sebagaiRupiah(),
            jumlahTransactionTunaiHariIni = txTunaiHariIni.size,
            jumlahTransactionQrisHariIni = txQrisHariIni.size,
            rataWaktuTungguHariIni = rataWaktu,
            jumlahAntrianPending = daftarPending.size,
            jumlahAntrianDiproses = daftarDiproses.size,
            produkTerlaris = produkTerlaris,
            trenHariIni = trenHari,
            trenMingguIni = trenMinggu,
            trenBulanIni = trenBulan,
            apakahTrenHariIniPositif = trenHariPos,
            apakahTrenMingguIniPositif = trenMingguPos,
            apakahTrenBulanIniPositif = trenBulanPos,
            rataRataPerTransaction = rataRata.sebagaiRupiah(),
            totalDiskonHariIni = totalDiskon.sebagaiRupiah(),
            dailySalesBreakdown = dailySales,
        )
    }

    // ═══════════════════════════════════════
    // DAILY SALES CHART
    // ═══════════════════════════════════════

    private fun hitungDailySales(transactions: List<Transaction>, sekarang: Calendar): List<DailySalesData> {
        val namaHari = listOf("Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab")
        return (6 downTo 0).map { i ->
            val hari = Calendar.getInstance().apply {
                timeInMillis = sekarang.timeInMillis
                add(Calendar.DAY_OF_MONTH, -i)
            }
            val awal = Calendar.getInstance().apply {
                timeInMillis = hari.timeInMillis
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val akhir = Calendar.getInstance().apply {
                timeInMillis = hari.timeInMillis
                set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
            }.timeInMillis

            val total = transactions
                .filter { t -> t.waktuTransactionEpochMili in awal..akhir }
                .sumOf { it.hitungTotalAkhirTransaction() }
            val label = if (i == 0) "Hari ini" else namaHari[hari.get(Calendar.DAY_OF_WEEK) - 1]

            DailySalesData(label = label, amount = total, isToday = i == 0)
        }
    }

    private fun hitungRataWaktuTunggu(transactions: List<Transaction>): String {
        val valid = transactions.filter { it.waktuSelesaiEpochMili != null || it.waktuDibayarEpochMili != null }
        if (valid.isEmpty()) return "-"
        val totalMenit = valid.sumOf { t ->
            val akhir = t.waktuSelesaiEpochMili ?: t.waktuDibayarEpochMili ?: 0L
            (akhir - t.waktuTransactionEpochMili) / 60_000
        }
        val rata = totalMenit / valid.size
        return if (rata < 1) "<1 mnt" else "$rata mnt"
    }

    private fun hitungProdukTerlaris(
        transactions: List<Transaction>,
        daftarProduk: List<Produk>,
        batas: Int,
    ): List<ProdukTerlaris> {
        val produkMap = daftarProduk.associateBy { it.id }
        val penjualan = mutableMapOf<String, Int>()

        transactions.forEach { t ->
            t.daftarCartItem.forEach { item ->
                penjualan[item.produk.id] = (penjualan[item.produk.id] ?: 0) + item.jumlah
            }
        }

        val sorted = penjualan.entries.sortedByDescending { it.value }.take(batas)
        val maxJumlah = sorted.firstOrNull()?.value ?: return emptyList()

        return sorted.mapIndexed { index, (id, jumlah) ->
            ProdukTerlaris(
                ranking = index + 1,
                nama = produkMap[id]?.nama ?: id,
                jumlah = jumlah,
                persentase = if (maxJumlah > 0) jumlah.toFloat() / maxJumlah else 0f,
            )
        }
    }

    private fun hitungTren(totalSekarang: Long, totalSebelumnya: Long): Pair<String, Boolean> {
        if (totalSebelumnya == 0L) {
            return if (totalSekarang > 0L) Pair("+100%", true) else Pair("0%", true)
        }
        val perubahan = ((totalSekarang - totalSebelumnya).toDouble() / totalSebelumnya.toDouble()) * 100.0
        val format = "${if (perubahan >= 0) "+" else ""}${String.format("%.0f", perubahan)}%"
        return Pair(format, perubahan >= 0)
    }
}

// ═══════════════════════════════════════
// Extension functions untuk filter tanggal
// ═══════════════════════════════════════

private fun List<Transaction>.filterTgl(sekarang: Calendar, hariOffsetMulai: Int, hariOffsetSelesai: Int): List<Transaction> {
    val mulai = Calendar.getInstance().apply {
        timeInMillis = sekarang.timeInMillis
        add(Calendar.DAY_OF_MONTH, hariOffsetMulai)
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    val selesai = Calendar.getInstance().apply {
        timeInMillis = sekarang.timeInMillis
        add(Calendar.DAY_OF_MONTH, hariOffsetSelesai)
        set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
    }.timeInMillis
    return filter { it.waktuTransactionEpochMili in mulai..selesai }
}

private fun List<Transaction>.filterMinggu(sekarang: Calendar, mingguOffset: Int): List<Transaction> {
    val acuan = Calendar.getInstance().apply {
        timeInMillis = sekarang.timeInMillis
        firstDayOfWeek = Calendar.MONDAY
        add(Calendar.WEEK_OF_YEAR, mingguOffset)
    }
    val mulai = Calendar.getInstance().apply {
        timeInMillis = acuan.timeInMillis
        set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    val selesai = Calendar.getInstance().apply {
        timeInMillis = acuan.timeInMillis
        set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
    }.timeInMillis
    return filter { it.waktuTransactionEpochMili in mulai..selesai }
}

private fun List<Transaction>.filterBulan(sekarang: Calendar, bulanOffset: Int): List<Transaction> {
    val acuan = Calendar.getInstance().apply {
        timeInMillis = sekarang.timeInMillis
        add(Calendar.MONTH, bulanOffset)
    }
    val mulai = Calendar.getInstance().apply {
        timeInMillis = acuan.timeInMillis
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    val selesai = Calendar.getInstance().apply {
        timeInMillis = acuan.timeInMillis
        set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
        set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
    }.timeInMillis
    return filter { it.waktuTransactionEpochMili in mulai..selesai }
}
