package id.flexi.kasir.domain.usecase

import id.flexi.kasir.domain.model.Meja
import id.flexi.kasir.domain.repository.TableRepository

class SaveTable(
    private val TableRepository: TableRepository,
) {
    suspend operator fun invoke(meja: Meja) {
        TableRepository.SaveTable(meja)
    }
}
