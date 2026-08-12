package id.flexi.kasir.domain.usecase

import id.flexi.kasir.domain.model.CartItem
import id.flexi.kasir.domain.model.Produk
import id.flexi.kasir.domain.model.SyncStatus
import id.flexi.kasir.domain.model.TransactionStatus
import id.flexi.kasir.domain.model.Transaction
import id.flexi.kasir.domain.model.Uang
import id.flexi.kasir.domain.model.Meja
import id.flexi.kasir.domain.model.TableStatus
import id.flexi.kasir.domain.model.Bahan
import id.flexi.kasir.domain.model.BahanResep
import id.flexi.kasir.domain.model.PembelianBahan
import id.flexi.kasir.domain.model.Resep
import id.flexi.kasir.domain.repository.BahanRepository
import id.flexi.kasir.domain.repository.TableRepository
import id.flexi.kasir.domain.repository.TransactionRepository
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pengujian unit untuk kasus penggunaan [CompleteLocalCheckout].
 */
class PengujianCompleteLocalCheckout {

    private val repositoriPalsu = TransactionRepositoryPalsu()
    private val repositoriMejaPalsu = TableRepositoryPalsu()
    private val CompleteLocalCheckout = CompleteLocalCheckout(repositoriPalsu, repositoriMejaPalsu)

    @Test
    fun checkoutBerhasilMenyimpanTransactionDanMengosongkanKeranjang() = runBlocking {
        val daftarItem = listOf(
            CartItem(
                produk = Produk(
                    id = "produk-1",
                    nama = "Kopi",
                    harga = 10_000L,
                    stokTersedia = 10,
                ),
                jumlah = 2,
            )
        )

        val hasil = CompleteLocalCheckout.eksekusi(daftarItem)

        // Verifikasi hasil keluaran kasus penggunaan.
        assertTrue(hasil.daftarCartItemBaru.isEmpty())
        assertEquals(SyncStatus.LocalChanges, hasil.SyncStatusBaru)
        assertEquals(2, hasil.jumlahItemCheckout)
        assertEquals(20_000L, hasil.totalCheckout)

        // Verifikasi apakah repositori dipanggil.
        assertEquals(1, repositoriPalsu.daftarTransactionTersimpan.size)
        val TransactionTersimpan = repositoriPalsu.daftarTransactionTersimpan.first()
        assertEquals(Uang.dariRupiah(20_000L), TransactionTersimpan.uangDibayar)
        assertEquals(1, TransactionTersimpan.daftarCartItem.size)
    }

    @Test(expected = IllegalArgumentException::class)
    fun checkoutGagalJikaKeranjangKosong() = runBlocking {
        CompleteLocalCheckout.eksekusi(emptyList())
        Unit
    }

    @Test(expected = IllegalArgumentException::class)
    fun checkoutGagalJikaStokTidakCukup() = runBlocking {
        val daftarItem = listOf(
            CartItem(
                produk = Produk(
                    id = "produk-1",
                    nama = "Kopi",
                    harga = 10_000L,
                    stokTersedia = 1,
                    apakahStokDiaktifkan = true,
                ),
                jumlah = 2,
            )
        )

        CompleteLocalCheckout.eksekusi(daftarItem)
        Unit
    }

    @Test(expected = IllegalArgumentException::class)
    fun checkoutGagalJikaProdukTidakAktif() = runBlocking {
        val daftarItem = listOf(
            CartItem(
                produk = Produk(
                    id = "produk-1",
                    nama = "Kopi",
                    harga = 10_000L,
                    stokTersedia = 10,
                    aktif = false,
                ),
                jumlah = 1,
            )
        )

        CompleteLocalCheckout.eksekusi(daftarItem)
        Unit
    }

    // ── Pengujian delta stok bahan (resep) ──

    private val resepKopi = Resep(
        id = "resep-1",
        produkId = "produk-1",
        daftarBahan = listOf(BahanResep(bahanId = "bahan-1", jumlah = 1.5)),
    )

    @Test
    fun checkoutDibayarMengurangiStokBahanSesuaiResep() = runBlocking {
        val bahanRepo = BahanRepositoryPalsu(petaResep = mapOf("produk-1" to resepKopi))
        val checkout = CompleteLocalCheckout(TransactionRepositoryPalsu(), TableRepositoryPalsu(), bahanRepo)
        val daftarItem = listOf(
            CartItem(
                produk = Produk(id = "produk-1", nama = "Kopi", harga = 10_000L, stokTersedia = 10),
                jumlah = 2,
            )
        )

        checkout.eksekusi(daftarItem, status = TransactionStatus.Paid)

        // 2 item × 1.5 bahan = 3.0 bahan dikurangi.
        assertEquals(listOf("bahan-1" to -3.0), bahanRepo.pemanggilanPerbaruiStok)
    }

