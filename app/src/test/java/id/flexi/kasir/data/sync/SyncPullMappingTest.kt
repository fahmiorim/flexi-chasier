package id.flexi.kasir.data.sync

import id.flexi.kasir.data.network.model.BahanResepSinkron
import id.flexi.kasir.data.network.model.BahanSinkron
import id.flexi.kasir.data.network.model.ItemTransaksiSinkron
import id.flexi.kasir.data.network.model.MejaSinkron
import id.flexi.kasir.data.network.model.MutasiKasSinkron
import id.flexi.kasir.data.network.model.PembelianBahanSinkron
import id.flexi.kasir.data.network.model.PengaturanTokoSinkron
import id.flexi.kasir.data.network.model.PerubahanResponse
import id.flexi.kasir.data.network.model.ProdukSinkron
import id.flexi.kasir.data.network.model.ResepSinkron
import id.flexi.kasir.data.network.model.SetoranSinkron
import id.flexi.kasir.data.network.model.ShiftKasSinkron
import id.flexi.kasir.data.network.model.TransaksiSinkron
import id.flexi.kasir.domain.model.CatalogDisplay
import id.flexi.kasir.domain.model.StoreSetting
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Memastikan pemetaan pull `/api/sync/perubahan` → entitas lokal:
 * - item `dihapus` tidak disimpan, id-nya masuk daftar penghapusan
 * - geraiId terisi di produk, status/kolom transaksi benar
 * - item transaksi & bahan resep mengikuti induknya
 */
class SyncPullMappingTest {

    private val geraiId = "gerai-1"

    private fun responsContoh(): PerubahanResponse = PerubahanResponse(
        waktuServerEpochMili = 1_700_000_000_000L,
        terpotong = false,
        products = listOf(
            ProdukSinkron(id = "p-1", versi = 5L, nama = "Es Teh", harga = 10_000L, stok = 4, aktif = true, kategori = "Minuman"),
            ProdukSinkron(id = "p-2", versi = 6L, nama = "Kopi", harga = 12_000L, stok = 0, dihapus = true),
        ),
        transactions = listOf(
            TransaksiSinkron(
                id = "t-1", versi = 3L, nomor = "ANTRI-1", waktuEpochMili = 1_600_000_000_000L,
                metodePembayaran = "Qris", jumlahItem = 2, total = 20_000L, dibayar = 20_000L,
                kembalian = 0L, dibatalkan = false,
            ),
            TransaksiSinkron(
                id = "t-2", versi = 4L, nomor = "ANTRI-2", waktuEpochMili = 1_610_000_000_000L,
                metodePembayaran = "Cash", jumlahItem = 1, total = 10_000L, dibayar = 10_000L,
                kembalian = 0L, dibatalkan = false, dihapus = true,
            ),
        ),
        transactionItems = listOf(
            ItemTransaksiSinkron(
                id = "t-1-1", versi = 3L, transactionId = "t-1", productId = "p-1",
                namaProduk = "Es Teh", hargaSatuan = 10_000L, jumlah = 2, subtotal = 20_000L,
            ),
        ),
        tables = listOf(MejaSinkron(id = "m-1", versi = 1L, nomor = "1")),
        cashShifts = listOf(
            ShiftKasSinkron(id = "s-1", versi = 2L, waktuBukaEpochMili = 1_600_000_000_000L, saldoAwal = 100_000L),
        ),
        cashMutations = listOf(
            MutasiKasSinkron(
                id = "mu-1", versi = 1L, shiftId = "s-1", tipe = "Pengeluaran",
                kategori = "BelanjaBahan", nominal = 25_000L, waktuEpochMili = 1_600_000_000_000L,
            ),
        ),
        setoran = listOf(
            SetoranSinkron(id = "st-1", versi = 1L, shiftId = "s-1", nominal = 100_000L, waktuEpochMili = 1_600_000_000_000L),
        ),
        bahan = listOf(BahanSinkron(id = "b-1", versi = 1L, nama = "Gula", satuan = "gram", stok = 500, hargaBeli = 12_000L)),
        pembelianBahan = listOf(
            PembelianBahanSinkron(id = "pb-1", versi = 1L, bahanId = "b-1", namaBahan = "Gula", jumlah = 5, hargaTotal = 60_000L, waktuEpochMili = 1_600_000_000_000L),
        ),
        resep = listOf(ResepSinkron(id = "r-1", versi = 1L, productId = "p-1", namaProduk = "Es Teh")),
        resepBahan = listOf(
            BahanResepSinkron(id = "r-1-b-1", versi = 1L, resepId = "r-1", bahanId = "b-1", namaBahan = "Gula", jumlah = 10),
        ),
        storeSettings = listOf(
            PengaturanTokoSinkron(
                id = "pengaturan-1", versi = 7L, namaUsaha = "Kopi Nusantara",
                alamat = "Jl. Merdeka 1", tagline = "Kopi lokal pilihan", logoUri = "file:///logo.png",
            ),
        ),
    )

