package id.flexi.kasir.data.auth

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStoreKategoriUrutan by preferencesDataStore(name = "kategori_urutan")

/**
 * Menyimpan urutan kustom kategori produk.
 *
 * Kategori disimpan sebagai string JSON array (e.g. `["Signature","Americano Series"]`).
 * Saat kategori baru ditambahkan (dari produk yang di-sync), ia ditempatkan di akhir
 * daftar. Kategori yang dihapus dari semua produk otomatis hilang dari daftar.
 */
class KategoriUrutanStore(context: Context) {

    private val dataStore = context.dataStoreKategoriUrutan

    /**
     * Flow urutan kategori kustom.
     * Mengembalikan list nama kategori dalam urutan yang diinginkan.
     * List kosong berarti belum ada urutan kustom (default: alphabetical).
     */
    val urutanKategori: Flow<List<String>> = dataStore.data.map { pref ->
        pref[KUNCI]?.split(SEPARATOR).orEmpty().filter { it.isNotBlank() }
    }

    /**
     * Simpan urutan kategori baru.
     */
    suspend fun simpanUrutan(urutan: List<String>) {
        dataStore.edit { pref ->
            pref[KUNCI] = urutan.joinToString(SEPARATOR)
        }
    }

    /**
     * Urutkan daftar kategori berdasarkan urutan kustom.
     * Kategori yang ada di [urutanKustom] diurutkan sesuai posisi,
     * kategori baru (belum ada di urutan) ditempatkan di akhir (urut abjad).
     */
    companion object {
        private val KUNCI = stringPreferencesKey("urutan_kategori")
        private const val SEPARATOR = "|"

        fun urutkan(
            kategori: List<String>,
            urutanKustom: List<String>,
        ): List<String> {
            if (urutanKustom.isEmpty()) return kategori.sorted()

            val peta = urutanKustom.withIndex().associate { (i, nama) -> nama to i.toLong() }
            return kategori.sortedBy { nama ->
                peta[nama] ?: (Int.MAX_VALUE.toLong() + nama.hashCode())
            }
        }
    }
}
