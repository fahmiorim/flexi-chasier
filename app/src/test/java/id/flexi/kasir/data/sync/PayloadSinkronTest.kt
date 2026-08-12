package id.flexi.kasir.data.sync

import id.flexi.kasir.data.network.config.CashierNetworkProvider
import id.flexi.kasir.domain.model.Bahan
import id.flexi.kasir.domain.model.CartItem
import id.flexi.kasir.domain.model.CashKas
import id.flexi.kasir.domain.model.CashKasStatus
import id.flexi.kasir.domain.model.CashExpenseCategory
import id.flexi.kasir.domain.model.CashMutation
import id.flexi.kasir.domain.model.CashMutationType
import id.flexi.kasir.domain.model.Meja
import id.flexi.kasir.domain.model.PaymentMethod
import id.flexi.kasir.domain.model.Produk
import id.flexi.kasir.domain.model.Resep
import id.flexi.kasir.domain.model.BahanResep
import id.flexi.kasir.domain.model.Setoran
import id.flexi.kasir.domain.model.StoreSetting
import id.flexi.kasir.domain.model.Transaction
import id.flexi.kasir.domain.model.TransactionStatus
import id.flexi.kasir.domain.model.Uang
import id.flexi.kasir.domain.model.Varian
import kotlinx.serialization.encodeToString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Memastikan payload push PERSIS kontrak backend:
 * - nama field (snake/camel sesuai API)
 * - versi wajib & monotonik
 * - perhitungan total/kembalian/jumlahItem
 * - ID item deterministik agar retry idempotent
 */
class PayloadSinkronTest {

    private val produkEsTeh = Produk(
        id = "p-1",
        nama = "Es Teh",
        harga = 10_000L,
        stokTersedia = 8,
        deskripsi = "Teh manis dingin",
        kategori = "Minuman",
        aktif = true,
    )

    private fun transaksiContoh(): Transaction = Transaction(
        id = "trx-abc123",
        daftarCartItem = listOf(
            CartItem(produk = produkEsTeh, jumlah = 2, varian = Varian(nama = "Ice", harga = 10_000L)),
        ),
        uangDibayar = Uang.dariRupiah(50_000L),
        waktuTransactionEpochMili = 1_700_000_000_000L,
        paymentMethod = PaymentMethod.Cash,
        status = TransactionStatus.Paid,
        nomorAntrian = 7,
        mejaId = "meja-3",
        waktuDiprosesEpochMili = 1_700_000_100_000L,
        waktuSelesaiEpochMili = 1_700_000_200_000L,
        waktuDibayarEpochMili = 1_700_000_300_000L,
    )

    @Test
    fun `produk - varian, HPP, dan toggle kelola stok ikut dikirim`() {
        val produkDenganVarian = produkEsTeh.copy(
            varian = listOf(
                Varian(nama = "Ice", harga = 10_000L),
                Varian(nama = "Hot", harga = 8_000L),
            ),
            hargaModal = 6_000L,
            apakahStokDiaktifkan = true,
        )

        val payload = PayloadSinkron.produk(produkDenganVarian, versi = 1_000L)

        // varianJson berisi JSON varian (bukan sentinel "").
        assertTrue(payload.varianJson?.contains("\"Ice\"") == true)
        assertTrue(payload.varianJson?.contains("\"Hot\"") == true)
        assertEquals(6_000L, payload.hargaModal)
        assertEquals(true, payload.apakahStokDiaktifkan)

        // Tanpa varian → sentinel "" (server tahu "tidak ada varian").
        val payloadPolos = PayloadSinkron.produk(produkEsTeh, versi = 1_000L)
        assertEquals("", payloadPolos.varianJson)
        assertEquals(null, payloadPolos.hargaModal)
        assertEquals(false, payloadPolos.apakahStokDiaktifkan)
    }

    @Test
    fun `versi monotonik - naik dari waktu, dan lebih besar dari versi tersimpan`() {
        assertEquals(1_000L, PayloadSinkron.hitungVersiBaru(null, 1_000L))
        // Waktu mundur tidak boleh menurunkan versi.
        assertEquals(1_001L, PayloadSinkron.hitungVersiBaru(1_000L, 500L))
        // Waktu maju tetap menaikkan.
        assertEquals(2_000L, PayloadSinkron.hitungVersiBaru(1_000L, 2_000L))
    }

