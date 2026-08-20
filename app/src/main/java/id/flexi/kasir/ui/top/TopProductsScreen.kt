package id.flexi.kasir.ui.top

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.flexi.kasir.domain.model.PaymentStatus
import id.flexi.kasir.domain.model.Transaction
import id.flexi.kasir.ui.component.FlexiCard
import id.flexi.kasir.ui.theme.FlexiColors
import java.util.Calendar

data class TopProductItem(
    val ranking: Int,
    val nama: String,
    val jumlah: Int,
    val total: Long,
    val persentase: Float,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopProductsScreen(
    transactions: List<Transaction> = emptyList(),
    isLoading: Boolean = false,
    navigasiKembali: () -> Unit,
    saatBukaSidebar: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    var selectedPeriod by remember { mutableIntStateOf(0) } // 0=hari, 1=minggu, 2=bulan

    val filteredTransactions = remember(transactions, selectedPeriod) {
        val cal = Calendar.getInstance()
        val (offset, field) = when (selectedPeriod) {
            1 -> Pair(-1, Calendar.WEEK_OF_YEAR)
            2 -> Pair(-1, Calendar.MONTH)
            else -> Pair(0, Calendar.DAY_OF_MONTH)
        }
        val mulai = Calendar.getInstance().apply {
            add(field, offset)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val selesai = if (selectedPeriod == 0) {
            Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
            }.timeInMillis
        } else System.currentTimeMillis()

        transactions.filter {
            it.paymentStatus == PaymentStatus.SudahDibayar &&
                    !it.dibatalkan &&
                    it.waktuTransactionEpochMili in mulai..selesai
        }
    }

    val topProducts = remember(filteredTransactions) {
        val grouped = mutableMapOf<String, Pair<String, Int>>() // produkId -> (nama, jumlah)
        for (tx in filteredTransactions) {
            for (item in tx.daftarCartItem) {
                val id = item.produk.id
                val existing = grouped[id]
                grouped[id] = (item.produk.nama) to ((existing?.second ?: 0) + item.jumlah)
            }
        }
        val sorted = grouped.entries.sortedByDescending { it.value.second }
        val maxJumlah = sorted.firstOrNull()?.value?.second ?: 1
        sorted.mapIndexed { index, entry ->
            val harga = filteredTransactions.flatMap { it.daftarCartItem }
                .firstOrNull { it.produk.id == entry.key }?.produk?.harga ?: 0L
            TopProductItem(
                ranking = index + 1,
                nama = entry.value.first,
                jumlah = entry.value.second,
                total = harga * entry.value.second,
                persentase = entry.value.second.toFloat() / maxJumlah,
            )
        }
    }

    val totalItems = topProducts.sumOf { it.jumlah }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = { Text("Produk Terlaris", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = navigasiKembali) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { paddingDalam ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingDalam),
        ) {
            // Period filter chips
            item(key = "filters") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    val labels = listOf("Hari Ini", "Minggu Ini", "Bulan Ini")
                    labels.forEachIndexed { index, label ->
                        FilterChip(
                            selected = selectedPeriod == index,
                            onClick = { selectedPeriod = index },
                            label = { Text(label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                        )
                    }
                }
            }

            // Summary
            item(key = "summary") {
                FlexiCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Text(
                                text = "Total Item Terjual",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = "$totalItems",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Jenis Produk",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = "${topProducts.size}",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                color = FlexiColors.chartPurple,
                            )
                        }
                    }
                }
            }

            // Ranking list
            if (topProducts.isEmpty()) {
                item(key = "empty") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Belum ada data penjualan pada periode ini.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                items(topProducts, key = { it.ranking }) { item ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = tween(150)) + slideInVertically(animationSpec = tween(150)) { it / 8 },
                    ) {
                        TopProductCard(item = item)
                    }
                }
            }

            item(key = "spacer") {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun TopProductCard(item: TopProductItem, modifier: Modifier = Modifier) {
    val rankColor = when (item.ranking) {
        1 -> FlexiColors.chartAmber // Gold
        2 -> FlexiColors.chartPurple // Silver-ish
        3 -> FlexiColors.success // Bronze-ish
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    FlexiCard(modifier = modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Rank badge
            Surface(
                shape = CircleShape,
                color = rankColor.copy(alpha = 0.1f),
                modifier = Modifier.size(40.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "#${item.ranking}",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = rankColor,
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Product info + progress bar
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.nama,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { item.persentase },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = rankColor,
                    trackColor = rankColor.copy(alpha = 0.1f),
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Quantity
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${item.jumlah}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "terjual",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
