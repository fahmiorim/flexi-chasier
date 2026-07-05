package id.cassy.kasir.antarmuka.utama

import androidx.compose.runtime.Immutable
import id.cassy.kasir.ranah.model.ItemKeranjang
import id.cassy.kasir.ranah.model.Meja
import id.cassy.kasir.ranah.model.MetodeBayar
import id.cassy.kasir.ranah.model.TipeOrder
import id.cassy.kasir.ranah.model.Produk
import id.cassy.kasir.ranah.model.StatusSinkronisasi
import id.cassy.kasir.ranah.model.TampilanKatalog
import id.cassy.kasir.ranah.model.Transaksi

/**
 * Status ringkasan informasi aplikasi dan metrik cepat di beranda.
 *
 * @property namaAplikasi Nama brand aplikasi.
 * @property sloganAplikasi Pesan pemasaran aplikasi.
 * @property jumlahProdukTersedia Total produk dalam katalog.
 * @property jumlahItemKeranjang Total kuantitas item di keranjang.
 * @property totalBelanjaSementara Estimasi nilai belanja saat ini.
 * @property statusSinkronisasi Objek status sinkronisasi data.
 * @property labelAksiSinkronisasi Teks tombol sinkronisasi.
 * @property aksiSinkronisasiAktif Status aktif tombol sinkronisasi.
 * @property labelMetadataSinkronisasi Teks metadata sinkronisasi (misal: waktu terakhir).
 */
@Immutable
data class StatusBerandaKasir(
    val namaAplikasi: String,
    val sloganAplikasi: String,
    val jumlahProdukTersedia: Int,
    val jumlahItemKeranjang: Int,
    val totalBelanjaSementara: String,
    val statusSinkronisasi: StatusSinkronisasi,
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
data class StatusKeranjangKasir(
    val judul: String,
    val deskripsi: String,
    val jumlahItem: String,
)

/**
 * Representasi status rincian biaya dan tombol aksi pembayaran.
 *
 * @property subtotal Nilai kotor sebelum potongan/pajak.
 * @property potongan Nilai diskon.
 * @property pajak Nilai pajak.
 * @property totalAkhir Nilai bersih yang harus dibayar.
 * @property labelAksiUtama Teks pada tombol bayar/pilih.
 * @property aksiUtamaAktif Status apakah tombol dapat diklik.
 */
@Immutable
data class RingkasanPembayaranKasir(
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
data class StatusKonfirmasiCheckoutKasir(
    val apakahTampil: Boolean = false,
    val judul: String = "Konfirmasi pembayaran",
    val deskripsi: String = "",
    val labelKonfirmasi: String = "Bayar sekarang",
    val labelSimpanPesanan: String = "Simpan Pesanan",
    val catatan: String = "",
    val metodeBayar: MetodeBayar = MetodeBayar.Tunai,
    val metodeBayarTunaiTersedia: Boolean = true,
    val metodeBayarQrisTersedia: Boolean = true,
    val daftarMeja: List<Meja> = emptyList(),
    val tipeOrder: TipeOrder = TipeOrder.DineIn,
)

/**
 * Status hasil checkout yang ditampilkan sebagai banner.
 *
 * @property apakahTampil Status visibilitas banner.
 * @property judul Teks judul banner.
 * @property deskripsi Pesan sukses atau gagal.
 */
@Immutable
data class StatusHasilCheckoutKasir(
    val apakahTampil: Boolean = false,
    val judul: String = "",
    val deskripsi: String = "",
)

/**
 * Representasi seluruh status UI untuk Layar Utama Kasir.
 *
 * Model ini menjadi satu sumber kebenaran (single source of truth) untuk antarmuka,
 * mengelola data produk, status keranjang, checkout, serta kontrol kata kunci pencarian.
 *
 * @property statusBeranda Informasi dan metrik di bagian beranda.
 * @property daftarProdukTersaring Hasil filter katalog berdasarkan kata kunci pencarian efektif.
 * @property daftarItemKeranjang Koleksi item yang akan dibeli.
 * @property statusKeranjang Status panel keranjang belanja.
 * @property ringkasanPembayaran Status rincian biaya.
 * @property statusKonfirmasiCheckout Dialog persetujuan checkout.
 * @property statusHasilCheckout Pesan hasil transaksi (banner).
 * @property kataKunciPencarian Kata kunci mentah untuk sinkronisasi nilai TextField.
 * @property tampilkanAksiResetPencarian Flag untuk menampilkan tombol x/reset di kolom pencarian.
 * @property apakahRingkasanPembayaranTampil Flag visibilitas ringkasan bayar (khusus ponsel).
 */
@Immutable
data class ModelTampilanLayarUtamaKasir(
    val tampilanKatalog: TampilanKatalog = TampilanKatalog.Grid,
    val statusBeranda: StatusBerandaKasir = StatusBerandaKasir(
        namaAplikasi = "Cassy Kasir",
        sloganAplikasi = "Solusi Digital UMKM Modern",
        jumlahProdukTersedia = 0,
        jumlahItemKeranjang = 0,
        totalBelanjaSementara = "Rp0",
        statusSinkronisasi = StatusSinkronisasi.SinkronLokal,
        labelAksiSinkronisasi = "Perbarui katalog",
        aksiSinkronisasiAktif = true,
        labelMetadataSinkronisasi = "Katalog lokal siap digunakan.",
    ),
    val daftarProdukTersaring: List<Produk> = emptyList(),
    val daftarItemKeranjang: List<ItemKeranjang> = emptyList(),
    val statusKeranjang: StatusKeranjangKasir = StatusKeranjangKasir(
        judul = "Keranjang masih kosong",
        deskripsi = "Yuk, mulai transaksi dengan memilih produk favorit!",
        jumlahItem = "0 item",
    ),
    val ringkasanPembayaran: RingkasanPembayaranKasir = RingkasanPembayaranKasir(
        subtotal = "Rp0",
        potongan = "Rp0",
        pajak = "Rp0",
        totalAkhir = "Rp0",
        labelAksiUtama = "Pilih produk",
        aksiUtamaAktif = false,
    ),
    val statusKonfirmasiCheckout: StatusKonfirmasiCheckoutKasir = StatusKonfirmasiCheckoutKasir(),
    val statusHasilCheckout: StatusHasilCheckoutKasir = StatusHasilCheckoutKasir(),
    val kataKunciPencarian: String = "",
    val tampilkanAksiResetPencarian: Boolean = false,
    val apakahRingkasanPembayaranTampil: Boolean = true,
    val daftarPesananPending: List<Transaksi> = emptyList(),
    val apakahPanelPesananPendingTampil: Boolean = false,
)
