package id.cassy.kasir.ranah.kasuspenggunaan

import id.cassy.kasir.ranah.model.PengaturanToko
import id.cassy.kasir.ranah.repositori.RepositoriPengaturanToko
import kotlinx.coroutines.flow.Flow

class AmbilPengaturanToko(
    private val repositoriPengaturanToko: RepositoriPengaturanToko,
) {
    operator fun invoke(): Flow<PengaturanToko> {
        return repositoriPengaturanToko.ambilPengaturan()
    }
}
