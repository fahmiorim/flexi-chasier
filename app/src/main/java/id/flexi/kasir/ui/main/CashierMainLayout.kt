package id.flexi.kasir.ui.main

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import id.flexi.kasir.ui.component.SimpleEmptyStatus
import id.flexi.kasir.domain.model.CatalogDisplay

@Composable
private fun ManualItemForm(
    saatTambahItem: (nama: String, harga: Long) -> Unit,
) {
    var nama by remember { mutableStateOf("") }
    var harga by remember { mutableStateOf("") }

    ElevatedCard(
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Tambah Item Manual",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            OutlinedTextField(
                value = nama,
                onValueChange = { nama = it },
                label = { Text("Catatan") },
                placeholder = { Text("Misal: Es Teh Tawar") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small,
            )
            OutlinedTextField(
                value = harga,
                onValueChange = { harga = it.filter { c -> c.isDigit() } },
                label = { Text("Harga") },
                placeholder = { Text("10000") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small,
            )
            Button(
                onClick = {
                    val hargaLong = harga.toLongOrNull() ?: return@Button
                    if (nama.isNotBlank() && hargaLong > 0) {
                        saatTambahItem(nama.trim(), hargaLong)
                        nama = ""
                        harga = ""
                    }
                },
                enabled = nama.isNotBlank() && (harga.toLongOrNull() ?: 0) > 0,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Tambah ke Keranjang")
            }
        }
    }
}

/**
 * Tata letak khusus untuk perangkat dengan lebar layar terbatas (ponsel).
 *
 * @param modelTampilan Representasi status layar yang akan dirender.
 * @param saatAksiDikirim Callback untuk mengirimkan aksi pengguna ke ViewModel.
 * @param saatBukaDetailProduk Callback saat navigasi ke detail produk dipicu.
 * @param modifier Modifier untuk kustomisasi tata letak.
 */
