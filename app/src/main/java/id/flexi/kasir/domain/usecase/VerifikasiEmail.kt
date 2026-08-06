package id.flexi.kasir.domain.usecase

import id.flexi.kasir.domain.model.NetworkOperationResult
import id.flexi.kasir.domain.repository.AuthRepository

class VerifikasiEmail(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(
        email: String,
        kode: String,
    ): NetworkOperationResult<Unit> {
        return authRepository.verifikasiEmail(email = email, kode = kode)
    }
}
