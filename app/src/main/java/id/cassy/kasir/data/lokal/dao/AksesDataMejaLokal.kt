package id.cassy.kasir.data.lokal.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import id.cassy.kasir.data.lokal.entitas.EntitasMejaLokal
import kotlinx.coroutines.flow.Flow

@Dao
interface AksesDataMejaLokal {

    @Query("SELECT * FROM meja_lokal WHERE aktif = 1 ORDER BY CAST(nomor AS INTEGER) ASC")
    fun amatiSemuaMejaAktif(): Flow<List<EntitasMejaLokal>>

    @Query("SELECT * FROM meja_lokal ORDER BY CAST(nomor AS INTEGER) ASC")
    fun amatiSemuaMeja(): Flow<List<EntitasMejaLokal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun simpanMeja(meja: EntitasMejaLokal)

    @Query("DELETE FROM meja_lokal WHERE id = :id")
    suspend fun hapusMeja(id: String)
}
