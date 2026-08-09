package id.flexi.kasir.ui.main

import android.content.res.Configuration
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.flexi.kasir.ui.theme.FlexiKasirTheme
import id.flexi.kasir.domain.model.CartItem
import id.flexi.kasir.domain.model.Produk
import id.flexi.kasir.domain.model.SyncStatus
import id.flexi.kasir.domain.model.Varian
import id.flexi.kasir.domain.util.sebagaiRupiah
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emptyFlow

/**
 * Komposabel utama yang merangkai seluruh elemen layar kasir.
 * Mengatur koordinasi antara status (state), aksi (action), dan efek (effect).
 *
 * @param modelTampilan Representasi status layar yang akan dirender.
 * @param saatAksiDikirim Callback untuk mengirimkan aksi pengguna ke ViewModel.
 * @param alurEfek Aliran efek sekali pakai seperti tampilan pesan singkat.
 * @param modifier Modifikasi tata letak opsional.
 * @param saatBukaDetailProduk Aksi saat kartu produk diklik untuk melihat detail.
 */
@Composable
fun CashierMainScreen(
    modifier: Modifier = Modifier,
    modelTampilan: CashierMainUiState,
    saatAksiDikirim: (CashierMainAction) -> Unit,
    alurEfek: Flow<CashierMainEffect> = emptyFlow(),
    saatBukaSidebar: () -> Unit = {},
    saatBukaDetailProduk: (String) -> Unit = {},
    saatBukaKasir: () -> Unit = {},
) {
    val keadaanHostSnackbar: SnackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(alurEfek) {
        alurEfek.collectLatest { efek ->
            when (efek) {
                is CashierMainEffect.TampilkanPesanSingkat -> {
                    keadaanHostSnackbar.showSnackbar(
                        message = efek.pesan,
                    )
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = {
            SnackbarHost(
                hostState = keadaanHostSnackbar,
            )
        },
    ) { paddingKerangka ->
        // Blocking dialog: jika manajemen kas aktif tapi belum buka kas
        if (modelTampilan.apakahPerluBukaKas) {
            id.flexi.kasir.ui.component.FlexiDialog(
                onDismissRequest = {},
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(36.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "Buka Kas Terlebih Dahulu",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "Manajemen kas sedang aktif. Silakan buka kas terlebih dahulu sebelum memulai transaksi.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    id.flexi.kasir.ui.component.FlexiDialogSingleAction(
                        label = "Buka Kas",
                        onClick = saatBukaKasir,
                        icon = Icons.Default.Add,
                    )
                }
            }
        }

        if (modelTampilan.statusKonfirmasiCheckout.apakahTampil) {
            CheckoutConfirmDialog(
                statusKonfirmasiCheckout = modelTampilan.statusKonfirmasiCheckout,
                saatBatalkan = {
                    saatAksiDikirim(CashierMainAction.BatalkanKonfirmasiCheckout)
                },
                saatKonfirmasi = {
                    saatAksiDikirim(CashierMainAction.BayarSekarang)
                },
                saatBayarTunai = { nominal ->
                    saatAksiDikirim(CashierMainAction.BayarSekarangTunai(nominal))
                },
                saatSimpanPesanan = {
                    saatAksiDikirim(CashierMainAction.SimpanPesanan)
                },
                saatSimpanDanCetak = {
                    saatAksiDikirim(CashierMainAction.SimpanDanCetakPesanan)
                },
                saatGabungDanBayar = {
                    val mejaId = modelTampilan.statusKonfirmasiCheckout.mejaId
                    if (mejaId != null) {
                        saatAksiDikirim(CashierMainAction.GabungDanBayarBill(mejaId, modelTampilan.statusKonfirmasiCheckout.paymentMethod))
                    }
                },
                saatGabungDanBayarTunai = { nominal ->
                    val mejaId = modelTampilan.statusKonfirmasiCheckout.mejaId
                    if (mejaId != null) {
                        saatAksiDikirim(CashierMainAction.GabungDanBayarBill(mejaId, modelTampilan.statusKonfirmasiCheckout.paymentMethod, nominal))
                    }
                },
                saatGabungDanSimpan = {
                    val mejaId = modelTampilan.statusKonfirmasiCheckout.mejaId
                    if (mejaId != null) {
                        saatAksiDikirim(CashierMainAction.GabungDanSimpanBill(mejaId))
                    }
                },
                saatCatatanBerubah = { catatan ->
                    saatAksiDikirim(CashierMainAction.UbahCatatanCheckout(catatan))
                },
                saatPaymentMethodBerubah = { PaymentMethod ->
                    saatAksiDikirim(CashierMainAction.UbahPaymentMethod(PaymentMethod))
                },
                saatOrderTypeBerubah = { OrderType ->
                    saatAksiDikirim(CashierMainAction.UbahOrderType(OrderType))
                },
                saatMejaDipilih = { mejaId ->
                    saatAksiDikirim(CashierMainAction.PilihMeja(mejaId))
                },
                daftarCartItem = modelTampilan.daftarCartItem,
                saatSplitBill = { ids, metode, uangTunai ->
                    saatAksiDikirim(CashierMainAction.SplitBill(ids, metode, uangTunai))
                },
            )
        }

        PendingOrdersPanel(
            daftarMeja = modelTampilan.daftarMeja,
            daftarPesananPending = modelTampilan.daftarPesananPending,
            apakahTampil = modelTampilan.apakahPendingOrdersPanelTampil,
            saatTutup = { saatAksiDikirim(CashierMainAction.TutupPendingOrdersPanel) },
            saatLanjutkan = { id ->
                saatAksiDikirim(CashierMainAction.ResumePendingOrder(id))
            },
            saatHapus = { id ->
                saatAksiDikirim(CashierMainAction.DeletePendingOrder(id))
            },
        )

        QueuePanel(
            daftarMeja = modelTampilan.daftarMeja,
            daftarPesananDiproses = modelTampilan.daftarPesananDiproses,
            apakahTampil = modelTampilan.apakahAntrianPanelTampil,
            saatTutup = { saatAksiDikirim(CashierMainAction.TutupAntrianPanel) },
            saatSelesaikan = { id ->
                saatAksiDikirim(CashierMainAction.SelesaikanAntrian(id))
            },
        )

        // Dialog pilih varian
        modelTampilan.produkUntukPilihVarian?.let { produk ->
            VarianPickerDialog(
                produk = produk,
                saatPilihVarian = { varian ->
                    saatAksiDikirim(CashierMainAction.PilihVarianProduk(produk.id, varian))
                },
                saatBatal = {
                    saatAksiDikirim(CashierMainAction.BatalkanPilihVarian)
                },
            )
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingKerangka),
        ) {
            val gunakanDuaPanel = maxWidth >= 840.dp

            if (gunakanDuaPanel) {
                CashierTabletLayout(
                    modelTampilan = modelTampilan,
                    saatAksiDikirim = saatAksiDikirim,
                    saatBukaSidebar = saatBukaSidebar,
                    saatBukaDetailProduk = saatBukaDetailProduk,
                )
            } else {
                CashierPhoneLayout(
                    modelTampilan = modelTampilan,
                    saatAksiDikirim = saatAksiDikirim,
                    saatBukaSidebar = saatBukaSidebar,
                    saatBukaDetailProduk = saatBukaDetailProduk,
                )
            }
        }

        // Overlay hasil checkout — auto-dismiss 3 detik
        val apakahHasilTampil = modelTampilan.statusHasilCheckout.apakahTampil
        LaunchedEffect(apakahHasilTampil) {
            if (apakahHasilTampil) {
                delay(3000)
                saatAksiDikirim(CashierMainAction.TutupStatusHasilCheckout)
            }
        }

        if (apakahHasilTampil) {
            CheckoutResultOverlay(
                statusHasilCheckout = modelTampilan.statusHasilCheckout,
                saatTutup = {
                    saatAksiDikirim(CashierMainAction.TutupStatusHasilCheckout)
                },
            )
        }
    }
}

/** Daftar produk kecil untuk pratinjau Compose (bukan data produksi). */
private val daftarProdukPratinjau = listOf(
    Produk(id = "pratinjau-kopi", nama = "Kopi Susu", harga = 20_000, stokTersedia = 0),
    Produk(id = "pratinjau-cokelat", nama = "Chocolate", harga = 20_000, stokTersedia = 0),
    Produk(id = "pratinjau-kentang", nama = "Kentang Goreng", harga = 15_000, stokTersedia = 0),
)

@Preview(
    name = "Workspace tablet terang dialog checkout",
    showBackground = true,
    widthDp = 1280,
    heightDp = 800,
)
@Composable
private fun PreviewWorkspaceTabletTerangDialogCheckout() {
    val daftarProduk = daftarProdukPratinjau

    FlexiKasirTheme(
        modeGelap = false,
        gunakanWarnaDinamis = false,
    ) {
        CashierMainScreen(
            modelTampilan = CashierMainUiState(
                statusBeranda = HomeStatus(
                    namaAplikasi = "Flexi Kasir",
                    sloganAplikasi = "Solusi Digital UMKM Modern",
                    jumlahProdukTersedia = daftarProduk.size,
                    jumlahCartItem = 4,
                    totalBelanjaSementara = "Rp59000",
                    syncStatus = SyncStatus.LocalChanges,
                    labelAksiSinkronisasi = "Perbarui katalog",
                    aksiSinkronisasiAktif = true,
                    labelMetadataSinkronisasi = "Katalog lokal siap digunakan.",
                ),
                daftarProdukTersaring = daftarProduk,
                daftarCartItem = listOf(
                    CartItem(
                        produk = daftarProduk[0],
                        jumlah = 2,
                    ),
                    CartItem(
                        produk = daftarProduk[1],
                        jumlah = 1,
                    ),
                    CartItem(
                        produk = daftarProduk[2],
                        jumlah = 1,
                    ),
                ),
                statusKeranjang = CartStatus(
                    judul = "Keranjang aktif",
                    deskripsi = "Atur jumlah item sebelum lanjut ke Payment.",
                    jumlahItem = "4 item",
                ),
                ringkasanPayment = PaymentSummary(
                    subtotal = "Rp59000",
                    potongan = "Rp0",
                    pajak = "Rp0",
                    totalAkhir = "Rp59000",
                    labelAksiUtama = "Bayar sekarang",
                    aksiUtamaAktif = true,
                ),
                statusKonfirmasiCheckout = CheckoutConfirmStatus(
                    apakahTampil = true,
                    judul = "Konfirmasi Payment",
                    deskripsi = "Bayar 4 item dengan total Rp59000 sekarang?",
                    labelKonfirmasi = "Bayar sekarang",
                ),
                kataKunciPencarian = "",
                apakahRingkasanPaymentTampil = true,
            ),
            saatAksiDikirim = {},
        )
    }
}

@Composable
private fun VarianPickerDialog(
    produk: Produk,
    saatPilihVarian: (Varian) -> Unit,
    saatBatal: () -> Unit,
) {
    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = saatBatal,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 380.dp)
                .wrapContentHeight(),
            shape = RoundedCornerShape(20.dp),
            tonalElevation = 3.dp,
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // ── Header ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                        Column {
                            Text(
                                text = "Pilih Varian",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = produk.nama,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                            )
                        }
                    }
                    IconButton(
                        onClick = saatBatal,
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Tutup",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                // ── Daftar Varian ──
                produk.varian.forEach { varian ->
                    ElevatedCard(
                        onClick = { saatPilihVarian(varian) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text(
                                    text = varian.nama,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                )
                                Text(
                                    text = "(+ ${varian.harga.sebagaiRupiah()})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Text(
                                text = varian.harga.sebagaiRupiah(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }

                // ── Tombol Batal ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = saatBatal) {
                        Text("Batal", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Preview(
    name = "Workspace ponsel gelap hasil checkout",
    showBackground = true,
    widthDp = 411,
    heightDp = 891,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun PreviewWorkspacePonselGelapHasilCheckout() {
    val daftarProduk = daftarProdukPratinjau

    FlexiKasirTheme(
        modeGelap = true,
        gunakanWarnaDinamis = false,
    ) {
        CashierMainScreen(
            modelTampilan = CashierMainUiState(
                statusBeranda = HomeStatus(
                    namaAplikasi = "Flexi Kasir",
                    sloganAplikasi = "Solusi Digital UMKM Modern",
                    jumlahProdukTersedia = daftarProduk.size,
                    jumlahCartItem = 0,
                    totalBelanjaSementara = "Rp0",
                    syncStatus = SyncStatus.Synced,
                    labelAksiSinkronisasi = "Perbarui katalog",
                    aksiSinkronisasiAktif = true,
                    labelMetadataSinkronisasi = "Baru saja diperbarui.",
                ),
                daftarProdukTersaring = daftarProduk,
                daftarCartItem = emptyList(),
                statusKeranjang = CartStatus(
                    judul = "Keranjang kosong",
                    deskripsi = "Mulai Transaction dengan memilih produk.",
                    jumlahItem = "0 item",
                ),
                ringkasanPayment = PaymentSummary(
                    subtotal = "Rp0",
                    potongan = "Rp0",
                    pajak = "Rp0",
                    totalAkhir = "Rp0",
                    labelAksiUtama = "Pilih produk",
                    aksiUtamaAktif = false,
                ),
                statusHasilCheckout = CheckoutResultStatus(
                    apakahTampil = true,
                    judul = "Transaction berhasil",
                    deskripsi = "4 item dengan total Rp59000 siap disimpan ke riwayat lokal pada scope data berikutnya.",
                ),
                kataKunciPencarian = "",
                apakahRingkasanPaymentTampil = true,
            ),
            saatAksiDikirim = {},
        )
    }
}