    @Test
    fun resumeDariPendingMemotongStokBahanPenuh() = runBlocking {
        val bahanRepo = BahanRepositoryPalsu(petaResep = mapOf("produk-1" to resepKopi))
        val repositoriTransaksi = TransactionRepositoryPalsu()
        val checkout = CompleteLocalCheckout(repositoriTransaksi, TableRepositoryPalsu(), bahanRepo)
        val daftarItem = listOf(
            CartItem(
                produk = Produk(id = "produk-1", nama = "Kopi", harga = 10_000L, stokTersedia = 10),
                jumlah = 2,
            )
        )
        // Transaksi lama Pending — bahan BELUM pernah dipotong saat simpan Pending.
        repositoriTransaksi.transaksiUntukDiambil["trx-resume"] = Transaction(
            id = "trx-resume",
            daftarCartItem = daftarItem,
            waktuTransactionEpochMili = 0L,
            status = TransactionStatus.Pending,
        )

        checkout.eksekusi(
            daftarItem,
            status = TransactionStatus.Paid,
            identitasTransaction = "trx-resume",
        )

        // Bahan tetap dipotong PENUH karena Pending tidak pernah memotong.
        assertEquals(listOf("bahan-1" to -3.0), bahanRepo.pemanggilanPerbaruiStok)
    }

    @Test
    fun eksekusiUlangTransaksiDibayarTidakMemotongStokBahanDuaKali() = runBlocking {
        val bahanRepo = BahanRepositoryPalsu(petaResep = mapOf("produk-1" to resepKopi))
        val repositoriTransaksi = TransactionRepositoryPalsu()
        val checkout = CompleteLocalCheckout(repositoriTransaksi, TableRepositoryPalsu(), bahanRepo)
        val daftarItem = listOf(
            CartItem(
                produk = Produk(id = "produk-1", nama = "Kopi", harga = 10_000L, stokTersedia = 10),
                jumlah = 2,
            )
        )
        // Transaksi lama SUDAH dibayar — bahan sudah dipotong di checkout pertama.
        repositoriTransaksi.transaksiUntukDiambil["trx-1"] = Transaction(
            id = "trx-1",
            daftarCartItem = daftarItem,
            waktuTransactionEpochMili = 0L,
            status = TransactionStatus.Paid,
        )

        checkout.eksekusi(
            daftarItem,
            status = TransactionStatus.Paid,
            identitasTransaction = "trx-1",
        )

        // Delta pemakaian = 0 → stok bahan TIDAK disentuh.
        assertTrue(bahanRepo.pemanggilanPerbaruiStok.isEmpty())
    }

    /**
     * Implementasi palsu repositori untuk pengujian unit murni.
     */
    private class TransactionRepositoryPalsu : TransactionRepository {
        val daftarTransactionTersimpan = mutableListOf<Transaction>()

        override suspend fun simpanTransaction(Transaction: Transaction) {
            daftarTransactionTersimpan.add(Transaction)
        }

        override suspend fun simpanTransactionDanKurangiStok(Transaction: Transaction) {
            daftarTransactionTersimpan.add(Transaction)
        }

        override suspend fun simpanTransactionDenganDeltaStok(Transaction: Transaction, oldTransaction: Transaction?) {
            daftarTransactionTersimpan.add(Transaction)
        }

        /** Transaksi yang dikembalikan [ambilTransactionBerdasarkanIdentitas]. */
        val transaksiUntukDiambil = mutableMapOf<String, Transaction>()

        override fun amatiSemuaTransaction(): Flow<List<Transaction>> = throw NotImplementedError()
        override fun ObserveTransactionById(identitasTransaction: String): Flow<Transaction?> = throw NotImplementedError()
        override suspend fun ambilTransactionBerdasarkanIdentitas(identitasTransaction: String): Transaction? {
            return transaksiUntukDiambil[identitasTransaction]
                ?: daftarTransactionTersimpan.lastOrNull { it.id == identitasTransaction }
        }
        override fun amatiTransactionPending(): Flow<List<Transaction>> = throw NotImplementedError()
        override fun amatiTransactionDiproses(): Flow<List<Transaction>> = throw NotImplementedError()
        override suspend fun perbaruiStatusTransaction(identitasTransaction: String, status: TransactionStatus) = throw NotImplementedError()
        override suspend fun hapusTransactionDanKembalikanStok(identitasTransaction: String) = throw NotImplementedError()
        override suspend fun perbaruiStatusDanPaymentTransaction(identitasTransaction: String, status: TransactionStatus, uangDibayar: Uang, paymentMethod: id.flexi.kasir.domain.model.PaymentMethod, waktuDibayarEpochMili: Long?) = throw NotImplementedError()
        override suspend fun perbaruiStatusDanWaktuTransaction(identitasTransaction: String, status: TransactionStatus, waktuDiprosesEpochMili: Long?, waktuSelesaiEpochMili: Long?, waktuDibayarEpochMili: Long?) = throw NotImplementedError()
        override suspend fun perbaruiWaktuSelesai(identitasTransaction: String, waktuSelesaiEpochMili: Long) = throw NotImplementedError()
        override suspend fun perbaruiWaktuDibayar(identitasTransaction: String, waktuDibayarEpochMili: Long) = throw NotImplementedError()
        override suspend fun tandaiItemSelesai(identitasTransaction: String) = throw NotImplementedError()
        override suspend fun pastikanDataAwalTersedia() {}
        override suspend fun ambilNomorAntrianBerikutnya(): Int = 1
        override suspend fun ambilIdProdukTerpopuler(batasJumlah: Int): List<String> = emptyList()

