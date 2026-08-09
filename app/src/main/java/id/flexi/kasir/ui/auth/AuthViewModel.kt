package id.flexi.kasir.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.flexi.kasir.domain.model.NetworkOperationResult
import id.flexi.kasir.domain.usecase.KeluarAkun
import id.flexi.kasir.domain.usecase.KirimUlangVerifikasi
import id.flexi.kasir.domain.usecase.LoginUser
import id.flexi.kasir.domain.usecase.LupaPassword
import id.flexi.kasir.domain.usecase.RegisterAkun
import id.flexi.kasir.domain.usecase.ResetPassword
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
    val passwordBaru: String = "",
    val konfirmasiPassword: String = "",
    val sedangMemuat: Boolean = false,
    val pesanError: String? = null,
    val pesanInfo: String? = null,
) {
    enum class Mode {
        Login,
        Register,
        Verifikasi,
        LupaPassword,
        AturPasswordBaru,
    }
}

/**
 * Menangani form login/registrasi/verifikasi email/lupa password. Setelah
 * berhasil, navigasi diarahkan oleh aliran sesi
 * ([id.flexi.kasir.domain.usecase.AmatiSesi]) di tingkat navigasi.
 */
class AuthViewModel(
    private val loginUser: LoginUser,
    private val registerAkun: RegisterAkun,
    private val verifikasiEmail: VerifikasiEmail,
    private val kirimUlangVerifikasi: KirimUlangVerifikasi,
    private val lupaPassword: LupaPassword,
    private val resetPassword: ResetPassword,
    private val keluarAkun: KeluarAkun,
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    fun gantiMode(mode: AuthUiState.Mode) {
        // Form field lama (password/kode/dll) dibersihkan agar tidak bocor
        // lintas mode (mis. Register → Login masih menampilkan password lama).
        _state.value = _state.value.copy(
            mode = mode,
            email = "",
            password = "",
            namaUsaha = "",
            namaUser = "",
            kodeVerifikasi = "",
            passwordBaru = "",
            konfirmasiPassword = "",
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

    fun perbaruiPasswordBaru(passwordBaru: String) {
        _state.value = _state.value.copy(passwordBaru = passwordBaru, pesanError = null)
    }

    fun perbaruiKonfirmasiPassword(konfirmasi: String) {
        _state.value = _state.value.copy(konfirmasiPassword = konfirmasi, pesanError = null)
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
                AuthUiState.Mode.LupaPassword -> lupaPassword(
                    email = state.email.trim(),
                )
                AuthUiState.Mode.AturPasswordBaru -> {
                    // Defensif: layar ini memakai tombol simpanPasswordBaru()
                    // yang memvalidasi kode & kecocokan kata sandi.
                    if (state.kodeVerifikasi.length == 6 &&
                        state.passwordBaru.length >= 6 &&
                        state.passwordBaru == state.konfirmasiPassword
                    ) {
                        resetPassword(
                            email = state.email.trim(),
                            kode = state.kodeVerifikasi,
                            passwordBaru = state.passwordBaru,
                        )
                    } else {
                        NetworkOperationResult.GagalServer(
                            kode = 400,
                            pesan = "Data reset tidak lengkap. Gunakan tombol Simpan Kata Sandi Baru.",
                        )
                    }
                }
            }
            when (hasil) {
                is NetworkOperationResult.Berhasil -> {
                    if (state.mode == AuthUiState.Mode.LupaPassword) {
                        // Kode reset terkirim: pindah ke layar atur password baru.
                        _state.value = _state.value.copy(
                            mode = AuthUiState.Mode.AturPasswordBaru,
                            kodeVerifikasi = "",
                            passwordBaru = "",
                            konfirmasiPassword = "",
                            sedangMemuat = false,
                            pesanError = null,
                            pesanInfo = "Kode reset terkirim ke email Anda. Periksa kotak masuk (atau spam).",
                        )
                    } else {
                        _state.value = _state.value.copy(sedangMemuat = false)
                    }
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

    /**
     * Simpan password baru dari layar AturPasswordBaru, lalu kembali ke Login.
     */
    fun simpanPasswordBaru() {
        val state = _state.value
        if (state.sedangMemuat) return
        if (state.kodeVerifikasi.length != 6) {
            _state.value = state.copy(pesanError = "Masukkan kode 6 digit dari email.")
            return
        }
        if (state.passwordBaru.length < 6) {
            _state.value = state.copy(pesanError = "Kata sandi baru minimal 6 karakter.")
            return
        }
        if (state.passwordBaru != state.konfirmasiPassword) {
            _state.value = state.copy(pesanError = "Konfirmasi kata sandi tidak sama.")
            return
        }

        _state.value = state.copy(sedangMemuat = true, pesanError = null)
        viewModelScope.launch {
            val hasil = resetPassword(
                email = state.email.trim(),
                kode = state.kodeVerifikasi,
                passwordBaru = state.passwordBaru,
            )
            when (hasil) {
                is NetworkOperationResult.Berhasil -> {
                    _state.value = _state.value.copy(
                        mode = AuthUiState.Mode.Login,
                        password = "",
                        kodeVerifikasi = "",
                        passwordBaru = "",
                        konfirmasiPassword = "",
                        sedangMemuat = false,
                        pesanError = null,
                        pesanInfo = "Kata sandi berhasil direset. Silakan masuk dengan kata sandi baru.",
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

    fun kirimUlangKode() {
        val state = _state.value
        if (state.sedangMemuat) return

        _state.value = state.copy(sedangMemuat = true, pesanError = null)
        viewModelScope.launch {
            val hasil = if (state.mode == AuthUiState.Mode.AturPasswordBaru) {
                lupaPassword(email = state.email.trim())
            } else {
                kirimUlangVerifikasi(email = state.email.trim())
            }
            when (hasil) {
                is NetworkOperationResult.Berhasil -> {
                    _state.value = _state.value.copy(
                        sedangMemuat = false,
                        pesanInfo = if (state.mode == AuthUiState.Mode.AturPasswordBaru) {
                            "Kode reset baru terkirim."
                        } else {
                            "Kode verifikasi baru terkirim."
                        },
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
        if (state.mode == AuthUiState.Mode.LupaPassword) {
            // Cukup email untuk meminta kode reset.
            return null
        }
        if (state.mode == AuthUiState.Mode.Verifikasi ||
            state.mode == AuthUiState.Mode.AturPasswordBaru
        ) {
            // Layar ini tidak memiliki kolom password; validasi kode &
            // kecocokan kata sandi ditangani verifikasiKode()/simpanPasswordBaru().
            return null
        }
        if (password.isBlank()) return "Kata sandi wajib diisi."
        if (password.length < 6) return "Kata sandi minimal 6 karakter."
        return null
    }
}
