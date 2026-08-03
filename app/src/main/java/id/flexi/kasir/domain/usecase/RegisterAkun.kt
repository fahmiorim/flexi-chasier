package id.flexi.kasir.domain.usecase

import id.flexi.kasir.domain.model.AkunUser
import id.flexi.kasir.domain.model.NetworkOperationResult
import id.flexi.kasir.domain.repository.AuthRepository

class RegisterAkun(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(
        namaUsaha: String,
        namaUser: String,
        email: String,
        password: String,
    ): NetworkOperationResult<AkunUser> {
        return authRepository.register(
            namaUsaha = namaUsaha,
            namaUser = namaUser,
            email = email,
            password = password,
        )
    }
}
