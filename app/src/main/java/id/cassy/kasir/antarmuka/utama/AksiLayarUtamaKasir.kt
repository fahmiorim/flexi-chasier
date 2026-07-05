package id.cassy.kasir.antarmuka.utama

import androidx.compose.runtime.Immutable
import id.cassy.kasir.ranah.model.MetodeBayar
import id.cassy.kasir.ranah.model.TipeOrder

/**
 * Representasi aksi dari antarmuka ke pengelola status layar utama kasir.
 *
 * Seluruh interaksi pengguna dikirim sebagai aksi agar alur data tetap satu arah.
 */
@Immutable
sealed interface AksiLayarUtamaKasir {

    /**
     * Aksi saat pengguna mengubah isi kolom pencarian produk.
     */
    data class UbahKataKunciPencarian(
        val kataKunciBaru: String,
    ) : AksiLayarUtamaKasir

    /**
     * Aksi saat pengguna ingin menampilkan atau menyembunyikan
     * panel pembayaran.
     */
    data object UbahVisibilitasRingkasanPembayaran : AksiLayarUtamaKasir

    /**
     * Aksi saat pengguna mengetuk satu produk pada katalog
     * untuk menambahkannya ke keranjang.
     */
    data class TambahProdukKeKeranjang(
        val produkId: String,
    ) : AksiLayarUtamaKasir

    /**
     * Aksi saat pengguna ingin mengurangi jumlah produk
     * yang sudah ada di keranjang.
     */
    data class KurangiProdukDiKeranjang(
        val produkId: String,
    ) : AksiLayarUtamaKasir

    /**
     * Aksi saat pengguna ingin menghapus satu item sepenuhnya
     * dari keranjang.
     */
    data class HapusProdukDariKeranjang(
        val produkId: String,
    ) : AksiLayarUtamaKasir

    /**
     * Aksi saat pengguna menekan tombol checkout.
     */
    data object CobaCheckout : AksiLayarUtamaKasir

    /**
     * Aksi saat pengguna membatalkan dialog konfirmasi checkout.
     */
    data object BatalkanKonfirmasiCheckout : AksiLayarUtamaKasir

    /**
     * Aksi saat pengguna memilih simpan pesanan (pending).
     */
    data object SimpanPesanan : AksiLayarUtamaKasir

    /**
     * Aksi saat pengguna memilih bayar sekarang (lunas).
     */
    data object BayarSekarang : AksiLayarUtamaKasir

    /**
     * Aksi saat pengguna menutup banner hasil checkout.
     */
    data object TutupStatusHasilCheckout : AksiLayarUtamaKasir

    /**
     * Aksi saat pengguna ingin mengosongkan kolom pencarian produk.
     */
    data object ResetPencarian : AksiLayarUtamaKasir

    /**
     * Aksi saat pengguna meminta pembaruan katalog dari jaringan.
     */
    data object SinkronkanKatalogProduk : AksiLayarUtamaKasir

    /**
     * Aksi saat pengguna mengubah isi catatan/keterangan di dialog checkout.
     */
    data class UbahCatatanCheckout(
        val catatan: String,
    ) : AksiLayarUtamaKasir

    /**
     * Aksi saat pengguna memilih metode pembayaran di dialog checkout.
     */
    data class UbahMetodeBayar(
        val metodeBayar: MetodeBayar,
    ) : AksiLayarUtamaKasir

    data class UbahTipeOrder(
        val tipeOrder: TipeOrder,
    ) : AksiLayarUtamaKasir

    data object BukaPanelPesananPending : AksiLayarUtamaKasir

    data object TutupPanelPesananPending : AksiLayarUtamaKasir

    data class LanjutkanPesananPending(
        val identitasTransaksi: String,
    ) : AksiLayarUtamaKasir

    data class BayarPesananPending(
        val identitasTransaksi: String,
    ) : AksiLayarUtamaKasir

    data class HapusPesananPending(
        val identitasTransaksi: String,
    ) : AksiLayarUtamaKasir
}
