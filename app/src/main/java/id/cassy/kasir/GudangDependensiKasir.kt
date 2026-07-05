package id.cassy.kasir

import android.content.Context
import androidx.room.Room
import id.cassy.kasir.data.jaringan.konfigurasi.KonfigurasiJaringanKasir
import id.cassy.kasir.data.jaringan.konfigurasi.PenyediaJaringanKasir
import id.cassy.kasir.data.jaringan.layanan.LayananProdukJaringan
import id.cassy.kasir.data.lokal.basisdata.BasisDataCassyKasir
import id.cassy.kasir.data.lokal.basisdata.MigrasiBasisDataKasir
import id.cassy.kasir.data.lokal.preferensi.RepositoriPengaturanTokoDataStore
import id.cassy.kasir.data.lokal.preferensi.RepositoriPreferensiTokoDataStore
import id.cassy.kasir.data.lokal.repositori.RepositoriMejaLokal
import id.cassy.kasir.data.lokal.repositori.RepositoriTransaksiLokal
import id.cassy.kasir.data.repositori.RepositoriProdukLokalRemote
import id.cassy.kasir.ranah.kasuspenggunaan.AmbilDaftarMeja
import id.cassy.kasir.ranah.kasuspenggunaan.AmatiPesananPending
import id.cassy.kasir.ranah.kasuspenggunaan.AmatiPreferensiToko
import id.cassy.kasir.ranah.kasuspenggunaan.AmatiProdukBerdasarkanIdentitas
import id.cassy.kasir.ranah.kasuspenggunaan.AmatiRiwayatTransaksi
import id.cassy.kasir.ranah.kasuspenggunaan.AmatiTransaksiBerdasarkanIdentitas
import id.cassy.kasir.ranah.kasuspenggunaan.AmbilPengaturanToko
import id.cassy.kasir.ranah.kasuspenggunaan.BayarPesananPending
import id.cassy.kasir.ranah.kasuspenggunaan.HapusMeja
import id.cassy.kasir.ranah.kasuspenggunaan.HapusPesananPending
import id.cassy.kasir.ranah.kasuspenggunaan.HapusProduk
import id.cassy.kasir.ranah.kasuspenggunaan.LanjutkanPesananPending
import id.cassy.kasir.ranah.kasuspenggunaan.MuatKatalogProduk
import id.cassy.kasir.ranah.kasuspenggunaan.SelesaikanCheckoutLokalKasir
import id.cassy.kasir.ranah.kasuspenggunaan.SimpanMeja
import id.cassy.kasir.ranah.kasuspenggunaan.SimpanPreferensiToko
import id.cassy.kasir.ranah.kasuspenggunaan.SimpanPengaturanToko
import id.cassy.kasir.ranah.kasuspenggunaan.SimpanProduk
import id.cassy.kasir.ranah.repositori.RepositoriMeja
import id.cassy.kasir.ranah.repositori.RepositoriPengaturanToko
import id.cassy.kasir.ranah.repositori.RepositoriPreferensiToko
import id.cassy.kasir.ranah.repositori.RepositoriProduk
import id.cassy.kasir.ranah.repositori.RepositoriTransaksi

/**
 * Kontainer dependensi manual (Service Locator) untuk aplikasi Cassy Kasir.
 * Mengelola siklus hidup singleton basis data, repositori, kasus penggunaan, dan layanan jaringan.
 *
 * @param konteks Konteks aplikasi untuk inisialisasi Room dan sumber daya lainnya.
 */
