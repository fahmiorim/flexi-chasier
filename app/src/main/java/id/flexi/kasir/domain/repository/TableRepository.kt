package id.flexi.kasir.domain.repository

import id.flexi.kasir.domain.model.Meja
import id.flexi.kasir.domain.model.TableStatus
import kotlinx.coroutines.flow.Flow

interface TableRepository {
    fun amatiSemuaMeja(): Flow<List<Meja>>
    suspend fun SaveTable(meja: Meja)
    suspend fun DeleteTable(id: String)
    suspend fun perbaruiTableStatus(id: String, tableStatus: TableStatus, TransactionId: String? = null)
}
