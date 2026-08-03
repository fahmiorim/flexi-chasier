package id.flexi.kasir.domain.usecase

import id.flexi.kasir.domain.model.CashKas
import id.flexi.kasir.domain.repository.CashRepository
import kotlinx.coroutines.flow.Flow

class AmatiKasAktif(
    private val cashRepository: CashRepository,
) {
    operator fun invoke(): Flow<CashKas?> {
        return cashRepository.amatiKasAktif()
    }
}
