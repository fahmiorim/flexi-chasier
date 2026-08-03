package id.flexi.kasir.domain.usecase

import id.flexi.kasir.domain.model.StoreSetting
import id.flexi.kasir.domain.repository.RepositoriStoreSetting

class SimpanStoreSetting(
    private val repositoriStoreSetting: RepositoriStoreSetting,
) {
    suspend operator fun invoke(pengaturan: StoreSetting) {
        repositoriStoreSetting.simpanPengaturan(pengaturan)
    }
}
