package id.flexi.kasir.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import id.flexi.kasir.data.local.entity.LocalTableEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalTableDao {

    @Query("SELECT * FROM meja_lokal WHERE aktif = 1 ORDER BY CAST(nomor AS INTEGER) ASC")
    fun amatiSemuaMejaAktif(): Flow<List<LocalTableEntity>>

    @Query("SELECT * FROM meja_lokal ORDER BY CAST(nomor AS INTEGER) ASC")
    fun amatiSemuaMeja(): Flow<List<LocalTableEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun SaveTable(meja: LocalTableEntity)

    @Query("DELETE FROM meja_lokal WHERE id = :id")
    suspend fun DeleteTable(id: String)

    @Query("SELECT * FROM meja_lokal WHERE id = :id LIMIT 1")
    suspend fun ambilMeja(id: String): LocalTableEntity?

    @Query("""
        UPDATE meja_lokal 
        SET tableStatus = :tableStatus, TransactionId = :TransactionId, 
            waktuDudukEpochMili = :waktuDudukEpochMili 
        WHERE id = :id
    """)
    suspend fun perbaruiTableStatus(
        id: String,
        tableStatus: String,
        TransactionId: String?,
        waktuDudukEpochMili: Long?,
    )
}
