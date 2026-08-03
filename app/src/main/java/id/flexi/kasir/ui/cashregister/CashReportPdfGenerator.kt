package id.flexi.kasir.ui.cashregister

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import id.flexi.kasir.domain.model.CashKas
import id.flexi.kasir.domain.model.CashMutation
import id.flexi.kasir.domain.model.CashMutationType
import id.flexi.kasir.domain.model.PaymentMethod
import id.flexi.kasir.domain.model.Setoran
import id.flexi.kasir.domain.model.Transaction
import id.flexi.kasir.domain.util.hitungTotalAkhirTransaction
import id.flexi.kasir.domain.util.sebagaiRupiah
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal class BarisKasExport(
    val shift: CashKas,
    val penjualanTunai: Long,
    val penjualanQRIS: Long,
    val totalPemasukan: Long,
    val totalPengeluaran: Long,
) {
    val penjualan: Long get() = penjualanTunai + penjualanQRIS
}

// ═══════════════════════════════════════
// PDF GENERATOR KAS — Canvas, gaya konsisten dengan Riwayat Transaksi
// ═══════════════════════════════════════

internal class CashReportPdfGenerator {

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

    // Fonts
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
    private val monoFontKecil = Paint().apply { textSize = 7.5f; color = 0xFF333333.toInt(); typeface = Typeface.MONOSPACE }
    private val monoBoldKecil = Paint().apply { textSize = 7.5f; color = 0xFF333333.toInt(); typeface = Typeface.MONOSPACE; isFakeBoldText = true }
    private val greenFont = Paint().apply { textSize = 8.5f; color = 0xFF2E7D32.toInt(); isFakeBoldText = true }
    private val totalFont = Paint().apply { textSize = 11f; color = 0xFF2E7D32.toInt(); isFakeBoldText = true }
    private val totalFontKecil = Paint().apply { textSize = 9.5f; color = 0xFF2E7D32.toInt(); isFakeBoldText = true }
    private val labelFont = Paint().apply { textSize = 8f; color = 0xFF666666.toInt() }
    private val footerFont = Paint().apply { textSize = 7f; color = 0xFFAAAAAA.toInt() }

    // Paints
    private val greenLine = Paint().apply { color = 0xFF2E7D32.toInt(); strokeWidth = 2f }
    private val thinGreen = Paint().apply { color = 0xFF2E7D32.toInt(); strokeWidth = 1f }
    private val grayLine = Paint().apply { color = 0xFFE0E0E0.toInt(); strokeWidth = 0.5f }
    private val colLine = Paint().apply { color = 0xFFDBDBDB.toInt(); strokeWidth = 0.5f }
    private val sumFill = Paint().apply { color = 0xFFF7FBF7.toInt(); style = Paint.Style.FILL }
    private val sumBorder = Paint().apply { color = 0xFFC8E6C9.toInt(); strokeWidth = 1f; style = Paint.Style.STROKE }
    private val headerBg = Paint().apply { color = 0xFFE8F5E9.toInt(); style = Paint.Style.FILL }
    private val headerTxt = Paint().apply { textSize = 7.5f; color = 0xFF2E7D32.toInt(); isFakeBoldText = true }
    private val headerTxtKecil = Paint().apply { textSize = 7f; color = 0xFF2E7D32.toInt(); isFakeBoldText = true }
    private val totalFill = Paint().apply { color = 0xFFE8F5E9.toInt(); style = Paint.Style.FILL }
    private val rowEvenBg = Paint().apply { color = 0xFFFFFFFF.toInt(); style = Paint.Style.FILL }
    private val rowOddBg = Paint().apply { color = 0xFFF8FAF8.toInt(); style = Paint.Style.FILL }

    // Font metrics
    private val titleFm: Paint.FontMetrics = titleFont.fontMetrics
    private val normFm: Paint.FontMetrics = monoFont.fontMetrics
    private val sectionFm: Paint.FontMetrics = sectionFont.fontMetrics

