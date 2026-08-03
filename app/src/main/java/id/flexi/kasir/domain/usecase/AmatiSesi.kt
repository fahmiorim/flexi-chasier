package id.flexi.kasir.domain.usecase

import id.flexi.kasir.domain.model.AkunUser
import id.flexi.kasir.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

class AmatiSesi(
    private val authRepository: AuthRepository,
) {
    operator fun invoke(): Flow<AkunUser?> {
        return authRepository.amatiSesi()
    }
}
