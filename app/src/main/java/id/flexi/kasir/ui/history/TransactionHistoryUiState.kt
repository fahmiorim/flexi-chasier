package id.flexi.kasir.ui.history

import androidx.compose.runtime.Immutable
import id.flexi.kasir.domain.model.PaymentMethod
import id.flexi.kasir.domain.model.TransactionStatus

@Immutable
data class TransactionHistoryUiState(
    val judulLayar: String = "Riwayat Transaksi",
    val filterTanggal: FilterTanggalRiwayat = FilterTanggalRiwayat.Semua,
    val tanggalMulaiKustom: Long? = null,
    val tanggalSelesaiKustom: Long? = null,
    val labelRentangKustom: String = "",
    val statusMuat: StatusMuatRiwayatTransaction = StatusMuatRiwayatTransaction.Memuat,
)

@Immutable
sealed interface StatusMuatRiwayatTransaction {

    @Immutable
    data object Memuat : StatusMuatRiwayatTransaction

    @Immutable
    data class Berhasil(
        val judulBagian: String,
        val deskripsiBagian: String,
        val labelJumlahHasil: String,
        val labelKataKunciAktif: String? = null,
        val daftarGroupPerTanggal: List<GroupRiwayatPerTanggal> = emptyList(),
    ) : StatusMuatRiwayatTransaction

    @Immutable
    data class Kosong(
        val judul: String,
        val deskripsi: String,
    ) : StatusMuatRiwayatTransaction

    @Immutable
    data class Gagal(
        val judul: String,
        val deskripsi: String,
    ) : StatusMuatRiwayatTransaction
}

@Immutable
data class GroupRiwayatPerTanggal(
    val labelTanggal: String,
    val total: String,
    val totalTunai: String,
    val totalQris: String,
    val jumlahTransaksi: String,
    val daftarTransaksi: List<RingkasanTransactionRiwayat>,
)

@Immutable
data class RingkasanTransactionRiwayat(
    val TransactionId: String,
    val waktuTransactionEpochMili: Long = 0L,
    val labelIdentitasTransaction: String,
    val labelWaktu: String,
    /** Label nama meja ("Meja 5") bila transaksi punya meja; null untuk Take Away. */
    val labelMeja: String? = null,
    val labelJumlahItem: String,
    val totalAkhir: Long = 0L,
    val labelTotalAkhir: String,
    val ringkasanItem: String,
    val paymentMethod: PaymentMethod = PaymentMethod.Cash,
    val status: TransactionStatus = TransactionStatus.Paid,
    val dibatalkan: Boolean = false,
)

enum class FilterTanggalRiwayat {
    HariIni,
    Kemarin,
    BulanIni,
    Kustom,
    Semua,
}
