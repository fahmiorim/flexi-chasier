package id.flexi.kasir.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import id.flexi.kasir.data.local.entity.OutboxSinkronEntity
import kotlinx.coroutines.flow.Flow

/**
 * Akses data antrian outbox sinkronisasi.
 *
 * Tulis ulang entitas yang sama (entitas + itemId) akan mengganti payload dan
 * mengulang status ke "Antri" — pola outbox klasik (satu perubahan terbaru per item).
 */
@Dao
interface OutboxDao {

    /** Mengambil versi yang tersimpan untuk satu item (null jika belum pernah dicatat). */
    @Query(
        """
        SELECT versi FROM outbox_sinkron
        WHERE entitas = :entitas AND itemId = :itemId
        LIMIT 1
        """,
    )
    suspend fun ambilVersi(
        entitas: String,
        itemId: String,
    ): Long?

    /** Mengambil payload tersimpan untuk satu item (null jika belum pernah dicatat). */
    @Query(
        """
        SELECT payload FROM outbox_sinkron
        WHERE entitas = :entitas AND itemId = :itemId
        LIMIT 1
        """,
    )
    suspend fun ambilPayload(
        entitas: String,
        itemId: String,
    ): String?

    /**
     * Menulis (insert atau timpa) satu entri outbox.
     * Konflik pada (entitas, itemId) akan mengganti payload dan me-reset status.
     */
    @Query(
        """
        INSERT INTO outbox_sinkron (entitas, itemId, geraiId, versi, payload, status, jumlahPercobaan, waktuDibuat)
        VALUES (:entitas, :itemId, :geraiId, :versi, :payload, 'Antri', 0, :waktuDibuat)
        ON CONFLICT(entitas, itemId) DO UPDATE SET
            geraiId = :geraiId,
            versi = :versi,
            payload = :payload,
            status = 'Antri',
            jumlahPercobaan = 0,
            waktuDibuat = :waktuDibuat,
            pesanError = NULL
        """,
    )
    suspend fun tulis(
        entitas: String,
        itemId: String,
        geraiId: String,
        versi: Long,
        payload: String,
        waktuDibuat: Long,
    )

    /** Mengambil antrian yang belum dikirim, paling lama [batas] baris. */
    @Query(
        """
        SELECT * FROM outbox_sinkron
        WHERE status = 'Antri'
        ORDER BY waktuDibuat ASC
        LIMIT :batas
        """,
    )
    suspend fun ambilAntri(batas: Int): List<OutboxSinkronEntity>

    /** Mengambil antrian per jenis entitas (untuk dikelompokkan per endpoint push). */
    @Query(
        """
        SELECT * FROM outbox_sinkron
        WHERE status = 'Antri' AND entitas = :entitas
        ORDER BY waktuDibuat ASC
        LIMIT :batas
        """,
    )
    suspend fun ambilAntriPerEntitas(
        entitas: String,
        batas: Int,
    ): List<OutboxSinkronEntity>

    /**
     * Menghapus baris outbox yang sudah berhasil dikirim (pola outbox klasik:
     * hapus setelah deliver sukses) agar tabel tidak tumbuh tanpa batas.
     * Payload idempotent (id deterministik + versi), jadi perubahan berikutnya
     * pada item yang sama akan menulis baris baru lewat [tulis].
     */
    @Query("DELETE FROM outbox_sinkron WHERE id IN (:daftarId)")
    suspend fun hapusBanyak(daftarId: List<Long>)

    @Query("UPDATE outbox_sinkron SET jumlahPercobaan = jumlahPercobaan + 1 WHERE id = :id")
    suspend fun tambahPercobaan(id: Long)

    @Query("UPDATE outbox_sinkron SET status = 'Gagal', pesanError = :pesan WHERE id = :id")
    suspend fun tandaiGagal(
        id: Long,
        pesan: String,
    )

    /** Jumlah perubahan lokal yang belum berhasil dikirim. */
    @Query("SELECT COUNT(*) FROM outbox_sinkron WHERE status = 'Antri'")
    suspend fun hitungAntri(): Int

    /**
     * Mengamati jumlah perubahan lokal yang menunggu dikirim.
     * Dipakai UI untuk menampilkan status sinkronisasi secara reaktif.
     */
    @Query("SELECT COUNT(*) FROM outbox_sinkron WHERE status = 'Antri'")
    fun observasiHitungAntri(): Flow<Int>

    /** Membersihkan entri yang sudah berstatus Gagal (mis. setelah periode tertentu). */
    @Query("DELETE FROM outbox_sinkron WHERE status = 'Gagal'")
    suspend fun bersihkanGagal()
}
