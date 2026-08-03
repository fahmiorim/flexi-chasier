package id.flexi.kasir.ui.main

import androidx.compose.runtime.Immutable
import id.flexi.kasir.domain.model.CartItem
import id.flexi.kasir.domain.model.PaymentMethod
import id.flexi.kasir.domain.model.SyncStatus
import id.flexi.kasir.domain.model.OrderType

/**
 * Kelompok status yang merepresentasikan data Transaction aktif di kasir.
 *
 * Status ini bersifat atomik dan dikelola sebagai bagian dari status internal ViewModel.
 * Digunakan untuk melacak item yang dipilih pelanggan dan status persistensi data.
 *
 * @property daftarCartItem Daftar produk yang telah ditambahkan ke keranjang belanja.
 * @property SyncStatus Objek status yang menunjukkan kondisi sinkronisasi data.
 */
@Immutable
data class CashierMainTransactionState(
    val daftarCartItem: List<CartItem> = emptyList(),
    val syncStatus: SyncStatus = SyncStatus.LocalChanges,
)

/**
 * Kelompok status yang merepresentasikan elemen visual dan interaksi pada layar utama.
 *
 * Memisahkan status UI dari data Transaction membantu dalam mengurangi re-komposisi yang tidak perlu
 * dan mempermudah pengelolaan status sementara seperti visibilitas panel.
 *
 * @property apakahRingkasanPaymentTampil Menentukan apakah panel detail biaya ditampilkan atau disembunyikan.
 * @property apakahDialogKonfirmasiCheckoutTampil Menentukan visibilitas dialog finalisasi Transaction.
 * @property statusHasilCheckout Pesan dan judul hasil akhir setelah aksi checkout berhasil dilakukan.
 * @property catatanCheckout Catatan/keterangan yang diisi pengguna di dialog checkout.
 */
@Immutable
data class CashierMainElementState(
    val apakahRingkasanPaymentTampil: Boolean = true,
    val apakahDialogKonfirmasiCheckoutTampil: Boolean = false,
    val statusHasilCheckout: CheckoutResultStatus = CheckoutResultStatus(),
    val catatanCheckout: String = "",
    val paymentMethod: PaymentMethod = PaymentMethod.Cash,
    val orderType: OrderType = OrderType.DineIn,
    val apakahPendingOrdersPanelTampil: Boolean = false,
    val apakahAntrianPanelTampil: Boolean = false,
    val mejaId: String? = null,
    val modeSimpan: Boolean = false,
    val apakahSedangMenggabungkan: Boolean = false,
    val produkUntukPilihVarian: id.flexi.kasir.domain.model.Produk? = null,
    val uangDibayarTunai: Long? = null,
    val resumeTransactionId: String? = null,
    val apakahBaruSplitBill: Boolean = false,
)
