package id.flexi.kasir.ui.transaction

import androidx.compose.runtime.Immutable
import id.flexi.kasir.domain.model.PaymentMethod

/**
 * Representasi status data untuk layar detail Transaction.
 *
 * @property judulLayar Judul yang akan ditampilkan di bilah atas.
 * @property statusMuat Kondisi pemuatan rincian Transaction.
 */
@Immutable
data class TransactionDetailUiState(
    val judulLayar: String = "Detail Transaction",
    val statusMuat: StatusMuatDetailTransaction = StatusMuatDetailTransaction.Memuat,
    val apakahDialogBatalkanTerbuka: Boolean = false,
    val alasanPembatalan: String = "",
    // ── Edit dialog state ──
    val apakahDialogEditTerbuka: Boolean = false,
    val editPaymentMethod: PaymentMethod = PaymentMethod.Cash,
    val editUangDibayar: String = "",
    val editCatatan: String = "",
    val sedangMenyimpanEdit: Boolean = false,
)

/**
 * Definisi status pemuatan rincian Transaction.
 */
@Immutable
sealed interface StatusMuatDetailTransaction {

    /**
     * Sedang dalam proses mengambil data Transaction.
     */
    @Immutable
    data object Memuat : StatusMuatDetailTransaction

    /**
     * Berhasil mengambil rincian Transaction lengkap.
     *
     * @property TransactionId ID internal Transaction.
     * @property labelIdentitasTransaction Label Transaction yang ramah dibaca manusia.
     * @property labelWaktu Waktu Transaction terformat.
     * @property labelJumlahItem Deskripsi total kuantitas item.
     * @property labelSubtotal Nilai subtotal terformat.
     * @property labelPotongan Nilai potongan terformat.
     * @property labelBiayaLayanan Nilai biaya layanan terformat.
     * @property labelPajak Nilai pajak terformat.
     * @property labelTotalAkhir Nilai total yang harus dibayar terformat.
     * @property labelUangDibayar Nilai uang yang diberikan pelanggan terformat.
     * @property labelKembalian Nilai kembalian terformat.
     * @property daftarItem List item produk dalam Transaction ini.
     * @property daftarTimeline Riwayat waktu kronologis Transaction.
     * @property catatan Pesan tambahan dari Transaction, jika ada.
     */
    @Immutable
    data class Berhasil(
        val TransactionId: String,
        val labelIdentitasTransaction: String,
        val labelWaktu: String,
        val labelPembayaran: String,
        val labelStatus: String,
        val labelMeja: String?,
        val labelJumlahItem: String,
        val labelSubtotal: String,
        val labelPotongan: String,
        val labelBiayaLayanan: String,
        val labelPajak: String,
        val labelTotalAkhir: String,
        val labelUangDibayar: String,
        val labelKembalian: String,
        val daftarItem: List<ItemTampilanDetailTransaction>,
        val catatan: String?,
        val dibatalkan: Boolean = false,
        val alasanPembatalan: String? = null,
        // Raw data for editing
        val paymentMethod: PaymentMethod = PaymentMethod.Cash,
        val uangDibayar: Long = 0L,
    ) : StatusMuatDetailTransaction

    /**
     * Berhasil memproses tapi ID Transaction tidak ditemukan.
     *
     * @property judul Pesan utama ketidakterseidaan data.
     * @property deskripsi Penjelasan atau alasan data tidak ada.
     */
    @Immutable
    data class Kosong(
        val judul: String,
        val deskripsi: String,
    ) : StatusMuatDetailTransaction

    /**
     * Terjadi kesalahan teknis saat memuat detail.
     *
     * @property judul Pesan kegagalan utama.
     * @property deskripsi Rincian kesalahan atau saran perbaikan.
     */
    @Immutable
    data class Gagal(
        val judul: String,
        val deskripsi: String,
    ) : StatusMuatDetailTransaction
}

/**
 * Representasi satu baris produk dalam detail Transaction.
 *
 * @property namaProduk Nama barang yang dibeli.
 * @property labelJumlahKaliHarga Deskripsi kuantitas dan harga satuan (misal: "2 x Rp5.000").
 * @property labelSubtotalItem Total harga untuk item ini saja.
 */
@Immutable
data class ItemTampilanDetailTransaction(
    val namaProduk: String,
    val labelJumlahKaliHarga: String,
    val labelSubtotalItem: String,
)
