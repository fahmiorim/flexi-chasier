package id.cassy.kasir.ranah.repositori

import id.cassy.kasir.ranah.model.StatusTransaksi
import id.cassy.kasir.ranah.model.Transaksi
import id.cassy.kasir.ranah.model.Uang
import kotlinx.coroutines.flow.Flow

/**
 * Kontrak repository transaksi untuk layer ranah.
 *
 * Use case hanya bergantung pada kontrak ini agar aturan bisnis transaksi
 * tidak menempel langsung ke implementasi Room.
 */
interface RepositoriTransaksi {

    /**
     * Menyimpan transaksi baru beserta itemnya.
     *
     * Implementasi data wajib memastikan penyimpanan bersifat atomik agar
     * transaksi tidak tersimpan setengah.
     *
     * @param transaksi Transaksi final yang akan dicatat.
     */
    suspend fun simpanTransaksi(
        transaksi: Transaksi,
    )

    /**
     * Menyimpan transaksi dan mengurangi stok produk terkait secara atomik.
     *
     * Implementasi wajib memastikan transaksi tidak tersimpan jika stok produk
     * tidak cukup, produk tidak aktif, atau produk tidak ditemukan.
     *
     * @param transaksi Transaksi final yang akan dicatat.
     */
    suspend fun simpanTransaksiDanKurangiStok(
        transaksi: Transaksi,
    )

    /**
     * Mengamati semua transaksi yang sudah tersimpan.
     *
     * @return Aliran daftar transaksi dari sumber data aktif.
     */
    fun amatiSemuaTransaksi(): Flow<List<Transaksi>>

    /**
     * Mengamati satu transaksi berdasarkan identitas.
     *
     * @param identitasTransaksi Identitas unik transaksi.
     * @return Aliran transaksi, atau null bila tidak ditemukan.
     */
    fun amatiTransaksiBerdasarkanIdentitas(
        identitasTransaksi: String,
    ): Flow<Transaksi?>

    /**
     * Mengambil satu transaksi berdasarkan identitas secara sekali jalan.
     *
     * @param identitasTransaksi Identitas unik transaksi.
     * @return Transaksi bila ditemukan, atau null bila tidak ada.
     */
    suspend fun ambilTransaksiBerdasarkanIdentitas(
        identitasTransaksi: String,
    ): Transaksi?

    fun amatiTransaksiPending(): Flow<List<Transaksi>>

    suspend fun hapusTransaksiDanKembalikanStok(
        identitasTransaksi: String,
    )

    suspend fun perbaruiStatusDanPembayaranTransaksi(
        identitasTransaksi: String,
        status: StatusTransaksi,
        uangDibayar: Uang,
    )
}
