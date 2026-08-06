package id.flexi.kasir.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import id.flexi.kasir.CashierApp
import id.flexi.kasir.ui.dashboard.DashboardViewModel
import id.flexi.kasir.ui.detail.ProductDetailViewModel
import id.flexi.kasir.ui.table.SettingsScreenMejaViewModel
import id.flexi.kasir.ui.settings.SettingsViewModel
import id.flexi.kasir.ui.manage.ProductFormViewModel
import id.flexi.kasir.ui.manage.ManageProductsViewModel
import id.flexi.kasir.ui.history.TransactionHistoryViewModel
import id.flexi.kasir.ui.transaction.TransactionDetailViewModel
import id.flexi.kasir.ui.cashregister.CashRegisterViewModel
import id.flexi.kasir.ui.main.CashierMainViewModel
import id.flexi.kasir.ui.report.SalesReportViewModel
import id.flexi.kasir.ui.bahan.BahanViewModel
import id.flexi.kasir.ui.bahan.BahanFormViewModel
import id.flexi.kasir.ui.bahan.BahanDetailViewModel
import id.flexi.kasir.ui.resep.ResepViewModel
import id.flexi.kasir.ui.auth.AuthViewModel

/**
 * Penyedia (Factory) untuk membuat instansi ViewModel dengan dependensi yang diperlukan.
 * Menggunakan pola pencarian kontainer melalui objek Application.
 */
object CashierViewModelProvider {

