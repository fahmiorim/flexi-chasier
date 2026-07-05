package id.cassy.kasir.ranah.kasuspenggunaan

import id.cassy.kasir.ranah.repositori.RepositoriTransaksi

class HapusPesananPending(
    private val repositoriTransaksi: RepositoriTransaksi,
) {
    suspend fun eksekusi(
        identitasTransaksi: String,
    ) {
        repositoriTransaksi.hapusTransaksiDanKembalikanStok(identitasTransaksi)
    }
}
