package id.cassy.kasir.ranah.kasuspenggunaan

import id.cassy.kasir.ranah.model.Transaksi
import id.cassy.kasir.ranah.repositori.RepositoriTransaksi
import kotlinx.coroutines.flow.Flow

/**
 * Kasus penggunaan untuk mengamati seluruh riwayat transaksi.
 *
 * Use case ini menjadi pintu baca daftar transaksi untuk layar riwayat,
 * sehingga ViewModel tidak perlu mengakses repository secara langsung.
 */
class AmatiRiwayatTransaksi(
    private val repositoriTransaksi: RepositoriTransaksi,
) {
    operator fun invoke(): Flow<List<Transaksi>> {
        return repositoriTransaksi.amatiSemuaTransaksi()
    }
}
