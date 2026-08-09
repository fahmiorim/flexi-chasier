package id.flexi.kasir.data.sync

import id.flexi.kasir.data.network.model.BahanResepSinkron
import id.flexi.kasir.data.network.model.BahanSinkron
import id.flexi.kasir.data.network.model.ItemTransaksiSinkron
import id.flexi.kasir.data.network.model.MejaSinkron
import id.flexi.kasir.data.network.model.MutasiKasSinkron
import id.flexi.kasir.data.network.model.MutasiRekeningSinkron
import id.flexi.kasir.data.network.model.PembelianBahanSinkron
import id.flexi.kasir.data.network.model.PengaturanTokoSinkron
import id.flexi.kasir.data.network.model.PenyesuaianStokSinkron
import id.flexi.kasir.data.network.model.ProdukSinkron
import id.flexi.kasir.data.network.model.ResepSinkron
import id.flexi.kasir.data.network.model.SetoranSinkron
import id.flexi.kasir.data.network.model.ShiftKasSinkron
import id.flexi.kasir.data.network.model.TransaksiSinkron
import id.flexi.kasir.domain.model.Bahan
import id.flexi.kasir.domain.model.CashKas
import id.flexi.kasir.domain.model.CashMutation
import id.flexi.kasir.domain.model.Meja
import id.flexi.kasir.domain.model.MutasiRekening
import id.flexi.kasir.domain.model.PembelianBahan
import id.flexi.kasir.domain.model.PenyesuaianStok
import id.flexi.kasir.domain.model.Produk
import id.flexi.kasir.domain.model.Resep
import id.flexi.kasir.domain.model.Setoran
import id.flexi.kasir.domain.model.StoreSetting
import id.flexi.kasir.domain.model.Transaction
import kotlin.math.max

/**
 * Pembangun payload push sinkronisasi (murni, tanpa akses database).
 *
 * Setiap fungsi mengubah model domain lokal menjadi DTO jaringan yang bentuknya
 * PERSIS kontrak backend `POST /api/sync/<entitas>`. Payload ini disimpan ke
 * outbox pada saat perubahan terjadi, sehingga pengiriman ulang (retry) selalu
 * membawa data yang sama (idempotent) dengan `versi` yang sama.
 */
object PayloadSinkron {

    /**
     * Versi monotonik untuk outbox: naik seiring waktu, dan PASTI lebih besar
     * dari versi yang pernah disimpan sebelumnya untuk item yang sama.
     * (Server memakai aturan last-write-wins: versi lebih tua ditolak.)
     */
    fun hitungVersiBaru(
        versiTersimpan: Long?,
        waktuSekarang: Long,
    ): Long = max(waktuSekarang, (versiTersimpan ?: 0L) + 1L)

    fun produk(
        produk: Produk,
        versi: Long,
        dihapus: Boolean = false,
    ): ProdukSinkron = ProdukSinkron(
        id = produk.id,
        versi = versi,
        nama = produk.nama,
        harga = produk.harga,
        stok = produk.stokTersedia,
        kodePindai = produk.kodePindai?.ifBlank { null },
        kategori = produk.kategori.ifBlank { null },
        deskripsi = produk.deskripsi.ifBlank { null },
        fotoUri = produk.fotoUri,
        favorit = produk.favorit,
        aktif = produk.aktif,
        dihapus = dihapus,
    )

