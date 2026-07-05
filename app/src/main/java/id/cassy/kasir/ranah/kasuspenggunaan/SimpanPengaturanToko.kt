package id.cassy.kasir.ranah.kasuspenggunaan

import id.cassy.kasir.ranah.model.PengaturanToko
import id.cassy.kasir.ranah.repositori.RepositoriPengaturanToko

class SimpanPengaturanToko(
    private val repositoriPengaturanToko: RepositoriPengaturanToko,
) {
    suspend operator fun invoke(pengaturan: PengaturanToko) {
        repositoriPengaturanToko.simpanPengaturan(pengaturan)
    }
}