    @Test
    fun `transaksi - perhitungan total, dibayar, kembalian dan jumlah item`() {
        val payload = PayloadSinkron.transaksi(transaksiContoh(), versi = 1_000L)

        assertEquals("trx-abc123", payload.id)
        assertEquals(1_000L, payload.versi)
        assertEquals(2, payload.jumlahItem)
        assertEquals(20_000L, payload.total) // 2 x 10.000
        assertEquals(50_000L, payload.dibayar)
        assertEquals(30_000L, payload.kembalian)
        assertEquals("Cash", payload.metodePembayaran)
        assertEquals(1_700_000_000_000L, payload.waktuEpochMili)
        assertEquals("ANTRI-7", payload.nomor)
        assertEquals("meja-3", payload.mejaId)
        // Waktu tahapan ikut dikirim agar tersinkron lintas perangkat.
        assertEquals(1_700_000_100_000L, payload.waktuDiprosesEpochMili)
        assertEquals(1_700_000_200_000L, payload.waktuSelesaiEpochMili)
        assertEquals(1_700_000_300_000L, payload.waktuDibayarEpochMili)
        assertEquals(false, payload.dibatalkan)
    }

    @Test
    fun `transaksi - id item deterministik dan subtotal benar`() {
        val payloadA = PayloadSinkron.transaksi(transaksiContoh(), versi = 1_000L)
        val payloadB = PayloadSinkron.transaksi(transaksiContoh(), versi = 2_000L)

        val itemA = payloadA.items.single()
        val itemB = payloadB.items.single()

        // ID sama walaupun versi berbeda → push ulang idempotent di server.
        assertEquals(itemA.id, itemB.id)
        assertTrue(itemA.id.startsWith("trx-abc123-"))
        assertEquals("p-1", itemA.productId)
        assertEquals("Es Teh", itemA.namaProduk)
        assertEquals(10_000L, itemA.hargaSatuan)
        assertEquals(2, itemA.jumlah)
        assertEquals(20_000L, itemA.subtotal)
    }

    @Test
    fun `transaksi - bentuk JSON memakai nama field kontrak backend`() {
        val json = CashierNetworkProvider.jsonUtama.encodeToString(
            PayloadSinkron.transaksi(transaksiContoh(), versi = 1_000L),
        )

        assertTrue(json.contains("\"waktuEpochMili\""))
        assertTrue(json.contains("\"metodePembayaran\""))
        assertTrue(json.contains("\"jumlahItem\""))
        assertTrue(json.contains("\"productId\""))
        assertTrue(json.contains("\"namaProduk\""))
        assertTrue(json.contains("\"hargaSatuan\""))
        assertTrue(json.contains("\"items\""))
        // Meja & waktu tahapan disinkronkan lintas perangkat → nama field kontrak backend.
        assertTrue(json.contains("\"mejaId\""))
        assertTrue(json.contains("\"waktuDiprosesEpochMili\""))
        assertTrue(json.contains("\"waktuSelesaiEpochMili\""))
        assertTrue(json.contains("\"waktuDibayarEpochMili\""))
        // dibuatOleh null tidak ikut dikirim (explicitNulls = false).
        assertTrue(!json.contains("dibuatOleh"))
    }

    @Test
    fun `meja - kontrak nomor aktif dan dihapus`() {
        val payload = PayloadSinkron.meja(
            meja = Meja(id = "m-1", nomor = "Meja 3", aktif = true),
            versi = 5L,
            dihapus = true,
        )

        assertEquals("m-1", payload.id)
        assertEquals(5L, payload.versi)
        assertEquals("Meja 3", payload.nomor)
        assertTrue(payload.dihapus)
    }

    @Test
    fun `shift kas - buka tanpa waktu tutup dan catatan opsional null`() {
        val payload = PayloadSinkron.shiftKas(
            kas = CashKas(
                id = "s-1",
                saldoAwal = Uang.dariRupiah(100_000L),
                saldoAkhir = null,
                waktuBuka = 1_600_000_000_000L,
                waktuTutup = null,
                status = CashKasStatus.Buka,
                catatanBuka = null,
                catatanTutup = null,
            ),
            versi = 2L,
        )

        assertEquals(100_000L, payload.saldoAwal)
        assertNull(payload.saldoAkhir)
        assertNull(payload.waktuTutupEpochMili)
        assertNull(payload.catatanBuka)
    }

