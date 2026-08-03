package id.flexi.kasir.domain.usecase

import id.flexi.kasir.domain.model.StoreSetting
import id.flexi.kasir.domain.repository.RepositoriStoreSetting
import kotlinx.coroutines.flow.Flow

class AmbilStoreSetting(
    private val repositoriStoreSetting: RepositoriStoreSetting,
) {
    operator fun invoke(): Flow<StoreSetting> {
        return repositoriStoreSetting.ambilPengaturan()
    }
}
