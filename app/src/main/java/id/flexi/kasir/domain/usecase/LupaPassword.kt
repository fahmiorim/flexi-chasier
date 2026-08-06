package id.flexi.kasir.domain.usecase

import id.flexi.kasir.domain.model.NetworkOperationResult
import id.flexi.kasir.domain.repository.AuthRepository

/**
 * Minta kode reset password ke email terdaftar.
 */
class LupaPassword(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(
        email: String,
    ): NetworkOperationResult<Unit> {
        return authRepository.lupaPassword(email = email)
    }
}
