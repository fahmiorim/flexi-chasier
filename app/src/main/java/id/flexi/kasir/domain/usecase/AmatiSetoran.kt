package id.flexi.kasir.domain.usecase

import id.flexi.kasir.domain.model.Setoran
import id.flexi.kasir.domain.repository.CashRepository
import kotlinx.coroutines.flow.Flow

class AmatiSetoran(
    private val cashRepository: CashRepository,
) {
    operator fun invoke(): Flow<List<Setoran>> {
        return cashRepository.amatiSetoran()
    }
}
