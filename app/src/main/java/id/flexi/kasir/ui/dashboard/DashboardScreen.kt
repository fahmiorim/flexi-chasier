package id.flexi.kasir.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.Fastfood
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.flexi.kasir.domain.util.sebagaiRupiah
import id.flexi.kasir.ui.component.FlexiCard
import id.flexi.kasir.ui.component.FlexiDotBadge
import id.flexi.kasir.ui.component.FlexiGradientButton
import id.flexi.kasir.ui.component.FlexiSectionLabel
import id.flexi.kasir.ui.component.FlexiStatCard
import id.flexi.kasir.ui.component.FlexiTopAppBar
import id.flexi.kasir.ui.theme.FlexiColors
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    modelTampilan: DashboardUiState,
    saatKembali: () -> Unit,
    saatBukaSidebar: (() -> Unit)? = null,
    saatBukaTransaksi: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { paddingDalam ->
        if (modelTampilan.apakahSedangMemuat) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingDalam),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingDalam),
            ) {
                // ── Header ──
                item(key = "header") {
                    AnimatedSection(delayMs = 0) {
                        FlexiTopAppBar(
                            title = "Dashboard",
                            saatKembali = saatKembali,
                            saatBukaSidebar = saatBukaSidebar,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }

                // ── Baris 1: Penjualan + Transaksi + Item + Waktu Tunggu ──
                // Layar lebar: 4 kartu sejajar; layar sempit: 2×2 agar tetap lega.
                item(key = "hero_row") {
                    AnimatedSection(delayMs = 100) {
                        BoxWithConstraints(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                        ) {
                            if (maxWidth >= 600.dp) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    FlexiStatCard(
                                        label = "Penjualan",
                                        value = modelTampilan.totalPenjualanHariIni,
                                        icon = Icons.Default.ShoppingCart,
                                        accentColor = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.weight(1.2f),
                                    )
                                    FlexiStatCard(
                                        label = "Transaksi",
                                        value = modelTampilan.jumlahTransactionHariIni,
                                        icon = Icons.Outlined.Assignment,
                                        accentColor = FlexiColors.chartPurple,
                                        modifier = Modifier.weight(1f),
                                    )
                                    FlexiStatCard(
                                        label = "Item",
                                        value = "${modelTampilan.totalProdukTerjualHariIni}",
                                        icon = Icons.Outlined.Fastfood,
                                        accentColor = FlexiColors.chartAmber,
                                        modifier = Modifier.weight(1f),
                                    )
                                    FlexiStatCard(
                                        label = "Waktu Tunggu",
                                        value = modelTampilan.rataWaktuTungguHariIni,
                                        icon = Icons.Outlined.Timer,
                                        accentColor = FlexiColors.success,
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                            } else {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        FlexiStatCard(
                                            label = "Penjualan",
                                            value = modelTampilan.totalPenjualanHariIni,
                                            icon = Icons.Default.ShoppingCart,
                                            accentColor = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.weight(1f),
                                        )
                                        FlexiStatCard(
                                            label = "Transaksi",
                                            value = modelTampilan.jumlahTransactionHariIni,
                                            icon = Icons.Outlined.Assignment,
                                            accentColor = FlexiColors.chartPurple,
                                            modifier = Modifier.weight(1f),
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        FlexiStatCard(
                                            label = "Item",
                                            value = "${modelTampilan.totalProdukTerjualHariIni}",
                                            icon = Icons.Outlined.Fastfood,
                                            accentColor = FlexiColors.chartAmber,
                                            modifier = Modifier.weight(1f),
                                        )
                                        FlexiStatCard(
                                            label = "Waktu Tunggu",
                                            value = modelTampilan.rataWaktuTungguHariIni,
                                            icon = Icons.Outlined.Timer,
                                            accentColor = FlexiColors.success,
                                            modifier = Modifier.weight(1f),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ── Baris 2: Grafik (kiri) + Periode (kanan) ──
                item(key = "chart_periode_saldo") {
                    AnimatedSection(delayMs = 225) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            // Chart 7 Hari (lebih lebar)
                            SalesChart(
                                data = modelTampilan.dailySalesBreakdown,
                                modifier = Modifier.weight(2f),
                            )

                            // Kolom kanan: Minggu + Bulan + Saldo Kas
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                PeriodeCard(
                                    judul = "Minggu Ini",
                                    total = modelTampilan.totalPenjualanMingguIni,
                                    sub = modelTampilan.jumlahTransactionMingguIni,
                                    tren = modelTampilan.trenMingguIni,
                                    trenPositif = modelTampilan.apakahTrenMingguIniPositif,
                                    accentColor = FlexiColors.chartPurple,
                                )
                                PeriodeCard(
                                    judul = "Bulan Ini",
                                    total = modelTampilan.totalPenjualanBulanIni,
                                    sub = modelTampilan.jumlahTransactionBulanIni,
                                    tren = modelTampilan.trenBulanIni,
                                    trenPositif = modelTampilan.apakahTrenBulanIniPositif,
                                    accentColor = FlexiColors.chartAmber,
                                )
                                if (modelTampilan.apakahKasAktif) {
                                    SaldoKasCard(saldo = modelTampilan.saldoKasSaatIni)
                                }
                            }
                        }
                    }
                }

                // ── Tombol Buka Transaksi ──
                item(key = "buka_transaksi") {
                    AnimatedSection(delayMs = 350) {
                        FlexiGradientButton(
                            onClick = saatBukaTransaksi,
                            text = "Buka Transaksi",
                            icon = Icons.Default.ShoppingCart,
                            trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .height(48.dp),
                        )
                    }
                }

                // ── Produk Terlaris ──
                item(key = "label_produk") {
                    AnimatedSection(delayMs = 475) {
                        FlexiSectionLabel(text = "Produk Terlaris Hari Ini")
                    }
                }

                if (modelTampilan.produkTerlaris.isEmpty()) {
                    item(key = "produk_kosong") {
                        AnimatedSection(delayMs = 500) {
                            Text(
                                text = "Belum ada data penjualan hari ini.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            )
                        }
                    }
                } else {
                    items(
                        items = modelTampilan.produkTerlaris,
                        key = { "produk_${it.ranking}" },
                    ) { produk ->
                        AnimatedSection(delayMs = 500 + produk.ranking * 60) {
                            ProductRankCard(
                                ranking = produk.ranking,
                                nama = produk.nama,
                                jumlah = produk.jumlah,
                                persentase = produk.persentase,
                            )
                        }
                    }
                }

                item(key = "spacer_bawah") {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

// ═══════════════════════════════════════
// ANIMATED SECTION
// ═══════════════════════════════════════

@Composable
private fun AnimatedSection(
    delayMs: Int = 0,
    content: @Composable () -> Unit,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(delayMs) {
        delay(delayMs.toLong())
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(200)) +
                slideInVertically(animationSpec = tween(200)) { it / 6 },
    ) {
        content()
    }
}

// ═══════════════════════════════════════
// PERIODE CARD
// ═══════════════════════════════════════

@Composable
private fun PeriodeCard(
    judul: String,
    total: String,
    sub: String,
    tren: String,
    trenPositif: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    val trenColor = if (trenPositif) FlexiColors.success else FlexiColors.danger

    FlexiCard(modifier = modifier) {
        Column(modifier = Modifier.padding(14.dp)) {
            FlexiDotBadge(
                text = judul,
                dotColor = accentColor,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = total,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.3).sp,
                ),
                color = accentColor,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = sub,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = "•",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                )
                Text(
                    text = if (trenPositif) "▲" else "▼",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                    fontWeight = FontWeight.Bold,
                    color = trenColor,
                )
                Text(
                    text = tren,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = trenColor,
                )
            }
        }
    }
}

// ═══════════════════════════════════════
// SALDO KAS CARD
// ═══════════════════════════════════════

@Composable
private fun SaldoKasCard(
    saldo: String,
    modifier: Modifier = Modifier,
) {
    val green = FlexiColors.success

    FlexiCard(modifier = modifier) {
        Column(modifier = Modifier.padding(14.dp)) {
            FlexiDotBadge(
                text = "Saldo Kas",
                dotColor = green,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = saldo,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.3).sp,
                ),
                color = green,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = green.copy(alpha = 0.1f),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(green),
                    )
                    Text(
                        text = "Kas Aktif",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        fontWeight = FontWeight.Bold,
                        color = green,
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════
// SALES CHART — 7 Hari Premium
// ═══════════════════════════════════════

@Composable
private fun SalesChart(
    data: List<DailySalesData>,
    modifier: Modifier = Modifier,
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val chartColor = primaryColor.copy(alpha = 0.4f)
    val todayColor = primaryColor
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val density = LocalDensity.current
    val chartLabelTextSize = with(density) { 9.sp.toPx() }

    FlexiCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FlexiDotBadge(
                    text = "Penjualan 7 Hari",
                    dotColor = primaryColor,
                )
                Text(
                    text = "Total: ${data.sumOf { it.amount }.domainRupiah()}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = primaryColor,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val maxAmount = data.maxOfOrNull { it.amount } ?: 1L
            val animFraction by animateFloatAsState(
                targetValue = 1f,
                animationSpec = tween(600, delayMillis = 150),
                label = "chartAnim",
            )

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
            ) {
                val barCount = data.size
                val totalWidth = size.width
                val barSpacing = totalWidth / (barCount * 3.5f + 1f)
                val barWidth = barSpacing * 2.5f
                val chartHeight = size.height - 40f

                data.forEachIndexed { index, item ->
                    val barHeight = if (maxAmount > 0) {
                        (item.amount.toFloat() / maxAmount) * chartHeight * animFraction
                    } else 0f
                    val x = barSpacing + index * (barWidth + barSpacing)
                    val y = size.height - 30f - barHeight

                    // Gradient bar color for today, solid for other days
                    val barColor = if (item.isToday) todayColor else chartColor

                    // Draw rounded bar
                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(barWidth / 2f, 6f),
                    )

                    // Draw label
                    drawContext.canvas.nativeCanvas.drawText(
                        item.label,
                        x + barWidth / 2f,
                        size.height - 8f,
                        android.graphics.Paint().apply {
                            color = labelColor.hashCode()
                            textSize = chartLabelTextSize
                            textAlign = android.graphics.Paint.Align.CENTER
                            isAntiAlias = true
                        },
                    )
                }
            }
        }
    }
}

private fun Long.domainRupiah(): String = this.sebagaiRupiah()

// ═══════════════════════════════════════
// PRODUCT RANK CARD
// ═══════════════════════════════════════

@Composable
private fun ProductRankCard(
    ranking: Int,
    nama: String,
    jumlah: Int,
    persentase: Float,
    modifier: Modifier = Modifier,
) {
    val rankColor = when (ranking) {
        1 -> FlexiColors.chartBlue
        2 -> FlexiColors.chartPurple
        3 -> FlexiColors.chartTeal
        else -> MaterialTheme.colorScheme.outline
    }
    val medal = when (ranking) {
        1 -> "\uD83E\uDD47"
        2 -> "\uD83E\uDD48"
        3 -> "\uD83E\uDD49"
        else -> null
    }

    FlexiCard(modifier = modifier.padding(horizontal = 16.dp, vertical = 2.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Ranking badge
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(rankColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center,
            ) {
                if (medal != null) {
                    Text(text = medal, style = MaterialTheme.typography.labelMedium)
                } else {
                    Text(
                        text = "$ranking",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = rankColor,
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Product name + progress bar
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = nama,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(6.dp))
                // Progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = persentase.coerceIn(0.04f, 1f))
                            .matchParentSize()
                            .clip(RoundedCornerShape(2.dp))
                            .background(rankColor),
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Jumlah terjual badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = rankColor.copy(alpha = 0.1f),
            ) {
                Text(
                    text = "$jumlah terjual",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                    ),
                    color = rankColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                )
            }
        }
    }
}
