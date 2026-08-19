package id.flexi.kasir.ui.history

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.FontMetrics
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import id.flexi.kasir.domain.model.PaymentMethod
import id.flexi.kasir.domain.model.Meja
import id.flexi.kasir.domain.model.Transaction
import id.flexi.kasir.domain.model.TransactionStatus
import id.flexi.kasir.domain.repository.TransactionRepository
import id.flexi.kasir.domain.usecase.GetTableList
import id.flexi.kasir.domain.util.hitungTotalAkhirTransaction
import id.flexi.kasir.domain.util.sebagaiRupiah
import id.flexi.kasir.ui.format.hitungJumlahItemTransaction
import id.flexi.kasir.ui.format.sebagaiLabelIdentitasTransaction
import id.flexi.kasir.ui.format.sebagaiLabelWaktuTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionHistoryViewModel(
    private val transactionRepository: TransactionRepository,
    private val GetTableList: GetTableList,
) : ViewModel() {

    private val _filterTanggal = MutableStateFlow(FilterTanggalRiwayat.Semua)
    private val _tanggalMulaiKustom = MutableStateFlow<Long?>(null)
    private val _tanggalSelesaiKustom = MutableStateFlow<Long?>(null)

    /** Daftar meja (id → nomor) untuk menampilkan nama meja di kartu riwayat. */
    private val daftarMeja: StateFlow<List<Meja>> = GetTableList()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val pagingData: Flow<PagingData<RingkasanTransactionRiwayat>> =
        combine(
            _filterTanggal,
            _tanggalMulaiKustom,
            _tanggalSelesaiKustom,
        ) { filter, mulai, selesai ->
            hitungRentangFilter(filter, mulai, selesai)
        }
            .distinctUntilChanged()
            .flatMapLatest { (sejak, sampai) ->
                // Ambil paging transaksi, lalu map dengan daftar meja
                // menggunakan snapshot (daftarMeja.value) agar tidak
                // menggabungkan dua flow — menghindari pageEventFlow crash.
                transactionRepository.amatiTransactionPaged(sejak, sampai)
                    .mapLatest { pagingData ->
                        val meja = daftarMeja.value
                        val namaMeja = meja.associate { it.id to it.nomor }
                        pagingData.map { transaction ->
                            transaction.keRingkasanTransactionRiwayat(namaMeja)
                        }
                    }
            }
            .cachedIn(viewModelScope)

    val filterTanggal: StateFlow<FilterTanggalRiwayat> = _filterTanggal

    val labelRentangKustom: StateFlow<String> = combine(
        _tanggalMulaiKustom,
        _tanggalSelesaiKustom,
    ) { mulai, selesai ->
        if (mulai != null && selesai != null) {
            val fmt = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
            "${fmt.format(Date(mulai))} - ${fmt.format(Date(selesai))}"
        } else ""
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    fun perbaruiFilterTanggal(filter: FilterTanggalRiwayat) {
        _filterTanggal.value = filter
    }

    fun perbaruiFilterTanggalKustom(tanggalMulai: Long, tanggalSelesai: Long) {
        _tanggalMulaiKustom.value = tanggalMulai
        _tanggalSelesaiKustom.value = tanggalSelesai
        _filterTanggal.value = FilterTanggalRiwayat.Kustom
    }

    private fun hitungRentangFilter(
        filter: FilterTanggalRiwayat,
        tanggalMulaiKustom: Long?,
        tanggalSelesaiKustom: Long?,
    ): Pair<Long?, Long?> {
        val kal = Calendar.getInstance()
        kal.set(Calendar.HOUR_OF_DAY, 0)
        kal.set(Calendar.MINUTE, 0)
        kal.set(Calendar.SECOND, 0)
        kal.set(Calendar.MILLISECOND, 0)
        val awalHariIni = kal.timeInMillis
        val akhirHariIni = awalHariIni + 24 * 60 * 60 * 1000

        return when (filter) {
            FilterTanggalRiwayat.HariIni -> Pair(awalHariIni, akhirHariIni)
            FilterTanggalRiwayat.Kemarin -> Pair(awalHariIni - 86400000L, awalHariIni)
            FilterTanggalRiwayat.BulanIni -> {
                kal.set(Calendar.DAY_OF_MONTH, 1)
                Pair(kal.timeInMillis, null)
            }
            FilterTanggalRiwayat.Kustom -> {
                if (tanggalMulaiKustom != null && tanggalSelesaiKustom != null) {
                    Pair(tanggalMulaiKustom, tanggalSelesaiKustom + 86400000L)
                } else Pair(null, null)
            }
            FilterTanggalRiwayat.Semua -> Pair(null, null)
        }
    }

    // ── PDF Export ──

    private val _statusExport = MutableStateFlow<StatusExportPdf?>(null)
    val statusExport: StateFlow<StatusExportPdf?> = _statusExport

    fun bersihkanStatusExport() {
        _statusExport.value = null
    }

    fun exportPdf(
        context: Context,
        uri: Uri,
        namaUsaha: String = "",
        exportMulai: Long? = null,
        exportSelesai: Long? = null,
        alamat: String = "",
        tagline: String = "",
        logoUri: String = "",
    ) {
        _statusExport.value = StatusExportPdf.SedangMengexport
        viewModelScope.launch {
            try {
                val namaToko = namaUsaha.ifBlank { "FLEXI KASIR" }

                val (sejak, sampai) = if (exportMulai != null && exportSelesai != null) {
                    Pair(exportMulai, exportSelesai)
                } else {
                    hitungRentangFilter(
                        _filterTanggal.value,
                        _tanggalMulaiKustom.value,
                        _tanggalSelesaiKustom.value,
                    )
                }

                // Ambil langsung dari SQL saat rentang lengkap (hemat memori).
                val semuaTransaksi = if (sejak != null && sampai != null) {
                    transactionRepository.ambilTransactionRentang(sejak, sampai)
                } else {
                    transactionRepository.amatiTransactionLunas().first()
                }
                val filtered = semuaTransaksi.filter { t ->
                    (sejak == null || t.waktuTransactionEpochMili >= sejak) &&
                        (sampai == null || t.waktuTransactionEpochMili < sampai)
                }
                val daftarAktif = filtered.filter { !it.dibatalkan }
                if (daftarAktif.isEmpty()) {
                    _statusExport.value = StatusExportPdf.Gagal("Tidak ada data untuk diexport")
                    return@launch
                }

                val fmt = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
                val grouped = daftarAktif
                    .sortedByDescending { it.waktuTransactionEpochMili }
                    .groupBy { fmt.format(Date(it.waktuTransactionEpochMili)) }
                val fmtCetak = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
                val waktuCetak = fmtCetak.format(Date())

                // ─── Load logo bitmap (try cache, then content resolver) ───
                val logoBitmap = if (logoUri.isNotBlank()) {
                    try {
                        // Try cached file in internal cache dir
                        val cacheFile = File(context.cacheDir, "logo_cached.png")
                        if (cacheFile.exists()) {
                            BitmapFactory.decodeFile(cacheFile.absolutePath)
                        } else {
                            // Fallback to content resolver (needs Activity context with URI permission)
                            val u = Uri.parse(logoUri)
                            context.contentResolver.openInputStream(u)?.use { input ->
                                BitmapFactory.decodeStream(input)
                            }
                        }
                    } catch (_: Exception) { null }
                } else null

                // ─── Generate PDF via Canvas ───
                val pdfHasil = withContext(Dispatchers.IO) {
                    PdfGenerator().generate(
                        namaToko = namaToko,
                        alamat = alamat,
                        tagline = tagline,
                        logoBitmap = logoBitmap,
                        periodeLabel = periodeLabel(exportMulai, exportSelesai),
                        grouped = grouped,
                        daftarAktif = daftarAktif,
                        waktuCetak = waktuCetak,
                    )
                }

                // ─── Write to file ───
                withContext(Dispatchers.IO) {
                    context.contentResolver.openFileDescriptor(uri, "w")?.use { pfd ->
                        FileOutputStream(pfd.fileDescriptor).use { output ->
                            pdfHasil.writeTo(output)
                        }
                        pdfHasil.close()
                    } ?: throw Exception("Gagal membuka file")
                }

                _statusExport.value = StatusExportPdf.Berhasil
            } catch (e: Exception) {
                _statusExport.value = StatusExportPdf.Gagal("Gagal: ${e.message}")
            }
        }
    }

    private fun periodeLabel(
        exportMulai: Long?,
        exportSelesai: Long?,
    ): String {
        val fmt = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
        return if (exportMulai != null && exportSelesai != null) {
            val fmtP = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
            "${fmtP.format(Date(exportMulai))} - ${fmtP.format(Date(exportSelesai - 86400000L))}"
        } else {
            when (_filterTanggal.value) {
                FilterTanggalRiwayat.HariIni -> "Hari Ini (${fmt.format(Date())})"
                FilterTanggalRiwayat.Kemarin -> "Kemarin (${fmt.format(Date(System.currentTimeMillis() - 86400000L))})"
                FilterTanggalRiwayat.BulanIni -> {
                    val fmtB = SimpleDateFormat("MMMM yyyy", Locale("id", "ID"))
                    fmtB.format(Date())
                }
                FilterTanggalRiwayat.Kustom -> labelRentangKustom.value.ifBlank { "Kustom" }
                FilterTanggalRiwayat.Semua -> "Semua Riwayat"
            }
        }
    }
}

// ═══════════════════════════════════════
// PDF GENERATOR — Canvas-based, layout profesional
// ═══════════════════════════════════════

private class PdfGenerator {

    companion object {
        // A4 @ 72 DPI
        private const val PW = 595
        private const val PH = 842
        private const val MG = 30f
        private const val COL_PAD = 5f
    }

    private val xL = MG
    private val xR = PW - MG
    private val CW = xR - xL

    // Column widths (proportional) — sum + COL_PAD*5 = CW
    private val colWidths = floatArrayOf(
        20f,
        38f,
        52f,
        38f,
        CW - 20f - 38f - 52f - 38f - 84f - 25f,
        84f,
    )
    private val colX = floatArrayOf(
        xL,
        xL + colWidths[0] + COL_PAD,
        xL + colWidths[0] + colWidths[1] + COL_PAD * 2,
        xL + colWidths[0] + colWidths[1] + colWidths[2] + COL_PAD * 3,
        xL + colWidths[0] + colWidths[1] + colWidths[2] + colWidths[3] + COL_PAD * 4,
        xL + colWidths[0] + colWidths[1] + colWidths[2] + colWidths[3] + colWidths[4] + COL_PAD * 5,
    )
    // Last column right edge
    private val colEnd = colX[5] + colWidths[5]

    // Fonts dengan metrik proper
    private val titleFont = Paint().apply {
        typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD); textSize = 18f
        color = 0xFF1A1A1A.toInt()
    }
    private val alamatFont = Paint().apply { textSize = 8f; color = 0xFF777777.toInt() }
    private val taglineFont = Paint().apply { textSize = 9f; color = 0xFF666666.toInt(); isFakeBoldText = true }
    private val sectionFont = Paint().apply { textSize = 11f; color = 0xFF2E7D32.toInt(); isFakeBoldText = true }
    private val periodeFont = Paint().apply { textSize = 8f; color = 0xFF888888.toInt() }
    private val monoFont = Paint().apply { textSize = 8.5f; color = 0xFF333333.toInt(); typeface = Typeface.MONOSPACE }
    private val monoBold = Paint().apply { textSize = 8.5f; color = 0xFF333333.toInt(); typeface = Typeface.MONOSPACE; isFakeBoldText = true }
    private val greenFont = Paint().apply { textSize = 8.5f; color = 0xFF2E7D32.toInt(); isFakeBoldText = true }
    private val totalFont = Paint().apply { textSize = 11f; color = 0xFF2E7D32.toInt(); isFakeBoldText = true }
    private val labelFont = Paint().apply { textSize = 8f; color = 0xFF666666.toInt() }
    private val footerFont = Paint().apply { textSize = 7f; color = 0xFFAAAAAA.toInt() }
    private val badgeFont = Paint().apply { textSize = 7.5f; color = 0xFF888888.toInt() }

    private val greenLine = Paint().apply { color = 0xFF2E7D32.toInt(); strokeWidth = 2f }
    private val thinGreen = Paint().apply { color = 0xFF2E7D32.toInt(); strokeWidth = 1f }
    private val grayLine = Paint().apply { color = 0xFFE0E0E0.toInt(); strokeWidth = 0.5f }
    private val sumFill = Paint().apply { color = 0xFFF7FBF7.toInt(); style = Paint.Style.FILL }
    private val sumBorder = Paint().apply { color = 0xFFC8E6C9.toInt(); strokeWidth = 1f; style = Paint.Style.STROKE }
    private val headerBg = Paint().apply { color = 0xFFE8F5E9.toInt(); style = Paint.Style.FILL }
    private val headerTxt = Paint().apply { textSize = 7.5f; color = 0xFF2E7D32.toInt(); isFakeBoldText = true }
    private val colLine = Paint().apply { color = 0xFFDBDBDB.toInt(); strokeWidth = 0.5f }
    private val bandBg = Paint().apply { color = 0xFFF4FAF4.toInt(); style = Paint.Style.FILL }
    private val totalFill = Paint().apply { color = 0xFFE8F5E9.toInt(); style = Paint.Style.FILL }
    private val rowEvenBg = Paint().apply { color = 0xFFFFFFFF.toInt(); style = Paint.Style.FILL }
    private val rowOddBg = Paint().apply { color = 0xFFF8FAF8.toInt(); style = Paint.Style.FILL }

    // Cached font metrics untuk tinggi baris
    private val titleFm: FontMetrics = titleFont.fontMetrics
    private val normFm: FontMetrics = monoFont.fontMetrics
    private val sectionFm: FontMetrics = sectionFont.fontMetrics

    private val titleH = (-titleFm.ascent + titleFm.descent + titleFm.leading + 4f)
    private val normH = (-normFm.ascent + normFm.descent + normFm.leading + 5f)
    private val sectionH = (-sectionFm.ascent + sectionFm.descent + sectionFm.leading + 4f)

    private var doc: PdfDocument? = null
    private var canvas: Canvas? = null
    private var currentPage: PdfDocument.Page? = null
    private var y = 0f
    private var pageNum = 0

    private class SelRingkasan(val label: String?, val nilai: String?)

    fun generate(
        namaToko: String, alamat: String, tagline: String,
        logoBitmap: Bitmap?, periodeLabel: String,
        grouped: Map<String, List<Transaction>>,
        daftarAktif: List<Transaction>, waktuCetak: String,
    ): PdfDocument {
        doc = PdfDocument()
        pageNum = 0

        // --- Compute totals ---
        val totalPenjualan = daftarAktif.sumOf { it.hitungTotalAkhirTransaction() }
        val totalItem = daftarAktif.sumOf { it.hitungJumlahItemTransaction() }
        var totalTunai = 0L; var totalQris = 0L
        daftarAktif.forEach { t ->
            if (t.paymentMethod == PaymentMethod.Cash) totalTunai += t.hitungTotalAkhirTransaction()
            else totalQris += t.hitungTotalAkhirTransaction()
        }
        val adaNonTunai = totalQris > 0L
        val fmtJam = SimpleDateFormat("HH:mm", Locale("id", "ID"))

        // =============== PAGE 1 HEADER ===============
        bukaHalaman()
        gambarHeader(namaToko, alamat, tagline, logoBitmap, periodeLabel)

        // =============== SUMMARY — grid 2 kolom yang ringkas ===============
        val selTransaksi = SelRingkasan("Total Transaksi", daftarAktif.size.toString())
        val selPenjualan = SelRingkasan("Total Penjualan", totalPenjualan.sebagaiRupiah())
        val selItem = SelRingkasan("Total Item", totalItem.toString())
        val selTunai = SelRingkasan("Tunai", totalTunai.sebagaiRupiah())
        val selQris = SelRingkasan("QRIS", totalQris.sebagaiRupiah())

        val barisRingkasan = buildList<Pair<SelRingkasan, SelRingkasan>> {
            add(selTransaksi to selPenjualan)
            add(selItem to selTunai)
            if (adaNonTunai) add(selQris to SelRingkasan(null, null))
        }
        gambarRingkasan(barisRingkasan)

        // =============== TABLE HEADER ===============
        cekHalaman(sectionH + 2f)
        val headers = listOf("No", "Jam", "ID", "Metode", "Item", "Total")
        c.drawRect(xL, y - 4f, colEnd, y + normH, headerBg)
        garisKolom(y - 4f, y + normH)

        headers.forEachIndexed { idx, h ->
            val p = headerTxt
            val cx = colX[idx]
            val cw = colWidths[idx]
            val isRight = idx == 5
            val isCenter = idx <= 3
            val hw = p.measureText(h)
            val dx = when {
                isRight -> cx + cw - hw - 2f
                isCenter -> cx + (cw - hw) / 2f
                else -> cx + 2f
            }
            c.drawText(h, dx, y + normH - 3f, p)
        }
        c.drawLine(xL, y + normH + 1f, colEnd, y + normH + 1f, greenLine)
        y += normH + 5f

        // =============== TRANSACTION ROWS ===============
        var noUrut = 0
        grouped.forEach { (tglStr, daftar) ->
            val subTotal = daftar.sumOf { it.hitungTotalAkhirTransaction() }

            // Date band (tanggal kiri, subtotal kanan)
            cekHalaman(normH + 6f)
            val bandH = normH + 4f
            c.drawRect(xL, y - 4f, colEnd, y + bandH, bandBg)
            c.drawText(tglStr, xL + 3f, y + normH - 3f, greenFont)
            val stStr = subTotal.sebagaiRupiah(); val stW = greenFont.measureText(stStr)
            c.drawText(stStr, colEnd - stW - 3f, y + normH - 3f, greenFont)
            y += bandH + 5f

            daftar.forEach { t ->
                val jam = fmtJam.format(Date(t.waktuTransactionEpochMili))
                val idP = t.id.take(8).uppercase()
                val metode = if (t.paymentMethod == PaymentMethod.Cash) "Tunai" else "QRIS"
                val itemStr = ringkasanItem(t)
                val totalStr = t.hitungTotalAkhirTransaction().sebagaiRupiah()
                noUrut++

                cekHalaman(normH + 2f)

                // Row bg + vertical grid
                val rBg = if (noUrut % 2 == 0) rowEvenBg else rowOddBg
                c.drawRect(xL, y - 4f, colEnd, y + normH, rBg)
                garisKolom(y - 4f, y + normH)

                // Draw each cell
                val cells = listOf(noUrut.toString() to 0, jam to 1, idP to 2, metode to 3, itemStr to 4, totalStr to 5)
                cells.forEach { (teks, colIdx) ->
                    val p = if (colIdx == 2) monoFont else if (colIdx == 4 || colIdx == 5) monoBold else monoFont
                    val cx = colX[colIdx]
                    val cw = colWidths[colIdx]
                    val isRight = colIdx == 5
                    val isCenter = colIdx <= 3
                    val tw = p.measureText(teks)
                    val finalTeks = if (tw > cw - 4f) {
                        val maxChars = ((cw - 4f) / p.measureText("W")).toInt().coerceIn(1, teks.length - 1)
                        teks.take(maxChars - 1) + ".."
                    } else teks
                    val fw = p.measureText(finalTeks)
                    val dx = when {
                        isRight -> cx + cw - fw - 2f
                        isCenter -> cx + (cw - fw) / 2f
                        else -> cx + 2f
                    }
                    c.drawText(finalTeks, dx, y + normH - 3f, p)
                }
                y += normH + 1f
            }
        }

        // =============== GRAND TOTAL ===============
        y += 8f
        cekHalaman(sectionH + 14f)
        val boxH = sectionH + 8f
        c.drawRect(xL, y - 2f, colEnd, y + boxH, totalFill)
        c.drawLine(xL, y - 2f, colEnd, y - 2f, greenLine)
        c.drawText("TOTAL", xL + 4f, y + sectionH - 4f, totalFont)
        val gtStr = totalPenjualan.sebagaiRupiah(); val gtW = totalFont.measureText(gtStr)
        c.drawText(gtStr, colEnd - gtW - 4f, y + sectionH - 4f, totalFont)
        y += boxH + 5f

        // =============== PAYMENT FOOTER ===============
        val payStr = buildString {
            append("Tunai ${totalTunai.sebagaiRupiah()}")
            if (adaNonTunai) append("   |   QRIS ${totalQris.sebagaiRupiah()}")
        }
        cekHalaman(normH + 4f)
        c.drawText(payStr, xL, y + normH - 3f, badgeFont)
        y += normH + 5f
        cekHalaman(normH + 2f)
        c.drawLine(xL, y - 1f, colEnd, y - 1f, grayLine)
        c.drawText("Dicetak: $waktuCetak", xL, y + normH - 2f, footerFont)
        val wwwW = footerFont.measureText("www.flexikasir.id")
        c.drawText("www.flexikasir.id", colEnd - wwwW, y + normH - 2f, footerFont)

        selesaikanHalaman()
        return doc!!
    }

    // ── Helpers ──

    private val c: Canvas get() = canvas!!
    private val d: PdfDocument get() = doc!!

    private fun bukaHalaman() {
        val info = PdfDocument.PageInfo.Builder(PW, PH, pageNum + 1).create()
        currentPage = d.startPage(info)
        canvas = currentPage!!.canvas
        y = MG + 4f
        pageNum++
    }

    // ── Header: logo & identitas toko di tengah ──
    private fun gambarHeader(
        namaToko: String, alamat: String, tagline: String,
        logoBitmap: Bitmap?, periodeLabel: String,
    ) {
        val pageMid = (xL + xR) / 2f
        var cursorY = y

        // Logo di tengah, nama toko di bawahnya (semua center)
        if (logoBitmap != null) {
            val logoSize = 36
            val scaled = Bitmap.createScaledBitmap(logoBitmap, logoSize, logoSize, true)
            c.drawBitmap(scaled, pageMid - logoSize / 2f, cursorY, null)
            if (scaled != logoBitmap) scaled.recycle()
            cursorY += logoSize + 8f
        }

        val namaW = titleFont.measureText(namaToko)
        c.drawText(namaToko, pageMid - namaW / 2f, cursorY - titleFm.ascent, titleFont)
        cursorY += titleH

        if (tagline.isNotBlank()) {
            val taglineTeks = tagline.take(80)
            val tagW = taglineFont.measureText(taglineTeks)
            c.drawText(taglineTeks, pageMid - tagW / 2f, cursorY - normFm.ascent, taglineFont)
            cursorY += normH
        }

        if (alamat.isNotBlank()) {
            alamat.lines().forEach { baris ->
                val b = baris.trim()
                if (b.isNotEmpty()) {
                    val teks = b.take(90)
                    val aW = alamatFont.measureText(teks)
                    c.drawText(teks, pageMid - aW / 2f, cursorY - normFm.ascent, alamatFont)
                    cursorY += normH
                }
            }
        }

        cursorY += 6f
        y = cursorY

        // Garis pemisah tebal
        c.drawLine(xL, y, xR, y, greenLine)
        y += 7f

        // Judul bagian + periode
        c.drawText("RIWAYAT TRANSAKSI", xL, y + sectionH - 3f, sectionFont)
        if (periodeLabel.isNotBlank()) {
            val pw = periodeFont.measureText(periodeLabel)
            c.drawText(periodeLabel, xR - pw, y + sectionH - 3f, periodeFont)
        }
        y += sectionH + 2f
        c.drawLine(xL, y, xR, y, thinGreen)
        y += 7f
    }

    // ── Ringkasan ringkas: grid 2 kolom dengan kotak hijau kecil ──
    private fun gambarRingkasan(baris: List<Pair<SelRingkasan, SelRingkasan>>) {
        val midX = (xL + xR) / 2f
        val colGap = 24f
        val leftLabelX = xL + 12f
        val leftValX = midX - colGap / 2f
        val rightLabelX = midX + colGap / 2f
        val rightValX = xR - 12f

        val rowH = 13f
        val padV = 8f
        val boxHeight = baris.size * rowH + padV * 2

        cekHalaman(boxHeight + 8f)
        val boxTop = y
        c.drawRoundRect(xL, boxTop, xR, boxTop + boxHeight, 4f, 4f, sumFill)
        c.drawRoundRect(xL, boxTop, xR, boxTop + boxHeight, 4f, 4f, sumBorder)

        val baseline = -normFm.ascent
        baris.forEachIndexed { i, (kiri, kanan) ->
            val rowY = boxTop + padV + i * rowH
            kiri.label?.let { c.drawText(it, leftLabelX, rowY + baseline, labelFont) }
            kiri.nilai?.let {
                val vw = monoBold.measureText(it)
                c.drawText(it, leftValX - vw, rowY + baseline, monoBold)
            }
            kanan.label?.let { c.drawText(it, rightLabelX, rowY + baseline, labelFont) }
            kanan.nilai?.let {
                val vw = monoBold.measureText(it)
                c.drawText(it, rightValX - vw, rowY + baseline, monoBold)
            }
        }

        y = boxTop + boxHeight + 10f
    }

    private fun cekHalaman(minSpace: Float) {
        val avail = PH - MG - y
        if (avail < minSpace) {
            selesaikanHalaman()
            bukaHalaman()

            c.drawText("Riwayat Transaksi (hlm $pageNum)", xL, y + normH - 3f, sectionFont)
            c.drawLine(xL, y + normH + 1f, colEnd, y + normH + 1f, thinGreen)
            y += normH + 6f
        }
    }

    private fun garisKolom(yAtas: Float, yBawah: Float) {
        colX.forEach { x -> c.drawLine(x, yAtas, x, yBawah, colLine) }
        c.drawLine(colEnd, yAtas, colEnd, yBawah, colLine)
    }

    private fun selesaikanHalaman() {
        val fy = PH - 12f
        c.drawLine(xL, fy - 7f, xR, fy - 7f, grayLine)
        c.drawText("www.flexikasir.id", xL, fy, footerFont)
        val hlm = "hlm ${pageNum}"
        val hw = footerFont.measureText(hlm)
        c.drawText(hlm, xR - hw, fy, footerFont)
        d.finishPage(currentPage!!)
    }

    private fun ringkasanItem(t: Transaction): String {
        val items = t.daftarCartItem.map { item ->
            val qty = if (item.jumlah > 1) " x${item.jumlah}" else ""
            "${item.produk.nama}$qty"
        }
        if (items.isEmpty()) return "-"
        val firstTwo = items.take(2).joinToString(", ")
        val extra = items.size - 2
        return if (extra > 0) "$firstTwo +$extra lainnya" else firstTwo
    }
}

