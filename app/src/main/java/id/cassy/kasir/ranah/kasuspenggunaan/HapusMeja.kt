package id.cassy.kasir.ranah.kasuspenggunaan

import id.cassy.kasir.ranah.repositori.RepositoriMeja

class HapusMeja(
    private val repositoriMeja: RepositoriMeja,
) {
    suspend operator fun invoke(id: String) {
        repositoriMeja.hapusMeja(id)
    }
}
