package id.flexi.kasir.domain.usecase

import id.flexi.kasir.domain.model.CashMutation
import id.flexi.kasir.domain.repository.CashRepository
import kotlinx.coroutines.flow.Flow

class AmatiMutasiKas(
    private val cashRepository: CashRepository,
) {
    operator fun invoke(kasId: String): Flow<List<CashMutation>> {
        return cashRepository.amatiMutasiBerdasarkanKas(kasId)
    }
}
