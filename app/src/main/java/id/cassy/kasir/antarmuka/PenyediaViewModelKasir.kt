package id.cassy.kasir.antarmuka

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import id.cassy.kasir.AplikasiKasir
import id.cassy.kasir.antarmuka.dashboard.LayarDashboardViewModel
import id.cassy.kasir.antarmuka.detail.LayarDetailProdukViewModel
import id.cassy.kasir.antarmuka.meja.LayarPengaturanMejaViewModel
import id.cassy.kasir.antarmuka.pengaturan.LayarPengaturanViewModel
import id.cassy.kasir.antarmuka.kelola.ViewModelFormProduk
import id.cassy.kasir.antarmuka.kelola.ViewModelKelolaProduk
import id.cassy.kasir.antarmuka.riwayat.LayarRiwayatTransaksiViewModel
import id.cassy.kasir.antarmuka.transaksi.LayarDetailTransaksiViewModel
import id.cassy.kasir.antarmuka.utama.LayarUtamaKasirViewModel

/**
 * Penyedia (Factory) untuk membuat instansi ViewModel dengan dependensi yang diperlukan.
 * Menggunakan pola pencarian kontainer melalui objek Application.
 */
object PenyediaViewModelKasir {

    val Factory = viewModelFactory {
        initializer {
            val aplikasi = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AplikasiKasir
            LayarUtamaKasirViewModel(
                muatKatalogProduk = aplikasi.kontainer.muatKatalogProduk,
                selesaikanCheckoutLokalKasir = aplikasi.kontainer.selesaikanCheckoutLokalKasir,
                amatiPreferensiToko = aplikasi.kontainer.amatiPreferensiToko,
                simpanPreferensiToko = aplikasi.kontainer.simpanPreferensiToko,
                ambilPengaturanToko = aplikasi.kontainer.ambilPengaturanToko,
                amatiPesananPending = aplikasi.kontainer.amatiPesananPending,
                lanjutkanPesananPending = aplikasi.kontainer.lanjutkanPesananPending,
                bayarPesananPending = aplikasi.kontainer.bayarPesananPending,
                hapusPesananPending = aplikasi.kontainer.hapusPesananPending,
                ambilDaftarMeja = aplikasi.kontainer.ambilDaftarMeja,
            )
        }

        initializer {
            val aplikasi = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AplikasiKasir
            LayarRiwayatTransaksiViewModel(
                amatiRiwayatTransaksi = aplikasi.kontainer.amatiRiwayatTransaksi,
            )
        }

        initializer {
            val aplikasi = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AplikasiKasir
            LayarDetailTransaksiViewModel(
                amatiTransaksiBerdasarkanIdentitas = aplikasi.kontainer.amatiTransaksiBerdasarkanIdentitas,
                savedStateHandle = createSavedStateHandle(),
            )
        }

        initializer {
            val aplikasi = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AplikasiKasir

            LayarDetailProdukViewModel(
                amatiProdukBerdasarkanIdentitas = aplikasi.kontainer.amatiProdukBerdasarkanIdentitas,
                statusTersimpan = createSavedStateHandle(),
            )
        }

        initializer {
            val aplikasi = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AplikasiKasir
            ViewModelKelolaProduk(
                muatKatalogProduk = aplikasi.kontainer.muatKatalogProduk,
                hapusProduk = aplikasi.kontainer.hapusProduk,
            )
        }

        initializer {
            val aplikasi = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AplikasiKasir
            val handle = createSavedStateHandle()
            val idProduk: String? = handle["identitasProduk"]

            ViewModelFormProduk(
                idProduk = idProduk,
                amatiProduk = aplikasi.kontainer.amatiProdukBerdasarkanIdentitas,
                simpanProduk = aplikasi.kontainer.simpanProduk,
            )
        }

        initializer {
            val aplikasi = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AplikasiKasir
            LayarDashboardViewModel(
                amatiRiwayatTransaksi = aplikasi.kontainer.amatiRiwayatTransaksi,
            )
        }

        initializer {
            val aplikasi = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AplikasiKasir
            LayarPengaturanViewModel(
                ambilPengaturanToko = aplikasi.kontainer.ambilPengaturanToko,
                simpanPengaturanToko = aplikasi.kontainer.simpanPengaturanToko,
            )
        }

        initializer {
            val aplikasi = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AplikasiKasir
            LayarPengaturanMejaViewModel(
                ambilDaftarMeja = aplikasi.kontainer.ambilDaftarMeja,
                simpanMeja = aplikasi.kontainer.simpanMeja,
                hapusMeja = aplikasi.kontainer.hapusMeja,
            )
        }
    }
}
