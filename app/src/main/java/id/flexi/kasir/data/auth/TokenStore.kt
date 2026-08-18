package id.flexi.kasir.data.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Penyimpanan token JWT terenkripsi (Keystore-backed via EncryptedSharedPreferences).
 * Token tidak pernah disimpan dalam teks biasa.
 *
 * Jika Enkripsi gagal (misal signing key berubah antara debug ↔ release),
 * data lama dihapus agar aplikasi tidak crash.
 */
class TokenStore(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val preferences: SharedPreferences = buatPreferences(context, masterKey)

    val aksesToken: String?
        get() = preferences.getString(KUNCI_AKSES, null)

    val refreshToken: String?
        get() = preferences.getString(KUNCI_REFRESH, null)

    fun simpan(aksesToken: String, refreshToken: String) {
        preferences.edit()
            .putString(KUNCI_AKSES, aksesToken)
            .putString(KUNCI_REFRESH, refreshToken)
            .apply()
    }

    fun hapus() {
        preferences.edit().clear().apply()
    }

    private companion object {
        const val KUNCI_AKSES = "akses_token"
        const val KUNCI_REFRESH = "refresh_token"
        const val NAMA_FILE = "token_aman"

        /**
         * Membuat EncryptedSharedPreferences dengan fallback.
         * Jika decrypt gagal (AEADBadTagException / KeyStoreException),
         * file preferences lama dihapus dan dibuat ulang dari awal.
         */
        fun buatPreferences(context: Context, masterKey: MasterKey): SharedPreferences {
            return try {
                EncryptedSharedPreferences.create(
                    context,
                    NAMA_FILE,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
                )
            } catch (_: Exception) {
                // Signing key berubah atau data korup → hapus file lama
                context.getSharedPreferences(NAMA_FILE, Context.MODE_PRIVATE).edit().clear().apply()
                context.deleteSharedPreferences(NAMA_FILE)
                // Buat ulang dari awal
                EncryptedSharedPreferences.create(
                    context,
                    NAMA_FILE,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
                )
            }
        }
    }
}