    private val titleH = (-titleFm.ascent + titleFm.descent + titleFm.leading + 4f)
    private val normH = (-normFm.ascent + normFm.descent + normFm.leading + 5f)
    private val sectionH = (-sectionFm.ascent + sectionFm.descent + sectionFm.leading + 4f)

    private var doc: PdfDocument? = null
    private var canvas: Canvas? = null
    private var currentPage: PdfDocument.Page? = null
    private var y = 0f
    private var pageNum = 0
    private var judulHalaman = ""

    private class SelRingkasan(val label: String?, val nilai: String?)
    private class KolomTabel(
        val label: String,
        val lebar: Float,
        val rata: Int, // 0 = kiri, 1 = tengah, 2 = kanan
    )

    // ═══════════ DETAIL KAS ═══════════

    fun generateDetailKas(
        namaToko: String,
        alamat: String,
        tagline: String,
        logoBitmap: Bitmap?,
        shift: CashKas,
        saldoSaatIni: Long,
        penjualanTunai: Long,
        penjualanQRIS: Long,
        totalPemasukan: Long,
        totalPengeluaran: Long,
        daftarTransaksi: List<Transaction>,
        daftarMutasi: List<CashMutation>,
        waktuCetak: String,
    ): PdfDocument {
        doc = PdfDocument()
        pageNum = 0
        judulHalaman = "Detail Kas"

        val fmtTanggal = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
        val fmtJam = SimpleDateFormat("HH:mm", Locale("id", "ID"))
        val fmtCetak = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
        val isAktif = shift.saldoAkhir == null
        val statusLabel = if (isAktif) "Aktif" else "Selesai"

        bukaHalaman()
        gambarHeader(namaToko, alamat, tagline, logoBitmap, "DETAIL KAS", "Kas ${fmtTanggal.format(Date(shift.waktuBuka))}")

        val saldoAkhirLabel = if (isAktif) "Saldo Saat Ini" else "Saldo Akhir"
        val saldoAkhirNilai = if (isAktif) saldoSaatIni else (shift.saldoAkhir?.nilaiRupiah ?: saldoSaatIni)

        gambarRingkasan(
            listOf(
                Pair(SelRingkasan("Saldo Awal", shift.saldoAwal.nilaiRupiah.sebagaiRupiah()), SelRingkasan(saldoAkhirLabel, saldoAkhirNilai.sebagaiRupiah())),
                Pair(SelRingkasan("Penjualan Tunai", penjualanTunai.sebagaiRupiah()), SelRingkasan("Penjualan QRIS", penjualanQRIS.sebagaiRupiah())),
                Pair(SelRingkasan("Total Penjualan", (penjualanTunai + penjualanQRIS).sebagaiRupiah()), SelRingkasan("Total Pemasukan", totalPemasukan.sebagaiRupiah())),
                Pair(SelRingkasan("Total Pengeluaran", totalPengeluaran.sebagaiRupiah()), SelRingkasan("Status", statusLabel)),
            )
        )

        // Info periode (buka/tutup + durasi)
        gambarInfoPeriode(shift, fmtCetak)

        if (daftarTransaksi.isNotEmpty()) {
            gambarSectionTitle("PENJUALAN")
            gambarTabel(
                kolom = listOf(
                    KolomTabel("No", 20f, 2),
                    KolomTabel("Jam", 38f, 1),
                    KolomTabel("ID", 52f, 1),
                    KolomTabel("Metode", 38f, 1),
                    KolomTabel("Item", CW - 20f - 38f - 52f - 38f - 84f - 25f, 0),
                    KolomTabel("Total", 84f, 2),
                ),
                baris = daftarTransaksi.mapIndexed { i, t ->
                    listOf(
                        (i + 1).toString(),
                        fmtJam.format(Date(t.waktuTransactionEpochMili)),
                        t.id.take(8).uppercase(),
                        if (t.paymentMethod == PaymentMethod.Cash) "Tunai" else "QRIS",
                        ringkasanItem(t),
                        t.hitungTotalAkhirTransaction().sebagaiRupiah(),
                    )
                },
                totalRows = listOf(
                    listOf("", "", "", "", "TOTAL PENJUALAN", (penjualanTunai + penjualanQRIS).sebagaiRupiah()),
                ),
            )
        }

        if (daftarMutasi.isNotEmpty()) {
            gambarSectionTitle("MUTASI")
            gambarTabel(
                kolom = listOf(
                    KolomTabel("No", 20f, 2),
                    KolomTabel("Jam", 38f, 1),
                    KolomTabel("Tipe", 52f, 0),
                    KolomTabel("Kategori", 62f, 0),
                    KolomTabel("Nominal", 84f, 2),
                    KolomTabel("Catatan", CW - 20f - 38f - 52f - 62f - 84f - 25f, 0),
                ),
                baris = daftarMutasi.mapIndexed { i, m ->
                    listOf(
                        (i + 1).toString(),
                        fmtJam.format(Date(m.waktu)),
                        if (m.tipe == CashMutationType.Pemasukan) "Masuk" else "Keluar",
                        m.kategori.label,
                        m.nominal.nilaiRupiah.sebagaiRupiah(),
                        m.catatan.ifBlank { "-" },
                    )
                },
                totalRows = listOf(
                    listOf("", "", "TOTAL MASUK", "", totalPemasukan.sebagaiRupiah(), ""),
                    listOf("", "", "TOTAL KELUAR", "", totalPengeluaran.sebagaiRupiah(), ""),
                ),
            )
        }

        gambarCatatan(shift)

        gambarFooter(waktuCetak)
        selesaikanHalaman()
        return doc!!
    }

