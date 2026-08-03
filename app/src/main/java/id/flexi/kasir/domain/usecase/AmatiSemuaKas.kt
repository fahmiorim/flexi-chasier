package id.flexi.kasir.domain.usecase

import id.flexi.kasir.domain.model.CashKas
import id.flexi.kasir.domain.repository.CashRepository
import kotlinx.coroutines.flow.Flow

class AmatiSemuaKas(
    private val cashRepository: CashRepository,
) {
    operator fun invoke(): Flow<List<CashKas>> {
        return cashRepository.amatiSemuaKas()
    }
}