        override fun amatiTransactionLunas(): Flow<List<Transaction>> = throw NotImplementedError()

        override suspend fun batalkanTransaction(identitasTransaction: String, alasan: String?) {}

        override suspend fun ambilTransactionRentang(sejak: Long, sampai: Long): List<Transaction> = emptyList()
        override fun hitungTotalQRISSemua(): Flow<Long> = flowOf(0L)
        override fun hitungTotalTunaiSejak(sejak: Long): Flow<Long> = flowOf(0L)
        override fun hitungTotalQRISSejak(sejak: Long): Flow<Long> = flowOf(0L)
        override suspend fun hitungTotalTunaiRentang(sejak: Long, sampai: Long): Long = 0L
        override suspend fun hitungTotalQRISRentang(sejak: Long, sampai: Long): Long = 0L
        override fun amatiTransactionSejak(sejak: Long): Flow<List<Transaction>> = throw NotImplementedError()
        override fun amatiTransactionPaged(sejak: Long?, sampai: Long?): Flow<PagingData<Transaction>> = throw NotImplementedError()
    }

    private class TableRepositoryPalsu : TableRepository {
        private val daftarMeja = MutableStateFlow<List<Meja>>(emptyList())
        override fun amatiSemuaMeja(): Flow<List<Meja>> = daftarMeja
        override suspend fun SaveTable(meja: Meja) { daftarMeja.value = daftarMeja.value + meja }
        override suspend fun DeleteTable(id: String) { daftarMeja.value = daftarMeja.value.filter { it.id != id } }
        override suspend fun perbaruiTableStatus(id: String, tableStatus: TableStatus, TransactionId: String?) {
            daftarMeja.value = daftarMeja.value.map {
                if (it.id == id) it.copy(tableStatus = tableStatus, TransactionId = TransactionId) else it
            }
        }
    }

    /**
     * Implementasi palsu repositori bahan: hanya resep & pengurangan stok yang
     * dipakai checkout yang diimplementasikan.
     */
    private class BahanRepositoryPalsu(
        private val petaResep: Map<String, Resep> = emptyMap(),
    ) : BahanRepository {
        val pemanggilanPerbaruiStok = mutableListOf<Pair<String, Double>>()

        override suspend fun ambilResepByProdukId(produkId: String): Resep? = petaResep[produkId]

        override suspend fun perbaruiStokBahan(id: String, jumlah: Double) {
            pemanggilanPerbaruiStok += id to jumlah
        }

        override fun amatiSemuaBahan(): Flow<List<Bahan>> = throw NotImplementedError()
        override fun amatiBahanById(id: String): Flow<Bahan?> = throw NotImplementedError()
        override suspend fun ambilBahanById(id: String): Bahan? = throw NotImplementedError()
        override suspend fun saveBahan(bahan: Bahan) = throw NotImplementedError()
        override suspend fun deleteBahan(id: String) = throw NotImplementedError()
        override fun amatiPembelianBahan(bahanId: String): Flow<List<PembelianBahan>> = throw NotImplementedError()
        override suspend fun savePembelian(pembelian: PembelianBahan) = throw NotImplementedError()
        override suspend fun deletePembelian(id: String) = throw NotImplementedError()
        override suspend fun ambilPembelianTerakhir(bahanId: String): PembelianBahan? = throw NotImplementedError()
        override suspend fun perbaruiHargaSatuanBahan(id: String, harga: Long) = throw NotImplementedError()
        override fun amatiResepByProdukId(produkId: String): Flow<Resep?> = throw NotImplementedError()
        override suspend fun saveResep(resep: Resep) = throw NotImplementedError()
        override suspend fun deleteResep(id: String) = throw NotImplementedError()
        override suspend fun saveBahanResep(daftar: List<BahanResep>) = throw NotImplementedError()
        override suspend fun deleteBahanResepByResepId(resepId: String) = throw NotImplementedError()
        override suspend fun ambilSemuaResepWithBahan(): List<Resep> = throw NotImplementedError()
    }
}
