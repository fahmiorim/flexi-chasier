package id.flexi.kasir.data.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Penyimpanan token JWT terenkripsi (Keystore-backed via EncryptedSharedPreferences).
 * Token tidak pernah disimpan dalam teks biasa.
 */
class TokenStore(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val preferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "token_aman",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

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
    }
}
