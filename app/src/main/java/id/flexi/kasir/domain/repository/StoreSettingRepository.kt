package id.flexi.kasir.domain.repository

import id.flexi.kasir.domain.model.StoreSetting
import kotlinx.coroutines.flow.Flow

interface RepositoriStoreSetting {
    fun ambilPengaturan(): Flow<StoreSetting>
    suspend fun simpanPengaturan(pengaturan: StoreSetting)
}
