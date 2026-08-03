package id.flexi.kasir.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.flexi.kasir.domain.util.hitungSubtotalKeranjangUang
import id.flexi.kasir.domain.util.sebagaiRupiah
import id.flexi.kasir.domain.usecase.GetTableList
import id.flexi.kasir.domain.usecase.ObservePendingOrders
import id.flexi.kasir.domain.usecase.ObserveProcessingOrders
import id.flexi.kasir.domain.usecase.SelesaikanTransaction
import id.flexi.kasir.domain.usecase.AmatiStorePreference
import id.flexi.kasir.domain.usecase.AmbilStoreSetting
import id.flexi.kasir.domain.usecase.PayPendingOrder
import id.flexi.kasir.domain.usecase.DeletePendingOrder
import id.flexi.kasir.domain.usecase.RemoveProductFromCart
import id.flexi.kasir.domain.usecase.DecreaseProductInCart
import id.flexi.kasir.domain.usecase.ResumePendingOrder
import id.flexi.kasir.domain.usecase.LoadProductCatalog
import id.flexi.kasir.domain.usecase.CompleteLocalCheckout
import id.flexi.kasir.domain.usecase.SimpanStoreSetting
import id.flexi.kasir.domain.usecase.SimpanStorePreference
import id.flexi.kasir.domain.usecase.AddProductToCart
import id.flexi.kasir.domain.usecase.UpdatePopularFavorites
import id.flexi.kasir.domain.usecase.AmatiKasAktif
import id.flexi.kasir.domain.usecase.SeedDemoData
import id.flexi.kasir.domain.model.TaxRule
import id.flexi.kasir.domain.model.TransactionCostBreakdown
import id.flexi.kasir.print.PrintResult
import id.flexi.kasir.print.ThermalPrinterManager
import id.flexi.kasir.domain.model.NetworkOperationResult
import id.flexi.kasir.domain.model.Meja
import id.flexi.kasir.domain.model.PaymentMethod
import id.flexi.kasir.domain.model.ReceiptPrintFormat
import id.flexi.kasir.domain.model.StoreSetting
import id.flexi.kasir.domain.model.StorePreference
import id.flexi.kasir.domain.model.CartItem
import id.flexi.kasir.domain.model.Produk
import id.flexi.kasir.domain.model.SyncStatus
import id.flexi.kasir.domain.model.TransactionStatus
import id.flexi.kasir.domain.model.Transaction
import id.flexi.kasir.domain.model.OrderType
import id.flexi.kasir.domain.model.Uang
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Pengelola status layar utama kasir dengan pola alur data satu arah (UDF).
 * Mengatur keranjang belanja, proses checkout ke penyimpanan lokal,
 * serta fitur pencarian produk reaktif dengan debounce untuk performa optimal.
 */
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class CashierMainViewModel(
    private val LoadProductCatalog: LoadProductCatalog,
    private val CompleteLocalCheckout: CompleteLocalCheckout,
    private val amatiStorePreference: AmatiStorePreference,
    private val simpanStorePreference: SimpanStorePreference,
    private val ambilStoreSetting: AmbilStoreSetting,
    private val simpanStoreSetting: SimpanStoreSetting,
    private val ObservePendingOrders: ObservePendingOrders,
    private val ResumePendingOrder: ResumePendingOrder,
    private val PayPendingOrder: PayPendingOrder,
    private val DeletePendingOrder: DeletePendingOrder,
    private val GetTableList: GetTableList,
    private val ThermalPrinterManager: ThermalPrinterManager,
    private val AddProductToCart: AddProductToCart = AddProductToCart(),
    private val DecreaseProductInCart: DecreaseProductInCart = DecreaseProductInCart(),
    private val RemoveProductFromCart: RemoveProductFromCart = RemoveProductFromCart(),
    private val bentukModelTampilan: BentukCashierMainUiState = BentukCashierMainUiState(),
    private val updatePopularFavorites: UpdatePopularFavorites,
    private val ObserveProcessingOrders: ObserveProcessingOrders,
    private val SelesaikanTransaction: SelesaikanTransaction,
    private val amatiKasAktif: AmatiKasAktif,
    private val seedDemoData: SeedDemoData,
) : ViewModel() {

    private val daftarProdukPenuh = LoadProductCatalog.eksekusi()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList(),
        )

    private val StorePreference = amatiStorePreference()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = StorePreference(),
        )

    private val StoreSetting = ambilStoreSetting()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = StoreSetting(),
        )

    private val shiftKasAktif = amatiKasAktif()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )

    private val daftarPesananPending = ObservePendingOrders()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList(),
        )

    private val daftarPesananDiproses = ObserveProcessingOrders()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList(),
        )

    private val daftarMeja = GetTableList()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList(),
        )

    private val _TransactionStatus = MutableStateFlow(
        CashierMainTransactionState(),
    )

    private val _statusElemenLayar = MutableStateFlow(
        CashierMainElementState(),
    )

    private var resumeTransactionId: String? = null
    private var shiftKasDimuat = false

    private val _efek = MutableSharedFlow<CashierMainEffect>(
        extraBufferCapacity = 1,
    )
    val efek: SharedFlow<CashierMainEffect> = _efek.asSharedFlow()

    private val _kataKunciPencarian = MutableStateFlow("")
    private val _tabTransaksi = MutableStateFlow(1)
    private val _kategoriTerpilih = MutableStateFlow("")

    private val kataKunciPencarianEfektif =
        _kataKunciPencarian
            .debounce(250)
            .map { kataKunci ->
                kataKunci.trim()
            }
            .distinctUntilChanged()

    private val statusDasarModelTampilan = combine(
        daftarProdukPenuh,
        _TransactionStatus,
        _statusElemenLayar,
    ) { daftarProdukPenuh, TransactionStatus, statusElemenLayar ->
        StatusDasarModelTampilan(
            daftarProdukPenuh = daftarProdukPenuh,
            TransactionStatus = TransactionStatus,
            statusElemenLayar = statusElemenLayar,
        )
    }

    private val statusPencarianModelTampilan = combine(
        _kataKunciPencarian,
        kataKunciPencarianEfektif,
    ) { kataKunciMentah, kataKunciEfektif ->
        StatusPencarianModelTampilan(
            kataKunciMentah = kataKunciMentah,
            kataKunciEfektif = kataKunciEfektif,
        )
    }

    /**
     * Aliran status UI publik yang dirender oleh Compose.
     */
    private val daftarAntrian = combine(
        daftarPesananPending,
        daftarPesananDiproses,
    ) { pending, diproses ->
        (pending + diproses)
            .filter { it.status != TransactionStatus.Pending || it.daftarCartItem.any { item -> !item.apakahSelesai } }
            .sortedBy { it.waktuTransactionEpochMili }
    }

    private val statusPengaturanModelTampilan = combine(
        StorePreference,
        StoreSetting,
        daftarPesananPending,
        daftarAntrian,
        daftarMeja,
    ) { StorePreference, StoreSetting, daftarPesananPending, daftarAntrian, daftarMeja ->
        StatusPengaturanModelTampilan(
            StorePreference = StorePreference,
            StoreSetting = StoreSetting,
            daftarPesananPending = daftarPesananPending,
            daftarPesananDiproses = daftarAntrian,
            daftarMeja = daftarMeja,
        )
    }

    private val statusNavigasiModelTampilan = combine(
        _tabTransaksi,
        _kategoriTerpilih,
    ) { tabTransaksi, kategoriTerpilih ->
        StatusNavigasiModelTampilan(
            tabTransaksi = tabTransaksi,
            kategoriTerpilih = kategoriTerpilih,
        )
    }

    val modelTampilan = combine(
        statusDasarModelTampilan,
        statusPencarianModelTampilan,
        statusPengaturanModelTampilan,
        statusNavigasiModelTampilan,
        shiftKasAktif,
    ) { statusDasar, statusPencarian, statusPengaturan, statusNavigasi, shiftAktif ->
        val perluBukaKas = shiftKasDimuat && statusPengaturan.StoreSetting.manajemenKasAktif && shiftAktif == null
        bentukModelTampilan(
            daftarProdukPenuh = statusDasar.daftarProdukPenuh,
            TransactionStatus = statusDasar.TransactionStatus,
            statusElemenLayar = statusDasar.statusElemenLayar,
            kataKunciMentah = statusPencarian.kataKunciMentah,
            kataKunciEfektif = statusPencarian.kataKunciEfektif,
            StorePreference = statusPengaturan.StorePreference,
            StoreSetting = statusPengaturan.StoreSetting,
            daftarPesananPending = statusPengaturan.daftarPesananPending,
            daftarPesananDiproses = statusPengaturan.daftarPesananDiproses,
            daftarMeja = statusPengaturan.daftarMeja,
            tabTransaksi = statusNavigasi.tabTransaksi,
            kategoriTerpilih = statusNavigasi.kategoriTerpilih,
            apakahPerluBukaKas = perluBukaKas,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = bentukModelTampilan(
            daftarProdukPenuh = emptyList(),
            TransactionStatus = CashierMainTransactionState(),
            statusElemenLayar = CashierMainElementState(),
            kataKunciMentah = "",
            kataKunciEfektif = "",
            StorePreference = StorePreference(),
            apakahPerluBukaKas = false,
        ),
    )

    init {
        pastikanKatalogAwalTersedia()
        pastikanDemoDataTersedia()

        // Auto-pilih kategori pertama saat daftar produk tersedia
        viewModelScope.launch {
            daftarProdukPenuh.collect { produk ->
                if (_tabTransaksi.value == 1 && _kategoriTerpilih.value.isBlank() && produk.isNotEmpty()) {
                    val kategoriPertama = produk.map { it.kategori }
                        .filter { it.isNotBlank() }
                        .distinct()
                        .sorted()
                        .firstOrNull()
                    if (kategoriPertama != null) {
                        _kategoriTerpilih.value = kategoriPertama
                    }
                }
            }
        }

        // Tandai shift kas sudah dimuat untuk menghindari false-positive "Buka kas"
        viewModelScope.launch {
            amatiKasAktif().first {
                shiftKasDimuat = true
                true
            }
        }

        // Auto-favorit: produk paling laris otomatis jadi favorit
        viewModelScope.launch {
            try {
                updatePopularFavorites.eksekusi(StoreSetting.value.jumlahTopFavorit)
            } catch (_: Exception) {
                // Gagal hitung favorit — tidak perlu ganggu pengguna
            }
        }
    }

    /**
     * Titik masuk tunggal untuk semua aksi yang dipicu oleh UI.
     */
    fun tanganiAksi(aksi: CashierMainAction) {
        when (aksi) {
            is CashierMainAction.UbahKataKunciPencarian -> perbaruiPencarian(aksi.kataKunciBaru)
            CashierMainAction.UbahVisibilitasRingkasanPayment -> alihkanVisibilitasPayment()
            is CashierMainAction.AddProductToCart -> AddProductToCart(aksi.produkId, aksi.varianNama)
            is CashierMainAction.DecreaseProductInCart -> DecreaseProductInCart(aksi.produkId, aksi.varianNama)
            is CashierMainAction.RemoveProductFromCart -> RemoveProductFromCart(aksi.produkId, aksi.varianNama)
            is CashierMainAction.BukaDialogCheckout -> bukaDialogCheckout(aksi.modeSimpan)
            CashierMainAction.BatalkanKonfirmasiCheckout -> batalkanKonfirmasiCheckout()
            CashierMainAction.SimpanPesanan -> simpanPesanan()
            CashierMainAction.SimpanDanCetakPesanan -> simpanDanCetakPesanan()
            CashierMainAction.BayarSekarang -> bayarSekarang()
            is CashierMainAction.BayarSekarangTunai -> bayarSekarangTunai(aksi.nominalUang)
            CashierMainAction.TutupStatusHasilCheckout -> tutupStatusHasilCheckout()
            CashierMainAction.ResetPencarian -> resetPencarian()
            CashierMainAction.SinkronkanKatalogProduk -> sinkronkanKatalogProduk()
            is CashierMainAction.UbahCatatanCheckout -> perbaruiCatatanCheckout(aksi.catatan)
            is CashierMainAction.UbahPaymentMethod -> perbaruiPaymentMethod(aksi.paymentMethod)
            is CashierMainAction.UbahOrderType -> perbaruiOrderType(aksi.orderType)
            CashierMainAction.BukaPendingOrdersPanel -> bukaPendingOrdersPanel()
            CashierMainAction.TutupPendingOrdersPanel -> tutupPendingOrdersPanel()
            CashierMainAction.BukaAntrianPanel -> bukaAntrianPanel()
            CashierMainAction.TutupAntrianPanel -> tutupAntrianPanel()
            is CashierMainAction.SelesaikanAntrian -> selesaikanAntrian(aksi.identitasTransaction)
            is CashierMainAction.GabungDanBayarBill -> gabungDanBayarBill(aksi.mejaId, aksi.paymentMethod, aksi.uangDibayar)
            is CashierMainAction.GabungDanSimpanBill -> gabungDanSimpanBill(aksi.mejaId)
            is CashierMainAction.ResumePendingOrder -> ResumePendingOrder(aksi.identitasTransaction)
            is CashierMainAction.PayPendingOrder -> PayPendingOrder(aksi.identitasTransaction, aksi.paymentMethod, aksi.uangDibayar)
            is CashierMainAction.DeletePendingOrder -> DeletePendingOrder(aksi.identitasTransaction)
            is CashierMainAction.PilihMeja -> pilihMeja(aksi.mejaId)
            CashierMainAction.AlihkanCatalogDisplay -> alihkanCatalogDisplay()
            is CashierMainAction.UbahTabTransaksi -> {
                _tabTransaksi.value = aksi.tab
                if (aksi.tab == 1) {
                    // Auto-pilih kategori pertama saat masuk tab Produk
                    val produk = daftarProdukPenuh.value
                    val kategoriPertama = produk.map { it.kategori }
                        .filter { it.isNotBlank() }
                        .distinct()
                        .sorted()
                        .firstOrNull()
                    _kategoriTerpilih.value = kategoriPertama ?: ""
                } else {
                    _kategoriTerpilih.value = ""
                }
            }
            is CashierMainAction.UbahKategoriTerpilih -> {
                _kategoriTerpilih.value = aksi.kategori
            }
            is CashierMainAction.TambahItemManual -> {
                tambahItemManual(aksi.nama, aksi.harga)
            }
            is CashierMainAction.SplitBill -> {
                konfirmasiSplitBill(aksi.daftarIdProduk, aksi.paymentMethod, aksi.uangTunai)
            }
            is CashierMainAction.PilihVarianProduk -> {
                pilihVarianProduk(aksi.produkId, aksi.varian)
            }
            CashierMainAction.BatalkanPilihVarian -> {
                _statusElemenLayar.update { it.copy(produkUntukPilihVarian = null) }
            }
        }
    }

    private fun perbaruiPencarian(kataKunciBaru: String) {
        _kataKunciPencarian.value = kataKunciBaru
    }

    private fun pastikanKatalogAwalTersedia() {
        viewModelScope.launch {
            try {
                LoadProductCatalog.pastikanKatalogAwalTersedia()
            } catch (_: Exception) {
                kirimPesanSingkat("Katalog awal belum siap. Coba buka ulang aplikasi.")
            }
        }
    }

    private fun pastikanDemoDataTersedia() {
        viewModelScope.launch {
            try {
                seedDemoData()
            } catch (e: Exception) {
                android.util.Log.w("CashierMainVM", "Demo data seeding skipped: ${e.message}")
            }
        }
    }

    private fun resetPencarian() {
        _kataKunciPencarian.value = ""
    }

    private fun sinkronkanKatalogProduk() {
        _TransactionStatus.update { statusLama ->
            statusLama.copy(
                syncStatus = SyncStatus.Syncing,
            )
        }

        viewModelScope.launch {
            when (val hasilSinkronisasi = LoadProductCatalog.sinkronkan()) {
                is NetworkOperationResult.Berhasil -> {
                    _TransactionStatus.update { statusLama ->
                        statusLama.copy(
                            syncStatus = SyncStatus.Synced,
                        )
                    }

                    simpanStorePreference.simpanSinkronisasiKatalogBerhasil(
                        waktuEpochMili = System.currentTimeMillis(),
                    )

                    kirimPesanSingkat("Katalog produk berhasil diperbarui.")
                }

                is NetworkOperationResult.GagalJaringan -> {
                    _TransactionStatus.update { statusLama ->
                        statusLama.copy(
                            syncStatus = SyncStatus.Gagal(
                                pesan = hasilSinkronisasi.pesan,
                            ),
                        )
                    }

                    simpanStorePreference.simpanSinkronisasiKatalogGagal(
                        pesan = hasilSinkronisasi.pesan,
                    )

                    kirimPesanSingkat(hasilSinkronisasi.pesan)
                }

                is NetworkOperationResult.GagalServer -> {
                    _TransactionStatus.update { statusLama ->
                        statusLama.copy(
                            syncStatus = SyncStatus.Gagal(
                                pesan = hasilSinkronisasi.pesan,
                            ),
                        )
                    }

                    simpanStorePreference.simpanSinkronisasiKatalogGagal(
                        pesan = hasilSinkronisasi.pesan,
                    )

                    kirimPesanSingkat(hasilSinkronisasi.pesan)
                }

                is NetworkOperationResult.FallbackLokal -> {
                    _TransactionStatus.update { statusLama ->
                        statusLama.copy(
                            syncStatus = SyncStatus.Gagal(
                                pesan = hasilSinkronisasi.alasanGagal,
                            ),
                        )
                    }

                    simpanStorePreference.simpanSinkronisasiKatalogGagal(
                        pesan = hasilSinkronisasi.alasanGagal,
                    )

                    kirimPesanSingkat(hasilSinkronisasi.alasanGagal)
                }
            }
        }
    }

    private fun alihkanVisibilitasPayment() {
        _statusElemenLayar.update { statusLama ->
            statusLama.copy(
                apakahRingkasanPaymentTampil = !statusLama.apakahRingkasanPaymentTampil,
            )
        }
    }

    private fun AddProductToCart(produkId: String, varianNama: String? = null) {
        val produk = daftarProdukPenuh.value.firstOrNull { it.id == produkId } ?: return

        // Jika datang dari katalog (tanpa varianNama) dan produk punya varian,
        // tampilkan dialog pilih varian. Jika dari cart (dengan varianNama),
        // langsung increment tanpa dialog.
        if (varianNama == null && produk.varian.isNotEmpty()) {
            _statusElemenLayar.update { it.copy(produkUntukPilihVarian = produk) }
            return
        }

        // Cari varian dari nama (untuk increment dari cart)
        val varian = varianNama?.let { nama ->
            produk.varian.firstOrNull { it.nama == nama }
        }

        var apakahStokPenuh = false

        _TransactionStatus.update { statusLama ->
            val daftarLama = statusLama.daftarCartItem
            val daftarBaru = AddProductToCart(
                daftarCartItem = daftarLama,
                produk = produk,
                varian = varian,
            )

            apakahStokPenuh = daftarBaru == daftarLama

            statusLama.copy(
                daftarCartItem = daftarBaru,
                syncStatus = SyncStatus.LocalChanges,
            )
        }

        _statusElemenLayar.update { statusLama ->
            statusLama.copy(
                statusHasilCheckout = CheckoutResultStatus(),
            )
        }

        if (apakahStokPenuh) {
            kirimPesanSingkat("Stok produk sudah mencapai batas maksimum.")
        }
    }

    private fun pilihVarianProduk(produkId: String, varian: id.flexi.kasir.domain.model.Varian) {
        val produk = daftarProdukPenuh.value.firstOrNull { it.id == produkId } ?: return

        var apakahStokPenuh = false

        _TransactionStatus.update { statusLama ->
            val daftarLama = statusLama.daftarCartItem
            val daftarBaru = AddProductToCart(
                daftarCartItem = daftarLama,
                produk = produk,
                varian = varian,
            )

            apakahStokPenuh = daftarBaru == daftarLama

            statusLama.copy(
                daftarCartItem = daftarBaru,
                syncStatus = SyncStatus.LocalChanges,
            )
        }

        _statusElemenLayar.update { it.copy(produkUntukPilihVarian = null) }

        if (apakahStokPenuh) {
            kirimPesanSingkat("Stok produk sudah mencapai batas maksimum.")
        }
    }

    private fun DecreaseProductInCart(produkId: String, varianNama: String? = null) {
        _TransactionStatus.update { statusLama ->
            statusLama.copy(
                daftarCartItem = DecreaseProductInCart(
                    daftarCartItem = statusLama.daftarCartItem,
                    produkId = produkId,
                    varianNama = varianNama,
                ),
                syncStatus = SyncStatus.LocalChanges,
            )
        }

        resetStatusHasil()
    }

    private fun RemoveProductFromCart(produkId: String, varianNama: String? = null) {
        _TransactionStatus.update { statusLama ->
            statusLama.copy(
                daftarCartItem = RemoveProductFromCart(
                    daftarCartItem = statusLama.daftarCartItem,
                    produkId = produkId,
                    varianNama = varianNama,
                ),
                syncStatus = SyncStatus.LocalChanges,
            )
        }

        resetStatusHasil()
    }

    private fun bukaDialogCheckout(modeSimpan: Boolean = false) {
        if (_TransactionStatus.value.daftarCartItem.isEmpty()) {
            kirimPesanSingkat("Keranjang masih kosong. Yuk, tambah produk!")
            return
        }

        if (StoreSetting.value.manajemenKasAktif && shiftKasAktif.value == null) {
            kirimPesanSingkat("Buka kas terlebih dahulu sebelum melakukan transaksi")
            return
        }

        _statusElemenLayar.update { statusLama ->
            statusLama.copy(
                apakahDialogKonfirmasiCheckoutTampil = true,
                statusHasilCheckout = CheckoutResultStatus(),
                modeSimpan = modeSimpan,
            )
        }
    }

    private fun batalkanKonfirmasiCheckout() {
        _statusElemenLayar.update { statusLama ->
            statusLama.copy(
                apakahDialogKonfirmasiCheckoutTampil = false,
            )
        }
    }

    private fun perbaruiCatatanCheckout(catatan: String) {
        _statusElemenLayar.update { statusLama ->
            statusLama.copy(
                catatanCheckout = catatan,
            )
        }
    }

    private fun perbaruiPaymentMethod(paymentMethod: PaymentMethod) {
        _statusElemenLayar.update { statusLama ->
            statusLama.copy(
                paymentMethod = paymentMethod,
            )
        }
    }

    private fun perbaruiOrderType(orderType: OrderType) {
        _statusElemenLayar.update { statusLama ->
            statusLama.copy(
                orderType = orderType,
                catatanCheckout = if (orderType == OrderType.TakeAway) "" else statusLama.catatanCheckout,
                mejaId = if (orderType == OrderType.TakeAway) null else statusLama.mejaId,
            )
        }
    }

    private fun pilihMeja(mejaId: String?) {
        _statusElemenLayar.update { statusLama ->
            statusLama.copy(mejaId = mejaId)
        }
    }

    private fun alihkanCatalogDisplay() {
        val pengaturanSaatIni = StoreSetting.value
        val tampilanBaru = if (pengaturanSaatIni.catalogDisplay == id.flexi.kasir.domain.model.CatalogDisplay.Grid)
            id.flexi.kasir.domain.model.CatalogDisplay.List
        else
            id.flexi.kasir.domain.model.CatalogDisplay.Grid
        viewModelScope.launch {
            simpanStoreSetting(pengaturanSaatIni.copy(catalogDisplay = tampilanBaru))
        }
    }

    private fun simpanPesanan() {
        checkoutDenganStatus(TransactionStatus.Pending)
    }

    private fun simpanDanCetakPesanan() {
        checkoutDenganStatus(TransactionStatus.Pending, apakahCetakStruk = true)
    }

    private fun bayarSekarang() {
        checkoutDenganStatus(TransactionStatus.Processing)
    }

    private fun bayarSekarangTunai(nominalUang: Long) {
        _statusElemenLayar.update { it.copy(uangDibayarTunai = nominalUang) }
        checkoutDenganStatus(TransactionStatus.Processing)
        _statusElemenLayar.update { it.copy(uangDibayarTunai = null) }
    }

    private fun checkoutDenganStatus(status: TransactionStatus, apakahCetakStruk: Boolean = false) {
        val daftarKeranjangSaatIni = _TransactionStatus.value.daftarCartItem
        if (daftarKeranjangSaatIni.isEmpty()) {
            kirimPesanSingkat("Keranjang masih kosong. Yuk, tambah produk!")
            return
        }

        if (StoreSetting.value.manajemenKasAktif && shiftKasAktif.value == null) {
            _statusElemenLayar.update { it.copy(apakahDialogKonfirmasiCheckoutTampil = false) }
            kirimPesanSingkat("Buka kas terlebih dahulu sebelum melakukan transaksi")
            return
        }

        val elemenLayar = _statusElemenLayar.value
        val catatan = elemenLayar.catatanCheckout
        val paymentMethod = elemenLayar.paymentMethod
        val orderType = elemenLayar.orderType
        val mejaId = elemenLayar.mejaId
        val uangDibayarTunai = elemenLayar.uangDibayarTunai
        val preferensi = StorePreference.value
        val taxRule = if (preferensi.basisPoinPajakDefault > 0) {
            id.flexi.kasir.domain.model.TaxRule(
                nama = "PPN",
                basisPoin = preferensi.basisPoinPajakDefault,
                aktif = true,
            )
        } else {
            id.flexi.kasir.domain.model.TaxRule.NoTax
        }
        val biayaLayanan = Uang.dariRupiah(preferensi.biayaLayananDefault)

        // Cek apakah transaksi yang di-resume sudah semua itemnya selesai
        // Kalau semua item sudah selesai (oleh barista), jangan masuk Processing,
        // langsung Paid agar tidak muncul lagi di Antrian Dapur.
        val oldTransaction = resumeTransactionId?.let { id ->
            daftarPesananPending.value.firstOrNull { it.id == id }
        }
        val semuaItemSelesai = (oldTransaction?.daftarCartItem?.all { it.apakahSelesai }
            ?: daftarKeranjangSaatIni.all { it.apakahSelesai }) == true

        // Kalau semua item sudah selesai & user cuma mau "Simpan" (Pending),
        // simpan tetap dilakukan agar data tidak hilang saat app di-restart.
        // Tapi skip jika baru split bill — item sisa perlu disimpan sebagai pending
        if (semuaItemSelesai && status == TransactionStatus.Pending &&
            !elemenLayar.apakahBaruSplitBill
        ) {
            kirimPesanSingkat("${daftarKeranjangSaatIni.size} item akan disimpan ke pesanan pending.")
        }

        val statusFinal = if (semuaItemSelesai && status == TransactionStatus.Processing) {
            TransactionStatus.Paid
        } else {
            status
        }

        val judulHasil = when (statusFinal) {
            TransactionStatus.Pending -> "Pesanan Disimpan"
            TransactionStatus.Processing -> "Pesanan Diproses"
            TransactionStatus.Paid -> "Pembayaran Berhasil"
            else -> "Transaction Berhasil"
        }

        viewModelScope.launch {
            try {
                val hasilCheckout = CompleteLocalCheckout.eksekusi(
                    daftarCartItem = daftarKeranjangSaatIni,
                    catatan = catatan.ifBlank { null },
                    status = statusFinal,
                    taxRule = taxRule,
                    biayaLayanan = biayaLayanan,
                    paymentMethod = paymentMethod,
                    orderType = orderType,
                    mejaId = mejaId,
                    uangDibayar = uangDibayarTunai,
                    identitasTransaction = resumeTransactionId,
                )

                resumeTransactionId = null

                _TransactionStatus.update {
                    CashierMainTransactionState(
                        daftarCartItem = hasilCheckout.daftarCartItemBaru,
                        syncStatus = hasilCheckout.SyncStatusBaru,
                    )
                }

                val deskripsiHasil = buildString {
                    append("Sebanyak ${hasilCheckout.jumlahItemCheckout} item dengan total ${hasilCheckout.totalCheckout.sebagaiRupiah()}")
                    hasilCheckout.nomorAntrian?.let { antrian ->
                        append("\nNomor antrian: $antrian")
                    }
                    append(" telah diproses.")
                }

                _statusElemenLayar.update { statusLama ->
                    statusLama.copy(
                        apakahDialogKonfirmasiCheckoutTampil = false,
                        catatanCheckout = "",
                        mejaId = null,
                        apakahBaruSplitBill = false,
                        statusHasilCheckout = CheckoutResultStatus(
                            apakahTampil = true,
                            judul = judulHasil,
                            deskripsi = deskripsiHasil,
                            nomorAntrian = hasilCheckout.nomorAntrian,
                            paymentMethod = paymentMethod,
                        ),
                    )
                }

                // Perbarui favorit: produk paling laris otomatis jadi favorit
                try {
                    updatePopularFavorites.eksekusi(StoreSetting.value.jumlahTopFavorit)
                } catch (_: Exception) {
                    // Gagal hitung favorit — tidak perlu ganggu pengguna
                }

                // Cetak struk setelah checkout
                if (apakahCetakStruk ||
                    (status == TransactionStatus.Processing && StoreSetting.value.receiptPrintFormat == ReceiptPrintFormat.Automatic)
                ) {
                    cetakStruk()
                }
            } catch (kesalahanValidasi: IllegalArgumentException) {
                _statusElemenLayar.update { statusLama ->
                    statusLama.copy(
                        apakahDialogKonfirmasiCheckoutTampil = false,
                        apakahBaruSplitBill = false,
                    )
                }

                kirimPesanSingkat(
                    pesan = kesalahanValidasi.message
                        ?: "Transaction belum valid untuk disimpan.",
                )
            } catch (_: Exception) {
                _statusElemenLayar.update { statusLama ->
                    statusLama.copy(
                        apakahDialogKonfirmasiCheckoutTampil = false,
                        apakahBaruSplitBill = false,
                    )
                }

                kirimPesanSingkat("Transaction belum tersimpan. Coba lagi.")
            }
        }
    }

    private fun tutupStatusHasilCheckout() {
        _statusElemenLayar.update { statusLama ->
            statusLama.copy(
                statusHasilCheckout = CheckoutResultStatus(),
            )
        }
    }

    private fun resetStatusHasil() {
        _statusElemenLayar.update { statusLama ->
            statusLama.copy(
                statusHasilCheckout = CheckoutResultStatus(),
            )
        }
    }

    fun tampilkanPesanOperasional(
        pesan: String,
    ) {
        if (pesan.isBlank()) return
        kirimPesanSingkat(pesan)
    }

    private fun bukaPendingOrdersPanel() {
        _statusElemenLayar.update { it.copy(apakahPendingOrdersPanelTampil = true) }
    }

    private fun tutupPendingOrdersPanel() {
        _statusElemenLayar.update { it.copy(apakahPendingOrdersPanelTampil = false) }
    }

    private fun bukaAntrianPanel() {
        _statusElemenLayar.update { it.copy(apakahAntrianPanelTampil = true) }
    }

    private fun tutupAntrianPanel() {
        _statusElemenLayar.update { it.copy(apakahAntrianPanelTampil = false) }
    }

    private fun gabungDanBayarBill(mejaId: String, paymentMethod: PaymentMethod, uangDibayar: Long? = null) {
        if (StoreSetting.value.manajemenKasAktif && shiftKasAktif.value == null) {
            kirimPesanSingkat("Buka kas terlebih dahulu sebelum melakukan transaksi")
            return
        }

        viewModelScope.launch {
            try {
                // Ambil bill lain (kecuali transaksi yang sedang di-resume)
                val billLain = daftarPesananPending.value.filter {
                    it.mejaId == mejaId && it.id != resumeTransactionId
                }
                if (billLain.isEmpty()) {
                    kirimPesanSingkat("Tidak ada bill lain untuk digabung.")
                    return@launch
                }

                // Gabung item dari keranjang saat ini + item dari bill lain
                val itemKeranjang = _TransactionStatus.value.daftarCartItem
                val itemGabungan = (itemKeranjang + billLain.flatMap { it.daftarCartItem })
                    .groupBy { it.produk.id to it.varian?.nama }
                    .map { (_, items) ->
                        // Preserve apakahSelesai: true hanya jika SEMUA item yang digabung sudah selesai
                        items.reduce { a, b ->
                            a.copy(
                                jumlah = a.jumlah + b.jumlah,
                                apakahSelesai = a.apakahSelesai && b.apakahSelesai,
                            )
                        }
                    }

                // Cek original state item dari resumed transaction (sebelum di-reset ResumePendingOrder)
                val oldTransaction = resumeTransactionId?.let { id ->
                    daftarPesananPending.value.firstOrNull { it.id == id }
                }
                val semuaItemResumedSelesai = oldTransaction?.daftarCartItem?.all { it.apakahSelesai } == true

                // Cek bill lain (masih original dari DB, belum di-reset)
                val semuaItemBillLainSelesai = billLain.all { bill ->
                    bill.daftarCartItem.all { it.apakahSelesai }
                }

                // Jika semua item sudah selesai (oleh barista), langsung Paid agar tidak masuk Antrian
                val semuaItemSelesai = semuaItemResumedSelesai && semuaItemBillLainSelesai
                val status = if (semuaItemSelesai) TransactionStatus.Paid else TransactionStatus.Processing

                // Buat satu transaksi baru dengan metode bayar
                val preferensi = StorePreference.value
                val taxRule = if (preferensi.basisPoinPajakDefault > 0) {
                    TaxRule(nama = "PPN", basisPoin = preferensi.basisPoinPajakDefault, aktif = true)
                } else {
                    TaxRule.NoTax
                }
                val biayaLayanan = Uang.dariRupiah(preferensi.biayaLayananDefault)

                val hasilCheckout = CompleteLocalCheckout.eksekusi(
                    daftarCartItem = itemGabungan,
                    catatan = null,
                    status = status,
                    taxRule = taxRule,
                    biayaLayanan = biayaLayanan,
                    paymentMethod = paymentMethod,
                    mejaId = mejaId,
                    uangDibayar = uangDibayar,
                    identitasTransaction = resumeTransactionId,
                )

                // Setelah transaksi baru berhasil, baru hapus bill lain (jangan hapus resumed)
                billLain.forEach { DeletePendingOrder.eksekusi(it.id) }

                // Clear resumeTransactionId karena sudah digabung
                resumeTransactionId = null

                _TransactionStatus.update {
                    CashierMainTransactionState(
                        daftarCartItem = hasilCheckout.daftarCartItemBaru,
                        syncStatus = hasilCheckout.SyncStatusBaru,
                    )
                }

                _statusElemenLayar.update { statusLama ->
                    statusLama.copy(
                        apakahDialogKonfirmasiCheckoutTampil = false,
                        catatanCheckout = "",
                        mejaId = null,
                        resumeTransactionId = null,
                        statusHasilCheckout = CheckoutResultStatus(
                            apakahTampil = true,
                            judul = "${billLain.size + 1} Bill Digabung & Dibayar",
                            deskripsi = "${hasilCheckout.jumlahItemCheckout} item dengan total ${hasilCheckout.totalCheckout.sebagaiRupiah()} (${paymentMethod.label}).",
                            paymentMethod = paymentMethod,
                        ),
                    )
                }

                // Cetak struk jika setting otomatis
                if (StoreSetting.value.receiptPrintFormat == ReceiptPrintFormat.Automatic) {
                    cetakStruk(itemGabungan)
                }

                kirimPesanSingkat("${billLain.size + 1} bill berhasil digabung dan dibayar")
            } catch (_: Exception) {
                kirimPesanSingkat("Gagal menggabungkan bill. Coba lagi.")
            }
        }
    }

    private fun gabungDanSimpanBill(mejaId: String) {
        if (StoreSetting.value.manajemenKasAktif && shiftKasAktif.value == null) {
            kirimPesanSingkat("Buka kas terlebih dahulu sebelum melakukan transaksi")
            return
        }

        viewModelScope.launch {
            try {
                // Ambil bill lain (kecuali transaksi yang sedang di-resume)
                val billLain = daftarPesananPending.value.filter {
                    it.mejaId == mejaId && it.id != resumeTransactionId
                }
                if (billLain.isEmpty()) {
                    kirimPesanSingkat("Tidak ada bill lain untuk digabung.")
                    return@launch
                }

                // Gabung item dari keranjang saat ini + item dari bill lain
                val itemKeranjang = _TransactionStatus.value.daftarCartItem
                val itemGabungan = (itemKeranjang + billLain.flatMap { it.daftarCartItem })
                    .groupBy { it.produk.id to it.varian?.nama }
                    .map { (_, items) ->
                        items.reduce { a, b -> a.copy(jumlah = a.jumlah + b.jumlah, apakahSelesai = false) }
                    }

                // Buat satu transaksi Pending baru (gabungan)
                val preferensi = StorePreference.value
                val taxRule = if (preferensi.basisPoinPajakDefault > 0) {
                    TaxRule(nama = "PPN", basisPoin = preferensi.basisPoinPajakDefault, aktif = true)
                } else {
                    TaxRule.NoTax
                }
                val biayaLayanan = Uang.dariRupiah(preferensi.biayaLayananDefault)

                val hasilCheckout = CompleteLocalCheckout.eksekusi(
                    daftarCartItem = itemGabungan,
                    catatan = null,
                    status = TransactionStatus.Pending,
                    taxRule = taxRule,
                    biayaLayanan = biayaLayanan,
                    paymentMethod = _statusElemenLayar.value.paymentMethod,
                    mejaId = mejaId,
                    identitasTransaction = resumeTransactionId,
                )

                // Setelah transaksi baru berhasil, baru hapus bill lain (jangan hapus resumed)
                billLain.forEach { DeletePendingOrder.eksekusi(it.id) }

                resumeTransactionId = null

                _TransactionStatus.update {
                    CashierMainTransactionState(
                        daftarCartItem = hasilCheckout.daftarCartItemBaru,
                        syncStatus = hasilCheckout.SyncStatusBaru,
                    )
                }

                _statusElemenLayar.update { statusLama ->
                    statusLama.copy(
                        apakahDialogKonfirmasiCheckoutTampil = false,
                        catatanCheckout = "",
                        mejaId = null,
                        resumeTransactionId = null,
                        statusHasilCheckout = CheckoutResultStatus(
                            apakahTampil = true,
                            judul = "${billLain.size + 1} Bill Digabung",
                            deskripsi = "${hasilCheckout.jumlahItemCheckout} item dengan total ${hasilCheckout.totalCheckout.sebagaiRupiah()} disimpan sebagai 1 bill.",
                            paymentMethod = _statusElemenLayar.value.paymentMethod,
                        ),
                    )
                }

                kirimPesanSingkat("${billLain.size + 1} bill berhasil digabung")
            } catch (_: Exception) {
                kirimPesanSingkat("Gagal menggabungkan bill. Coba lagi.")
            }
        }
    }

    private fun selesaikanAntrian(identitasTransaction: String) {
        // Cari status pesanan dari data yang ada
        val pesanan = daftarAntrianSaatIni(identitasTransaction)
        if (pesanan == null) {
            kirimPesanSingkat("Pesanan tidak ditemukan")
            return
        }

        viewModelScope.launch {
            try {
                if (pesanan.status == TransactionStatus.Processing) {
                    // Sudah dibayar → ubah status jadi Ready + catat waktu selesai
                    SelesaikanTransaction(identitasTransaction, ubahStatusKeReady = true)
                } else {
                    // Pending (belum bayar) → tandai item selesai, status tetap Pending
                    SelesaikanTransaction(identitasTransaction, ubahStatusKeReady = false)
                }
                kirimPesanSingkat("Pesanan selesai")
            } catch (_: Exception) {
                kirimPesanSingkat("Gagal menyelesaikan pesanan")
            }
        }
    }

    private fun daftarAntrianSaatIni(id: String): Transaction? {
        // Gabung pending + diproses untuk lookup
        val semua = daftarPesananPending.value + daftarPesananDiproses.value
        return semua.firstOrNull { it.id == id }
    }

    private fun ResumePendingOrder(identitasTransaction: String) {
        viewModelScope.launch {
            try {
                val hasilResume = ResumePendingOrder.eksekusi(
                    identitasTransaction = identitasTransaction,
                    daftarProdukSaatIni = daftarProdukPenuh.value,
                )

                // Simpan ID transaksi lama agar dipakai ulang saat Simpan nanti
                resumeTransactionId = hasilResume.identitasTransaction

                _TransactionStatus.update {
                    CashierMainTransactionState(
                        daftarCartItem = hasilResume.daftarCartItem,
                        syncStatus = SyncStatus.LocalChanges,
                    )
                }

                _statusElemenLayar.update {
                    it.copy(
                        apakahPendingOrdersPanelTampil = false,
                        statusHasilCheckout = CheckoutResultStatus(),
                        catatanCheckout = hasilResume.catatan.orEmpty(),
                        mejaId = hasilResume.mejaId,
                        orderType = hasilResume.orderType,
                        resumeTransactionId = hasilResume.identitasTransaction,
                    )
                }

                kirimPesanSingkat("Pesanan dilanjutkan ke keranjang.")
            } catch (kesalahan: IllegalArgumentException) {
                kirimPesanSingkat(kesalahan.message ?: "Pesanan gagal dilanjutkan.")
            }
        }
    }

    private fun PayPendingOrder(identitasTransaction: String, paymentMethod: PaymentMethod, uangDibayar: Long?) {
        if (StoreSetting.value.manajemenKasAktif && shiftKasAktif.value == null) {
            kirimPesanSingkat("Buka kas terlebih dahulu sebelum melakukan transaksi")
            return
        }

        viewModelScope.launch {
            try {
                PayPendingOrder.eksekusi(identitasTransaction, paymentMethod, uangDibayar)

                val pesanan = daftarPesananPending.value.firstOrNull { it.id == identitasTransaction }

                val deskripsiHasil = buildString {
                    append("Pesanan pending telah dibayar lunas (${paymentMethod.label})")
                    if (paymentMethod == PaymentMethod.Cash && uangDibayar != null && pesanan != null) {
                        val subtotal = pesanan.daftarCartItem.hitungSubtotalKeranjangUang().nilaiRupiah
                        val breakdown = TransactionCostBreakdown(
                            subtotal = Uang.dariRupiah(subtotal),
                            potongan = pesanan.potongan,
                            biayaLayanan = pesanan.biayaLayanan,
                            pajak = pesanan.pajak,
                        )
                        val total = breakdown.totalAkhir.nilaiRupiah
                        val kembalian = if (uangDibayar >= total) uangDibayar - total else 0L
                        if (kembalian > 0) {
                            append(".\nKembalian: ${kembalian.sebagaiRupiah()}")
                        }
                    }
                }

                _statusElemenLayar.update {
                    it.copy(
                        apakahPendingOrdersPanelTampil = false,
                        statusHasilCheckout = CheckoutResultStatus(
                            apakahTampil = true,
                            judul = "Payment Berhasil",
                            deskripsi = deskripsiHasil,
                            paymentMethod = paymentMethod,
                        ),
                    )
                }

                kirimPesanSingkat("Pembayaran ${paymentMethod.label} berhasil")

                // Cetak struk jika setting otomatis
                if (pesanan != null && StoreSetting.value.receiptPrintFormat == ReceiptPrintFormat.Automatic) {
                    cetakStruk(pesanan.daftarCartItem)
                }
            } catch (kesalahan: IllegalArgumentException) {
                kirimPesanSingkat(kesalahan.message ?: "Payment gagal.")
            }
        }
    }

    private fun DeletePendingOrder(identitasTransaction: String) {
        viewModelScope.launch {
            try {
                DeletePendingOrder.eksekusi(identitasTransaction)
                kirimPesanSingkat("Pesanan pending dibatalkan.")
            } catch (_: Exception) {
                kirimPesanSingkat("Pesanan gagal dibatalkan.")
            }
        }
    }

    private fun cetakStruk(daftarItem: List<CartItem>? = null) {
        viewModelScope.launch {
            val daftarKeranjang = daftarItem ?: _TransactionStatus.value.daftarCartItem
            val elemenLayar = _statusElemenLayar.value
            val preferensi = StorePreference.value
            val pengaturan = StoreSetting.value

            if (daftarKeranjang.isEmpty()) {
                kirimPesanSingkat("Tidak ada item untuk dicetak.")
                return@launch
            }

            val taxRule = if (preferensi.basisPoinPajakDefault > 0) {
                id.flexi.kasir.domain.model.TaxRule(
                    nama = "PPN",
                    basisPoin = preferensi.basisPoinPajakDefault,
                    aktif = true,
                )
            } else {
                id.flexi.kasir.domain.model.TaxRule.NoTax
            }
            val biayaLayanan = Uang.dariRupiah(preferensi.biayaLayananDefault)

            val rincianPajak = taxRule.hitungDariSubtotal(
                daftarKeranjang.hitungSubtotalKeranjangUang(),
            )

            val Transaction = Transaction(
                id = "",
                daftarCartItem = daftarKeranjang,
                potongan = Uang.Nol,
                biayaLayanan = biayaLayanan,
                pajak = rincianPajak,
                waktuTransactionEpochMili = System.currentTimeMillis(),
                catatan = elemenLayar.catatanCheckout.ifBlank { null },
                status = TransactionStatus.Pending,
                orderType = elemenLayar.orderType,
                mejaId = elemenLayar.mejaId,
            )

            // Gunakan printer yang dikonfigurasi user jika ada, fallback ke auto-detect
            val hasil = if (pengaturan.printerType != id.flexi.kasir.domain.model.PrinterType.None) {
                ThermalPrinterManager.cetakStrukDenganKonfigurasi(
                    Transaction = Transaction,
                    printerType = pengaturan.printerType,
                    printerAddress = pengaturan.printerAddress,
                    pengaturanStruk = pengaturan,
                )
            } else {
                ThermalPrinterManager.cetakStruk(Transaction)
            }

            when (hasil) {
                is PrintResult.Berhasil -> {
                    kirimPesanSingkat("Struk berhasil dicetak.")
                }
                is PrintResult.Gagal -> {
                    kirimPesanSingkat("Cetak struk gagal: ${hasil.pesan}")
                }
            }
        }
    }

    private fun kirimPesanSingkat(pesan: String) {
        _efek.tryEmit(
            CashierMainEffect.TampilkanPesanSingkat(
                pesan = pesan,
            ),
        )
    }

    private fun konfirmasiSplitBill(daftarIdProduk: Set<String>, paymentMethod: PaymentMethod, uangTunai: Long = 0L) {
        if (StoreSetting.value.manajemenKasAktif && shiftKasAktif.value == null) {
            kirimPesanSingkat("Buka kas terlebih dahulu sebelum melakukan transaksi")
            return
        }

        val semuaItem = _TransactionStatus.value.daftarCartItem
        if (semuaItem.isEmpty()) return

        // Filter menggunakan composite key (produk.id + varian.nama) agar varian berbeda dianggap item terpisah
        fun itemKey(item: CartItem): String = "${item.produk.id}|${item.varian?.nama ?: ""}"

        val itemDibayar = semuaItem.filter { itemKey(it) in daftarIdProduk }
        val itemSisa = semuaItem.filter { itemKey(it) !in daftarIdProduk }

        if (itemDibayar.isEmpty()) {
            kirimPesanSingkat("Pilih minimal satu item untuk dibayar.")
            return
        }

        val elemenLayar = _statusElemenLayar.value
        val preferensi = StorePreference.value
        val taxRule = if (preferensi.basisPoinPajakDefault > 0) {
            id.flexi.kasir.domain.model.TaxRule(
                nama = "PPN", basisPoin = preferensi.basisPoinPajakDefault, aktif = true,
            )
        } else {
            id.flexi.kasir.domain.model.TaxRule.NoTax
        }
        val biayaLayanan = Uang.dariRupiah(preferensi.biayaLayananDefault)

        viewModelScope.launch {
            try {
                // Hapus pending order asli jika ada (dari resume)
                val oldResumeId = resumeTransactionId
                resumeTransactionId = null

                val hasilBayar = CompleteLocalCheckout.eksekusi(
                    daftarCartItem = itemDibayar,
                    catatan = null,
                    status = TransactionStatus.Paid,
                    taxRule = taxRule,
                    biayaLayanan = biayaLayanan,
                    paymentMethod = paymentMethod,
                    orderType = elemenLayar.orderType,
                    mejaId = elemenLayar.mejaId,
                    uangDibayar = if (paymentMethod == PaymentMethod.Cash) uangTunai else null,
                )

                // Ambil status apakahSelesai asli dari pending order sebelum dihapus
                val itemSisaFinal = if (oldResumeId != null) {
                    val oldTransaction = daftarPesananPending.value.firstOrNull { it.id == oldResumeId }
                    if (oldTransaction != null) {
                        // Cocokkan item sisa dengan item asli berdasarkan product ID + varian
                        val oldStatusMap = oldTransaction.daftarCartItem.associateBy { itemKey(it) }
                        itemSisa.map { item ->
                            val oldItem = oldStatusMap[itemKey(item)]
                            if (oldItem != null && oldItem.apakahSelesai) {
                                item.copy(apakahSelesai = true)
                            } else {
                                item
                            }
                        }
                    } else {
                        itemSisa
                    }
                } else {
                    itemSisa
                }

                // Hapus pending order asli SEBELUM update UI agar jika gagal, error tertangkap rapi
                if (oldResumeId != null) {
                    DeletePendingOrder.eksekusi(oldResumeId)
                }

                _TransactionStatus.update {
                    CashierMainTransactionState(
                        daftarCartItem = itemSisaFinal,
                        syncStatus = SyncStatus.LocalChanges,
                    )
                }

                _statusElemenLayar.update { statusLama ->
                    statusLama.copy(
                        apakahDialogKonfirmasiCheckoutTampil = false,
                        catatanCheckout = "",
                        apakahBaruSplitBill = true,
                        statusHasilCheckout = CheckoutResultStatus(
                            apakahTampil = true,
                            judul = "Split Bill Berhasil",
                            deskripsi = "${hasilBayar.jumlahItemCheckout} item dibayar (${paymentMethod.label}). ${itemSisa.size} item tersisa di keranjang.",
                            paymentMethod = paymentMethod,
                        ),
                    )
                }

                // Cetak struk jika setting otomatis
                if (StoreSetting.value.receiptPrintFormat == ReceiptPrintFormat.Automatic) {
                    cetakStruk(itemDibayar)
                }
            } catch (kesalahan: IllegalArgumentException) {
                kirimPesanSingkat(kesalahan.message ?: "Split bill gagal.")
            } catch (_: Exception) {
                kirimPesanSingkat("Split bill gagal. Coba lagi.")
            }
        }
    }

    private fun tambahItemManual(nama: String, harga: Long) {
        val id = "manual_${System.nanoTime()}"
        val produk = Produk(
            id = id,
            nama = nama.ifBlank { "Item Manual" },
            harga = harga,
            stokTersedia = Int.MAX_VALUE,
        )
        _TransactionStatus.update { statusLama ->
            val daftarLama = statusLama.daftarCartItem
            val daftarBaru = AddProductToCart(
                daftarCartItem = daftarLama,
                produk = produk,
            )
            statusLama.copy(
                daftarCartItem = daftarBaru,
                syncStatus = SyncStatus.LocalChanges,
            )
        }
    }
}

private data class StatusDasarModelTampilan(
    val daftarProdukPenuh: List<Produk>,
    val TransactionStatus: CashierMainTransactionState,
    val statusElemenLayar: CashierMainElementState,
)

private data class StatusPencarianModelTampilan(
    val kataKunciMentah: String,
    val kataKunciEfektif: String,
)

private data class StatusPengaturanModelTampilan(
    val StorePreference: StorePreference,
    val StoreSetting: StoreSetting,
    val daftarPesananPending: List<Transaction>,
    val daftarPesananDiproses: List<Transaction>,
    val daftarMeja: List<Meja>,
)

private data class StatusNavigasiModelTampilan(
    val tabTransaksi: Int,
    val kategoriTerpilih: String,
)

