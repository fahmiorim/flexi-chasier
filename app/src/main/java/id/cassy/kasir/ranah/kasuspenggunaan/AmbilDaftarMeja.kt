package id.cassy.kasir.ranah.kasuspenggunaan

import id.cassy.kasir.ranah.model.Meja
import id.cassy.kasir.ranah.repositori.RepositoriMeja
import kotlinx.coroutines.flow.Flow

class AmbilDaftarMeja(
    private val repositoriMeja: RepositoriMeja,
) {
    operator fun invoke(): Flow<List<Meja>> {
        return repositoriMeja.amatiSemuaMeja()
    }
}
