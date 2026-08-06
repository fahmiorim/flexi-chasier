package id.flexi.kasir.data.network.config

import id.flexi.kasir.BuildConfig

/**
 * Konfigurasi dasar untuk akses jaringan Flexi Kasir.
 *
 * Alamat dasar API diambil dari BuildConfig agar setiap varian build dapat
 * memakai endpoint berbeda tanpa mengubah source code jaringan.
 *
 * Catatan:
 * - Debug memakai 10.0.2.2 agar emulator Android dapat mengakses localhost mesin host.
 * - Release wajib diganti ke alamat backend produksi saat backend sudah tersedia.
 */
object CashierNetworkConfig {

    /**
     * Alamat dasar API sesuai varian build aktif.
     */
    const val alamatDasarApi: String = BuildConfig.ALAMAT_DASAR_API

    /**
     * Batas waktu koneksi HTTP dalam detik.
     */
    const val batasWaktuKoneksiDetik = 15L

    /**
     * Batas waktu membaca respons HTTP dalam detik.
     */
    const val batasWaktuBacaDetik = 15L

    /**
     * Batas waktu menulis request HTTP dalam detik.
     */
    const val batasWaktuTulisDetik = 15L
}
