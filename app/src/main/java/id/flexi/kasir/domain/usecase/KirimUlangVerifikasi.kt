package id.flexi.kasir.domain.usecase

import id.flexi.kasir.domain.model.NetworkOperationResult
import id.flexi.kasir.domain.repository.AuthRepository

class KirimUlangVerifikasi(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(
        email: String,
    ): NetworkOperationResult<Unit> {
        return authRepository.kirimUlangVerifikasi(email = email)
    }
}
