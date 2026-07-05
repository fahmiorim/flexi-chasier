package id.cassy.kasir.antarmuka.transaksi

import androidx.lifecycle.SavedStateHandle
import id.cassy.kasir.ranah.kasuspenggunaan.AmatiTransaksiBerdasarkanIdentitas
import id.cassy.kasir.ranah.model.ItemKeranjang
import id.cassy.kasir.ranah.model.Produk
import id.cassy.kasir.ranah.model.StatusTransaksi
import id.cassy.kasir.ranah.model.Transaksi
import id.cassy.kasir.ranah.model.Uang
import id.cassy.kasir.ranah.repositori.RepositoriTransaksi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
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

@OptIn(ExperimentalCoroutinesApi::class)
class PengujianLayarDetailTransaksiViewModel {

    private val pengaturUji = StandardTestDispatcher()
    private val cakupanPengujian = TestScope(pengaturUji)
    private val repositoriPalsu = RepositoriTransaksiPalsu()
    private val amatiTransaksiBerdasarkanIdentitas = AmatiTransaksiBerdasarkanIdentitas(repositoriPalsu)

    private lateinit var pengelolaTampilan: LayarDetailTransaksiViewModel

    @Before
    fun siapkan() {
        Dispatchers.setMain(pengaturUji)
    }

    @After
    fun bersihkan() {
        Dispatchers.resetMain()
    }

    private fun buatViewModel(identitasTransaksi: String) {
        val savedStateHandle = SavedStateHandle(mapOf("identitasTransaksi" to identitasTransaksi))
        pengelolaTampilan = LayarDetailTransaksiViewModel(
            savedStateHandle = savedStateHandle,
            amatiTransaksiBerdasarkanIdentitas = amatiTransaksiBerdasarkanIdentitas,
        )
    }

    @Test
    fun berhasilMemuatDetailTransaksi() = cakupanPengujian.runTest {
        val identitasTransaksi = "TRX-123"
        buatViewModel(identitasTransaksi)

        // Mulai mengumpulkan aliran agar StateFlow aktif.
        val pekerjaanPengumpul = launch(UnconfinedTestDispatcher(testScheduler)) {
            pengelolaTampilan.modelTampilan.collect()
        }

        val transaksi = Transaksi(
            id = identitasTransaksi,
            daftarItemKeranjang = listOf(
                ItemKeranjang(
                    produk = Produk(id = "P1", nama = "Kopi", harga = 10_000L, stokTersedia = 10),
                    jumlah = 2
                )
            ),
            uangDibayar = Uang.dariRupiah(20_000L),
            waktuTransaksiEpochMili = System.currentTimeMillis()
        )

        repositoriPalsu.emit(transaksi)
        advanceUntilIdle()

        val statusMuat = pengelolaTampilan.modelTampilan.value.statusMuat
        assertTrue("Status seharusnya Berhasil, tapi: $statusMuat", statusMuat is StatusMuatDetailTransaksi.Berhasil)
        val berhasil = statusMuat as StatusMuatDetailTransaksi.Berhasil
        assertEquals(identitasTransaksi, berhasil.transaksiId)
        assertEquals(1, berhasil.daftarItem.size)
        assertEquals("Kopi", berhasil.daftarItem.first().namaProduk)

        pekerjaanPengumpul.cancel()
    }

    @Test
    fun transaksiTidakDitemukan() = cakupanPengujian.runTest {
        val identitasTransaksi = "TRX-404"
        buatViewModel(identitasTransaksi)

        val pekerjaanPengumpul = launch(UnconfinedTestDispatcher(testScheduler)) {
            pengelolaTampilan.modelTampilan.collect()
        }

        // Mengirim nilai null untuk meniru transaksi yang tidak ditemukan.
        repositoriPalsu.emit(null)
        advanceUntilIdle()

        val statusMuat = pengelolaTampilan.modelTampilan.value.statusMuat
        assertTrue("Status seharusnya Kosong, tapi: $statusMuat", statusMuat is StatusMuatDetailTransaksi.Kosong)

        pekerjaanPengumpul.cancel()
    }

    private class RepositoriTransaksiPalsu : RepositoriTransaksi {
        private val flow = MutableSharedFlow<Transaksi?>(replay = 1)

        suspend fun emit(transaksi: Transaksi?) {
            flow.emit(transaksi)
        }

        override fun amatiSemuaTransaksi(): Flow<List<Transaksi>> = throw NotImplementedError()

        override fun amatiTransaksiBerdasarkanIdentitas(identitasTransaksi: String): Flow<Transaksi?> {
            return flow
        }

        override suspend fun simpanTransaksi(transaksi: Transaksi) = throw NotImplementedError()

        override suspend fun simpanTransaksiDanKurangiStok(transaksi: Transaksi) = throw NotImplementedError()

        override suspend fun ambilTransaksiBerdasarkanIdentitas(identitasTransaksi: String): Transaksi? = throw NotImplementedError()
        override fun amatiTransaksiPending(): Flow<List<Transaksi>> = throw NotImplementedError()
        override suspend fun hapusTransaksiDanKembalikanStok(identitasTransaksi: String) = throw NotImplementedError()
        override suspend fun perbaruiStatusDanPembayaranTransaksi(identitasTransaksi: String, status: StatusTransaksi, uangDibayar: Uang) = throw NotImplementedError()
    }
}
