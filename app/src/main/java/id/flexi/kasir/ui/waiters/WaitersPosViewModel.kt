package id.flexi.kasir.ui.waiters

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.flexi.kasir.domain.model.Meja
import id.flexi.kasir.domain.model.OrderType
import id.flexi.kasir.domain.model.PaymentMethod
import id.flexi.kasir.domain.model.Produk
import id.flexi.kasir.domain.model.TransactionStatus
import id.flexi.kasir.domain.usecase.CompleteLocalCheckout
import id.flexi.kasir.domain.usecase.GetTableList
import id.flexi.kasir.domain.usecase.LoadProductCatalog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WaitersPosViewModel(
    private val loadProductCatalog: LoadProductCatalog,
    private val getTableList: GetTableList,
    private val completeLocalCheckout: CompleteLocalCheckout,
) : ViewModel() {

    private val _produkList = MutableStateFlow<List<Produk>>(emptyList())
    val produkList: StateFlow<List<Produk>> = _produkList.asStateFlow()

    private val _mejaList = MutableStateFlow<List<Meja>>(emptyList())
    val mejaList: StateFlow<List<Meja>> = _mejaList.asStateFlow()

    private val _pesan = MutableStateFlow<String?>(null)
    val pesan: StateFlow<String?> = _pesan.asStateFlow()

    init {
        viewModelScope.launch {
            loadProductCatalog.eksekusi().collect { list ->
                _produkList.value = list
            }
        }
        viewModelScope.launch {
            getTableList().collect { list ->
                _mejaList.value = list
            }
        }
    }

    fun simpanPesanan(mejaId: String, catatan: String, items: List<WaitersCartItem>) {
        viewModelScope.launch {
            try {
                val total = items.sumOf { it.produk.harga * it.jumlah }
                val jumlahItem = items.sumOf { it.jumlah }
                val cartItems = items.map { item ->
                    id.flexi.kasir.domain.model.CartItem(
                        produk = item.produk,
                        jumlah = item.jumlah,
                        catatan = item.catatan.ifBlank { null },
                    )
                }

                completeLocalCheckout.eksekusi(
                    daftarCartItem = cartItems,
                    paymentMethod = PaymentMethod.Cash,
                    status = TransactionStatus.Pending,
                    uangDibayar = 0,
                    mejaId = mejaId,
                    catatan = catatan.ifBlank { null },
                    orderType = OrderType.DineIn,
                )

                _pesan.value = "Pesanan berhasil disimpan!"
            } catch (e: Exception) {
                _pesan.value = "Gagal menyimpan: ${e.message}"
            }
        }
    }

    fun bersihkanPesan() {
        _pesan.value = null
    }
}