    val Factory = viewModelFactory {
        initializer {
            val aplikasi = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CashierApp
            CashierMainViewModel(
                LoadProductCatalog = aplikasi.kontainer.LoadProductCatalog,
                CompleteLocalCheckout = aplikasi.kontainer.CompleteLocalCheckout,
                amatiStorePreference = aplikasi.kontainer.amatiStorePreference,
                simpanStorePreference = aplikasi.kontainer.simpanStorePreference,
                ambilStoreSetting = aplikasi.kontainer.ambilStoreSetting,
                simpanStoreSetting = aplikasi.kontainer.simpanStoreSetting,
                ObservePendingOrders = aplikasi.kontainer.ObservePendingOrders,
                ResumePendingOrder = aplikasi.kontainer.ResumePendingOrder,
                PayPendingOrder = aplikasi.kontainer.PayPendingOrder,
                DeletePendingOrder = aplikasi.kontainer.DeletePendingOrder,
                GetTableList = aplikasi.kontainer.GetTableList,
                ThermalPrinterManager = aplikasi.kontainer.ThermalPrinterManager,
                updatePopularFavorites = aplikasi.kontainer.UpdatePopularFavorites,
                ObserveProcessingOrders = aplikasi.kontainer.ObserveProcessingOrders,
                SelesaikanTransaction = aplikasi.kontainer.SelesaikanTransaction,
                amatiKasAktif = aplikasi.kontainer.amatiKasAktif,
                seedDemoData = aplikasi.kontainer.seedDemoData,
                sinkronStatusPengamat = aplikasi.kontainer.SinkronStatusPengamat,
            )
        }

        initializer {
            val aplikasi = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CashierApp
            TransactionHistoryViewModel(
                transactionRepository = aplikasi.kontainer.TransactionRepository,
            )
        }

        initializer {
            val aplikasi = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CashierApp
            TransactionDetailViewModel(
                ObserveTransactionById = aplikasi.kontainer.ObserveTransactionById,
                GetTableList = aplikasi.kontainer.GetTableList,
                batalkanTransaction = aplikasi.kontainer.batalkanTransaction,
                savedStateHandle = createSavedStateHandle(),
            )
        }

        initializer {
            val aplikasi = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CashierApp

            ProductDetailViewModel(
                ObserveProductById = aplikasi.kontainer.ObserveProductById,
                AmatiResepByProduk = aplikasi.kontainer.AmatiResepByProduk,
                LoadBahanCatalog = aplikasi.kontainer.LoadBahanCatalog,
                statusTersimpan = createSavedStateHandle(),
            )
        }

        initializer {
            val aplikasi = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CashierApp
            ManageProductsViewModel(
                LoadProductCatalog = aplikasi.kontainer.LoadProductCatalog,
                DeleteProduct = aplikasi.kontainer.DeleteProduct,
                SaveProduct = aplikasi.kontainer.SaveProduct,
                BahanRepository = aplikasi.kontainer.bahanRepository,
                LoadBahanCatalog = aplikasi.kontainer.LoadBahanCatalog,
            )
        }

        initializer {
            val aplikasi = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CashierApp
            val handle = createSavedStateHandle()
            val idProduk: String? = handle["identitasProduk"]

            ProductFormViewModel(
                idProduk = idProduk,
                amatiProduk = aplikasi.kontainer.ObserveProductById,
                SaveProduct = aplikasi.kontainer.SaveProduct,
                muatKatalog = aplikasi.kontainer.LoadProductCatalog,
                deleteProduct = aplikasi.kontainer.DeleteProduct,
            )
        }

        initializer {
            val aplikasi = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CashierApp
            DashboardViewModel(
                transactionRepository = aplikasi.kontainer.TransactionRepository,
                ObservePendingOrders = aplikasi.kontainer.ObservePendingOrders,
                ObserveProcessingOrders = aplikasi.kontainer.ObserveProcessingOrders,
                LoadProductCatalog = aplikasi.kontainer.LoadProductCatalog,
                amatiKasAktif = aplikasi.kontainer.amatiKasAktif,
                amatiMutasiKas = aplikasi.kontainer.amatiMutasiKas,
            )
        }

        initializer {
            val aplikasi = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CashierApp
            SettingsViewModel(
                ambilStoreSetting = aplikasi.kontainer.ambilStoreSetting,
                simpanStoreSetting = aplikasi.kontainer.simpanStoreSetting,
                sinkronStatusPengamat = aplikasi.kontainer.SinkronStatusPengamat,
            )
        }

        initializer {
            val aplikasi = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CashierApp
            SettingsScreenMejaViewModel(
                GetTableList = aplikasi.kontainer.GetTableList,
                SaveTable = aplikasi.kontainer.SaveTable,
                DeleteTable = aplikasi.kontainer.DeleteTable,
                amatiStorePreference = aplikasi.kontainer.amatiStorePreference,
                simpanStorePreference = aplikasi.kontainer.simpanStorePreference,
            )
        }

        initializer {
            val aplikasi = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CashierApp
            SalesReportViewModel(
                observeTransactionHistory = aplikasi.kontainer.ObserveTransactionHistory,
            )
        }

        initializer {
            val aplikasi = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CashierApp
            CashRegisterViewModel(
                bukaKas = aplikasi.kontainer.bukaKas,
                tutupKas = aplikasi.kontainer.tutupKas,
                amatiKasAktif = aplikasi.kontainer.amatiKasAktif,
                amatiSemuaKas = aplikasi.kontainer.amatiSemuaKas,
                catatMutasiKas = aplikasi.kontainer.catatMutasiKas,
                amatiMutasiKas = aplikasi.kontainer.amatiMutasiKas,
                hapusMutasiKas = aplikasi.kontainer.hapusMutasiKas,
                catatSetoran = aplikasi.kontainer.catatSetoran,
                amatiSetoran = aplikasi.kontainer.amatiSetoran,
                hapusSetoran = aplikasi.kontainer.hapusSetoran,
                perbaruiSetoran = aplikasi.kontainer.perbaruiSetoran,
                transactionRepository = aplikasi.kontainer.TransactionRepository,
                cashRepository = aplikasi.kontainer.CashRepository,
            )
        }

        initializer {
            val aplikasi = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CashierApp
            BahanViewModel(
                LoadBahanCatalog = aplikasi.kontainer.LoadBahanCatalog,
                HapusBahan = aplikasi.kontainer.HapusBahan,
            )
        }

        initializer {
            val aplikasi = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CashierApp
            val handle = createSavedStateHandle()
            val idBahan: String? = handle["idBahan"]

            BahanFormViewModel(
                idBahan = idBahan,
                ObserveBahanById = aplikasi.kontainer.ObserveBahanById,
                SimpanBahan = aplikasi.kontainer.SimpanBahan,
            )
        }

        initializer {
            val aplikasi = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CashierApp
            val handle = createSavedStateHandle()
            val idBahan: String = handle["idBahan"] ?: ""

            BahanDetailViewModel(
                idBahan = idBahan,
                ObserveBahanById = aplikasi.kontainer.ObserveBahanById,
                CatatPembelianBahan = aplikasi.kontainer.CatatPembelianBahan,
                AmatiPembelianBahan = aplikasi.kontainer.AmatiPembelianBahan,
                HapusPembelianBahan = aplikasi.kontainer.HapusPembelianBahan,
            )
        }

        initializer {
            val aplikasi = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CashierApp
            AuthViewModel(
                loginUser = aplikasi.kontainer.loginUser,
                registerAkun = aplikasi.kontainer.registerAkun,
                verifikasiEmail = aplikasi.kontainer.verifikasiEmail,
                kirimUlangVerifikasi = aplikasi.kontainer.kirimUlangVerifikasi,
                lupaPassword = aplikasi.kontainer.lupaPassword,
                resetPassword = aplikasi.kontainer.resetPassword,
                keluarAkun = aplikasi.kontainer.keluarAkun,
            )
        }

        initializer {
            val aplikasi = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CashierApp
            ResepViewModel(
                LoadProductCatalog = aplikasi.kontainer.LoadProductCatalog,
                LoadBahanCatalog = aplikasi.kontainer.LoadBahanCatalog,
                SimpanResep = aplikasi.kontainer.SimpanResep,
                HapusResep = aplikasi.kontainer.HapusResep,
                AmatiResepByProduk = aplikasi.kontainer.AmatiResepByProduk,
                BahanRepository = aplikasi.kontainer.bahanRepository,
            )
        }

    }
}
