package id.cassy.kasir.antarmuka.utama

import id.cassy.kasir.ranah.kasuspenggunaan.AmbilDaftarMeja
import id.cassy.kasir.ranah.kasuspenggunaan.AmatiPesananPending
import id.cassy.kasir.ranah.kasuspenggunaan.AmatiPreferensiToko
import id.cassy.kasir.ranah.kasuspenggunaan.AmbilPengaturanToko
import id.cassy.kasir.ranah.kasuspenggunaan.BayarPesananPending
import id.cassy.kasir.ranah.kasuspenggunaan.HapusPesananPending
import id.cassy.kasir.ranah.kasuspenggunaan.LanjutkanPesananPending
import id.cassy.kasir.ranah.kasuspenggunaan.MuatKatalogProduk
import id.cassy.kasir.ranah.kasuspenggunaan.SelesaikanCheckoutLokalKasir
import id.cassy.kasir.ranah.kasuspenggunaan.SimpanPreferensiToko
import id.cassy.kasir.ranah.model.HasilOperasiJaringan
import id.cassy.kasir.ranah.model.PengaturanToko
import id.cassy.kasir.ranah.model.PreferensiToko
import id.cassy.kasir.ranah.model.Meja
import id.cassy.kasir.ranah.model.Produk
import id.cassy.kasir.ranah.model.StatusSinkronisasi
import id.cassy.kasir.ranah.model.StatusTransaksi
import id.cassy.kasir.ranah.model.Transaksi
import id.cassy.kasir.ranah.model.Uang
import id.cassy.kasir.ranah.repositori.RepositoriPengaturanToko
import id.cassy.kasir.ranah.repositori.RepositoriPreferensiToko
import id.cassy.kasir.ranah.repositori.RepositoriProduk
import id.cassy.kasir.ranah.repositori.RepositoriMeja
import id.cassy.kasir.ranah.repositori.RepositoriTransaksi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Pengujian unit untuk [LayarUtamaKasirViewModel].
 *
 * Pengujian memakai fake repository agar perilaku layar utama bisa dikunci
 * tanpa ketergantungan ke Room, Retrofit, atau DataStore asli.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PengujianLayarUtamaKasirViewModel {

    private val pengaturUji = StandardTestDispatcher()
    private val cakupanPengujian = TestScope(pengaturUji)
    private val repositoriProdukPalsu = RepositoriProdukPalsu()
    private val repositoriTransaksiPalsu = RepositoriTransaksiPalsu()
    private val repositoriPreferensiTokoPalsu = RepositoriPreferensiTokoPalsu()
    private val muatKatalogProduk = MuatKatalogProduk(repositoriProdukPalsu)
    private val selesaikanCheckoutLokalKasir = SelesaikanCheckoutLokalKasir(repositoriTransaksiPalsu)
    private val repositoriPengaturanTokoPalsu = RepositoriPengaturanTokoPalsu()
    private val amatiPreferensiToko = AmatiPreferensiToko(repositoriPreferensiTokoPalsu)
    private val simpanPreferensiToko = SimpanPreferensiToko(repositoriPreferensiTokoPalsu)
    private val ambilPengaturanToko = AmbilPengaturanToko(repositoriPengaturanTokoPalsu)
    private val amatiPesananPending = AmatiPesananPending(repositoriTransaksiPalsu)
    private val lanjutkanPesananPending = LanjutkanPesananPending(repositoriTransaksiPalsu)
    private val bayarPesananPending = BayarPesananPending(repositoriTransaksiPalsu)
    private val hapusPesananPending = HapusPesananPending(repositoriTransaksiPalsu)

    private val repositoriMejaPalsu = RepositoriMejaPalsu()
    private val ambilDaftarMeja = AmbilDaftarMeja(repositoriMejaPalsu)

    private lateinit var pengelolaTampilan: LayarUtamaKasirViewModel

    @Before
    fun siapkan() {
        Dispatchers.setMain(pengaturUji)
        pengelolaTampilan = LayarUtamaKasirViewModel(
            muatKatalogProduk = muatKatalogProduk,
            selesaikanCheckoutLokalKasir = selesaikanCheckoutLokalKasir,
            amatiPreferensiToko = amatiPreferensiToko,
            simpanPreferensiToko = simpanPreferensiToko,
            ambilPengaturanToko = ambilPengaturanToko,
            amatiPesananPending = amatiPesananPending,
            lanjutkanPesananPending = lanjutkanPesananPending,
            bayarPesananPending = bayarPesananPending,
            hapusPesananPending = hapusPesananPending,
            ambilDaftarMeja = ambilDaftarMeja,
        )
    }

    @After
    fun bersihkan() {
        Dispatchers.resetMain()
    }

    @Test
    fun inisialisasiMemintaKatalogAwalTersedia() = cakupanPengujian.runTest {
        advanceUntilIdle()

        assertEquals(1, repositoriProdukPalsu.jumlahPermintaanKatalogAwal)
    }

    @Test
    fun tambahProdukMemasukkanItemKeKeranjang() = cakupanPengujian.runTest {
        val produk = buatProdukContoh(
            identitasProduk = "produk-kopi",
            namaProduk = "Kopi Susu",
            stokTersedia = 5,
        )
        repositoriProdukPalsu.aturDaftarProduk(listOf(produk))

        val pekerjaanPengumpul = launch(UnconfinedTestDispatcher(testScheduler)) {
            pengelolaTampilan.modelTampilan.collect()
        }

        advanceUntilIdle()

        pengelolaTampilan.tanganiAksi(
            AksiLayarUtamaKasir.TambahProdukKeKeranjang(
                produkId = produk.id,
            ),
        )
        advanceUntilIdle()

        val modelTampilan = pengelolaTampilan.modelTampilan.value
        assertEquals(1, modelTampilan.daftarItemKeranjang.size)
        assertEquals(1, modelTampilan.daftarItemKeranjang.first().jumlah)
        assertEquals(produk.id, modelTampilan.daftarItemKeranjang.first().produk.id)
        assertEquals(1, modelTampilan.statusBeranda.jumlahItemKeranjang)
        assertTrue(modelTampilan.statusBeranda.statusSinkronisasi is StatusSinkronisasi.SinkronLokal)

        pekerjaanPengumpul.cancel()
    }

    @Test
    fun tambahProdukMelebihiStokMengirimPesanSingkat() = cakupanPengujian.runTest {
        val produk = buatProdukContoh(
            identitasProduk = "produk-teh",
            namaProduk = "Teh Manis",
            stokTersedia = 1,
        )
        repositoriProdukPalsu.aturDaftarProduk(listOf(produk))

        val daftarEfek = mutableListOf<EfekLayarUtamaKasir>()
        val pekerjaanPengumpulModel = launch(UnconfinedTestDispatcher(testScheduler)) {
            pengelolaTampilan.modelTampilan.collect()
        }
        val pekerjaanPengumpulEfek = launch(UnconfinedTestDispatcher(testScheduler)) {
            pengelolaTampilan.efek.collect { efek ->
                daftarEfek.add(efek)
            }
        }

        advanceUntilIdle()

        pengelolaTampilan.tanganiAksi(
            AksiLayarUtamaKasir.TambahProdukKeKeranjang(
                produkId = produk.id,
            ),
        )
        advanceUntilIdle()

        pengelolaTampilan.tanganiAksi(
            AksiLayarUtamaKasir.TambahProdukKeKeranjang(
                produkId = produk.id,
            ),
        )
        advanceUntilIdle()

        val modelTampilan = pengelolaTampilan.modelTampilan.value
        assertEquals(1, modelTampilan.daftarItemKeranjang.size)
        assertEquals(1, modelTampilan.daftarItemKeranjang.first().jumlah)

        val efekTerakhir = daftarEfek.last() as EfekLayarUtamaKasir.TampilkanPesanSingkat
        assertEquals(
            "Stok produk sudah mencapai batas maksimum.",
            efekTerakhir.pesan,
        )

        pekerjaanPengumpulEfek.cancel()
        pekerjaanPengumpulModel.cancel()
    }

    @Test
    fun checkoutBerhasilMengosongkanKeranjang() = cakupanPengujian.runTest {
        val produk = buatProdukContoh(
            identitasProduk = "produk-roti",
            namaProduk = "Roti Bakar",
            harga = 12_000L,
            stokTersedia = 3,
        )
        repositoriProdukPalsu.aturDaftarProduk(listOf(produk))

        val pekerjaanPengumpul = launch(UnconfinedTestDispatcher(testScheduler)) {
            pengelolaTampilan.modelTampilan.collect()
        }

        advanceUntilIdle()

        pengelolaTampilan.tanganiAksi(
            AksiLayarUtamaKasir.TambahProdukKeKeranjang(
                produkId = produk.id,
            ),
        )
        advanceUntilIdle()

        pengelolaTampilan.tanganiAksi(AksiLayarUtamaKasir.CobaCheckout)
        advanceUntilIdle()

        pengelolaTampilan.tanganiAksi(AksiLayarUtamaKasir.BayarSekarang)
        advanceUntilIdle()

        val modelTampilan = pengelolaTampilan.modelTampilan.value
        assertTrue(modelTampilan.daftarItemKeranjang.isEmpty())
        assertTrue(modelTampilan.statusHasilCheckout.apakahTampil)
        assertEquals("Transaksi Berhasil", modelTampilan.statusHasilCheckout.judul)
        assertEquals(1, repositoriTransaksiPalsu.daftarTransaksiTersimpan.size)
        assertTrue(modelTampilan.statusBeranda.statusSinkronisasi is StatusSinkronisasi.SinkronLokal)

        pekerjaanPengumpul.cancel()
    }

    @Test
    fun kurangiProdukMengurangiJumlahItemKeranjang() = cakupanPengujian.runTest {
        val produk = buatProdukContoh(
            identitasProduk = "produk-kopi",
            namaProduk = "Kopi Susu",
            stokTersedia = 5,
        )
        repositoriProdukPalsu.aturDaftarProduk(listOf(produk))

        val pekerjaanPengumpul = launch(UnconfinedTestDispatcher(testScheduler)) {
            pengelolaTampilan.modelTampilan.collect()
        }

        advanceUntilIdle()

        repeat(2) {
            pengelolaTampilan.tanganiAksi(
                AksiLayarUtamaKasir.TambahProdukKeKeranjang(
                    produkId = produk.id,
                ),
            )
            advanceUntilIdle()
        }

        pengelolaTampilan.tanganiAksi(
            AksiLayarUtamaKasir.KurangiProdukDiKeranjang(
                produkId = produk.id,
            ),
        )
        advanceUntilIdle()

        val modelTampilan = pengelolaTampilan.modelTampilan.value

        assertEquals(1, modelTampilan.daftarItemKeranjang.size)
        assertEquals(1, modelTampilan.daftarItemKeranjang.first().jumlah)

        pekerjaanPengumpul.cancel()
    }

    @Test
    fun hapusProdukMenghapusItemDariKeranjang() = cakupanPengujian.runTest {
        val produk = buatProdukContoh(
            identitasProduk = "produk-roti",
            namaProduk = "Roti Bakar",
            stokTersedia = 5,
        )
        repositoriProdukPalsu.aturDaftarProduk(listOf(produk))

        val pekerjaanPengumpul = launch(UnconfinedTestDispatcher(testScheduler)) {
            pengelolaTampilan.modelTampilan.collect()
        }

        advanceUntilIdle()

        pengelolaTampilan.tanganiAksi(
            AksiLayarUtamaKasir.TambahProdukKeKeranjang(
                produkId = produk.id,
            ),
        )
        advanceUntilIdle()

        pengelolaTampilan.tanganiAksi(
            AksiLayarUtamaKasir.HapusProdukDariKeranjang(
                produkId = produk.id,
            ),
        )
        advanceUntilIdle()

        val modelTampilan = pengelolaTampilan.modelTampilan.value

        assertTrue(modelTampilan.daftarItemKeranjang.isEmpty())
        assertEquals(0, modelTampilan.statusBeranda.jumlahItemKeranjang)

        pekerjaanPengumpul.cancel()
    }

    @Test
    fun resetPencarianMengosongkanKataKunci() = cakupanPengujian.runTest {
        repositoriProdukPalsu.aturDaftarProduk(
            listOf(
                buatProdukContoh(
                    identitasProduk = "produk-kopi",
                    namaProduk = "Kopi Susu",
                    stokTersedia = 5,
                ),
                buatProdukContoh(
                    identitasProduk = "produk-teh",
                    namaProduk = "Teh Manis",
                    stokTersedia = 5,
                ),
            ),
        )

        val pekerjaanPengumpul = launch(UnconfinedTestDispatcher(testScheduler)) {
            pengelolaTampilan.modelTampilan.collect()
        }

        advanceUntilIdle()

        pengelolaTampilan.tanganiAksi(
            AksiLayarUtamaKasir.UbahKataKunciPencarian(
                kataKunciBaru = "kopi",
            ),
        )
        advanceUntilIdle()

        pengelolaTampilan.tanganiAksi(AksiLayarUtamaKasir.ResetPencarian)
        advanceUntilIdle()

        val modelTampilan = pengelolaTampilan.modelTampilan.value

        assertEquals("", modelTampilan.kataKunciPencarian)

        pekerjaanPengumpul.cancel()
    }

    private fun buatProdukContoh(
        identitasProduk: String,
        namaProduk: String,
        harga: Long = 10_000L,
        stokTersedia: Int,
    ): Produk {
        return Produk(
            id = identitasProduk,
            nama = namaProduk,
            harga = harga,
            stokTersedia = stokTersedia,
        )
    }

    private class RepositoriProdukPalsu : RepositoriProduk {
        private val daftarProduk = MutableStateFlow<List<Produk>>(emptyList())
        var jumlahPermintaanKatalogAwal: Int = 0
            private set

        fun aturDaftarProduk(daftarBaru: List<Produk>) {
            daftarProduk.value = daftarBaru
        }

        override fun amatiSemuaProduk(): Flow<List<Produk>> {
            return daftarProduk
        }

        override fun amatiProdukBerdasarkanIdentitas(identitasProduk: String): Flow<Produk?> {
            return daftarProduk.map { daftarProduk ->
                daftarProduk.firstOrNull { produk ->
                    produk.id == identitasProduk
                }
            }
        }

        override fun cariProdukLokal(kataKunci: String): Flow<List<Produk>> {
            return daftarProduk.map { daftarProduk ->
                daftarProduk.filter { produk ->
                    produk.nama.contains(kataKunci, ignoreCase = true)
                }
            }
        }

        override suspend fun pastikanKatalogAwalTersedia() {
            jumlahPermintaanKatalogAwal += 1
        }

        override suspend fun sinkronkanKatalog(): HasilOperasiJaringan<Unit> {
            return HasilOperasiJaringan.Berhasil(Unit)
        }

        override suspend fun simpanProduk(produk: Produk) {
            daftarProduk.value = daftarProduk.value
                .filterNot { produkLama ->
                    produkLama.id == produk.id
                } + produk
        }

        override suspend fun hapusProduk(identitasProduk: String) {
            daftarProduk.value = daftarProduk.value
                .filterNot { produk ->
                    produk.id == identitasProduk
                }
        }
    }

    private class RepositoriTransaksiPalsu : RepositoriTransaksi {
        val daftarTransaksiTersimpan = mutableListOf<Transaksi>()

        override fun amatiSemuaTransaksi(): Flow<List<Transaksi>> {
            return MutableStateFlow(daftarTransaksiTersimpan.toList())
        }

        override suspend fun simpanTransaksi(transaksi: Transaksi) {
            daftarTransaksiTersimpan.add(transaksi)
        }

        override suspend fun simpanTransaksiDanKurangiStok(transaksi: Transaksi) {
            daftarTransaksiTersimpan.add(transaksi)
        }

        override fun amatiTransaksiBerdasarkanIdentitas(identitasTransaksi: String): Flow<Transaksi?> {
            return MutableStateFlow(
                daftarTransaksiTersimpan.firstOrNull { transaksi ->
                    transaksi.id == identitasTransaksi
                },
            )
        }

        override suspend fun ambilTransaksiBerdasarkanIdentitas(identitasTransaksi: String): Transaksi? {
            return daftarTransaksiTersimpan.firstOrNull { transaksi ->
                transaksi.id == identitasTransaksi
            }
        }

        override fun amatiTransaksiPending(): Flow<List<Transaksi>> {
            return MutableStateFlow(
                daftarTransaksiTersimpan.filter { it.status == StatusTransaksi.Pending },
            )
        }

        override suspend fun hapusTransaksiDanKembalikanStok(identitasTransaksi: String) {
            daftarTransaksiTersimpan.removeAll { it.id == identitasTransaksi }
        }

        override suspend fun perbaruiStatusDanPembayaranTransaksi(
            identitasTransaksi: String,
            status: StatusTransaksi,
            uangDibayar: Uang,
        ) {
            val indeks = daftarTransaksiTersimpan.indexOfFirst { it.id == identitasTransaksi }
            if (indeks >= 0) {
                daftarTransaksiTersimpan[indeks] = daftarTransaksiTersimpan[indeks].copy(
                    status = status,
                    uangDibayar = uangDibayar,
                )
            }
        }
    }

    private class RepositoriPengaturanTokoPalsu : RepositoriPengaturanToko {
        private val pengaturanToko = MutableStateFlow(PengaturanToko())

        override fun ambilPengaturan(): Flow<PengaturanToko> {
            return pengaturanToko
        }

        override suspend fun simpanPengaturan(pengaturan: PengaturanToko) {
            this.pengaturanToko.value = pengaturan
        }
    }

    private class RepositoriMejaPalsu : RepositoriMeja {
        private val daftarMeja = MutableStateFlow<List<Meja>>(emptyList())

        override fun amatiSemuaMeja(): Flow<List<Meja>> = daftarMeja

        override suspend fun simpanMeja(meja: Meja) {
            daftarMeja.value = daftarMeja.value + meja
        }

        override suspend fun hapusMeja(id: String) {
            daftarMeja.value = daftarMeja.value.filter { it.id != id }
        }
    }

    private class RepositoriPreferensiTokoPalsu : RepositoriPreferensiToko {
        private val preferensiToko = MutableStateFlow(PreferensiToko())

        override fun amatiPreferensiToko(): Flow<PreferensiToko> {
            return preferensiToko
        }

        override suspend fun simpanPreferensiToko(preferensiToko: PreferensiToko) {
            this.preferensiToko.value = preferensiToko
        }

        override suspend fun simpanSinkronisasiKatalogBerhasil(waktuEpochMili: Long) {
            preferensiToko.value = preferensiToko.value.copy(
                waktuSinkronisasiKatalogTerakhirEpochMili = waktuEpochMili,
                pesanGagalSinkronisasiKatalogTerakhir = null,
            )
        }

        override suspend fun simpanSinkronisasiKatalogGagal(pesan: String) {
            preferensiToko.value = preferensiToko.value.copy(
                pesanGagalSinkronisasiKatalogTerakhir = pesan,
            )
        }
    }
}
