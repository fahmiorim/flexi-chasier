package id.cassy.kasir.antarmuka.utama

import android.content.res.Configuration
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.cassy.kasir.antarmuka.tema.TemaCassyKasir
import id.cassy.kasir.ranah.contoh.KatalogProdukContoh
import id.cassy.kasir.ranah.model.ItemKeranjang
import id.cassy.kasir.ranah.model.StatusSinkronisasi
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
fun LayarUtamaKasir(
    modifier: Modifier = Modifier,
    modelTampilan: ModelTampilanLayarUtamaKasir,
    saatAksiDikirim: (AksiLayarUtamaKasir) -> Unit,
    alurEfek: Flow<EfekLayarUtamaKasir> = emptyFlow(),
    saatBukaSidebar: () -> Unit = {},
    saatBukaDetailProduk: (String) -> Unit = {},
) {
    val keadaanHostSnackbar: SnackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(alurEfek) {
        alurEfek.collectLatest { efek ->
            when (efek) {
                is EfekLayarUtamaKasir.TampilkanPesanSingkat -> {
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
        if (modelTampilan.statusKonfirmasiCheckout.apakahTampil) {
            DialogKonfirmasiCheckoutKasir(
                statusKonfirmasiCheckout = modelTampilan.statusKonfirmasiCheckout,
                saatBatalkan = {
                    saatAksiDikirim(AksiLayarUtamaKasir.BatalkanKonfirmasiCheckout)
                },
                saatKonfirmasi = {
                    saatAksiDikirim(AksiLayarUtamaKasir.BayarSekarang)
                },
                saatSimpanPesanan = {
                    saatAksiDikirim(AksiLayarUtamaKasir.SimpanPesanan)
                },
                saatCatatanBerubah = { catatan ->
                    saatAksiDikirim(AksiLayarUtamaKasir.UbahCatatanCheckout(catatan))
                },
                saatMetodeBayarBerubah = { metodeBayar ->
                    saatAksiDikirim(AksiLayarUtamaKasir.UbahMetodeBayar(metodeBayar))
                },
                saatTipeOrderBerubah = { tipeOrder ->
                    saatAksiDikirim(AksiLayarUtamaKasir.UbahTipeOrder(tipeOrder))
                },
            )
        }

        PanelPesananPending(
            daftarPesananPending = modelTampilan.daftarPesananPending,
            apakahTampil = modelTampilan.apakahPanelPesananPendingTampil,
            saatTutup = { saatAksiDikirim(AksiLayarUtamaKasir.TutupPanelPesananPending) },
            saatLanjutkan = { id ->
                saatAksiDikirim(AksiLayarUtamaKasir.LanjutkanPesananPending(id))
            },
            saatBayar = { id ->
                saatAksiDikirim(AksiLayarUtamaKasir.BayarPesananPending(id))
            },
            saatHapus = { id ->
                saatAksiDikirim(AksiLayarUtamaKasir.HapusPesananPending(id))
            },
        )

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingKerangka),
        ) {
            val gunakanDuaPanel = maxWidth >= 840.dp

            if (gunakanDuaPanel) {
                TataLetakTabletKasir(
                    modelTampilan = modelTampilan,
                    saatAksiDikirim = saatAksiDikirim,
                    saatBukaSidebar = saatBukaSidebar,
                    saatBukaDetailProduk = saatBukaDetailProduk,
                )
            } else {
                TataLetakPonselKasir(
                    modelTampilan = modelTampilan,
                    saatAksiDikirim = saatAksiDikirim,
                    saatBukaSidebar = saatBukaSidebar,
                    saatBukaDetailProduk = saatBukaDetailProduk,
                )
            }
        }
    }
}

@Preview(
    name = "Workspace tablet terang dialog checkout",
    showBackground = true,
    widthDp = 1280,
    heightDp = 800,
)
@Composable
private fun PreviewWorkspaceTabletTerangDialogCheckout() {
    val daftarProduk = KatalogProdukContoh.daftarAwal()

    TemaCassyKasir(
        modeGelap = false,
        gunakanWarnaDinamis = false,
    ) {
        LayarUtamaKasir(
            modelTampilan = ModelTampilanLayarUtamaKasir(
                statusBeranda = StatusBerandaKasir(
                    namaAplikasi = "Cassy Kasir",
                    sloganAplikasi = "Solusi Digital UMKM Modern",
                    jumlahProdukTersedia = daftarProduk.size,
                    jumlahItemKeranjang = 4,
                    totalBelanjaSementara = "Rp59000",
                    statusSinkronisasi = StatusSinkronisasi.SinkronLokal,
                    labelAksiSinkronisasi = "Perbarui katalog",
                    aksiSinkronisasiAktif = true,
                    labelMetadataSinkronisasi = "Katalog lokal siap digunakan.",
                ),
                daftarProdukTersaring = daftarProduk,
                daftarItemKeranjang = listOf(
                    ItemKeranjang(
                        produk = daftarProduk[0],
                        jumlah = 2,
                    ),
                    ItemKeranjang(
                        produk = daftarProduk[1],
                        jumlah = 1,
                    ),
                    ItemKeranjang(
                        produk = daftarProduk[2],
                        jumlah = 1,
                    ),
                ),
                statusKeranjang = StatusKeranjangKasir(
                    judul = "Keranjang aktif",
                    deskripsi = "Atur jumlah item sebelum lanjut ke pembayaran.",
                    jumlahItem = "4 item",
                ),
                ringkasanPembayaran = RingkasanPembayaranKasir(
                    subtotal = "Rp59000",
                    potongan = "Rp0",
                    pajak = "Rp0",
                    totalAkhir = "Rp59000",
                    labelAksiUtama = "Bayar sekarang",
                    aksiUtamaAktif = true,
                ),
                statusKonfirmasiCheckout = StatusKonfirmasiCheckoutKasir(
                    apakahTampil = true,
                    judul = "Konfirmasi pembayaran",
                    deskripsi = "Bayar 4 item dengan total Rp59000 sekarang?",
                    labelKonfirmasi = "Bayar sekarang",
                ),
                kataKunciPencarian = "",
                apakahRingkasanPembayaranTampil = true,
            ),
            saatAksiDikirim = {},
        )
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
    val daftarProduk = KatalogProdukContoh.daftarAwal()

    TemaCassyKasir(
        modeGelap = true,
        gunakanWarnaDinamis = false,
    ) {
        LayarUtamaKasir(
            modelTampilan = ModelTampilanLayarUtamaKasir(
                statusBeranda = StatusBerandaKasir(
                    namaAplikasi = "Cassy Kasir",
                    sloganAplikasi = "Solusi Digital UMKM Modern",
                    jumlahProdukTersedia = daftarProduk.size,
                    jumlahItemKeranjang = 0,
                    totalBelanjaSementara = "Rp0",
                    statusSinkronisasi = StatusSinkronisasi.Berhasil,
                    labelAksiSinkronisasi = "Perbarui katalog",
                    aksiSinkronisasiAktif = true,
                    labelMetadataSinkronisasi = "Baru saja diperbarui.",
                ),
                daftarProdukTersaring = daftarProduk,
                daftarItemKeranjang = emptyList(),
                statusKeranjang = StatusKeranjangKasir(
                    judul = "Keranjang kosong",
                    deskripsi = "Mulai transaksi dengan memilih produk.",
                    jumlahItem = "0 item",
                ),
                ringkasanPembayaran = RingkasanPembayaranKasir(
                    subtotal = "Rp0",
                    potongan = "Rp0",
                    pajak = "Rp0",
                    totalAkhir = "Rp0",
                    labelAksiUtama = "Pilih produk",
                    aksiUtamaAktif = false,
                ),
                statusHasilCheckout = StatusHasilCheckoutKasir(
                    apakahTampil = true,
                    judul = "Transaksi berhasil",
                    deskripsi = "4 item dengan total Rp59000 siap disimpan ke riwayat lokal pada scope data berikutnya.",
                ),
                kataKunciPencarian = "",
                apakahRingkasanPembayaranTampil = true,
            ),
            saatAksiDikirim = {},
        )
    }
}
