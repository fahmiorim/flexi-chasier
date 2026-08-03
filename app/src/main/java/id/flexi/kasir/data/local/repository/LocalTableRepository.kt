package id.flexi.kasir.data.local.repository

import id.flexi.kasir.data.local.database.FlexiCashierDatabase
import id.flexi.kasir.data.local.entity.LocalTableEntity
import id.flexi.kasir.domain.model.Meja
import id.flexi.kasir.domain.model.TableStatus
import id.flexi.kasir.domain.repository.TableRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TableRepositoryLokal(
    private val basisData: FlexiCashierDatabase,
) : TableRepository {

    override fun amatiSemuaMeja(): Flow<List<Meja>> {
        return basisData.LocalTableDao().amatiSemuaMeja().map { daftarEntitas ->
            daftarEntitas.map { it.sebagaiMeja() }
        }
    }

    override suspend fun SaveTable(meja: Meja) {
        basisData.LocalTableDao().SaveTable(
            LocalTableEntity(
                id = meja.id,
                nomor = meja.nomor,
                aktif = meja.aktif,
                tableStatus = meja.tableStatus.name,
                TransactionId = meja.TransactionId,
                waktuDudukEpochMili = meja.waktuDudukEpochMili,
            ),
        )
    }

    override suspend fun DeleteTable(id: String) {
        basisData.LocalTableDao().DeleteTable(id)
    }

    override suspend fun perbaruiTableStatus(id: String, tableStatus: TableStatus, TransactionId: String?) {
        basisData.LocalTableDao().perbaruiTableStatus(
            id = id,
            tableStatus = tableStatus.name,
            TransactionId = TransactionId,
            waktuDudukEpochMili = if (tableStatus == TableStatus.Occupied) System.currentTimeMillis() else null,
        )
    }

    private fun LocalTableEntity.sebagaiMeja(): Meja = Meja(
        id = id,
        nomor = nomor,
        aktif = aktif,
        tableStatus = try { TableStatus.valueOf(tableStatus) } catch (_: Exception) { TableStatus.Available },
        TransactionId = TransactionId,
        waktuDudukEpochMili = waktuDudukEpochMili,
    )
}
