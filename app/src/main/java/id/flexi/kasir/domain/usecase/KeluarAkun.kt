package id.flexi.kasir.domain.usecase

import id.flexi.kasir.domain.repository.AuthRepository

class KeluarAkun(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke() {
        authRepository.logout()
    }
}
