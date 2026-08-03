package id.flexi.kasir.ui.main

import androidx.compose.runtime.Immutable
import id.flexi.kasir.domain.model.CartItem
import id.flexi.kasir.domain.model.Meja
import id.flexi.kasir.domain.model.PaymentMethod
import id.flexi.kasir.domain.model.OrderType
import id.flexi.kasir.domain.model.Produk
import id.flexi.kasir.domain.model.SyncStatus
import id.flexi.kasir.domain.model.CatalogDisplay
import id.flexi.kasir.domain.model.Transaction
import id.flexi.kasir.domain.model.Varian

/**
 * Status ringkasan informasi aplikasi dan metrik cepat di beranda.
 *
 * @property namaAplikasi Nama brand aplikasi.
 * @property sloganAplikasi Pesan pemasaran aplikasi.
 * @property jumlahProdukTersedia Total produk dalam katalog.
 * @property jumlahCartItem Total kuantitas item di keranjang.
 * @property totalBelanjaSementara Estimasi nilai belanja saat ini.
 * @property syncStatus Objek status sinkronisasi data.
 * @property labelAksiSinkronisasi Teks tombol sinkronisasi.
 * @property aksiSinkronisasiAktif Status aktif tombol sinkronisasi.
 * @property labelMetadataSinkronisasi Teks metadata sinkronisasi (misal: waktu terakhir).
 */
@Immutable
data class HomeStatus(
    val namaAplikasi: String,
    val sloganAplikasi: String,
    val jumlahProdukTersedia: Int,
    val jumlahCartItem: Int,
    val totalBelanjaSementara: String,
    val syncStatus: SyncStatus,
    val labelAksiSinkronisasi: String,
    val aksiSinkronisasiAktif: Boolean,
    val labelMetadataSinkronisasi: String,
)

/**
 * Representasi status visual panel keranjang belanja.
 *
 * @property judul Teks judul panel keranjang.
 * @property deskripsi Pesan informatif di panel keranjang.
 * @property jumlahItem Label jumlah item saat ini.
 */
@Immutable
data class CartStatus(
    val judul: String,
    val deskripsi: String,
    val jumlahItem: String,
)

/**
 * Representasi status rincian biaya dan tombol aksi Payment.
 *
 * @property subtotal Nilai kotor sebelum potongan/pajak.
 * @property potongan Nilai diskon.
 * @property pajak Nilai pajak.
 * @property totalAkhir Nilai bersih yang harus dibayar.
 * @property labelAksiUtama Teks pada tombol bayar/pilih.
 * @property aksiUtamaAktif Status apakah tombol dapat diklik.
 */
@Immutable
data class PaymentSummary(
    val subtotal: String,
    val potongan: String,
    val pajak: String,
    val totalAkhir: String,
    val labelAksiUtama: String,
    val aksiUtamaAktif: Boolean,
)

/**
 * Status dialog konfirmasi checkout.
 *
 * @property apakahTampil Status visibilitas dialog.
 * @property judul Teks judul dialog.
 * @property deskripsi Pesan rincian dalam dialog.
 * @property labelKonfirmasi Teks tombol eksekusi bayar.
 * @property labelSimpanPesanan Teks tombol simpan pesanan (pending).
 * @property catatan Catatan/keterangan yang diisi pengguna (nomor meja, dll).
 */
@Immutable
data class CheckoutConfirmStatus(
    val apakahTampil: Boolean = false,
    val judul: String = "Konfirmasi Payment",
    val deskripsi: String = "",
    val labelKonfirmasi: String = "Bayar sekarang",
    val labelSimpanPesanan: String = "Simpan Pesanan",
    val catatan: String = "",
    val paymentMethod: PaymentMethod = PaymentMethod.Cash,
    val PaymentMethodTunaiTersedia: Boolean = true,
    val PaymentMethodQrisTersedia: Boolean = true,
    val daftarMeja: List<Meja> = emptyList(),
    val orderType: OrderType = OrderType.DineIn,
    val mejaId: String? = null,
    val modeSimpan: Boolean = false,
    val apakahSedangMenggabungkan: Boolean = false,
    val totalAkhirNilai: Long = 0L,
    val billLainDiMeja: List<Transaction> = emptyList(),
)