class GudangDependensiKasir(
    konteks: Context,
) {

    /**
     * Instansi tunggal (singleton) untuk basis data Room.
     */
    private val basisData: BasisDataCassyKasir by lazy {
        Room.databaseBuilder(
            konteks.applicationContext,
            BasisDataCassyKasir::class.java,
            "kasir.db",
        )
            .addMigrations(
                MigrasiBasisDataKasir.DARI_1_KE_2,
                MigrasiBasisDataKasir.DARI_2_KE_3,
                MigrasiBasisDataKasir.DARI_3_KE_4,
                MigrasiBasisDataKasir.DARI_4_KE_5,
                MigrasiBasisDataKasir.DARI_5_KE_6,
                MigrasiBasisDataKasir.DARI_6_KE_7,
                MigrasiBasisDataKasir.DARI_7_KE_8,
            )
            .build()
    }

    /**
     * Repositori transaksi sebagai sumber data tunggal untuk transaksi lokal.
     *
     * Kontainer menyimpan implementasi konkret, sedangkan use case menerima kontrak
     * dari layer ranah.
     */
    private val repositoriTransaksi: RepositoriTransaksi by lazy {
        RepositoriTransaksiLokal(basisData)
    }

    /**
     * Repositori produk yang menggabungkan sumber data lokal dan jaringan.
     */
    private val repositoriProduk: RepositoriProduk by lazy {
        RepositoriProdukLokalRemote(
            basisData = basisData,
            layananJaringan = layananProdukJaringan,
        )
    }

    /**
     * Repositori preferensi toko berbasis DataStore Preferences.
     */
    private val repositoriPreferensiToko: RepositoriPreferensiToko by lazy {
        RepositoriPreferensiTokoDataStore(konteks.applicationContext)
    }

    private val repositoriPengaturanToko: RepositoriPengaturanToko by lazy {
        RepositoriPengaturanTokoDataStore(konteks.applicationContext)
    }

    private val repositoriMeja: RepositoriMeja by lazy {
        RepositoriMejaLokal(basisData)
    }

    /**
     * Layanan API produk untuk komunikasi jaringan.
     *
     * Dipakai oleh repositori produk untuk sinkronisasi katalog secara local-first.
     * UI tetap membaca katalog dari Room melalui Flow.
     */
    val layananProdukJaringan: LayananProdukJaringan by lazy {
        PenyediaJaringanKasir.buatLayananProdukJaringan(
            alamatDasarApi = KonfigurasiJaringanKasir.alamatDasarApi,
            modeDebug = BuildConfig.DEBUG,
        )
    }

    /**
     * Kasus penggunaan untuk memuat katalog produk dengan skema local-first.
     */
    val muatKatalogProduk: MuatKatalogProduk by lazy {
        MuatKatalogProduk(repositoriProduk)
    }

    /**
     * Kasus penggunaan untuk mengamati detail produk dari sumber lokal.
     */
    val amatiProdukBerdasarkanIdentitas: AmatiProdukBerdasarkanIdentitas by lazy {
        AmatiProdukBerdasarkanIdentitas(repositoriProduk)
    }

    /**
     * Kasus penggunaan untuk menyelesaikan proses checkout di tingkat lokal.
     */
    val selesaikanCheckoutLokalKasir: SelesaikanCheckoutLokalKasir by lazy {
        SelesaikanCheckoutLokalKasir(repositoriTransaksi)
    }

    /**
     * Kasus penggunaan untuk mengamati aliran data riwayat transaksi.
     */
    val amatiRiwayatTransaksi: AmatiRiwayatTransaksi by lazy {
        AmatiRiwayatTransaksi(repositoriTransaksi)
    }

    /**
     * Kasus penggunaan untuk mengamati detail transaksi tunggal berdasarkan identitas.
     */
    val amatiTransaksiBerdasarkanIdentitas: AmatiTransaksiBerdasarkanIdentitas by lazy {
        AmatiTransaksiBerdasarkanIdentitas(repositoriTransaksi)
    }

    /**
     * Kasus penggunaan untuk mengamati preferensi toko secara reaktif.
     */
    val amatiPreferensiToko: AmatiPreferensiToko by lazy {
        AmatiPreferensiToko(repositoriPreferensiToko)
    }

    /**
     * Kasus penggunaan untuk menyimpan preferensi toko.
     */
    val simpanPreferensiToko: SimpanPreferensiToko by lazy {
        SimpanPreferensiToko(repositoriPreferensiToko)
    }

    /**
     * Kasus penggunaan untuk menyimpan atau memperbarui produk.
     */
    val simpanProduk: SimpanProduk by lazy {
        SimpanProduk(repositoriProduk)
    }

    /**
     * Kasus penggunaan untuk menghapus produk dari katalog.
     */
    val hapusProduk: HapusProduk by lazy {
        HapusProduk(repositoriProduk)
    }

    val ambilPengaturanToko: AmbilPengaturanToko by lazy {
        AmbilPengaturanToko(repositoriPengaturanToko)
    }

    val simpanPengaturanToko: SimpanPengaturanToko by lazy {
        SimpanPengaturanToko(repositoriPengaturanToko)
    }

    val amatiPesananPending: AmatiPesananPending by lazy {
        AmatiPesananPending(repositoriTransaksi)
    }

    val lanjutkanPesananPending: LanjutkanPesananPending by lazy {
        LanjutkanPesananPending(repositoriTransaksi)
    }

    val bayarPesananPending: BayarPesananPending by lazy {
        BayarPesananPending(repositoriTransaksi)
    }

    val hapusPesananPending: HapusPesananPending by lazy {
        HapusPesananPending(repositoriTransaksi)
    }

    val ambilDaftarMeja: AmbilDaftarMeja by lazy {
        AmbilDaftarMeja(repositoriMeja)
    }

    val simpanMeja: SimpanMeja by lazy {
        SimpanMeja(repositoriMeja)
    }

    val hapusMeja: HapusMeja by lazy {
        HapusMeja(repositoriMeja)
    }
}
