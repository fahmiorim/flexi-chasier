package id.flexi.kasir.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import id.flexi.kasir.data.local.entity.LocalProductEntity
import kotlinx.coroutines.flow.Flow

/**
 * Kontrak akses data untuk entitas produk di database lokal.
 */
@Dao
interface LocalProductDao {

    /**
     * Mengambil seluruh produk yang tersimpan di lokal.
     */
    @Query("SELECT * FROM produk ORDER BY kategori, nama")
    fun amatiSemuaProduk(): Flow<List<LocalProductEntity>>

    /**
     * Mengambil satu produk lokal berdasarkan identitas produk.
     */
    @Query("SELECT * FROM produk WHERE id = :identitasProduk LIMIT 1")
    fun ObserveProductById(
        identitasProduk: String,
    ): Flow<LocalProductEntity?>

    /**
     * Menghitung jumlah produk lokal.
     */
    @Query("SELECT COUNT(*) FROM produk")
    suspend fun hitungJumlahProduk(): Int

    /**
     * Mencari produk berdasarkan kata kunci di nama atau deskripsi.
     */
    @Query(
        """
        SELECT * FROM produk
        WHERE nama LIKE '%' || :kataKunci || '%'
        OR deskripsi LIKE '%' || :kataKunci || '%'
        """,
    )
    fun cariProduk(kataKunci: String): Flow<List<LocalProductEntity>>

    /**
     * Menyimpan banyak produk sekaligus.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun simpanBanyakProduk(
        daftarProduk: List<LocalProductEntity>,
    )

    /**
     * Menyimpan satu produk.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun SaveProduct(produk: LocalProductEntity)

    /**
     * Menghapus produk berdasarkan identitas unik.
     */
    @Query("DELETE FROM produk WHERE id = :identitasProduk")
    suspend fun DeleteProduct(identitasProduk: String)

    /**
     * Mengambil daftar produk lokal berdasarkan kumpulan identitas produk.
     */
    @Query("SELECT * FROM produk WHERE id IN (:daftarIdentitasProduk)")
    suspend fun ambilProdukBerdasarkanDaftarIdentitas(
        daftarIdentitasProduk: List<String>,
    ): List<LocalProductEntity>

    /**
     * Mengurangi stok produk jika stok tersedia masih mencukupi.
     *
     * @return Jumlah baris yang berhasil diperbarui. Nilai 1 berarti sukses,
     * nilai 0 berarti produk tidak ditemukan, tidak aktif, atau stok tidak cukup.
     */
    @Query(
        """
        UPDATE produk
        SET stokTersedia = stokTersedia - :jumlahPengurang
        WHERE id = :identitasProduk
        AND apakahAktif = 1
        AND stokTersedia >= :jumlahPengurang
        """,
    )
    suspend fun kurangiStokJikaCukup(
        identitasProduk: String,
        jumlahPengurang: Int,
    ): Int

    /**
     * Menambah stok produk (pengembalian stok dari pembatalan pesanan pending).
     */
    @Query(
        """
        UPDATE produk
        SET stokTersedia = stokTersedia + :jumlahPenambah
        WHERE id = :identitasProduk
        """,
    )
    suspend fun tambahStok(
        identitasProduk: String,
        jumlahPenambah: Int,
    )

    /**
     * Menandai daftar produk sebagai favorit berdasarkan ID.
     */
    @Query(
        """
        UPDATE produk SET favorit = 1 WHERE id IN (:daftarIdProduk)
        """
    )
    suspend fun tandaiSebagaiFavorit(
        daftarIdProduk: List<String>,
    )

    /**
     * Menghapus semua produk dari tabel lokal.
     */
    @Query("DELETE FROM produk")
    suspend fun hapusSemuaProduk()
}