    // ═══════════ REKAP KAS ═══════════

    fun generateRekapKas(
        namaToko: String,
        alamat: String,
        tagline: String,
        logoBitmap: Bitmap?,
        periodeLabel: String,
        barisKas: List<BarisKasExport>,
        daftarSetoran: List<Setoran>,
        waktuCetak: String,
    ): PdfDocument {
        doc = PdfDocument()
        pageNum = 0
        judulHalaman = "Rekap Kas"

        val fmtTanggal = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
        val fmtJam = SimpleDateFormat("HH:mm", Locale("id", "ID"))

        val totalTunai = barisKas.sumOf { it.penjualanTunai }
        val totalQris = barisKas.sumOf { it.penjualanQRIS }
        val totalPemasukan = barisKas.sumOf { it.totalPemasukan }
        val totalPengeluaran = barisKas.sumOf { it.totalPengeluaran }
        val totalSetoranNilai = daftarSetoran.sumOf { it.nominal.nilaiRupiah }
        val totalSaldoAwal = barisKas.sumOf { it.shift.saldoAwal.nilaiRupiah }

        bukaHalaman()
        gambarHeader(namaToko, alamat, tagline, logoBitmap, "REKAP KAS", periodeLabel)

        gambarRingkasan(
            listOf(
                Pair(SelRingkasan("Total Shift Kas", barisKas.size.toString()), SelRingkasan("Total Setoran", daftarSetoran.size.toString())),
                Pair(SelRingkasan("Total Saldo Awal", totalSaldoAwal.sebagaiRupiah()), SelRingkasan("Total Penjualan", (totalTunai + totalQris).sebagaiRupiah())),
                Pair(SelRingkasan("Penjualan Tunai", totalTunai.sebagaiRupiah()), SelRingkasan("Penjualan QRIS", totalQris.sebagaiRupiah())),
                Pair(SelRingkasan("Total Pemasukan", totalPemasukan.sebagaiRupiah()), SelRingkasan("Total Pengeluaran", totalPengeluaran.sebagaiRupiah())),
            )
        )

        if (barisKas.isNotEmpty()) {
            gambarSectionTitle("DAFTAR KAS")
            gambarTabel(
                kolom = listOf(
                    KolomTabel("No", 18f, 2),
                    KolomTabel("Tanggal", 62f, 0),
                    KolomTabel("Buka", 26f, 1),
                    KolomTabel("Status", 32f, 1),
                    KolomTabel("Saldo Awal", 56f, 2),
                    KolomTabel("Tunai", 56f, 2),
                    KolomTabel("QRIS", 56f, 2),
                    KolomTabel("Penjualan", 56f, 2),
                    KolomTabel("Pengeluaran", 56f, 2),
                    KolomTabel("Saldo Akhir", 56f, 2),
                ),
                baris = barisKas.mapIndexed { i, b ->
                    listOf(
                        (i + 1).toString(),
                        fmtTanggal.format(Date(b.shift.waktuBuka)),
                        fmtJam.format(Date(b.shift.waktuBuka)),
                        if (b.shift.saldoAkhir == null) "Aktif" else "Selesai",
                        b.shift.saldoAwal.nilaiRupiah.sebagaiRupiah(),
                        b.penjualanTunai.sebagaiRupiah(),
                        b.penjualanQRIS.sebagaiRupiah(),
                        b.penjualan.sebagaiRupiah(),
                        b.totalPengeluaran.sebagaiRupiah(),
                        b.shift.saldoAkhir?.nilaiRupiah?.sebagaiRupiah() ?: "-",
                    )
                },
                totalRows = listOf(
                    listOf("", "", "", "TOTAL", totalSaldoAwal.sebagaiRupiah(), totalTunai.sebagaiRupiah(), totalQris.sebagaiRupiah(), (totalTunai + totalQris).sebagaiRupiah(), totalPengeluaran.sebagaiRupiah(), ""),
                ),
                fontKecil = true,
            )
        }

        if (daftarSetoran.isNotEmpty()) {
            gambarSectionTitle("SETORAN")
            gambarTabel(
                kolom = listOf(
                    KolomTabel("No", 20f, 2),
                    KolomTabel("Jam", 40f, 1),
                    KolomTabel("Catatan", CW - 20f - 40f - 84f - 15f, 0),
                    KolomTabel("Nominal", 84f, 2),
                ),
                baris = daftarSetoran.mapIndexed { i, s ->
                    listOf(
                        (i + 1).toString(),
                        fmtJam.format(Date(s.waktu)),
                        s.catatan.ifBlank { "-" },
                        s.nominal.nilaiRupiah.sebagaiRupiah(),
                    )
                },
                totalRows = listOf(
                    listOf("", "", "TOTAL SETORAN", totalSetoranNilai.sebagaiRupiah()),
                ),
            )
        }

        gambarFooter(waktuCetak)
        selesaikanHalaman()
        return doc!!
    }