// ═══════════════════════════════════════
// EXTENSION FUNCTIONS
// ═══════════════════════════════════════

internal fun Transaction.keRingkasanTransactionRiwayat(
    namaMeja: Map<String, String> = emptyMap(),
): RingkasanTransactionRiwayat {
    return RingkasanTransactionRiwayat(
        TransactionId = id,
        waktuTransactionEpochMili = waktuTransactionEpochMili,
        labelIdentitasTransaction = id.sebagaiLabelIdentitasTransaction(),
        labelWaktu = waktuTransactionEpochMili.sebagaiLabelWaktuTransaction(),
        // Nama meja diselesaikan dari id meja (dipakai label riwayat lintas
        // perangkat setelah pull; Take Away tanpa meja → null).
        labelMeja = if (mejaId != null) {
            namaMeja[mejaId]?.let { "Meja $it" }
        } else null,
        labelJumlahItem = "${hitungJumlahItemTransaction()} item",
        totalAkhir = hitungTotalAkhirTransaction(),
        labelTotalAkhir = hitungTotalAkhirTransaction().sebagaiRupiah(),
        ringkasanItem = bentukRingkasanItem(),
        paymentMethod = paymentMethod,
        status = status,
        dibatalkan = dibatalkan,
    )
}

private fun Transaction.bentukRingkasanItem(): String {
    val daftarItem = daftarCartItem.map { CartItem ->
        val qty = if (CartItem.jumlah > 1) " x${CartItem.jumlah}" else ""
        "${CartItem.produk.nama}$qty"
    }
    if (daftarItem.isEmpty()) return "Tanpa item"
    val duaItemPertama = daftarItem.take(2).joinToString(separator = ", ")
    val sisaItem = daftarItem.size - 2
    return if (sisaItem > 0) "$duaItemPertama +$sisaItem lainnya" else duaItemPertama
}

sealed interface StatusExportPdf {
    data object SedangMengexport : StatusExportPdf
    data object Berhasil : StatusExportPdf
    data class Gagal(val pesan: String) : StatusExportPdf
}
