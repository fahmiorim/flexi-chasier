package id.cassy.kasir.data.lokal.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import id.cassy.kasir.data.lokal.entitas.EntitasProdukLokal
import kotlinx.coroutines.flow.Flow

/**
 * Kontrak akses data untuk entitas produk di database lokal.
 */
@Dao
interface AksesDataProdukLokal {

    /**
     * Mengambil seluruh produk yang tersimpan di lokal.
     */
    @Query("SELECT * FROM produk")
    fun amatiSemuaProduk(): Flow<List<EntitasProdukLokal>>

    /**
     * Mengambil satu produk lokal berdasarkan identitas produk.
     */
    @Query("SELECT * FROM produk WHERE id = :identitasProduk LIMIT 1")
    fun amatiProdukBerdasarkanIdentitas(
        identitasProduk: String,
    ): Flow<EntitasProdukLokal?>

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
    fun cariProduk(kataKunci: String): Flow<List<EntitasProdukLokal>>

    /**
     * Menyimpan banyak produk sekaligus.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun simpanBanyakProduk(
        daftarProduk: List<EntitasProdukLokal>,
    )

    /**
     * Menyimpan satu produk.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun simpanProduk(produk: EntitasProdukLokal)

    /**
     * Menghapus produk berdasarkan identitas unik.
     */
    @Query("DELETE FROM produk WHERE id = :identitasProduk")
    suspend fun hapusProduk(identitasProduk: String)

    /**
     * Mengambil daftar produk lokal berdasarkan kumpulan identitas produk.
     */
    @Query("SELECT * FROM produk WHERE id IN (:daftarIdentitasProduk)")
    suspend fun ambilProdukBerdasarkanDaftarIdentitas(
        daftarIdentitasProduk: List<String>,
    ): List<EntitasProdukLokal>

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
     * Menghapus semua produk dari tabel lokal.
     */
    @Query("DELETE FROM produk")
    suspend fun hapusSemuaProduk()
}
