package id.flexi.kasir.ui.transaction

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import androidx.lifecycle.viewModelScope
import id.flexi.kasir.ui.format.hitungJumlahItemTransaction
import id.flexi.kasir.ui.format.hitungKembalianTransaction
import id.flexi.kasir.ui.format.hitungSubtotalTransaction
import id.flexi.kasir.domain.util.hitungTotalAkhirTransaction
import id.flexi.kasir.ui.format.sebagaiLabelIdentitasTransaction
import id.flexi.kasir.ui.format.sebagaiLabelWaktuTransaction
import id.flexi.kasir.ui.navigation.CashierNavigationDestination
import id.flexi.kasir.domain.usecase.BatalkanTransaction
import id.flexi.kasir.domain.usecase.GetTableList
import id.flexi.kasir.domain.usecase.ObserveTransactionById
import id.flexi.kasir.domain.util.sebagaiRupiah
import id.flexi.kasir.domain.model.Meja
import id.flexi.kasir.domain.model.KitchenStatus
import id.flexi.kasir.domain.model.PaymentMethod
import id.flexi.kasir.domain.model.Transaction
import id.flexi.kasir.domain.model.ReceiptPrintFormat
import id.flexi.kasir.domain.model.Uang
import id.flexi.kasir.domain.repository.TransactionRepository
import id.flexi.kasir.print.PrintResult
import id.flexi.kasir.print.ThermalPrinterManager
import id.flexi.kasir.domain.usecase.AmbilStoreSetting
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val kunciIdentitasTransaction = "identitasTransaction"

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionDetailViewModel(
    private val ObserveTransactionById: ObserveTransactionById,
    private val GetTableList: GetTableList,
    private val batalkanTransaction: BatalkanTransaction,
    private val ThermalPrinterManager: ThermalPrinterManager,
    private val ambilStoreSetting: AmbilStoreSetting,
    private val transactionRepository: TransactionRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val identitasTransaction: String =
        savedStateHandle.get<String>(kunciIdentitasTransaction)
            ?: savedStateHandle.toRoute<CashierNavigationDestination.DetailTransaction>().identitasTransaction

    private val _nomorPermintaanMuatUlang = MutableStateFlow(0)
    private val _apakahDialogBatalkanTerbuka = MutableStateFlow(false)
    private val _alasanPembatalan = MutableStateFlow("")
    private val _efekCetak = MutableStateFlow<String?>(null)
    val efekCetak: StateFlow<String?> = _efekCetak

    // ── Edit dialog state ──
    private val _apakahDialogEditTerbuka = MutableStateFlow(false)
    private val _editPaymentMethod = MutableStateFlow(PaymentMethod.Cash)
    private val _editUangDibayar = MutableStateFlow("")
    private val _editCatatan = MutableStateFlow("")
    private val _sedangMenyimpanEdit = MutableStateFlow(false)

    private val daftarMeja: StateFlow<List<Meja>> = GetTableList()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _baseState = _nomorPermintaanMuatUlang.flatMapLatest {
        combine(
            ObserveTransactionById(identitasTransaction),
            daftarMeja,
        ) { Transaction, tables ->
            Transaction.keTransactionDetailUiState(identitasTransaction, tables)
        }.catch {
            emit(
                TransactionDetailUiState(
                    judulLayar = "Detail Transaksi",
                    statusMuat = StatusMuatDetailTransaction.Gagal(
                        judul = "Gagal memuat detail transaksi",
                        deskripsi = "Terjadi gangguan saat membaca transaksi. Silakan coba lagi.",
                    ),
                ),
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TransactionDetailUiState(
            judulLayar = "Detail Transaksi",
            statusMuat = StatusMuatDetailTransaction.Memuat,
        ),
    )

    val modelTampilan: StateFlow<TransactionDetailUiState> =
        combine(
            _baseState,
            _apakahDialogBatalkanTerbuka,
            _alasanPembatalan,
        ) { state, dialogTerbuka, alasan ->
            state.copy(
                apakahDialogBatalkanTerbuka = dialogTerbuka,
                alasanPembatalan = alasan,
            )
        }.combine(
            _apakahDialogEditTerbuka,
        ) { state, dialogEdit ->
            state.copy(apakahDialogEditTerbuka = dialogEdit)
        }.combine(
            _editPaymentMethod,
        ) { state, editMetode ->
            state.copy(editPaymentMethod = editMetode)
        }.combine(
            _editUangDibayar,
        ) { state, editUang ->
            state.copy(editUangDibayar = editUang)
        }.combine(
            _editCatatan,
        ) { state, editCatatan ->
            state.copy(editCatatan = editCatatan)
        }.combine(
            _sedangMenyimpanEdit,
        ) { state, sedangSimpan ->
            state.copy(sedangMenyimpanEdit = sedangSimpan)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TransactionDetailUiState(
                judulLayar = "Detail Transaksi",
                statusMuat = StatusMuatDetailTransaction.Memuat,
            ),
        )

    fun muatUlang() {
        _nomorPermintaanMuatUlang.update { nomorLama ->
            nomorLama + 1
        }
    }

    fun bukaDialogBatalkan() {
        _apakahDialogBatalkanTerbuka.value = true
        _alasanPembatalan.value = ""
    }

    fun tutupDialogBatalkan() {
        _apakahDialogBatalkanTerbuka.value = false
    }

    fun perbaruiAlasanPembatalan(alasan: String) {
        _alasanPembatalan.value = alasan
    }

    fun batalkan() {
        val alasan = _alasanPembatalan.value
        viewModelScope.launch {
            try {
                batalkanTransaction(identitasTransaction, alasan.ifBlank { null })
                _apakahDialogBatalkanTerbuka.value = false
            } catch (_: Exception) { }
        }
    }

    // ── Edit transaction functions ──

    fun bukaDialogEdit() {
        val currentState = modelTampilan.value
        val data = currentState.statusMuat as? StatusMuatDetailTransaction.Berhasil ?: return
        _apakahDialogEditTerbuka.value = true
        _editPaymentMethod.value = data.paymentMethod
        _editUangDibayar.value = if (data.uangDibayar > 0) data.uangDibayar.toString() else ""
        _editCatatan.value = data.catatan.orEmpty()
    }

    fun tutupDialogEdit() {
        _apakahDialogEditTerbuka.value = false
    }

    fun perbaruiEditPaymentMethod(method: PaymentMethod) {
        _editPaymentMethod.value = method
    }

    fun perbaruiEditUangDibayar(nominal: String) {
        _editUangDibayar.value = nominal.filter { it.isDigit() }
    }

    fun perbaruiEditCatatan(catatan: String) {
        _editCatatan.value = catatan
    }

    fun simpanEdit() {
        val uangDibayar = _editUangDibayar.value.toLongOrNull() ?: 0L
        val catatan = _editCatatan.value.ifBlank { null }
        val paymentMethod = _editPaymentMethod.value

        _sedangMenyimpanEdit.value = true
        viewModelScope.launch {
            try {
                transactionRepository.perbaruiPaymentMethodTransaction(
                    identitasTransaction = identitasTransaction,
                    paymentMethod = paymentMethod,
                    uangDibayar = uangDibayar,
                    catatan = catatan,
                )
                _apakahDialogEditTerbuka.value = false
                _sedangMenyimpanEdit.value = false
                muatUlang()
            } catch (_: Exception) {
                _sedangMenyimpanEdit.value = false
            }
        }
    }

    /**
     * Cetak ulang struk untuk transaksi ini.
     */
    fun cetakUlangStruk() {
        viewModelScope.launch {
            val Transaction = ObserveTransactionById(identitasTransaction).first()
            if (Transaction == null) {
                return@launch
            }
            val pengaturan = ambilStoreSetting().first()
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
                    _efekCetak.value = "Struk berhasil dicetak."
                }
                is PrintResult.Gagal -> {
                    _efekCetak.value = "Cetak gagal: ${hasil.pesan}"
                }
            }
        }
    }
}

