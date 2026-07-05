package id.cassy.kasir.antarmuka.transaksi

import androidx.compose.runtime.Immutable

/**
 * Representasi status data untuk layar detail transaksi.
 *
 * @property judulLayar Judul yang akan ditampilkan di bilah atas.
 * @property statusMuat Kondisi pemuatan rincian transaksi.
 */
@Immutable
data class ModelTampilanDetailTransaksi(
    val judulLayar: String = "Detail Transaksi",
    val statusMuat: StatusMuatDetailTransaksi = StatusMuatDetailTransaksi.Memuat,
)

/**
 * Definisi status pemuatan rincian transaksi.
 */
@Immutable
sealed interface StatusMuatDetailTransaksi {

    /**
     * Sedang dalam proses mengambil data transaksi.
     */
    @Immutable
    data object Memuat : StatusMuatDetailTransaksi

    /**
     * Berhasil mengambil rincian transaksi lengkap.
     *
     * @property transaksiId ID internal transaksi.
     * @property labelIdentitasTransaksi Label transaksi yang ramah dibaca manusia.
     * @property labelWaktu Waktu transaksi terformat.
     * @property labelJumlahItem Deskripsi total kuantitas item.
     * @property labelSubtotal Nilai subtotal terformat.
     * @property labelPotongan Nilai potongan terformat.
     * @property labelBiayaLayanan Nilai biaya layanan terformat.
     * @property labelPajak Nilai pajak terformat.
     * @property labelTotalAkhir Nilai total yang harus dibayar terformat.
     * @property labelUangDibayar Nilai uang yang diberikan pelanggan terformat.
     * @property labelKembalian Nilai kembalian terformat.
     * @property daftarItem List item produk dalam transaksi ini.
     * @property catatan Pesan tambahan dari transaksi, jika ada.
     */
    @Immutable
    data class Berhasil(
        val transaksiId: String,
        val labelIdentitasTransaksi: String,
        val labelWaktu: String,
        val labelJumlahItem: String,
        val labelSubtotal: String,
        val labelPotongan: String,
        val labelBiayaLayanan: String,
        val labelPajak: String,
        val labelTotalAkhir: String,
        val labelUangDibayar: String,
        val labelKembalian: String,
        val daftarItem: List<ItemTampilanDetailTransaksi>,
        val catatan: String?,
    ) : StatusMuatDetailTransaksi

    /**
     * Berhasil memproses tapi ID transaksi tidak ditemukan.
     *
     * @property judul Pesan utama ketidakterseidaan data.
     * @property deskripsi Penjelasan atau alasan data tidak ada.
     */
    @Immutable
    data class Kosong(
        val judul: String,
        val deskripsi: String,
    ) : StatusMuatDetailTransaksi

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
    ) : StatusMuatDetailTransaksi
}

/**
 * Representasi satu baris produk dalam detail transaksi.
 *
 * @property namaProduk Nama barang yang dibeli.
 * @property labelJumlahKaliHarga Deskripsi kuantitas dan harga satuan (misal: "2 x Rp5.000").
 * @property labelSubtotalItem Total harga untuk item ini saja.
 */
@Immutable
data class ItemTampilanDetailTransaksi(
    val namaProduk: String,
    val labelJumlahKaliHarga: String,
    val labelSubtotalItem: String,
)
