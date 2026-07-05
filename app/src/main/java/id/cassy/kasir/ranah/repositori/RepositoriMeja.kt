package id.cassy.kasir.ranah.repositori

import id.cassy.kasir.ranah.model.Meja
import kotlinx.coroutines.flow.Flow

interface RepositoriMeja {
    fun amatiSemuaMeja(): Flow<List<Meja>>
    suspend fun simpanMeja(meja: Meja)
    suspend fun hapusMeja(id: String)
}