/**
 * Status hasil checkout yang ditampilkan sebagai banner.
 *
 * @property apakahTampil Status visibilitas banner.
 * @property judul Teks judul banner.
 * @property deskripsi Pesan sukses atau gagal.
 */
@Immutable
data class CheckoutResultStatus(
    val apakahTampil: Boolean = false,
    val judul: String = "",
    val deskripsi: String = "",
    val nomorAntrian: Int? = null,
    val paymentMethod: PaymentMethod = PaymentMethod.Cash,
)

/**
 * Representasi seluruh status UI untuk Layar Utama Kasir.
 *
 * Model ini menjadi satu sumber kebenaran (single source of truth) untuk antarmuka,
 * mengelola data produk, status keranjang, checkout, serta kontrol kata kunci pencarian.
 *
 * @property statusBeranda Informasi dan metrik di bagian beranda.
 * @property daftarProdukTersaring Hasil filter katalog berdasarkan kata kunci pencarian efektif.
 * @property daftarCartItem Koleksi item yang akan dibeli.
 * @property statusKeranjang Status panel keranjang belanja.
 * @property ringkasanPayment Status rincian biaya.
 * @property statusKonfirmasiCheckout Dialog persetujuan checkout.
 * @property statusHasilCheckout Pesan hasil Transaction (banner).
 * @property kataKunciPencarian Kata kunci mentah untuk sinkronisasi nilai TextField.
 * @property tampilkanAksiResetPencarian Flag untuk menampilkan tombol x/reset di kolom pencarian.
 * @property apakahRingkasanPaymentTampil Flag visibilitas ringkasan bayar (khusus ponsel).
 */
@Immutable
data class CashierMainUiState(
    val catalogDisplay: CatalogDisplay = CatalogDisplay.Grid,
    val statusBeranda: HomeStatus = HomeStatus(
        namaAplikasi = "Flexi Cashier",
        sloganAplikasi = "Solusi Digital UMKM Modern",
        jumlahProdukTersedia = 0,
        jumlahCartItem = 0,
        totalBelanjaSementara = "Rp0",
        syncStatus = SyncStatus.LocalChanges,
        labelAksiSinkronisasi = "Perbarui katalog",
        aksiSinkronisasiAktif = true,
        labelMetadataSinkronisasi = "Katalog lokal siap digunakan.",
    ),
    val daftarProdukTersaring: List<Produk> = emptyList(),
    val daftarCartItem: List<CartItem> = emptyList(),
    val statusKeranjang: CartStatus = CartStatus(
        judul = "Keranjang masih kosong",
        deskripsi = "Yuk, mulai Transaction dengan memilih produk favorit!",
        jumlahItem = "0 item",
    ),
    val ringkasanPayment: PaymentSummary = PaymentSummary(
        subtotal = "Rp0",
        potongan = "Rp0",
        pajak = "Rp0",
        totalAkhir = "Rp0",
        labelAksiUtama = "Pilih produk",
        aksiUtamaAktif = false,
    ),
    val statusKonfirmasiCheckout: CheckoutConfirmStatus = CheckoutConfirmStatus(),
    val statusHasilCheckout: CheckoutResultStatus = CheckoutResultStatus(),
    val kataKunciPencarian: String = "",
    val tampilkanAksiResetPencarian: Boolean = false,
    val apakahRingkasanPaymentTampil: Boolean = true,
    val daftarMeja: List<Meja> = emptyList(),
    val daftarPesananPending: List<Transaction> = emptyList(),
    val apakahPendingOrdersPanelTampil: Boolean = false,
    val daftarPesananDiproses: List<Transaction> = emptyList(),
    val apakahAntrianPanelTampil: Boolean = false,
    val tabTransaksi: Int = 1,
    val daftarKategori: List<String> = emptyList(),
    val kategoriTerpilih: String = "",
    val produkUntukPilihVarian: Produk? = null,
    val apakahPerluBukaKas: Boolean = false,
)
