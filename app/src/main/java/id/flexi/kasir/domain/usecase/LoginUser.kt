package id.flexi.kasir.domain.usecase

import id.flexi.kasir.domain.model.AkunUser
import id.flexi.kasir.domain.model.NetworkOperationResult
import id.flexi.kasir.domain.repository.AuthRepository

class LoginUser(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(
        email: String,
        password: String,
    ): NetworkOperationResult<AkunUser> {
        return authRepository.login(email = email, password = password)
    }
}