    fun transaksi(
        transaction: Transaction,
        versi: Long,
        dihapus: Boolean = false,
    ): TransaksiSinkron {
        val daftarItem = transaction.daftarCartItem.map { cartItem ->
            // Harga efektif: varian lebih diutamakan, lalu harga produk dasar.
            val hargaSatuan = cartItem.varian?.harga ?: cartItem.produk.harga
            ItemTransaksiSinkron(
                // ID deterministik dari isi baris agar retry & edit ringan tetap idempotent.
                id = "${transaction.id}-${(cartItem.produk.id + "|" + (cartItem.varian?.nama ?: "")).hashCode() and Int.MAX_VALUE}",
                versi = versi,
                transactionId = transaction.id,
                productId = cartItem.produk.id,
                namaProduk = cartItem.produk.nama,
                hargaSatuan = hargaSatuan,
                jumlah = cartItem.jumlah,
                subtotal = hargaSatuan * cartItem.jumlah,
            )
        }
        val total = daftarItem.sumOf { it.subtotal } -
            transaction.potongan.nilaiRupiah +
            transaction.biayaLayanan.nilaiRupiah +
            transaction.pajak.nilaiRupiah
        val dibayar = transaction.uangDibayar.nilaiRupiah

        return TransaksiSinkron(
            id = transaction.id,
            versi = versi,
            nomor = transaction.nomorAntrian?.let { "ANTRI-$it" }
                ?: "TRX-${transaction.id.take(8)}",
            waktuEpochMili = transaction.waktuTransactionEpochMili,
            metodePembayaran = transaction.paymentMethod.name, // "Cash" | "Qris"
            jumlahItem = daftarItem.sumOf { it.jumlah },
            total = total,
            dibayar = dibayar,
            kembalian = max(0L, dibayar - total),
            potongan = transaction.potongan.nilaiRupiah,
            biayaLayanan = transaction.biayaLayanan.nilaiRupiah,
            pajak = transaction.pajak.nilaiRupiah,
            status = transaction.status.name,
            orderType = transaction.orderType.name,
            catatan = transaction.catatan,
            dibatalkan = transaction.dibatalkan,
            dihapus = dihapus,
            items = daftarItem,
        )
    }

    fun meja(
        meja: Meja,
        versi: Long,
        dihapus: Boolean = false,
    ): MejaSinkron = MejaSinkron(
        id = meja.id,
        versi = versi,
        nomor = meja.nomor,
        aktif = meja.aktif,
        dihapus = dihapus,
    )

    fun shiftKas(
        kas: CashKas,
        versi: Long,
        dihapus: Boolean = false,
    ): ShiftKasSinkron = ShiftKasSinkron(
        id = kas.id,
        versi = versi,
        waktuBukaEpochMili = kas.waktuBuka,
        waktuTutupEpochMili = kas.waktuTutup,
        saldoAwal = kas.saldoAwal.nilaiRupiah,
        saldoAkhir = kas.saldoAkhir?.nilaiRupiah,
        catatanBuka = kas.catatanBuka,
        catatanTutup = kas.catatanTutup,
        dihapus = dihapus,
    )

    fun mutasiKas(
        mutasi: CashMutation,
        versi: Long,
        dihapus: Boolean = false,
    ): MutasiKasSinkron = MutasiKasSinkron(
        id = mutasi.id,
        versi = versi,
        shiftId = mutasi.shiftId,
        tipe = mutasi.tipe.name, // "Pemasukan" | "Pengeluaran"
        kategori = mutasi.kategori.name,
        nominal = mutasi.nominal.nilaiRupiah,
        catatan = mutasi.catatan.ifBlank { null },
        waktuEpochMili = mutasi.waktu,
        dihapus = dihapus,
    )

    fun setoran(
        setoran: Setoran,
        shiftId: String,
        versi: Long,
    ): SetoranSinkron = SetoranSinkron(
        id = setoran.id,
        versi = versi,
        shiftId = shiftId,
        nominal = setoran.nominal.nilaiRupiah,
        catatan = setoran.catatan.ifBlank { null },
        waktuEpochMili = setoran.waktu,
        dihapus = setoran.dihapus,
    )

    fun bahan(
        bahan: Bahan,
        versi: Long,
        dihapus: Boolean = false,
    ): BahanSinkron = BahanSinkron(
        id = bahan.id,
        versi = versi,
        nama = bahan.nama,
        satuan = bahan.satuan,
        stok = bahan.stokTersedia,
        hargaBeli = bahan.hargaPerSatuan,
        stokMinimum = bahan.stokMinimum.toLong(),
        aktif = bahan.aktif,
        dihapus = dihapus,
    )

