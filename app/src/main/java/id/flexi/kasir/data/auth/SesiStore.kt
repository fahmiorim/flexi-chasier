package id.flexi.kasir.data.auth

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStoreSesi by preferencesDataStore(name = "sesi_pengguna")

/**
 * Bentuk sesi yang disimpan di DataStore. Berisi data akun + daftar gerai + gerai aktif.
 */
@Serializable
data class SesiTersimpan(
    val id: String,
    val nama: String,
    val email: String,
    val peran: String,
    val daftarGerai: List<GeraiTersimpan> = emptyList(),
    val geraiAktifId: String? = null,
)

@Serializable
data class GeraiTersimpan(
    val id: String,
    val nama: String,
    val alamat: String? = null,
)

/**
 * Menyimpan sesi login pengguna (tanpa token — token disimpan di [TokenStore]).
 * Sesi ini menentukan layar mana yang ditampilkan aplikasi saat dibuka.
 */
class SesiStore(context: Context) {

    private val dataStore = context.dataStoreSesi
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun amatiSesi(): Flow<SesiTersimpan?> =
        dataStore.data.map { preferensi ->
            preferensi[Kunci.SESI]?.let { mentah ->
                runCatching { json.decodeFromString<SesiTersimpan>(mentah) }.getOrNull()
            }
        }

    suspend fun simpan(sesi: SesiTersimpan) {
        dataStore.edit { preferensi ->
            preferensi[Kunci.SESI] = json.encodeToString(sesi)
        }
    }

    suspend fun hapus() {
        dataStore.edit { preferensi ->
            preferensi.remove(Kunci.SESI)
        }
    }

    private object Kunci {
        val SESI = stringPreferencesKey("sesi_json")
    }
}
