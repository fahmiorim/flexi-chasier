package id.flexi.kasir.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.flexi.kasir.domain.model.NetworkOperationResult
import id.flexi.kasir.domain.usecase.KeluarAkun
import id.flexi.kasir.domain.usecase.LoginUser
import id.flexi.kasir.domain.usecase.RegisterAkun
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val mode: Mode = Mode.Login,
    val email: String = "",
    val password: String = "",
    val namaUsaha: String = "",
    val namaUser: String = "",
    val sedangMemuat: Boolean = false,
    val pesanError: String? = null,
) {
    enum class Mode {
        Login,
        Register,
    }
}

/**
 * Menangani form login/registrasi. Setelah berhasil, navigasi diarahkan oleh
 * aliran sesi ([id.flexi.kasir.domain.usecase.AmatiSesi]) di tingkat navigasi.
 */
class AuthViewModel(
    private val loginUser: LoginUser,
    private val registerAkun: RegisterAkun,
    private val keluarAkun: KeluarAkun,
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    fun gantiMode(mode: AuthUiState.Mode) {
        _state.value = _state.value.copy(
            mode = mode,
            pesanError = null,
        )
    }

    fun perbaruiEmail(email: String) {
        _state.value = _state.value.copy(email = email, pesanError = null)
    }

    fun perbaruiPassword(password: String) {
        _state.value = _state.value.copy(password = password, pesanError = null)
    }

    fun perbaruiNamaUsaha(namaUsaha: String) {
        _state.value = _state.value.copy(namaUsaha = namaUsaha, pesanError = null)
    }

    fun perbaruiNamaUser(namaUser: String) {
        _state.value = _state.value.copy(namaUser = namaUser, pesanError = null)
    }

    fun kirim() {
        val state = _state.value
        if (state.sedangMemuat) return

        val pesanKesalahan = validasi(state)
        if (pesanKesalahan != null) {
            _state.value = state.copy(pesanError = pesanKesalahan)
            return
        }

        _state.value = state.copy(sedangMemuat = true, pesanError = null)
        viewModelScope.launch {
            val hasil = when (state.mode) {
                AuthUiState.Mode.Login -> loginUser(
                    email = state.email.trim(),
                    password = state.password,
                )
                AuthUiState.Mode.Register -> registerAkun(
                    namaUsaha = state.namaUsaha.trim(),
                    namaUser = state.namaUser.trim(),
                    email = state.email.trim(),
                    password = state.password,
                )
            }
            when (hasil) {
                is NetworkOperationResult.Berhasil -> {
                    _state.value = _state.value.copy(sedangMemuat = false)
                }
                is NetworkOperationResult.GagalJaringan -> {
                    _state.value = _state.value.copy(
                        sedangMemuat = false,
                        pesanError = hasil.pesan,
                    )
                }
                is NetworkOperationResult.GagalServer -> {
                    _state.value = _state.value.copy(
                        sedangMemuat = false,
                        pesanError = hasil.pesan,
                    )
                }
                is NetworkOperationResult.FallbackLokal -> {
                    _state.value = _state.value.copy(sedangMemuat = false)
                }
            }
        }
    }

    fun keluar() {
        viewModelScope.launch {
            keluarAkun()
        }
    }

    private fun validasi(state: AuthUiState): String? {
        val email = state.email.trim()
        val password = state.password

        if (state.mode == AuthUiState.Mode.Register) {
            if (state.namaUsaha.isBlank()) return "Nama usaha wajib diisi."
            if (state.namaUser.isBlank()) return "Nama pengguna wajib diisi."
        }
        if (email.isBlank()) return "Email wajib diisi."
        if (!email.contains("@")) return "Format email tidak valid."
        if (password.isBlank()) return "Kata sandi wajib diisi."
        if (password.length < 6) return "Kata sandi minimal 6 karakter."
        return null
    }
}