    @Test
    fun `produk - geraiId terisi dan item dihapus masuk daftar penghapusan`() {
        val hasil = responsContoh().kePerubahanLokal(geraiId)

        assertEquals(1, hasil.produk.size)
        assertEquals("p-1", hasil.produk.single().id)
        assertEquals(geraiId, hasil.produk.single().geraiId)
        assertEquals(4, hasil.produk.single().stokTersedia)
        assertEquals("Minuman", hasil.produk.single().kategori)
        assertEquals(listOf("p-2"), hasil.produkDihapus)
    }

    @Test
    fun `transaksi - hanya yang aktif disimpan, status dan metode pembayaran benar`() {
        val hasil = responsContoh().kePerubahanLokal(geraiId)

        assertEquals(1, hasil.transaksi.size)
        val transaksi = hasil.transaksi.single()
        assertEquals("t-1", transaksi.id)
        assertEquals("Qris", transaksi.PaymentMethod)
        assertEquals("Paid", transaksi.status)
        assertEquals(1_600_000_000_000L, transaksi.waktuTransactionEpochMili)
        assertEquals(20_000L, transaksi.uangDibayar)
        assertFalse(transaksi.dibatalkan)
        assertEquals(listOf("t-2"), hasil.transaksiDihapus)
    }

    @Test
    fun `item transaksi - mengikuti induk dan snapshot harga produk`() {
        val hasil = responsContoh().kePerubahanLokal(geraiId)

        assertEquals(1, hasil.itemTransaksi.size)
        val item = hasil.itemTransaksi.single()
        assertEquals("t-1", item.TransactionId)
        assertEquals("p-1", item.produkId)
        assertEquals("Es Teh", item.namaProduk)
        assertEquals(10_000L, item.hargaProduk)
        assertEquals(2, item.jumlah)
    }

    @Test
    fun `kas - shift aktif berstatus Buka`() {
        val hasil = responsContoh().kePerubahanLokal(geraiId)

        val shift = hasil.shift.single()
        assertEquals("s-1", shift.id)
        assertEquals("Buka", shift.status)
        assertEquals(100_000L, shift.saldoAwal)
        assertTrue(shift.waktuTutup == null)
    }

    @Test
    fun `pengaturan toko - diteruskan ke hasil pemetaan`() {
        val hasil = responsContoh().kePerubahanLokal(geraiId)

        val pengaturan = hasil.pengaturanToko.single()
        assertEquals("Kopi Nusantara", pengaturan.namaUsaha)
        assertEquals("Jl. Merdeka 1", pengaturan.alamat)
        assertEquals("Kopi lokal pilihan", pengaturan.tagline)
        assertEquals("file:///logo.png", pengaturan.logoUri)
    }

    @Test
    fun `pengaturan toko - field server menimpa, field device dipertahankan`() {
        val saatIni = StoreSetting(
            namaUsaha = "Warung Lama",
            alamat = "Alamat Lama",
            tagline = "Tagline Lama",
            logoUri = "file:///lama.png",
            catalogDisplay = CatalogDisplay.List,
            printerName = "Printer Kasir",
            strukFooter = "Footer lokal",
        )

        val hasil = PengaturanTokoSinkron(
            id = "pengaturan-1", versi = 7L, namaUsaha = "Kopi Nusantara",
            alamat = "Jl. Merdeka 1", tagline = null, logoUri = null,
        ).terapkanKe(saatIni)

        assertEquals("Kopi Nusantara", hasil.namaUsaha)
        assertEquals("Jl. Merdeka 1", hasil.alamat)
        // Field null dari server → nilai lokal dipertahankan.
        assertEquals("Tagline Lama", hasil.tagline)
        assertEquals("file:///lama.png", hasil.logoUri)
        // Field khusus perangkat tetap utuh.
        assertEquals(CatalogDisplay.List, hasil.catalogDisplay)
        assertEquals("Printer Kasir", hasil.printerName)
        assertEquals("Footer lokal", hasil.strukFooter)
    }

    @Test
    fun `mutasi setoran bahan pembelian resep - dipetakan dengan nilai domain`() {
        val hasil = responsContoh().kePerubahanLokal(geraiId)

        assertEquals("Pengeluaran", hasil.mutasi.single().tipe)
        assertEquals("s-1", hasil.mutasi.single().shiftId)
        assertEquals(25_000L, hasil.mutasi.single().nominal)

        assertEquals(100_000L, hasil.setoran.single().nominal)
        assertFalse(hasil.setoran.single().dihapus)

        assertEquals("Gula", hasil.bahan.single().nama)
        assertEquals(500.0, hasil.bahan.single().stokTersedia, 0.001)

        assertEquals(5.0, hasil.pembelian.single().jumlah, 0.001)
        assertEquals("b-1", hasil.pembelian.single().bahanId)

        assertEquals("p-1", hasil.resep.single().produkId)
        val bahanResep = hasil.resepBahan.single()
        assertEquals("r-1", bahanResep.resepId)
        assertEquals(10.0, bahanResep.jumlah, 0.001)
        assertEquals("gram", bahanResep.satuan)
    }
}
