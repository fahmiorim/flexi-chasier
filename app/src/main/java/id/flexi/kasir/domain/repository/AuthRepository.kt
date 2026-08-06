package id.flexi.kasir.domain.repository

import id.flexi.kasir.domain.model.AkunUser
import id.flexi.kasir.domain.model.NetworkOperationResult
import kotlinx.coroutines.flow.Flow

/**
 * Sumber kebenaran sesi pengguna di perangkat. Menyimpan token & akun ke penyimpanan aman.
 */
interface AuthRepository {
    /**
     * Memantau sesi yang sedang login. Null berarti belum login / sudah logout.
     */
    fun amatiSesi(): Flow<AkunUser?>

    /**
     * Login dengan username/email dan password. Berhasil bila token tersimpan dan sesi aktif.
     */
    suspend fun login(email: String, password: String): NetworkOperationResult<AkunUser>

    /**
     * Registrasi tenant baru (nama usaha + akun pemilik pertama).
     * Kembali [NetworkOperationResult.PerluVerifikasiEmail] bila email belum
     * diverifikasi — token tidak diberikan sampai verifikasi selesai.
     */
    suspend fun register(
        namaUsaha: String,
        namaUser: String,
        email: String,
        password: String,
    ): NetworkOperationResult<AkunUser>

    /**
     * Verifikasi email dengan kode 6 digit. Berhasil bila kode benar.
     */
    suspend fun verifikasiEmail(email: String, kode: String): NetworkOperationResult<Unit>

    /**
     * Minta kirim ulang kode verifikasi email.
     */
    suspend fun kirimUlangVerifikasi(email: String): NetworkOperationResult<Unit>

    /**
     * Minta kode reset password ke email terdaftar.
     */
    suspend fun lupaPassword(email: String): NetworkOperationResult<Unit>

    /**
     * Set password baru memakai kode reset 6 digit dari email.
     */
    suspend fun resetPassword(
        email: String,
        kode: String,
        passwordBaru: String,
    ): NetworkOperationResult<Unit>

    /**
     * Memilih gerai aktif dari daftar gerai milik pengguna.
     */
    suspend fun pilihGerai(geraiId: String)

    /**
     * Membersihkan token dan sesi lokal.
     */
    suspend fun logout()
}
