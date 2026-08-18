package id.flexi.kasir.ui.main

import androidx.compose.runtime.Immutable
import id.flexi.kasir.domain.model.PaymentMethod
import id.flexi.kasir.domain.model.OrderType

/**
 * Representasi aksi dari antarmuka ke pengelola status layar utama kasir.
 *
 * Seluruh interaksi pengguna dikirim sebagai aksi agar alur data tetap satu arah.
 */
@Immutable
sealed interface CashierMainAction {

    /**
     * Aksi saat pengguna mengubah isi kolom pencarian produk.
     */
    data class UbahKataKunciPencarian(
        val kataKunciBaru: String,
    ) : CashierMainAction

    /**
     * Aksi saat pengguna ingin menampilkan atau menyembunyikan
     * panel Payment.
     */
    data object UbahVisibilitasRingkasanPayment : CashierMainAction

    /**
     * Aksi saat pengguna mengetuk satu produk pada katalog
     * untuk menambahkannya ke keranjang.
     *
     * @property varianNama Jika diisi (dari tombol + di keranjang),
     * tambah langsung tanpa dialog pilih varian.
     * Jika null (dari katalog), tampilkan dialog varian jika produk punya varian.
     */
    data class AddProductToCart(
        val produkId: String,
        val varianNama: String? = null,
    ) : CashierMainAction

    /**
     * Aksi saat pengguna ingin mengurangi jumlah produk
     * yang sudah ada di keranjang.
     */
    data class DecreaseProductInCart(
        val produkId: String,
        val varianNama: String? = null,
    ) : CashierMainAction

    /**
     * Aksi saat pengguna ingin menghapus satu item sepenuhnya
     * dari keranjang.
     */
    data class RemoveProductFromCart(
        val produkId: String,
        val varianNama: String? = null,
    ) : CashierMainAction

    /**
     * Aksi saat pengguna menekan tombol checkout/bayar.
     */
    data class BukaDialogCheckout(
        val modeSimpan: Boolean = false,
    ) : CashierMainAction

    /**
     * Aksi saat pengguna membatalkan dialog konfirmasi checkout.
     */
    data object BatalkanKonfirmasiCheckout : CashierMainAction

    /**
     * Aksi saat pengguna memilih simpan pesanan (pending).
     */
    data object SimpanPesanan : CashierMainAction

    /**
     * Aksi saat pengguna memilih simpan pesanan sekaligus cetak struk.
     */
    data object SimpanDanCetakPesanan : CashierMainAction

    /**
     * Aksi saat pengguna memilih bayar sekarang (lunas).
     */
    data object BayarSekarang : CashierMainAction

    /**
     * Aksi saat pengguna bayar tunai dengan nominal tertentu.
     */
    data class BayarSekarangTunai(
        val nominalUang: Long,
    ) : CashierMainAction

    /**
     * Aksi saat pengguna menutup banner hasil checkout.
     */
    data object TutupStatusHasilCheckout : CashierMainAction

    /**
     * Aksi saat pengguna ingin mengosongkan kolom pencarian produk.
     */
    data object ResetPencarian : CashierMainAction

    /**
     * Aksi saat pengguna meminta pembaruan katalog dari jaringan.
     */
    data object SinkronkanKatalogProduk : CashierMainAction

    /**
     * Aksi saat pengguna meminta sinkronisasi penuh (push outbox + pull
     * perubahan) lewat mesin sinkronisasi.
     */
    data object SinkronkanSekarang : CashierMainAction

    data object AlihkanCatalogDisplay : CashierMainAction

    /**
     * Aksi saat pengguna mengubah isi catatan/keterangan di dialog checkout.
     */
    data class UbahCatatanCheckout(
        val catatan: String,
    ) : CashierMainAction

    /**
     * Aksi saat pengguna memilih metode Payment di dialog checkout.
     */
    data class UbahPaymentMethod(
        val paymentMethod: PaymentMethod,
    ) : CashierMainAction

    data class UbahOrderType(
        val orderType: OrderType,
    ) : CashierMainAction

    data object BukaPendingOrdersPanel : CashierMainAction

    data object TutupPendingOrdersPanel : CashierMainAction

    data class ResumePendingOrder(
        val identitasTransaction: String,
    ) : CashierMainAction

    data class PayPendingOrder(
        val identitasTransaction: String,
        val paymentMethod: PaymentMethod = PaymentMethod.Cash,
        val uangDibayar: Long? = null,
    ) : CashierMainAction

    data class DeletePendingOrder(
        val identitasTransaction: String,
    ) : CashierMainAction

    data object BukaAntrianPanel : CashierMainAction

    data object TutupAntrianPanel : CashierMainAction

    data class SelesaikanAntrian(
        val identitasTransaction: String,
    ) : CashierMainAction

    /**
     * Aksi saat pengguna memilih gabung semua bill di meja yang sama lalu bayar.
     */
    data class GabungDanBayarBill(
        val mejaId: String,
        val paymentMethod: PaymentMethod = PaymentMethod.Cash,
        val uangDibayar: Long? = null,
    ) : CashierMainAction

    /**
     * Aksi saat pengguna memilih gabung semua bill di meja yang sama lalu simpan.
     */
    data class GabungDanSimpanBill(
        val mejaId: String,
    ) : CashierMainAction

    data class PilihMeja(
        val mejaId: String?,
    ) : CashierMainAction

    data class UbahTabTransaksi(val tab: Int) : CashierMainAction

    data class UbahKategoriTerpilih(val kategori: String) : CashierMainAction

    data class TambahItemManual(val nama: String, val harga: Long) : CashierMainAction

    /**
     * Aksi split bill: bayar hanya item yang dipilih, sisanya tetap di keranjang.
     */
    data class SplitBill(
        val daftarIdProduk: Set<String>,
        val paymentMethod: PaymentMethod = PaymentMethod.Cash,
        val uangTunai: Long = 0L,
    ) : CashierMainAction

    /**
     * Aksi saat pengguna memilih varian produk dari dialog pemilih varian.
     */
    data class PilihVarianProduk(
        val produkId: String,
        val varian: id.flexi.kasir.domain.model.Varian,
    ) : CashierMainAction

    /**
     * Aksi saat pengguna membatalkan pemilihan varian produk.
     */
    data object BatalkanPilihVarian : CashierMainAction

    /**
     * Aksi saat pengguna mengubah urutan kategori via drag & drop.
     */
    data class UbahUrutanKategori(
        val urutanBaru: List<String>,
    ) : CashierMainAction

}
