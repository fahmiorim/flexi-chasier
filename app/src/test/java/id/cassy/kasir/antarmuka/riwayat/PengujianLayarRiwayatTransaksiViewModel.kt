package id.cassy.kasir.antarmuka.riwayat

import id.cassy.kasir.ranah.kasuspenggunaan.AmatiRiwayatTransaksi
import id.cassy.kasir.ranah.model.ItemKeranjang
import id.cassy.kasir.ranah.model.Produk
import id.cassy.kasir.ranah.model.StatusTransaksi
import id.cassy.kasir.ranah.model.Transaksi
import id.cassy.kasir.ranah.model.Uang
import id.cassy.kasir.ranah.repositori.RepositoriTransaksi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
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
 * Pengujian unit untuk [LayarRiwayatTransaksiViewModel].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PengujianLayarRiwayatTransaksiViewModel {

    private val pengaturUji = StandardTestDispatcher()
    private val cakupanPengujian = kotlinx.coroutines.test.TestScope(pengaturUji)
    private val repositoriPalsu = RepositoriTransaksiPalsu()
    private val amatiRiwayatTransaksi = AmatiRiwayatTransaksi(repositoriPalsu)
    private lateinit var pengelolaTampilan: LayarRiwayatTransaksiViewModel

    @Before
    fun siapkan() {
        Dispatchers.setMain(pengaturUji)
        pengelolaTampilan = LayarRiwayatTransaksiViewModel(amatiRiwayatTransaksi)
    }

    @After
    fun bersihkan() {
        Dispatchers.resetMain()
    }

    @Test
    fun awalMuatMenampilkanStatusMemuat() = runTest {
        val statusMuat = pengelolaTampilan.modelTampilan.value.statusMuat
        assertTrue(statusMuat is StatusMuatRiwayatTransaksi.Memuat)
    }

    @Test
    fun berhasilMemuatDaftarTransaksi() = cakupanPengujian.runTest {
        // Mulai mengumpulkan aliran agar StateFlow aktif.
        val pekerjaanPengumpul = launch(UnconfinedTestDispatcher(testScheduler)) {
            pengelolaTampilan.modelTampilan.collect()
        }

        val daftarTransaksi = listOf(
            Transaksi(
                id = "TRX-1",
                daftarItemKeranjang = listOf(
                    ItemKeranjang(
                        produk = Produk(id = "P1", nama = "Kopi", harga = 10_000L, stokTersedia = 10),
                        jumlah = 1
                    )
                ),
                    uangDibayar = Uang.dariRupiah(10_000L),
                waktuTransaksiEpochMili = System.currentTimeMillis()
            )
        )
        repositoriPalsu.emit(daftarTransaksi)
        advanceUntilIdle()

        val statusMuat = pengelolaTampilan.modelTampilan.value.statusMuat
        if (statusMuat !is StatusMuatRiwayatTransaksi.Berhasil) {
            val pesan = when (statusMuat) {
                is StatusMuatRiwayatTransaksi.Gagal -> "Status Gagal: ${statusMuat.deskripsi}"
                is StatusMuatRiwayatTransaksi.Kosong -> "Status Kosong: ${statusMuat.deskripsi}"
                StatusMuatRiwayatTransaksi.Memuat -> "Status Memuat"
            }
            pekerjaanPengumpul.cancel()
            throw AssertionError("Seharusnya Status Berhasil, tapi: $pesan")
        }
        val berhasil = statusMuat
        assertEquals(1, berhasil.daftarRingkasanTransaksi.size)
        assertEquals("TRX-1", berhasil.daftarRingkasanTransaksi.first().transaksiId)
        pekerjaanPengumpul.cancel()
    }

    @Test
    fun pencarianBerhasilMenyaringTransaksi() = cakupanPengujian.runTest {
        // Mulai mengumpulkan aliran agar StateFlow aktif.
        val pekerjaanPengumpul = launch(UnconfinedTestDispatcher(testScheduler)) {
            pengelolaTampilan.modelTampilan.collect()
        }

        val daftarTransaksi = listOf(
            Transaksi(
                id = "TRX-KOPI",
                daftarItemKeranjang = listOf(
                    ItemKeranjang(
                        produk = Produk(id = "P1", nama = "Kopi", harga = 10_000L, stokTersedia = 10),
                        jumlah = 1
                    )
                ),
                uangDibayar = Uang.dariRupiah(10_000L),
                waktuTransaksiEpochMili = System.currentTimeMillis()
            ),
            Transaksi(
                id = "TRX-TEH",
                daftarItemKeranjang = listOf(
                    ItemKeranjang(
                        produk = Produk(id = "P2", nama = "Teh", harga = 5_000L, stokTersedia = 10),
                        jumlah = 1
                    )
                ),
                uangDibayar = Uang.dariRupiah(5_000L),
                waktuTransaksiEpochMili = System.currentTimeMillis()
            )
        )
        repositoriPalsu.emit(daftarTransaksi)
        advanceUntilIdle()

        pengelolaTampilan.perbaruiKataKunciPencarian("Kopi")
        advanceUntilIdle() // Menunggu debounce 250ms

        val statusMuat = pengelolaTampilan.modelTampilan.value.statusMuat
        assertTrue("Status seharusnya Berhasil, tapi: $statusMuat", statusMuat is StatusMuatRiwayatTransaksi.Berhasil)
        val berhasil = statusMuat as StatusMuatRiwayatTransaksi.Berhasil
        assertEquals(1, berhasil.daftarRingkasanTransaksi.size)
        assertEquals("TRX-KOPI", berhasil.daftarRingkasanTransaksi.first().transaksiId)
        pekerjaanPengumpul.cancel()
    }

    private class RepositoriTransaksiPalsu : RepositoriTransaksi {
        private val flow = MutableStateFlow<List<Transaksi>>(emptyList())

        fun emit(daftar: List<Transaksi>) {
            flow.value = daftar
        }

        override fun amatiSemuaTransaksi(): Flow<List<Transaksi>> = flow

        override suspend fun simpanTransaksi(transaksi: Transaksi) {}
        override suspend fun simpanTransaksiDanKurangiStok(transaksi: Transaksi) {}
        override fun amatiTransaksiBerdasarkanIdentitas(identitasTransaksi: String): Flow<Transaksi?> = throw NotImplementedError()
        override suspend fun ambilTransaksiBerdasarkanIdentitas(identitasTransaksi: String): Transaksi? = throw NotImplementedError()
        override fun amatiTransaksiPending(): Flow<List<Transaksi>> = MutableStateFlow(emptyList())
        override suspend fun hapusTransaksiDanKembalikanStok(identitasTransaksi: String) = throw NotImplementedError()
        override suspend fun perbaruiStatusDanPembayaranTransaksi(identitasTransaksi: String, status: StatusTransaksi, uangDibayar: Uang) = throw NotImplementedError()
    }
}
