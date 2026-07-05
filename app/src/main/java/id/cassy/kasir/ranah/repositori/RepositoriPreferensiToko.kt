package id.cassy.kasir.ranah.repositori

import id.cassy.kasir.ranah.model.PreferensiToko
import kotlinx.coroutines.flow.Flow

/**
 * Kontrak repository untuk preferensi toko.
 *
 * Mengikuti pola UDF, pengambilan preferensi bersifat reaktif melalui [Flow].
 */
interface RepositoriPreferensiToko {

    /**
     * Mengamati preferensi toko secara reaktif.
     */
    fun amatiPreferensiToko(): Flow<PreferensiToko>

    /**
     * Menyimpan preferensi toko ke media penyimpanan permanen.
     */
    suspend fun simpanPreferensiToko(
        preferensiToko: PreferensiToko,
    )

    /**
     * Menyimpan metadata keberhasilan sinkronisasi katalog.
     */
    suspend fun simpanSinkronisasiKatalogBerhasil(
        waktuEpochMili: Long,
    )

    /**
     * Menyimpan metadata kegagalan sinkronisasi katalog.
     */
    suspend fun simpanSinkronisasiKatalogGagal(
        pesan: String,
    )
}
