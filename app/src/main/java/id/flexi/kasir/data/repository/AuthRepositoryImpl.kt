package id.flexi.kasir.data.repository

import id.flexi.kasir.data.auth.SesiStore
import id.flexi.kasir.data.auth.SesiTersimpan
import id.flexi.kasir.data.auth.GeraiTersimpan
import id.flexi.kasir.data.auth.TokenStore
import id.flexi.kasir.data.network.model.ApiErrorResponse
import id.flexi.kasir.data.network.model.AuthNetworkResponse
import id.flexi.kasir.data.network.model.KirimUlangVerifikasiRequest
import id.flexi.kasir.data.network.model.LoginRequest
import id.flexi.kasir.data.network.model.LogoutRequest
import id.flexi.kasir.data.network.model.LupaPasswordRequest
import id.flexi.kasir.data.network.model.ResetPasswordRequest
import id.flexi.kasir.data.network.model.RefreshRequest
import id.flexi.kasir.data.network.model.RegisterRequest
import id.flexi.kasir.data.network.model.VerifikasiEmailRequest
import id.flexi.kasir.data.network.service.AuthNetworkService
import id.flexi.kasir.domain.model.AkunUser
import id.flexi.kasir.domain.model.GeraiSederhana
import id.flexi.kasir.domain.model.NetworkOperationResult
import id.flexi.kasir.domain.model.PeranAkun
import id.flexi.kasir.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException

/**
 * Implementasi [AuthRepository] yang menggabungkan jaringan (backend SaaS),
 * penyimpanan token terenkripsi, dan sesi di DataStore.
 */
