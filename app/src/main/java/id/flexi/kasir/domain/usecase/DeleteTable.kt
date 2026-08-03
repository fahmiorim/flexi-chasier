package id.flexi.kasir.domain.usecase

import id.flexi.kasir.domain.repository.TableRepository

class DeleteTable(
    private val TableRepository: TableRepository,
) {
    suspend operator fun invoke(id: String) {
        TableRepository.DeleteTable(id)
    }
}