private fun Transaction?.keTransactionDetailUiState(
    identitasTransaction: String,
    tables: List<Meja> = emptyList(),
): TransactionDetailUiState {
    if (this == null) {
        return TransactionDetailUiState(
            judulLayar = "Detail Transaksi",
            statusMuat = StatusMuatDetailTransaction.Kosong(
                judul = "Transaksi tidak ditemukan",
                deskripsi = "Data untuk ID $identitasTransaction tidak ada di database lokal.",
            ),
        )
    }

    val subtotal = hitungSubtotalTransaction()
    val totalAkhir = hitungTotalAkhirTransaction()
    val kembalian = hitungKembalianTransaction()

    val labelMeja = if (mejaId != null) {
        tables.firstOrNull { it.id == mejaId }?.let { "Meja ${it.nomor}" }
    } else null
    val labelPembayaran = paymentMethod.label
    val labelStatus = when (kitchenStatus) {
        KitchenStatus.Selesai -> "Lunas"
        KitchenStatus.Diproses -> "Diproses"
        KitchenStatus.Siap -> "Siap"
        KitchenStatus.Dicatat -> "Dicatat"
    }

    return TransactionDetailUiState(
        judulLayar = "Detail Transaksi",
        statusMuat = StatusMuatDetailTransaction.Berhasil(
            TransactionId = id,
            labelIdentitasTransaction = id.sebagaiLabelIdentitasTransaction(),
            labelWaktu = waktuTransactionEpochMili.sebagaiLabelWaktuTransaction(),
            labelPembayaran = labelPembayaran,
            labelStatus = labelStatus,
            labelMeja = labelMeja,
            labelJumlahItem = "${hitungJumlahItemTransaction()} item",
            labelSubtotal = subtotal.sebagaiRupiah(),
            labelPotongan = potongan.sebagaiRupiah(),
            labelBiayaLayanan = biayaLayanan.sebagaiRupiah(),
            labelPajak = pajak.sebagaiRupiah(),
            labelTotalAkhir = totalAkhir.sebagaiRupiah(),
            labelUangDibayar = uangDibayar.sebagaiRupiah(),
            labelKembalian = kembalian.sebagaiRupiah(),
            daftarItem = daftarCartItem.map { CartItem ->
                ItemTampilanDetailTransaction(
                    namaProduk = CartItem.produk.nama,
                    labelJumlahKaliHarga = "${CartItem.jumlah} x ${CartItem.produk.harga.sebagaiRupiah()}",
                    labelSubtotalItem = (CartItem.produk.harga * CartItem.jumlah)
                        .sebagaiRupiah(),
                )
            },
            catatan = catatan,
            dibatalkan = dibatalkan,
            alasanPembatalan = alasanPembatalan,
            paymentMethod = paymentMethod,
            uangDibayar = uangDibayar.nilaiRupiah,
        ),
    )
}
