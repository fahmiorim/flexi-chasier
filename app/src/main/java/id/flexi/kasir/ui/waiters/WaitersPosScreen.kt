package id.flexi.kasir.ui.waiters

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TableRestaurant
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.flexi.kasir.domain.model.Meja
import id.flexi.kasir.domain.model.Produk
import id.flexi.kasir.domain.util.sebagaiRupiah
import id.flexi.kasir.ui.component.FlexiCard
import id.flexi.kasir.ui.component.FlexiGradientButton
import id.flexi.kasir.ui.theme.FlexiColors
import kotlinx.coroutines.launch

data class WaitersCartItem(
    val produk: Produk,
    var jumlah: Int,
    var catatan: String = "",
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WaitersPosScreen(
    daftarProduk: List<Produk>,
    daftarMeja: List<Meja>,
    navigasiKembali: () -> Unit,
    saatBukaSidebar: (() -> Unit)? = null,
    saatSimpanPesanan: (mejaId: String, catatan: String, items: List<WaitersCartItem>) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier,
) {
    val cart = remember { mutableStateListOf<WaitersCartItem>() }
    var selectedMeja by remember { mutableStateOf<Meja?>(null) }
    var catatan by remember { mutableStateOf("") }
    var showMejaDialog by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val produkFiltered = remember(daftarProduk, searchQuery) {
        if (searchQuery.isBlank()) daftarProduk.filter { it.aktif && it.stokTersedia > 0 }
        else daftarProduk.filter {
            it.aktif && it.stokTersedia > 0 &&
                    (it.nama.contains(searchQuery, ignoreCase = true) ||
                            it.kategori.contains(searchQuery, ignoreCase = true))
        }
    }

    val totalHarga = cart.sumOf { it.produk.harga * it.jumlah }
    val totalItem = cart.sumOf { it.jumlah }

    fun tambahKeKeranjang(produk: Produk) {
        val existing = cart.find { it.produk.id == produk.id }
        if (existing != null) {
            existing.jumlah++
        } else {
            cart.add(WaitersCartItem(produk = produk, jumlah = 1))
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Catat Pesanan", fontWeight = FontWeight.Bold)
                        if (selectedMeja != null) {
                            Text(
                                text = "Meja: ${selectedMeja!!.nomor}",
                                style = MaterialTheme.typography.labelSmall,
                                color = FlexiColors.success,
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = navigasiKembali) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    // Tombol pilih meja
                    IconButton(onClick = { showMejaDialog = true }) {
                        Icon(
                            Icons.Default.TableRestaurant,
                            contentDescription = "Pilih Meja",
                            tint = if (selectedMeja != null) FlexiColors.success else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    // Cart badge
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = "Keranjang",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        if (totalItem > 0) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(1.dp),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "$totalItem",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                    )
                                }
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { paddingDalam ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingDalam),
        ) {
            // Kolom kiri: Produk grid
            Column(modifier = Modifier.weight(1f)) {
                // Search
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari menu...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                )

                // Produk grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items(produkFiltered, key = { it.id }) { produk ->
                        val inCart = cart.find { it.produk.id == produk.id }
                        ProductCard(
                            produk = produk,
                            jumlahDiKeranjang = inCart?.jumlah ?: 0,
                            onClick = { tambahKeKeranjang(produk) },
                        )
                    }
                }
            }

            // Kolom kanan: Keranjang
            Surface(
                modifier = Modifier
                    .width(280.dp)
                    .fillMaxSize(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Header keranjang
                    Text(
                        text = "Pesanan ($totalItem item)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Catatan
                    OutlinedTextField(
                        value = catatan,
                        onValueChange = { catatan = it },
                        placeholder = { Text("Catatan pesanan...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Item list
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        items(cart, key = { it.produk.id }) { item ->
                            CartItemRow(
                                item = item,
                                onTambah = { item.jumlah++ },
                                onKurangi = {
                                    if (item.jumlah <= 1) cart.remove(item)
                                    else item.jumlah--
                                },
                                onHapus = { cart.remove(item) },
                            )
                        }
                    }

                    // Total + Simpan
                    if (cart.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text("Total", fontWeight = FontWeight.Bold)
                                    Text(
                                        text = totalHarga.sebagaiRupiah(),
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                FlexiGradientButton(
                                    onClick = {
                                        if (selectedMeja == null) {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Pilih meja terlebih dahulu!")
                                            }
                                            return@FlexiGradientButton
                                        }
                                        showConfirmDialog = true
                                    },
                                    text = "Simpan Pesanan",
                                    icon = Icons.Default.CheckCircle,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog pilih meja
    if (showMejaDialog) {
        AlertDialog(
            onDismissRequest = { showMejaDialog = false },
            title = { Text("Pilih Meja", fontWeight = FontWeight.Bold) },
            text = {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    daftarMeja.filter { it.aktif }.forEach { meja ->
                        val isSelected = selectedMeja?.id == meja.id
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            ),
                            modifier = Modifier
                                .clickable {
                                    selectedMeja = meja
                                    showMejaDialog = false
                                }
                                .padding(4.dp),
                        ) {
                            Text(
                                text = meja.nomor,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMejaDialog = false }) {
                    Text("Tutup")
                }
            },
        )
    }

    // Dialog konfirmasi
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Konfirmasi Pesanan") },
            text = {
                Column {
                    Text("Meja: ${selectedMeja?.nomor}")
                    Text("Item: $totalItem")
                    Text("Total: ${totalHarga.sebagaiRupiah()}")
                    if (catatan.isNotBlank()) {
                        Text("Catatan: $catatan")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDialog = false
                    saatSimpanPesanan(selectedMeja!!.id, catatan, cart.toList())
                    cart.clear()
                    catatan = ""
                    selectedMeja = null
                    scope.launch {
                        snackbarHostState.showSnackbar("Pesanan tersimpan!")
                    }
                }) {
                    Text("Ya, Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Batal")
                }
            },
        )
    }
}

@Composable
private fun ProductCard(
    produk: Produk,
    jumlahDiKeranjang: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (jumlahDiKeranjang > 0) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface,
        border = if (jumlahDiKeranjang > 0) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (jumlahDiKeranjang > 0) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "$jumlahDiKeranjang",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
            Text(
                text = produk.nama,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = produk.harga.sebagaiRupiah(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun CartItemRow(
    item: WaitersCartItem,
    onTambah: () -> Unit,
    onKurangi: () -> Unit,
    onHapus: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.produk.nama,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = item.produk.harga.sebagaiRupiah(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Quantity controls
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onKurangi,
                    modifier = Modifier.size(28.dp),
                ) {
                    Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(14.dp))
                }
                Text(
                    text = "${item.jumlah}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
                IconButton(
                    onClick = onTambah,
                    modifier = Modifier.size(28.dp),
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                }
            }

            IconButton(
                onClick = onHapus,
                modifier = Modifier.size(24.dp),
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Hapus",
                    tint = FlexiColors.danger,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
    }
}
