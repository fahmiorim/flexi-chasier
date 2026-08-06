package id.flexi.kasir.domain.usecase

import id.flexi.kasir.domain.model.NetworkOperationResult
import id.flexi.kasir.domain.repository.AuthRepository

/**
 * Set password baru memakai kode reset 6 digit dari email.
 */
class ResetPassword(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(
        email: String,
        kode: String,
        passwordBaru: String,
    ): NetworkOperationResult<Unit> {
        return authRepository.resetPassword(
            email = email,
            kode = kode,
            passwordBaru = passwordBaru,
        )
    }
}