class AuthRepositoryImpl(
    private val layananJaringan: AuthNetworkService,
    private val tokenStore: TokenStore,
    private val sesiStore: SesiStore,
) : AuthRepository {

    override fun amatiSesi(): Flow<AkunUser?> {
        return sesiStore.amatiSesi().map { tersimpan ->
            tersimpan?.keDomain()
        }
    }

    override suspend fun login(
        email: String,
        password: String,
    ): NetworkOperationResult<AkunUser> {
        return cobaAutentikasi {
            layananJaringan.login(LoginRequest(email = email, password = password))
        }
    }

    override suspend fun register(
        namaUsaha: String,
        namaUser: String,
        email: String,
        password: String,
    ): NetworkOperationResult<AkunUser> {
        return try {
            val respons = layananJaringan.register(
                RegisterRequest(
                    namaUsaha = namaUsaha,
                    namaUser = namaUser,
                    email = email,
                    password = password,
                ),
            )
            if (respons.perluVerifikasiEmail) {
                NetworkOperationResult.PerluVerifikasiEmail(
                    email = respons.email ?: email,
                )
            } else {
                NetworkOperationResult.GagalServer(
                    kode = 500,
                    pesan = "Respons registrasi tidak dikenali. Hubungi bantuan teknis.",
                )
            }
        } catch (kesalahanJaringan: IOException) {
            NetworkOperationResult.GagalJaringan(
                pesan = "Koneksi internet bermasalah. Pastikan server aktif dan coba lagi.",
            )
        } catch (kesalahanHttp: HttpException) {
            NetworkOperationResult.GagalServer(
                kode = kesalahanHttp.code(),
                pesan = pesanDariHttp(kesalahanHttp),
            )
        } catch (kesalahanLain: Exception) {
            NetworkOperationResult.GagalServer(
                kode = 500,
                pesan = kesalahanLain.message
                    ?: "Terjadi kesalahan tidak terduga. Coba lagi?",
            )
        }
    }

    override suspend fun verifikasiEmail(
        email: String,
        kode: String,
    ): NetworkOperationResult<Unit> {
        return cobaOperasiUnit {
            layananJaringan.verifikasiEmail(
                VerifikasiEmailRequest(email = email, kode = kode),
            )
        }
    }

    override suspend fun kirimUlangVerifikasi(email: String): NetworkOperationResult<Unit> {
        return cobaOperasiUnit {
            layananJaringan.kirimUlangVerifikasi(
                KirimUlangVerifikasiRequest(email = email),
            )
        }
    }

    override suspend fun lupaPassword(email: String): NetworkOperationResult<Unit> {
        return cobaOperasiUnit {
            layananJaringan.lupaPassword(
                LupaPasswordRequest(email = email),
            )
        }
    }

    override suspend fun resetPassword(
        email: String,
        kode: String,
        passwordBaru: String,
    ): NetworkOperationResult<Unit> {
        return cobaOperasiUnit {
            layananJaringan.resetPassword(
                ResetPasswordRequest(
                    email = email,
                    kode = kode,
                    passwordBaru = passwordBaru,
                ),
            )
        }
    }

    override suspend fun pilihGerai(geraiId: String) {
        val sesi = sesiStore.amatiSesi().first() ?: return
        if (sesi.daftarGerai.none { it.id == geraiId }) return
        sesiStore.simpan(sesi.copy(geraiAktifId = geraiId))
    }

    override suspend fun logout() {
        runCatching {
            tokenStore.refreshToken?.let { refreshToken ->
                layananJaringan.logout(LogoutRequest(refreshToken))
            }
        }
        tokenStore.hapus()
        sesiStore.hapus()
    }

    private suspend fun <T> cobaOperasiUnit(
        operasi: suspend () -> T,
    ): NetworkOperationResult<Unit> {
        return try {
            operasi()
            NetworkOperationResult.Berhasil(Unit)
        } catch (kesalahanJaringan: IOException) {
            NetworkOperationResult.GagalJaringan(
                pesan = "Koneksi internet bermasalah. Pastikan server aktif dan coba lagi.",
            )
        } catch (kesalahanHttp: HttpException) {
            NetworkOperationResult.GagalServer(
                kode = kesalahanHttp.code(),
                pesan = pesanDariHttp(kesalahanHttp),
            )
        } catch (kesalahanLain: Exception) {
            NetworkOperationResult.GagalServer(
                kode = 500,
                pesan = kesalahanLain.message
                    ?: "Terjadi kesalahan tidak terduga. Coba lagi?",
            )
        }
    }

    private suspend fun cobaAutentikasi(
        operasi: suspend () -> AuthNetworkResponse,
    ): NetworkOperationResult<AkunUser> {
        return try {
            val respons = operasi()
            tokenStore.simpan(
                aksesToken = respons.accessToken,
                refreshToken = respons.refreshToken,
            )
            val akun = respons.keDomain()
            val geraiAktif = if (akun.daftarGerai.size == 1) {
                akun.daftarGerai.first().id
            } else {
                null
            }
            sesiStore.simpan(
                SesiTersimpan(
                    id = akun.id,
                    nama = akun.nama,
                    email = akun.email,
                    peran = akun.peran.name,
                    daftarGerai = akun.daftarGerai.map { gerai ->
                        GeraiTersimpan(
                            id = gerai.id,
                            nama = gerai.nama,
                            alamat = gerai.alamat,
                            tagline = gerai.tagline,
                            headerStruk = gerai.headerStruk,
                            footerStruk = gerai.footerStruk,
                            ukuranKertas = gerai.ukuranKertas,
                            tarifPajak = gerai.tarifPajak,
                            biayaLayanan = gerai.biayaLayanan,
                            metodePembayaran = gerai.metodePembayaran,
                            printerNama = gerai.printerNama,
                            printerTipe = gerai.printerTipe,
                            autoCetak = gerai.autoCetak,
                            logoUri = gerai.logoUri,
                            telepon = gerai.telepon,
                            emailToko = gerai.emailToko,
                            instagram = gerai.instagram,
                            whatsapp = gerai.whatsapp,
                        )
                    },
                    geraiAktifId = geraiAktif,
                ),
            )
            NetworkOperationResult.Berhasil(akun.copy(geraiAktifId = geraiAktif))
        } catch (kesalahanJaringan: IOException) {
            NetworkOperationResult.GagalJaringan(
                pesan = "Koneksi internet bermasalah. Pastikan server aktif dan coba lagi.",
            )
        } catch (kesalahanHttp: HttpException) {
            NetworkOperationResult.GagalServer(
                kode = kesalahanHttp.code(),
                pesan = pesanDariHttp(kesalahanHttp),
            )
        } catch (_: SerializationException) {
            NetworkOperationResult.GagalServer(
                kode = 500,
                pesan = "Format data dari server tidak sesuai. Hubungi bantuan teknis.",
            )
        } catch (kesalahanLain: Exception) {
            NetworkOperationResult.GagalServer(
                kode = 500,
                pesan = kesalahanLain.message
                    ?: "Terjadi kesalahan tidak terduga. Coba lagi?",
            )
        }
    }

    private fun pesanDariHttp(kesalahan: HttpException): String {
        val mentah = kesalahan.response()?.errorBody()?.string()
        val pesanServer = mentah?.let {
            runCatching {
                jsonPesanGagal.decodeFromString<ApiErrorResponse>(it).error
            }.getOrNull()
        }
        return pesanServer ?: when (kesalahan.code()) {
            401 -> "Email atau kata sandi salah."
            409 -> "Email sudah terdaftar. Gunakan email lain."
            403 -> "Email belum diverifikasi. Periksa kode di email Anda."
            else -> "Server bermasalah (Error ${kesalahan.code()}). Coba beberapa saat lagi."
        }
    }

    private fun AuthNetworkResponse.keDomain(): AkunUser {
        return AkunUser(
            id = user.id,
            nama = user.nama,
            email = user.email,
            peran = if (user.peran == PeranAkun.Kasir.name) {
                PeranAkun.Kasir
            } else {
                PeranAkun.Pemilik
            },
            daftarGerai = gerai.map { g ->
                GeraiSederhana(
                    id = g.id,
                    nama = g.nama,
                    alamat = g.alamat,
                    tagline = g.tagline,
                    headerStruk = g.headerStruk,
                    footerStruk = g.footerStruk,
                    ukuranKertas = g.ukuranKertas,
                    tarifPajak = g.tarifPajak,
                    biayaLayanan = g.biayaLayanan,
                    metodePembayaran = g.metodePembayaran,
                    printerNama = g.printerNama,
                    printerTipe = g.printerTipe,
                    autoCetak = g.autoCetak,
                    logoUri = g.logoUri,
                    telepon = g.telepon,
                    emailToko = g.emailToko,
                    instagram = g.instagram,
                    whatsapp = g.whatsapp,
                )
            },
        )
    }

    private fun SesiTersimpan.keDomain(): AkunUser {
        return AkunUser(
            id = id,
            nama = nama,
            email = email,
            peran = if (peran == PeranAkun.Kasir.name) {
                PeranAkun.Kasir
            } else {
                PeranAkun.Pemilik
            },
            daftarGerai = daftarGerai.map { g ->
                GeraiSederhana(
                    id = g.id,
                    nama = g.nama,
                    alamat = g.alamat,
                    tagline = g.tagline,
                    headerStruk = g.headerStruk,
                    footerStruk = g.footerStruk,
                    ukuranKertas = g.ukuranKertas,
                    tarifPajak = g.tarifPajak,
                    biayaLayanan = g.biayaLayanan,
                    metodePembayaran = g.metodePembayaran,
                    printerNama = g.printerNama,
                    printerTipe = g.printerTipe,
                    autoCetak = g.autoCetak,
                    logoUri = g.logoUri,
                    telepon = g.telepon,
                    emailToko = g.emailToko,
                    instagram = g.instagram,
                    whatsapp = g.whatsapp,
                )
            },
            geraiAktifId = geraiAktifId,
        )
    }

    private companion object {
        val jsonPesanGagal = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
    }
}
