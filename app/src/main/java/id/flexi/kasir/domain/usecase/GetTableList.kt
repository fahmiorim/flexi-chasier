package id.flexi.kasir.domain.usecase

import id.flexi.kasir.domain.model.Meja
import id.flexi.kasir.domain.repository.TableRepository
import kotlinx.coroutines.flow.Flow

class GetTableList(
    private val TableRepository: TableRepository,
) {
    operator fun invoke(): Flow<List<Meja>> {
        return TableRepository.amatiSemuaMeja()
    }
}
