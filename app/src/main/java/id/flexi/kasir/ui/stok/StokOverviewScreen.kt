package id.flexi.kasir.ui.stok

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.flexi.kasir.domain.model.Produk
import id.flexi.kasir.ui.component.FlexiCard
import id.flexi.kasir.ui.component.FlexiDotBadge
import id.flexi.kasir.ui.component.FlexiStatCard
import id.flexi.kasir.ui.theme.FlexiColors
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StokOverviewScreen(
    produkList: List<Produk>,
    isLoading: Boolean = false,
    navigasiKembali: () -> Unit,
    saatBukaSidebar: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    var kataKunci by remember { mutableStateOf("") }
    val produkFiltered = remember(produkList, kataKunci) {
        if (kataKunci.isBlank()) produkList
        else produkList.filter {
            it.nama.contains(kataKunci, ignoreCase = true) ||
                    it.kategori.contains(kataKunci, ignoreCase = true)
        }
    }
    val jumlahMenipis = remember(produkList) { produkList.count { it.apakahStokDiaktifkan && it.stokTersedia <= 5 } }
    val jumlahHabis = remember(produkList) { produkList.count { it.apakahStokDiaktifkan && it.stokTersedia <= 0 } }
    val jumlahAktif = remember(produkList) { produkList.count { it.aktif } }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = { Text("Stok Overview", fontWeight = FontWeight.Bold) },
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
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingDalam),
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
                // Stat cards
                item(key = "stats") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        FlexiStatCard(
                            label = "Total Produk",
                            value = "$jumlahAktif",
                            icon = Icons.Default.Search,
                            accentColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f),
                        )
                        FlexiStatCard(
                            label = "Stok Menipis",
                            value = "$jumlahMenipis",
                            icon = Icons.Default.Search,
                            accentColor = FlexiColors.chartAmber,
                            modifier = Modifier.weight(1f),
                        )
                        FlexiStatCard(
                            label = "Stok Habis",
                            value = "$jumlahHabis",
                            icon = Icons.Default.Search,
                            accentColor = FlexiColors.danger,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                // Search bar
                item(key = "search") {
                    OutlinedTextField(
                        value = kataKunci,
                        onValueChange = { kataKunci = it },
                        placeholder = { Text("Cari produk...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                        ),
                    )
                }

                // Product list
                items(produkFiltered, key = { it.id }) { produk ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = tween(150)) + slideInVertically(animationSpec = tween(150)) { it / 8 },
                    ) {
                        StokItemCard(produk = produk)
                    }
                }

                item(key = "spacer") {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun StokItemCard(produk: Produk, modifier: Modifier = Modifier) {
    val stokColor = when {
        !produk.apakahStokDiaktifkan -> MaterialTheme.colorScheme.onSurfaceVariant
        produk.stokTersedia <= 0 -> FlexiColors.danger
        produk.stokTersedia <= 5 -> FlexiColors.chartAmber
        else -> FlexiColors.success
    }
    val stokLabel = when {
        !produk.apakahStokDiaktifkan -> "Nonaktif"
        produk.stokTersedia <= 0 -> "Habis"
        produk.stokTersedia <= 5 -> "Menipis"
        else -> "Tersedia"
    }

    FlexiCard(modifier = modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Stok number badge
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = stokColor.copy(alpha = 0.1f),
                modifier = Modifier.size(48.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "${produk.stokTersedia}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = stokColor,
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Product info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = produk.nama,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (produk.kategori.isNotBlank()) {
                    Text(
                        text = produk.kategori,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Status badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = stokColor.copy(alpha = 0.1f),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(stokColor),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stokLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                        ),
                        color = stokColor,
                    )
                }
            }
        }
    }
}
