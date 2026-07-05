package id.cassy.kasir.antarmuka.riwayat

import androidx.compose.runtime.Immutable

@Immutable
data class ModelTampilanRiwayatTransaksi(
    val judulLayar: String = "Riwayat Transaksi",
    val kataKunciPencarian: String = "",
    val petunjukPencarian: String = "Cari id transaksi, catatan, atau nama produk",
    val filterTanggal: FilterTanggalRiwayat = FilterTanggalRiwayat.Semua,
    val tampilkanAksiResetPencarian: Boolean = false,
    val statusMuat: StatusMuatRiwayatTransaksi = StatusMuatRiwayatTransaksi.Memuat,
)

@Immutable
sealed interface StatusMuatRiwayatTransaksi {

    @Immutable
    data object Memuat : StatusMuatRiwayatTransaksi

    @Immutable
    data class Berhasil(
        val judulBagian: String,
        val deskripsiBagian: String,
        val labelJumlahHasil: String,
        val labelKataKunciAktif: String? = null,
        val daftarRingkasanTransaksi: List<RingkasanTransaksiRiwayat>,
    ) : StatusMuatRiwayatTransaksi

    @Immutable
    data class Kosong(
        val judul: String,
        val deskripsi: String,
    ) : StatusMuatRiwayatTransaksi

    @Immutable
    data class Gagal(
        val judul: String,
        val deskripsi: String,
    ) : StatusMuatRiwayatTransaksi
}

@Immutable
data class RingkasanTransaksiRiwayat(
    val transaksiId: String,
    val labelIdentitasTransaksi: String,
    val labelWaktu: String,
    val labelJumlahItem: String,
    val labelTotalAkhir: String,
    val ringkasanItem: String,
)

enum class FilterTanggalRiwayat {
    HariIni,
    Kemarin,
    BulanIni,
    Semua,
}
