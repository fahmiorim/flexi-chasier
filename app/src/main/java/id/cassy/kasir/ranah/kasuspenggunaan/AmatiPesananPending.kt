package id.cassy.kasir.ranah.kasuspenggunaan

import id.cassy.kasir.ranah.model.Transaksi
import id.cassy.kasir.ranah.repositori.RepositoriTransaksi
import kotlinx.coroutines.flow.Flow

class AmatiPesananPending(
    private val repositoriTransaksi: RepositoriTransaksi,
) {
    operator fun invoke(): Flow<List<Transaksi>> {
        return repositoriTransaksi.amatiTransaksiPending()
    }
}
