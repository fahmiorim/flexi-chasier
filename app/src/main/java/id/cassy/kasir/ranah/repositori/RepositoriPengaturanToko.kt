package id.cassy.kasir.ranah.repositori

import id.cassy.kasir.ranah.model.PengaturanToko
import kotlinx.coroutines.flow.Flow

interface RepositoriPengaturanToko {
    fun ambilPengaturan(): Flow<PengaturanToko>
    suspend fun simpanPengaturan(pengaturan: PengaturanToko)
}
