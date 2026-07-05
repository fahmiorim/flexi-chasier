package id.cassy.kasir.ranah.kasuspenggunaan

import id.cassy.kasir.ranah.model.Meja
import id.cassy.kasir.ranah.repositori.RepositoriMeja

class SimpanMeja(
    private val repositoriMeja: RepositoriMeja,
) {
    suspend operator fun invoke(meja: Meja) {
        repositoriMeja.simpanMeja(meja)
    }
}