@Composable
internal fun CashierPhoneLayout(
    modelTampilan: CashierMainUiState,
    saatAksiDikirim: (CashierMainAction) -> Unit,
    saatBukaSidebar: () -> Unit = {},
    saatBukaDetailProduk: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        CashierHomeHeader(
            namaAplikasi = modelTampilan.statusBeranda.namaAplikasi,
            sloganAplikasi = modelTampilan.statusBeranda.sloganAplikasi,
            saatBukaSidebar = saatBukaSidebar,
            nilaiPencarian = modelTampilan.kataKunciPencarian,
            saatNilaiPencarianBerubah = { kataKunciBaru ->
                saatAksiDikirim(CashierMainAction.UbahKataKunciPencarian(kataKunciBaru))
            },
            jumlahHasilPencarian = modelTampilan.daftarProdukTersaring.size,
            tampilkanAksiResetPencarian = modelTampilan.tampilkanAksiResetPencarian,
            saatResetPencarian = { saatAksiDikirim(CashierMainAction.ResetPencarian) },
            catalogDisplay = modelTampilan.catalogDisplay,
            saatAlihkanCatalogDisplay = { saatAksiDikirim(CashierMainAction.AlihkanCatalogDisplay) },
            jumlahPesananPending = modelTampilan.daftarPesananPending.size,
            saatBukaPesananPending = { saatAksiDikirim(CashierMainAction.BukaPendingOrdersPanel) },
            jumlahAntrian = modelTampilan.daftarPesananDiproses.size,
            saatBukaAntrian = { saatAksiDikirim(CashierMainAction.BukaAntrianPanel) },
        )

        SinkronStatusBar(
            status = modelTampilan.sinkronMesinStatus,
            saatSinkronkan = { saatAksiDikirim(CashierMainAction.SinkronkanSekarang) },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
        )

        PrimaryTabRow(
            selectedTabIndex = modelTampilan.tabTransaksi,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Tab(
                selected = modelTampilan.tabTransaksi == 0,
                onClick = { saatAksiDikirim(CashierMainAction.UbahTabTransaksi(0)) },
                text = { Text("Manual") },
            )
            Tab(
                selected = modelTampilan.tabTransaksi == 1,
                onClick = { saatAksiDikirim(CashierMainAction.UbahTabTransaksi(1)) },
                text = { Text("Produk") },
            )
            Tab(
                selected = modelTampilan.tabTransaksi == 2,
                onClick = { saatAksiDikirim(CashierMainAction.UbahTabTransaksi(2)) },
                text = { Text("Favorit") },
            )
        }

        if (modelTampilan.tabTransaksi == 1 && modelTampilan.daftarKategori.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                modelTampilan.daftarKategori.forEach { kategori ->
                    FilterChip(
                        selected = modelTampilan.kategoriTerpilih == kategori,
                        onClick = {
                            saatAksiDikirim(CashierMainAction.UbahKategoriTerpilih(kategori))
                        },
                        label = { Text(kategori) },
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (modelTampilan.tabTransaksi == 0) {
                item {
                    ManualItemForm(
                        saatTambahItem = { nama, harga ->
                            saatAksiDikirim(CashierMainAction.TambahItemManual(nama, harga))
                        },
                    )
                }
            }

            item {
                CartPanel(
                    daftarCartItem = modelTampilan.daftarCartItem,
                    statusKeranjang = modelTampilan.statusKeranjang,
                    saatTambahProduk = { produkId, varianNama ->
                        saatAksiDikirim(
                            CashierMainAction.AddProductToCart(
                                produkId = produkId,
                                varianNama = varianNama,
                            ),
                        )
                    },
                    saatKurangiProduk = { produkId, varianNama ->
                        saatAksiDikirim(
                            CashierMainAction.DecreaseProductInCart(
                                produkId = produkId,
                                varianNama = varianNama,
                            ),
                        )
                    },
                    saatDeleteProduct = { produkId, varianNama ->
                        saatAksiDikirim(
                            CashierMainAction.RemoveProductFromCart(
                                produkId = produkId,
                                varianNama = varianNama,
                            ),
                        )
                    },
                )
            }

            item {
                PaymentSummarySection(
                    ringkasanPayment = modelTampilan.ringkasanPayment,
                    apakahRingkasanPaymentTampil = modelTampilan.apakahRingkasanPaymentTampil,
                    saatUbahVisibilitasRingkasanPayment = {
                        saatAksiDikirim(
                            CashierMainAction.UbahVisibilitasRingkasanPayment,
                        )
                    },
                    saatSimpanPesanan = {
                        saatAksiDikirim(CashierMainAction.BukaDialogCheckout(modeSimpan = true))
                    },
                    saatCheckout = {
                        saatAksiDikirim(CashierMainAction.BukaDialogCheckout(modeSimpan = false))
                    },
                )
            }

            if (modelTampilan.tabTransaksi != 0) {
                item {
                    SectionTitle(
                        judul = if (modelTampilan.tabTransaksi == 2) "Produk favorit" else "Katalog produk",
                    )
                }

                if (modelTampilan.daftarProdukTersaring.isEmpty()) {
                    item {
                        SimpleEmptyStatus(
                            judul = "Produk tidak ditemukan",
                            deskripsi = "Coba gunakan kata kunci lain.",
                        )
                    }
                } else {
                    items(
                        items = modelTampilan.daftarProdukTersaring,
                        key = { produk -> produk.id },
                        contentType = { "KartuProduk" },
                    ) { produk ->
                        ProductCard(
                            produk = produk,
                            saatTambahProduk = {
                                saatAksiDikirim(
                                    CashierMainAction.AddProductToCart(
                                        produkId = produk.id,
                                    ),
                                )
                            },
                            saatBukaDetailProduk = {
                                saatBukaDetailProduk(produk.id)
                            },
                        )
                    }
                }
            }
        }
    }
}

/**
 * Tata letak khusus untuk perangkat dengan layar lebar (tablet atau desktop).
 *
 * @param modelTampilan Representasi status layar yang akan dirender.
 * @param saatAksiDikirim Callback untuk mengirimkan aksi pengguna ke ViewModel.
 * @param saatBukaDetailProduk Callback saat navigasi ke detail produk dipicu.
 * @param modifier Modifier untuk kustomisasi tata letak.
 */
@Composable
internal fun CashierTabletLayout(
    modelTampilan: CashierMainUiState,
    saatAksiDikirim: (CashierMainAction) -> Unit,
    saatBukaSidebar: () -> Unit = {},
    saatBukaDetailProduk: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp, top = 16.dp),
    ) {
        CashierHomeHeader(
            namaAplikasi = modelTampilan.statusBeranda.namaAplikasi,
            sloganAplikasi = modelTampilan.statusBeranda.sloganAplikasi,
            saatBukaSidebar = saatBukaSidebar,
            nilaiPencarian = modelTampilan.kataKunciPencarian,
            saatNilaiPencarianBerubah = { kataKunciBaru ->
                saatAksiDikirim(CashierMainAction.UbahKataKunciPencarian(kataKunciBaru))
            },
            jumlahHasilPencarian = modelTampilan.daftarProdukTersaring.size,
            tampilkanAksiResetPencarian = modelTampilan.tampilkanAksiResetPencarian,
            saatResetPencarian = { saatAksiDikirim(CashierMainAction.ResetPencarian) },
            catalogDisplay = modelTampilan.catalogDisplay,
            saatAlihkanCatalogDisplay = { saatAksiDikirim(CashierMainAction.AlihkanCatalogDisplay) },
            jumlahPesananPending = modelTampilan.daftarPesananPending.size,
            saatBukaPesananPending = { saatAksiDikirim(CashierMainAction.BukaPendingOrdersPanel) },
            jumlahAntrian = modelTampilan.daftarPesananDiproses.size,
            saatBukaAntrian = { saatAksiDikirim(CashierMainAction.BukaAntrianPanel) },
        )

        SinkronStatusBar(
            status = modelTampilan.sinkronMesinStatus,
            saatSinkronkan = { saatAksiDikirim(CashierMainAction.SinkronkanSekarang) },
        )

        PrimaryTabRow(
            selectedTabIndex = modelTampilan.tabTransaksi,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Tab(
                selected = modelTampilan.tabTransaksi == 0,
                onClick = { saatAksiDikirim(CashierMainAction.UbahTabTransaksi(0)) },
                text = { Text("Manual") },
            )
            Tab(
                selected = modelTampilan.tabTransaksi == 1,
                onClick = { saatAksiDikirim(CashierMainAction.UbahTabTransaksi(1)) },
                text = { Text("Produk") },
            )
            Tab(
                selected = modelTampilan.tabTransaksi == 2,
                onClick = { saatAksiDikirim(CashierMainAction.UbahTabTransaksi(2)) },
                text = { Text("Favorit") },
            )
        }

        if (modelTampilan.tabTransaksi == 1 && modelTampilan.daftarKategori.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                modelTampilan.daftarKategori.forEach { kategori ->
                    FilterChip(
                        selected = modelTampilan.kategoriTerpilih == kategori,
                        onClick = {
                            saatAksiDikirim(CashierMainAction.UbahKategoriTerpilih(kategori))
                        },
                        label = { Text(kategori) },
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            val lebarPanel = maxWidth
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
            if (modelTampilan.tabTransaksi != 0) {
                val kolomKatalog = when {
                    modelTampilan.catalogDisplay == CatalogDisplay.Grid -> GridCells.Fixed(4)
                    // Ponsel: 2 kolom di layar sempit, 3 kolom di layar lebar
                    lebarPanel >= 480.dp -> GridCells.Fixed(3)
                    else -> GridCells.Fixed(2)
                }

                LazyVerticalGrid(
                    columns = kolomKatalog,
                    modifier = Modifier
                        .weight(1.7f)
                        .fillMaxHeight(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item(span = { GridItemSpan(maxCurrentLineSpan) }) {
                        SectionTitle(
                            judul = if (modelTampilan.tabTransaksi == 2) "Produk favorit" else "Katalog produk",
                        )
                    }

                    if (modelTampilan.daftarProdukTersaring.isEmpty()) {
                        item(span = { GridItemSpan(maxCurrentLineSpan) }) {
                            SimpleEmptyStatus(
                                judul = "Produk tidak ditemukan",
                                deskripsi = "Coba gunakan kata kunci lain.",
                            )
                        }
                    } else {
                        modelTampilan.daftarProdukTersaring.forEach { produk ->
                            item(key = produk.id, contentType = "KartuProduk") {
                                ProductCard(
                                    produk = produk,
                                    saatTambahProduk = {
                                        saatAksiDikirim(
                                            CashierMainAction.AddProductToCart(
                                                produkId = produk.id,
                                            ),
                                        )
                                    },
                                    saatBukaDetailProduk = {
                                        saatBukaDetailProduk(produk.id)
                                    },
                                )
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(if (modelTampilan.tabTransaksi == 0) 1f else 0.7f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (modelTampilan.tabTransaksi == 0) {
                    ManualItemForm(
                        saatTambahItem = { nama, harga ->
                            saatAksiDikirim(CashierMainAction.TambahItemManual(nama, harga))
                        },
                    )
                }

                CartPanel(
                    daftarCartItem = modelTampilan.daftarCartItem,
                    statusKeranjang = modelTampilan.statusKeranjang,
                    saatTambahProduk = { produkId, _ ->
                        saatAksiDikirim(
                            CashierMainAction.AddProductToCart(
                                produkId = produkId,
                            ),
                        )
                    },
                    saatKurangiProduk = { produkId, varianNama ->
                        saatAksiDikirim(
                            CashierMainAction.DecreaseProductInCart(
                                produkId = produkId,
                                varianNama = varianNama,
                            ),
                        )
                    },
                    saatDeleteProduct = { produkId, varianNama ->
                        saatAksiDikirim(
                            CashierMainAction.RemoveProductFromCart(
                                produkId = produkId,
                                varianNama = varianNama,
                            ),
                        )
                    },
                )

                PaymentSummarySection(
                    ringkasanPayment = modelTampilan.ringkasanPayment,
                    apakahRingkasanPaymentTampil = modelTampilan.apakahRingkasanPaymentTampil,
                    saatUbahVisibilitasRingkasanPayment = {
                        saatAksiDikirim(
                            CashierMainAction.UbahVisibilitasRingkasanPayment,
                        )
                    },
                    saatSimpanPesanan = {
                        saatAksiDikirim(CashierMainAction.BukaDialogCheckout(modeSimpan = true))
                    },
                    saatCheckout = {
                        saatAksiDikirim(CashierMainAction.BukaDialogCheckout(modeSimpan = false))
                    },
                )
            }
            } // tutup Row
        } // tutup BoxWithConstraints
    }
}

@Composable
private fun PendingOrdersTabletButton(
    jumlahPesananPending: Int,
    saatKlik: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val warna = MaterialTheme.colorScheme.tertiary

    androidx.compose.material3.OutlinedCard(
        onClick = saatKlik,
        modifier = modifier.fillMaxWidth(),
        colors = androidx.compose.material3.CardDefaults.outlinedCardColors(
            containerColor = warna.copy(alpha = 0.08f),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            androidx.compose.material3.Badge(
                containerColor = warna,
            ) {
                Text(
                    text = "$jumlahPesananPending",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onTertiary,
                )
            }
            Text(
                text = "Pesanan Pending",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Buka",
                modifier = Modifier.size(16.dp),
                tint = warna,
            )
        }
    }
}
