package id.flexi.kasir.ui.navigation

import androidx.navigation.NavHostController
import kotlinx.serialization.Serializable

/**
 * Kontrak tujuan navigasi type-safe untuk Flexi Cashier.
 *
 * Setiap tujuan layar direpresentasikan sebagai tipe Kotlin, bukan string route.
 * Ini membuat argumen navigasi lebih aman saat compile-time dan lebih mudah
 * dirawat ketika jumlah layar bertambah.
 */
sealed interface CashierNavigationDestination {

    /**
     * Tujuan layar utama kasir.
     */
    @Serializable
    data object KasirUtama : CashierNavigationDestination

    /**
     * Tujuan layar dashboard.
     */
    @Serializable
    data object Dashboard : CashierNavigationDestination

    /**
     * Tujuan layar riwayat transaksi.
     */
    @Serializable
    data object RiwayatTransaction : CashierNavigationDestination

    /**
     * Tujuan layar detail produk.
     *
     * @property identitasProduk Identitas unik produk yang akan dibuka.
     */
    @Serializable
    data class DetailProduk(
        val identitasProduk: String,
    ) : CashierNavigationDestination

    /**
     * Tujuan layar detail Transaction.
     *
     * @property identitasTransaction Identitas unik Transaction yang akan dibuka.
     */
    @Serializable
    data class DetailTransaction(
        val identitasTransaction: String,
    ) : CashierNavigationDestination

    /**
     * Tujuan layar menu kelola produk.
     */
    @Serializable
    data object KelolaProduk : CashierNavigationDestination

    /**
     * Tujuan layar daftar produk (sub-menu dari Kelola Produk).
     */
    @Serializable
    data object DaftarProduk : CashierNavigationDestination

    /**
     * Tujuan layar formulir produk (Tambah/Ubah).
     *
     * @property identitasProduk ID produk untuk mode ubah, null untuk mode tambah.
     */
    @Serializable
    data object Pengaturan : CashierNavigationDestination

    @Serializable
    data class FormProduk(
        val identitasProduk: String? = null,
    ) : CashierNavigationDestination

    /**
     * Tujuan layar pengaturan meja.
     */
    @Serializable
    data object PengaturanMeja : CashierNavigationDestination

    /**
     * Tujuan layar kas (buka/tutup kas + mutasi).
     */
    @Serializable
    data object Kasir : CashierNavigationDestination

    /**
     * Tujuan layar laporan penjualan.
     */
    @Serializable
    data object Laporan : CashierNavigationDestination

    /**
     * Tujuan layar manajemen bahan baku.
     */
    @Serializable
    data object BahanBaku : CashierNavigationDestination

    /**
     * Tujuan layar formulir bahan baku (Tambah/Ubah).
     */
    @Serializable
    data class FormBahan(
        val idBahan: String? = null,
    ) : CashierNavigationDestination

    /**
     * Tujuan layar detail bahan baku.
     */
    @Serializable
    data class DetailBahan(
        val idBahan: String,
    ) : CashierNavigationDestination

    /**
     * Tujuan layar atur resep.
     */
    @Serializable
    data object AturResep : CashierNavigationDestination
}

/**
 * Membuka layar utama kasir.
 */
fun NavHostController.bukaKasirUtama() {
    navigate(CashierNavigationDestination.KasirUtama) {
        launchSingleTop = true
    }
}

/**
 * Membuka layar dashboard.
 */
fun NavHostController.bukaDashboard() {
    navigate(CashierNavigationDestination.Dashboard) {
        launchSingleTop = true
    }
}

/**
 * Membuka layar riwayat transaksi.
 */
fun NavHostController.bukaRiwayatTransaction() {
    navigate(CashierNavigationDestination.RiwayatTransaction) {
        launchSingleTop = true
    }
}

/**
 * Membuka layar detail produk berdasarkan identitas produk.
 */
fun NavHostController.bukaDetailProduk(
    identitasProduk: String,
) {
    navigate(
        CashierNavigationDestination.DetailProduk(
            identitasProduk = identitasProduk,
        ),
    ) {
        launchSingleTop = true
    }
}

/**
 * Membuka layar detail Transaction berdasarkan identitas Transaction.
 */
fun NavHostController.bukaDetailTransaction(
    identitasTransaction: String,
) {
    navigate(
        CashierNavigationDestination.DetailTransaction(
            identitasTransaction = identitasTransaction,
        ),
    ) {
        launchSingleTop = true
    }
}

/**
 * Membuka layar menu kelola produk.
 */
fun NavHostController.bukaKelolaProduk() {
    navigate(CashierNavigationDestination.KelolaProduk) {
        launchSingleTop = true
    }
}

/**
 * Membuka layar daftar produk.
 */
fun NavHostController.bukaDaftarProduk() {
    navigate(CashierNavigationDestination.DaftarProduk) {
        launchSingleTop = true
    }
}

/**
 * Membuka layar formulir produk (Tambah atau Ubah).
 */
fun NavHostController.bukaFormProduk(identitasProduk: String? = null) {
    navigate(CashierNavigationDestination.FormProduk(identitasProduk)) {
        launchSingleTop = true
    }
}

/**
 * Membuka layar pengaturan toko.
 */
fun NavHostController.bukaPengaturan() {
    navigate(CashierNavigationDestination.Pengaturan) {
        launchSingleTop = true
    }
}

fun NavHostController.bukaPengaturanMeja() {
    navigate(CashierNavigationDestination.PengaturanMeja) {
        launchSingleTop = true
    }
}

/**
 * Membuka layar kas (buka/tutup kas + mutasi).
 */
fun NavHostController.bukaKasir() {
    navigate(CashierNavigationDestination.Kasir) {
        launchSingleTop = true
    }
}

/**
 * Membuka layar manajemen bahan baku.
 */
fun NavHostController.bukaBahanBaku() {
    navigate(CashierNavigationDestination.BahanBaku) {
        launchSingleTop = true
    }
}

/**
 * Membuka layar formulir bahan baku (Tambah atau Ubah).
 */
fun NavHostController.bukaFormBahan(idBahan: String? = null) {
    navigate(CashierNavigationDestination.FormBahan(idBahan)) {
        launchSingleTop = true
    }
}

/**
 * Membuka layar detail bahan baku.
 */
fun NavHostController.bukaDetailBahan(idBahan: String) {
    navigate(CashierNavigationDestination.DetailBahan(idBahan)) {
        launchSingleTop = true
    }
}

/**
 * Membuka layar atur resep.
 */
fun NavHostController.bukaAturResep() {
    navigate(CashierNavigationDestination.AturResep) {
        launchSingleTop = true
    }
}