    fun pembelianBahan(
        pembelian: PembelianBahan,
        namaBahan: String,
        versi: Long,
        dihapus: Boolean = false,
    ): PembelianBahanSinkron = PembelianBahanSinkron(
        id = pembelian.id,
        versi = versi,
        bahanId = pembelian.bahanId,
        namaBahan = namaBahan,
        jumlah = pembelian.jumlah,
        hargaTotal = pembelian.totalHarga,
        waktuEpochMili = pembelian.tanggalBeli,
        dihapus = dihapus,
    )

    /**
     * Pengaturan toko: hanya field yang dibagikan lintas perangkat yang dikirim
     * (nama usaha/alamat/tagline/logo); pengaturan khusus perangkat (printer,
     * struk, tampilan) TIDAK ikut — server-authoritative hanya untuk field ini.
     *
     * @param id ID deterministik satu baris per gerai (lihat OutboxPencatat).
     */
    fun pengaturanToko(
        pengaturan: StoreSetting,
        id: String,
        versi: Long,
    ): PengaturanTokoSinkron = PengaturanTokoSinkron(
        id = id,
        versi = versi,
        namaUsaha = pengaturan.namaUsaha,
        alamat = pengaturan.alamat.ifBlank { null },
        tagline = pengaturan.tagline.ifBlank { null },
        logoUri = pengaturan.logoUri.ifBlank { null },
    )

    fun penyesuaianStok(
        penyesuaian: PenyesuaianStok,
        versi: Long,
        dihapus: Boolean = false,
    ): PenyesuaianStokSinkron = PenyesuaianStokSinkron(
        id = penyesuaian.id,
        versi = versi,
        jenis = penyesuaian.jenis.name, // "Bahan" | "Produk"
        entitasId = penyesuaian.entitasId,
        namaEntitas = penyesuaian.namaEntitas.ifBlank { null },
        stokSebelum = penyesuaian.stokSebelum,
        stokSesudah = penyesuaian.stokSesudah,
        selisih = penyesuaian.selisih,
        alasan = penyesuaian.alasan.ifBlank { null },
        waktuEpochMili = penyesuaian.waktu,
        dihapus = dihapus,
    )

    fun mutasiRekening(
        mutasi: MutasiRekening,
        versi: Long,
        dihapus: Boolean = false,
    ): MutasiRekeningSinkron = MutasiRekeningSinkron(
        id = mutasi.id,
        versi = versi,
        tipe = mutasi.tipe.name, // "SaldoAwal" | "Pemasukan" | "Penarikan"
        nominal = mutasi.nominal.nilaiRupiah,
        catatan = mutasi.catatan.ifBlank { null },
        waktuEpochMili = mutasi.waktu,
        dihapus = dihapus,
    )

    /**
     * @param namaBahan Pemetaan id bahan → nama (dilakukan pemanggil agar fungsi tetap murni).
     */
    fun resep(
        resep: Resep,
        namaProduk: String,
        namaBahan: Map<String, String>,
        versi: Long,
        dihapus: Boolean = false,
    ): ResepSinkron = ResepSinkron(
        id = resep.id,
        versi = versi,
        productId = resep.produkId,
        namaProduk = namaProduk,
        dihapus = dihapus,
        bahan = resep.daftarBahan.map { bahanResep ->
            BahanResepSinkron(
                id = bahanResep.id.ifBlank {
                    "${resep.id}-${bahanResep.bahanId.hashCode() and Int.MAX_VALUE}"
                },
                versi = versi,
                resepId = resep.id,
                bahanId = bahanResep.bahanId,
                namaBahan = namaBahan[bahanResep.bahanId] ?: "Bahan",
                jumlah = bahanResep.jumlah,
            )
        },
    )
}