    @Test
    fun `mutasi kas - tipe dan nominal mengikuti domain`() {
        val payload = PayloadSinkron.mutasiKas(
            mutasi = CashMutation(
                id = "mu-1",
                shiftId = "s-1",
                tipe = CashMutationType.Pengeluaran,
                kategori = CashExpenseCategory.BelanjaBahan,
                nominal = Uang.dariRupiah(25_000L),
                catatan = "Beli gula",
                waktu = 1_600_000_000_000L,
            ),
            versi = 1L,
        )

        assertEquals("Pengeluaran", payload.tipe)
        assertEquals("BelanjaBahan", payload.kategori)
        assertEquals(25_000L, payload.nominal)
        assertEquals(1_600_000_000_000L, payload.waktuEpochMili)
    }

    @Test
    fun `setoran - membawa shiftId induk dan flag dihapus dari domain`() {
        val payload = PayloadSinkron.setoran(
            setoran = Setoran(
                id = "st-1",
                nominal = Uang.dariRupiah(100_000L),
                catatan = "",
                waktu = 1_600_000_000_000L,
                dihapus = true,
            ),
            shiftId = "s-1",
            versi = 3L,
        )

        assertEquals("s-1", payload.shiftId)
        assertEquals(100_000L, payload.nominal)
        assertTrue(payload.dihapus)
    }

    @Test
    fun `pengaturan toko - kontrak id versi dan field opsional blank menjadi null`() {
        val payload = PayloadSinkron.pengaturanToko(
            pengaturan = StoreSetting(
                namaUsaha = "Kopi Nusantara",
                alamat = "Jl. Merdeka No. 1",
                tagline = "",
                logoUri = "",
            ),
            id = "pengaturan-toko-gerai-1",
            versi = 9L,
        )

        assertEquals("pengaturan-toko-gerai-1", payload.id)
        assertEquals(9L, payload.versi)
        assertEquals("Kopi Nusantara", payload.namaUsaha)
        assertEquals("Jl. Merdeka No. 1", payload.alamat)
        // Field opsional kosong → null (kontrak nullable server).
        assertNull(payload.tagline)
        assertNull(payload.logoUri)
    }

    @Test
    fun `pengaturan toko - bentuk JSON memakai nama field kontrak backend`() {
        val json = CashierNetworkProvider.jsonUtama.encodeToString(
            PayloadSinkron.pengaturanToko(
                pengaturan = StoreSetting(namaUsaha = "Kopi Nusantara", logoUri = "file:///logo.png"),
                id = "pengaturan-toko-gerai-1",
                versi = 1L,
            ),
        )

        assertTrue(json.contains("\"namaUsaha\""))
        assertTrue(json.contains("\"logoUri\""))
        assertTrue(json.contains("\"pengaturan-toko-gerai-1\""))
    }

    @Test
    fun `bahan - stok pecahan dikirim apa adanya (Double) agar stok gram tidak terpotong`() {
        val payload = PayloadSinkron.bahan(
            bahan = Bahan(
                id = "b-1",
                nama = "Gula",
                satuan = "gram",
                stokTersedia = 2.6,
                hargaPerSatuan = 12_000L,
            ),
            versi = 1L,
        )

        // Kontrak server menerima Float (z.number tanpa .int) & Prisma Float —
        // membulatkan ke Int saat push akan menghilangkan 0.6 gram dari stok.
        assertEquals(2.6, payload.stok, 0.001)
        assertEquals(12_000L, payload.hargaBeli)
    }

    @Test
    fun `resep - nama produk dan bahan diambil dari pemetaan`() {
        val payload = PayloadSinkron.resep(
            resep = Resep(
                id = "r-1",
                produkId = "p-1",
                daftarBahan = listOf(
                    BahanResep(id = "", resepId = "r-1", bahanId = "b-1", jumlah = 10.0, satuan = "gram"),
                ),
                createdAt = 1_600_000_000_000L,
            ),
            namaProduk = "Es Teh",
            namaBahan = mapOf("b-1" to "Gula"),
            versi = 1L,
        )

        assertEquals("p-1", payload.productId)
        assertEquals("Es Teh", payload.namaProduk)
        val bahan = payload.bahan.single()
        assertEquals("b-1", bahan.bahanId)
        assertEquals("Gula", bahan.namaBahan)
        assertEquals(10.0, bahan.jumlah, 0.001)
        // id deterministik ketika id lokal kosong
        assertTrue(bahan.id.startsWith("r-1-"))
    }
}
