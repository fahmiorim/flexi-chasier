package id.cassy.kasir.ranah.kasuspenggunaan

import id.cassy.kasir.ranah.model.ItemKeranjang
import id.cassy.kasir.ranah.model.Produk
import id.cassy.kasir.ranah.repositori.RepositoriTransaksi

class LanjutkanPesananPending(
    private val repositoriTransaksi: RepositoriTransaksi,
) {
    suspend fun eksekusi(
        identitasTransaksi: String,
        daftarProdukSaatIni: List<Produk>,
    ): List<ItemKeranjang> {
        val transaksi = repositoriTransaksi.ambilTransaksiBerdasarkanIdentitas(identitasTransaksi)
            ?: throw IllegalArgumentException("Pesanan tidak ditemukan.")

        val produkSaatIni = daftarProdukSaatIni.associateBy { it.id }

        val daftarItemBaru = transaksi.daftarItemKeranjang.mapNotNull { item ->
            val produkSaatIniItem = produkSaatIni[item.produk.id] ?: return@mapNotNull null
            item.copy(produk = produkSaatIniItem)
        }

        if (daftarItemBaru.isEmpty()) {
            throw IllegalArgumentException("Tidak ada produk yang tersedia untuk dilanjutkan.")
        }

        repositoriTransaksi.hapusTransaksiDanKembalikanStok(identitasTransaksi)

        return daftarItemBaru
    }
}
