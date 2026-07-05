package id.cassy.kasir.ranah.kasuspenggunaan

import id.cassy.kasir.ranah.model.Transaksi
import id.cassy.kasir.ranah.repositori.RepositoriTransaksi
import kotlinx.coroutines.flow.Flow

/**
 * Kasus penggunaan untuk mengamati satu transaksi berdasarkan identitas.
 *
 * Dipakai oleh layar detail transaksi agar pembacaan detail tidak
 * menembus repository langsung dari ViewModel.
 */
class AmatiTransaksiBerdasarkanIdentitas(
    private val repositoriTransaksi: RepositoriTransaksi,
) {
    operator fun invoke(
        identitasTransaksi: String,
    ): Flow<Transaksi?> {
        return repositoriTransaksi.amatiTransaksiBerdasarkanIdentitas(
            identitasTransaksi = identitasTransaksi,
        )
    }
}
