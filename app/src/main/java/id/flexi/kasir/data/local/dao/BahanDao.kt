package id.flexi.kasir.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import id.flexi.kasir.data.local.entity.LocalBahanEntity
import id.flexi.kasir.data.local.entity.LocalBahanResepEntity
import id.flexi.kasir.data.local.entity.LocalPembelianBahanEntity
import id.flexi.kasir.data.local.entity.LocalResepEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object untuk mengelola bahan baku, pembelian, resep, dan komposisinya.
 */
@Dao
interface BahanDao {

    // ── Bahan ──

    /** Mengamati seluruh bahan baku. */
    @Query("SELECT * FROM bahan ORDER BY nama ASC")
    fun amatiSemuaBahan(): Flow<List<LocalBahanEntity>>

    /** Mengamati satu bahan berdasarkan ID. */
    @Query("SELECT * FROM bahan WHERE id = :id LIMIT 1")
    fun amatiBahanBerdasarkanId(id: String): Flow<LocalBahanEntity?>

    /** Mengambil satu bahan berdasarkan ID (sekali jalan). */
    @Query("SELECT * FROM bahan WHERE id = :id LIMIT 1")
    suspend fun ambilBahanBerdasarkanId(id: String): LocalBahanEntity?

    /** Menyimpan bahan baru atau memperbarui yang sudah ada. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun simpanBahan(bahan: LocalBahanEntity)

    /** Menghapus bahan berdasarkan ID. */
    @Query("DELETE FROM bahan WHERE id = :id")
    suspend fun hapusBahan(id: String)

    /** Memperbarui stok bahan (menambah atau mengurangi). */
    @Query("UPDATE bahan SET stokTersedia = stokTersedia + :jumlah WHERE id = :id")
    suspend fun perbaruiStokBahan(id: String, jumlah: Double)

    /** Memperbarui harga per satuan bahan. */
    @Query("UPDATE bahan SET hargaPerSatuan = :harga WHERE id = :id")
    suspend fun perbaruiHargaSatuanBahan(id: String, harga: Long)

    // ── Pembelian Bahan ──

    /** Mengamati seluruh pembelian bahan. */
    @Query("SELECT * FROM pembelian_bahan ORDER BY tanggalBeli DESC")
    fun amatiSemuaPembelian(): Flow<List<LocalPembelianBahanEntity>>

    /** Mengamati pembelian untuk satu bahan tertentu. */
    @Query("SELECT * FROM pembelian_bahan WHERE bahanId = :bahanId ORDER BY tanggalBeli DESC")
    fun amatiPembelianBerdasarkanBahan(bahanId: String): Flow<List<LocalPembelianBahanEntity>>

    /** Menyimpan pembelian baru. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun simpanPembelian(pembelian: LocalPembelianBahanEntity)

    /** Menghapus pembelian berdasarkan ID. */
    @Query("DELETE FROM pembelian_bahan WHERE id = :id")
    suspend fun hapusPembelian(id: String)

    /** Mengambil pembelian terakhir untuk suatu bahan (untuk hitung harga satuan). */
    @Query("SELECT * FROM pembelian_bahan WHERE bahanId = :bahanId ORDER BY tanggalBeli DESC LIMIT 1")
    suspend fun ambilPembelianTerakhir(bahanId: String): LocalPembelianBahanEntity?

    // ── Resep ──

    /** Mengamati seluruh resep. */
    @Query("SELECT * FROM resep ORDER BY createdAt DESC")
    fun amatiSemuaResep(): Flow<List<LocalResepEntity>>

    /** Mengamati resep untuk satu produk. */
    @Query("SELECT * FROM resep WHERE produkId = :produkId")
    fun amatiResepBerdasarkanProduk(produkId: String): Flow<List<LocalResepEntity>>

    /** Mengambil resep untuk satu produk (sekali jalan). */
    @Query("SELECT * FROM resep WHERE produkId = :produkId LIMIT 1")
    suspend fun ambilResepBerdasarkanProduk(produkId: String): LocalResepEntity?

    /** Menyimpan resep baru atau memperbarui. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun simpanResep(resep: LocalResepEntity)

    /** Menghapus resep berdasarkan ID. */
    @Query("DELETE FROM resep WHERE id = :id")
    suspend fun hapusResep(id: String)

    /** Menghapus semua resep untuk satu produk. */
    @Query("DELETE FROM resep WHERE produkId = :produkId")
    suspend fun hapusResepBerdasarkanProduk(produkId: String)

    // ── Bahan Resep ──

    /** Mengamati bahan-bahan dalam satu resep. */
    @Query("SELECT * FROM bahan_resep WHERE resepId = :resepId ORDER BY id ASC")
    fun amatiBahanResepBerdasarkanResep(resepId: String): Flow<List<LocalBahanResepEntity>>

    /** Mengambil bahan-bahan dalam satu resep (sekali jalan). */
    @Query("SELECT * FROM bahan_resep WHERE resepId = :resepId ORDER BY id ASC")
    suspend fun ambilBahanResepBerdasarkanResep(resepId: String): List<LocalBahanResepEntity>

    /** Menyimpan satu baris bahan resep. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun simpanBahanResep(bahanResep: LocalBahanResepEntity)

    /** Menyimpan banyak baris bahan resep sekaligus. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun simpanBanyakBahanResep(daftar: List<LocalBahanResepEntity>)

    /** Menghapus semua bahan dalam satu resep. */
    @Query("DELETE FROM bahan_resep WHERE resepId = :resepId")
    suspend fun hapusBahanResepBerdasarkanResep(resepId: String)
}
