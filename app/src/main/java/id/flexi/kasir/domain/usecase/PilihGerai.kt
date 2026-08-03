package id.flexi.kasir.domain.usecase

import id.flexi.kasir.domain.repository.AuthRepository

class PilihGerai(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(geraiId: String) {
        authRepository.pilihGerai(geraiId)
    }
}