    // ═══════════ HELPERS ═══════════

    private val c: Canvas get() = canvas!!
    private val d: PdfDocument get() = doc!!

    private fun bukaHalaman() {
        val info = PdfDocument.PageInfo.Builder(PW, PH, pageNum + 1).create()
        currentPage = d.startPage(info)
        canvas = currentPage!!.canvas
        y = MG + 4f
        pageNum++
    }

    private fun cekHalaman(minSpace: Float) {
        val avail = PH - MG - y
        if (avail < minSpace) {
            selesaikanHalaman()
            bukaHalaman()
            c.drawText("$judulHalaman (hlm $pageNum)", xL, y + normH - 3f, sectionFont)
            c.drawLine(xL, y + normH + 1f, xR, y + normH + 1f, thinGreen)
            y += normH + 6f
        }
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

    // ── Header toko di tengah ──
    private fun gambarHeader(
        namaToko: String,
        alamat: String,
        tagline: String,
        logoBitmap: Bitmap?,
        judul: String,
        subJudul: String,
    ) {
        val pageMid = (xL + xR) / 2f
        var cursorY = y

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

        c.drawLine(xL, y, xR, y, greenLine)
        y += 7f

        c.drawText(judul, xL, y + sectionH - 3f, sectionFont)
        if (subJudul.isNotBlank()) {
            val sw = periodeFont.measureText(subJudul)
            c.drawText(subJudul, xR - sw, y + sectionH - 3f, periodeFont)
        }
        y += sectionH + 2f
        c.drawLine(xL, y, xR, y, thinGreen)
        y += 7f
    }

    // ── Ringkasan: grid 2 kolom ──
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

    private fun gambarInfoPeriode(shift: CashKas, fmtCetak: SimpleDateFormat) {
        cekHalaman(normH + 6f)
        val periodeStr = "Periode: ${fmtCetak.format(Date(shift.waktuBuka))} - " +
            if (shift.waktuTutup != null) fmtCetak.format(Date(shift.waktuTutup)) else "Masih aktif"
        c.drawText(periodeStr, xL, y + normH - 3f, monoFont)
        val durasiStr = "Durasi: ${durasiKas(shift)}"
        val dW = monoFont.measureText(durasiStr)
        c.drawText(durasiStr, xR - dW, y + normH - 3f, monoFont)
        y += normH + 4f
        c.drawLine(xL, y, xR, y, grayLine)
        y += 6f
    }

    private fun gambarSectionTitle(t: String) {
        cekHalaman(sectionH + 4f)
        y += 4f
        c.drawText(t, xL, y + sectionH - 3f, sectionFont)
        y += sectionH + 2f
        c.drawLine(xL, y, xR, y, thinGreen)
        y += 6f
    }

    // ── Tabel generik ──
    private fun gambarTabel(
        kolom: List<KolomTabel>,
        baris: List<List<String>>,
        totalRows: List<List<String>> = emptyList(),
        fontKecil: Boolean = false,
    ) {
        val dataFont = if (fontKecil) monoFontKecil else monoFont
        val dataBoldFont = if (fontKecil) monoBoldKecil else monoBold
        val headerPaint = if (fontKecil) headerTxtKecil else headerTxt
        val totalPaint = if (fontKecil) totalFontKecil else totalFont

        val xs = FloatArray(kolom.size)
        var acc = xL
        kolom.forEachIndexed { i, k ->
            xs[i] = acc
            acc += k.lebar + COL_PAD
        }
        val colEndX = acc - COL_PAD

        // Header
        cekHalaman(normH + 6f)
        c.drawRect(xL, y - 4f, colEndX, y + normH, headerBg)
        garisKolomTabel(xs, colEndX, y - 4f, y + normH)
        kolom.forEachIndexed { i, k ->
            val hw = headerPaint.measureText(k.label)
            val dx = when (k.rata) {
                0 -> xs[i] + 2f
                1 -> xs[i] + (k.lebar - hw) / 2f
                else -> xs[i] + k.lebar - hw - 2f
            }
            c.drawText(k.label, dx, y + normH - 3f, headerPaint)
        }
        c.drawLine(xL, y + normH + 1f, colEndX, y + normH + 1f, greenLine)
        y += normH + 5f

        // Baris data
        baris.forEachIndexed { idx, sel ->
            cekHalaman(normH + 2f)
            val rBg = if (idx % 2 == 0) rowEvenBg else rowOddBg
            c.drawRect(xL, y - 4f, colEndX, y + normH, rBg)
            garisKolomTabel(xs, colEndX, y - 4f, y + normH)

            sel.forEachIndexed { i, teks ->
                val k = kolom[i]
                val p = if (i == kolom.size - 1) dataBoldFont else dataFont
                val fw = teksPas(teks, p, k.lebar - 4f)
                val finalW = p.measureText(fw)
                val dx = when (k.rata) {
                    0 -> xs[i] + 2f
                    1 -> xs[i] + (k.lebar - finalW) / 2f
                    else -> xs[i] + k.lebar - finalW - 2f
                }
                c.drawText(fw, dx, y + normH - 3f, p)
            }
            y += normH + 1f
        }

        // Baris total
        totalRows.forEachIndexed { _, sel ->
            cekHalaman(normH + 12f)
            y += 4f
            val boxH = sectionH + 6f
            c.drawRect(xL, y - 2f, colEndX, y + boxH, totalFill)
            c.drawLine(xL, y - 2f, colEndX, y - 2f, greenLine)
            sel.forEachIndexed { i, teks ->
                if (teks.isNotBlank()) {
                    val k = kolom[i]
                    val w = totalPaint.measureText(teks)
                    val dx = when (k.rata) {
                        0 -> xs[i] + 4f
                        1 -> xs[i] + (k.lebar - w) / 2f
                        else -> xs[i] + k.lebar - w - 4f
                    }
                    c.drawText(teks, dx, y + sectionH - 4f, totalPaint)
                }
            }
            y += boxH + 6f
        }
    }

    private fun garisKolomTabel(xs: FloatArray, colEndX: Float, yAtas: Float, yBawah: Float) {
        xs.forEach { x -> c.drawLine(x, yAtas, x, yBawah, colLine) }
        c.drawLine(colEndX, yAtas, colEndX, yBawah, colLine)
    }

    private fun gambarCatatan(shift: CashKas) {
        val catBuka = shift.catatanBuka
        val catTutup = shift.catatanTutup
        if (catBuka.isNullOrBlank() && catTutup.isNullOrBlank()) return
        gambarSectionTitle("CATATAN")
        if (!catBuka.isNullOrBlank()) gambarBarisCatatan("Catatan Buka", catBuka)
        if (!catTutup.isNullOrBlank()) gambarBarisCatatan("Catatan Tutup", catTutup)
    }

    private fun gambarBarisCatatan(label: String, teks: String) {
        cekHalaman(normH + 4f)
        c.drawText(label, xL, y + normH - 3f, greenFont)
        y += normH + 1f
        wrapTeks(teks, monoFont, CW - 4f).forEach { baris ->
            cekHalaman(normH + 2f)
            c.drawText(baris, xL, y + normH - 3f, monoFont)
            y += normH + 1f
        }
        y += 4f
    }

    private fun gambarFooter(waktuCetak: String) {
        cekHalaman(normH + 2f)
        c.drawLine(xL, y - 1f, xR, y - 1f, grayLine)
        c.drawText("Dicetak: $waktuCetak", xL, y + normH - 2f, footerFont)
        val wwwW = footerFont.measureText("www.flexikasir.id")
        c.drawText("www.flexikasir.id", xR - wwwW, y + normH - 2f, footerFont)
    }

    private fun durasiKas(shift: CashKas): String {
        val mulai = shift.waktuBuka
        val akhir = shift.waktuTutup ?: System.currentTimeMillis()
        val selisihMs = akhir - mulai
        val jam = selisihMs / 3_600_000
        val menit = (selisihMs % 3_600_000) / 60_000
        return if (jam > 0) "${jam}j ${menit}m" else "${menit}m"
    }

    private fun teksPas(teks: String, p: Paint, maxW: Float): String {
        if (teks.isEmpty()) return teks
        if (p.measureText(teks) <= maxW) return teks
        val maxChars = (maxW / p.measureText("W")).toInt().coerceIn(1, teks.length - 1)
        return teks.take(maxChars - 1) + ".."
    }

    private fun wrapTeks(teks: String, p: Paint, maxW: Float): List<String> {
        if (p.measureText(teks) <= maxW) return listOf(teks)
        val out = mutableListOf<String>()
        val cur = StringBuilder()
        teks.split(" ").forEach { word ->
            val test = if (cur.isEmpty()) word else "$cur $word"
            if (p.measureText(test) <= maxW) {
                if (cur.isNotEmpty()) cur.append(' ')
                cur.append(word)
            } else {
                if (cur.isNotEmpty()) {
                    out += cur.toString()
                    cur.setLength(0)
                }
                var sisa = word
                while (sisa.isNotEmpty()) {
                    var take = sisa.length
                    while (take > 1 && p.measureText(sisa.take(take)) > maxW) take--
                    if (cur.isNotEmpty()) cur.append(' ')
                    cur.append(sisa.take(take))
                    sisa = sisa.drop(take)
                    if (cur.length > 0 && p.measureText(cur.toString()) > maxW) {
                        out += cur.toString()
                        cur.setLength(0)
                    }
                }
            }
        }
        if (cur.isNotEmpty()) out += cur.toString()
        return out
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
