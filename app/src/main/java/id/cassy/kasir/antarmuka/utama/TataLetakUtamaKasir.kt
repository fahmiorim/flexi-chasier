package id.cassy.kasir.antarmuka.utama

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Badge
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.cassy.kasir.antarmuka.komponen.StatusKosongSederhana
import id.cassy.kasir.ranah.model.TampilanKatalog

/**
 * Tata letak khusus untuk perangkat dengan lebar layar terbatas (ponsel).
 *
 * @param modelTampilan Representasi status layar yang akan dirender.
 * @param saatAksiDikirim Callback untuk mengirimkan aksi pengguna ke ViewModel.
 * @param saatBukaDetailProduk Callback saat navigasi ke detail produk dipicu.
 * @param modifier Modifier untuk kustomisasi tata letak.
 */
@Composable
internal fun TataLetakPonselKasir(
    modelTampilan: ModelTampilanLayarUtamaKasir,
    saatAksiDikirim: (AksiLayarUtamaKasir) -> Unit,
    saatBukaSidebar: () -> Unit = {},
    saatBukaDetailProduk: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
                HeaderBerandaKasir(
                    namaAplikasi = modelTampilan.statusBeranda.namaAplikasi,
                    sloganAplikasi = modelTampilan.statusBeranda.sloganAplikasi,
                    saatBukaSidebar = saatBukaSidebar,
                )
        }

        item {
            BagianPencarianProdukKasir(
                nilaiPencarian = modelTampilan.kataKunciPencarian,
                saatNilaiPencarianBerubah = { kataKunciBaru ->
                    saatAksiDikirim(
                        AksiLayarUtamaKasir.UbahKataKunciPencarian(
                            kataKunciBaru = kataKunciBaru,
                        ),
                    )
                },
                jumlahHasil = modelTampilan.daftarProdukTersaring.size,
                tampilkanAksiResetPencarian = modelTampilan.tampilkanAksiResetPencarian,
                saatResetPencarian = {
                    saatAksiDikirim(AksiLayarUtamaKasir.ResetPencarian)
                },
            )
        }

        if (modelTampilan.statusHasilCheckout.apakahTampil) {
            item {
                KartuHasilCheckoutKasir(
                    statusHasilCheckout = modelTampilan.statusHasilCheckout,
                    saatTutup = {
                        saatAksiDikirim(AksiLayarUtamaKasir.TutupStatusHasilCheckout)
                    },
                )
            }
        }

        if (modelTampilan.daftarPesananPending.isNotEmpty()) {
            item {
                TombolPesananPendingTablet(
                    jumlahPesananPending = modelTampilan.daftarPesananPending.size,
                    saatKlik = {
                        saatAksiDikirim(AksiLayarUtamaKasir.BukaPanelPesananPending)
                    },
                )
            }
        }

        item {
            PanelKeranjangKasir(
                daftarItemKeranjang = modelTampilan.daftarItemKeranjang,
                statusKeranjang = modelTampilan.statusKeranjang,
                saatTambahProduk = { produkId ->
                    saatAksiDikirim(
                        AksiLayarUtamaKasir.TambahProdukKeKeranjang(
                            produkId = produkId,
                        ),
                    )
                },
                saatKurangiProduk = { produkId ->
                    saatAksiDikirim(
                        AksiLayarUtamaKasir.KurangiProdukDiKeranjang(
                            produkId = produkId,
                        ),
                    )
                },
                saatHapusProduk = { produkId ->
                    saatAksiDikirim(
                        AksiLayarUtamaKasir.HapusProdukDariKeranjang(
                            produkId = produkId,
                        ),
                    )
                },
            )
        }

        item {
            BagianRingkasanPembayaranKasir(
                ringkasanPembayaran = modelTampilan.ringkasanPembayaran,
                apakahRingkasanPembayaranTampil = modelTampilan.apakahRingkasanPembayaranTampil,
                saatUbahVisibilitasRingkasanPembayaran = {
                    saatAksiDikirim(
                        AksiLayarUtamaKasir.UbahVisibilitasRingkasanPembayaran,
                    )
                },
                saatSimpanPesanan = {
                    saatAksiDikirim(AksiLayarUtamaKasir.SimpanPesanan)
                },
                saatCheckout = {
                    saatAksiDikirim(AksiLayarUtamaKasir.CobaCheckout)
                },
            )
        }

        item {
            JudulBagianKasir(
                judul = "Katalog produk",
            )
        }

        if (modelTampilan.daftarProdukTersaring.isEmpty()) {
            item {
                StatusKosongSederhana(
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
                KartuProdukKasir(
                    produk = produk,
                    saatTambahProduk = {
                        saatAksiDikirim(
                            AksiLayarUtamaKasir.TambahProdukKeKeranjang(
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

/**
 * Tata letak khusus untuk perangkat dengan layar lebar (tablet atau desktop).
 *
 * @param modelTampilan Representasi status layar yang akan dirender.
 * @param saatAksiDikirim Callback untuk mengirimkan aksi pengguna ke ViewModel.
 * @param saatBukaDetailProduk Callback saat navigasi ke detail produk dipicu.
 * @param modifier Modifier untuk kustomisasi tata letak.
 */
@Composable
internal fun TataLetakTabletKasir(
    modelTampilan: ModelTampilanLayarUtamaKasir,
    saatAksiDikirim: (AksiLayarUtamaKasir) -> Unit,
    saatBukaSidebar: () -> Unit = {},
    saatBukaDetailProduk: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        val kolomKatalog = if (modelTampilan.tampilanKatalog == TampilanKatalog.Grid) {
            GridCells.Fixed(4)
        } else {
            GridCells.Fixed(1)
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
                HeaderBerandaKasir(
                    namaAplikasi = modelTampilan.statusBeranda.namaAplikasi,
                    sloganAplikasi = modelTampilan.statusBeranda.sloganAplikasi,
                    saatBukaSidebar = saatBukaSidebar,
                )
            }

            item(span = { GridItemSpan(maxCurrentLineSpan) }) {
                BagianPencarianProdukKasir(
                    nilaiPencarian = modelTampilan.kataKunciPencarian,
                    saatNilaiPencarianBerubah = { kataKunciBaru ->
                        saatAksiDikirim(
                            AksiLayarUtamaKasir.UbahKataKunciPencarian(
                                kataKunciBaru = kataKunciBaru,
                            ),
                        )
                    },
                    jumlahHasil = modelTampilan.daftarProdukTersaring.size,
                    tampilkanAksiResetPencarian = modelTampilan.tampilkanAksiResetPencarian,
                    saatResetPencarian = {
                        saatAksiDikirim(AksiLayarUtamaKasir.ResetPencarian)
                    },
                )
            }

            item(span = { GridItemSpan(maxCurrentLineSpan) }) {
                JudulBagianKasir(
                    judul = "Katalog produk",
                )
            }

            if (modelTampilan.daftarProdukTersaring.isEmpty()) {
                item(span = { GridItemSpan(maxCurrentLineSpan) }) {
                    StatusKosongSederhana(
                        judul = "Produk tidak ditemukan",
                        deskripsi = "Coba gunakan kata kunci lain.",
                    )
                }
            } else {
                modelTampilan.daftarProdukTersaring.forEach { produk ->
                    item(key = produk.id, contentType = "KartuProduk") {
                        KartuProdukKasir(
                            produk = produk,
                            saatTambahProduk = {
                                saatAksiDikirim(
                                    AksiLayarUtamaKasir.TambahProdukKeKeranjang(
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

        Column(
            modifier = Modifier
                .weight(0.7f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (modelTampilan.statusHasilCheckout.apakahTampil) {
                KartuHasilCheckoutKasir(
                    statusHasilCheckout = modelTampilan.statusHasilCheckout,
                    saatTutup = {
                        saatAksiDikirim(AksiLayarUtamaKasir.TutupStatusHasilCheckout)
                    },
                )
            }

            if (modelTampilan.daftarPesananPending.isNotEmpty()) {
                TombolPesananPendingTablet(
                    jumlahPesananPending = modelTampilan.daftarPesananPending.size,
                    saatKlik = {
                        saatAksiDikirim(AksiLayarUtamaKasir.BukaPanelPesananPending)
                    },
                )
            }

            PanelKeranjangKasir(
                daftarItemKeranjang = modelTampilan.daftarItemKeranjang,
                statusKeranjang = modelTampilan.statusKeranjang,
                saatTambahProduk = { produkId ->
                    saatAksiDikirim(
                        AksiLayarUtamaKasir.TambahProdukKeKeranjang(
                            produkId = produkId,
                        ),
                    )
                },
                saatKurangiProduk = { produkId ->
                    saatAksiDikirim(
                        AksiLayarUtamaKasir.KurangiProdukDiKeranjang(
                            produkId = produkId,
                        ),
                    )
                },
                saatHapusProduk = { produkId ->
                    saatAksiDikirim(
                        AksiLayarUtamaKasir.HapusProdukDariKeranjang(
                            produkId = produkId,
                        ),
                    )
                },
            )

            BagianRingkasanPembayaranKasir(
                ringkasanPembayaran = modelTampilan.ringkasanPembayaran,
                apakahRingkasanPembayaranTampil = modelTampilan.apakahRingkasanPembayaranTampil,
                saatUbahVisibilitasRingkasanPembayaran = {
                    saatAksiDikirim(
                        AksiLayarUtamaKasir.UbahVisibilitasRingkasanPembayaran,
                    )
                },
                saatSimpanPesanan = {
                    saatAksiDikirim(AksiLayarUtamaKasir.SimpanPesanan)
                },
                saatCheckout = {
                    saatAksiDikirim(AksiLayarUtamaKasir.CobaCheckout)
                },
            )
        }
    }
}

@Composable
private fun TombolPesananPendingTablet(
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
