package id.flexi.kasir.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.flexi.kasir.domain.model.NetworkOperationResult
import id.flexi.kasir.domain.usecase.KeluarAkun
import id.flexi.kasir.domain.usecase.KirimUlangVerifikasi
import id.flexi.kasir.domain.usecase.LoginUser
import id.flexi.kasir.domain.usecase.RegisterAkun
import id.flexi.kasir.domain.usecase.VerifikasiEmail
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
    val kodeVerifikasi: String = "",
    val sedangMemuat: Boolean = false,
    val pesanError: String? = null,
    val pesanInfo: String? = null,
) {
    enum class Mode {
        Login,
        Register,
        Verifikasi,
    }
}

/**
 * Menangani form login/registrasi/verifikasi email. Setelah berhasil, navigasi
 * diarahkan oleh aliran sesi ([id.flexi.kasir.domain.usecase.AmatiSesi]) di tingkat navigasi.
 */
class AuthViewModel(
    private val loginUser: LoginUser,
    private val registerAkun: RegisterAkun,
    private val verifikasiEmail: VerifikasiEmail,
    private val kirimUlangVerifikasi: KirimUlangVerifikasi,
    private val keluarAkun: KeluarAkun,
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    fun gantiMode(mode: AuthUiState.Mode) {
        _state.value = _state.value.copy(
            mode = mode,
            pesanError = null,
            pesanInfo = null,
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

    fun perbaruiKodeVerifikasi(kode: String) {
        _state.value = _state.value.copy(
            kodeVerifikasi = kode.take(6).filter { it.isDigit() },
            pesanError = null,
        )
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
                AuthUiState.Mode.Verifikasi -> verifikasiEmail(
                    email = state.email.trim(),
                    kode = state.kodeVerifikasi,
                )
            }
            when (hasil) {
                is NetworkOperationResult.Berhasil -> {
                    _state.value = _state.value.copy(sedangMemuat = false)
                }
                is NetworkOperationResult.PerluVerifikasiEmail -> {
                    // Registrasi sukses: pindah ke layar verifikasi, email diisi.
                    _state.value = _state.value.copy(
                        mode = AuthUiState.Mode.Verifikasi,
                        email = hasil.email,
                        kodeVerifikasi = "",
                        sedangMemuat = false,
                        pesanError = null,
                        pesanInfo = "Kode verifikasi terkirim ke email Anda.",
                    )
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

    /**
     * Verifikasi kode dari layar Verifikasi, lalu auto-login bila berhasil.
     */
    fun verifikasiKode() {
        val state = _state.value
        if (state.sedangMemuat) return
        if (state.kodeVerifikasi.length != 6) {
            _state.value = state.copy(pesanError = "Masukkan kode 6 digit dari email.")
            return
        }

        _state.value = state.copy(sedangMemuat = true, pesanError = null)
        viewModelScope.launch {
            val hasil = verifikasiEmail(
                email = state.email.trim(),
                kode = state.kodeVerifikasi,
            )
            when (hasil) {
                is NetworkOperationResult.Berhasil -> {
                    // Email terverifikasi: login otomatis dengan data yang sudah diisi.
                    val login = loginUser(
                        email = state.email.trim(),
                        password = state.password,
                    )
                    when (login) {
                        is NetworkOperationResult.Berhasil -> {
                            _state.value = _state.value.copy(sedangMemuat = false)
                        }
                        is NetworkOperationResult.GagalJaringan -> {
                            _state.value = _state.value.copy(
                                mode = AuthUiState.Mode.Login,
                                sedangMemuat = false,
                                pesanError = login.pesan,
                                pesanInfo = null,
                            )
                        }
                        is NetworkOperationResult.GagalServer -> {
                            _state.value = _state.value.copy(
                                mode = AuthUiState.Mode.Login,
                                sedangMemuat = false,
                                pesanError = login.pesan,
                                pesanInfo = null,
                            )
                        }
                        else -> {
                            _state.value = _state.value.copy(
                                mode = AuthUiState.Mode.Login,
                                sedangMemuat = false,
                                pesanInfo = "Email terverifikasi. Silakan masuk.",
                            )
                        }
                    }
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
                else -> {
                    _state.value = _state.value.copy(sedangMemuat = false)
                }
            }
        }
    }

    fun kirimUlangKode() {
        val state = _state.value
        if (state.sedangMemuat) return

        _state.value = state.copy(sedangMemuat = true, pesanError = null)
        viewModelScope.launch {
            val hasil = kirimUlangVerifikasi(email = state.email.trim())
            when (hasil) {
                is NetworkOperationResult.Berhasil -> {
                    _state.value = _state.value.copy(
                        sedangMemuat = false,
                        pesanInfo = "Kode verifikasi baru terkirim.",
                    )
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
                else -> {
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
