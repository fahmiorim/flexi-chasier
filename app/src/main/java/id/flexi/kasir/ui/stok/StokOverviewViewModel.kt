package id.flexi.kasir.ui.stok

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.flexi.kasir.domain.model.Produk
import id.flexi.kasir.domain.usecase.LoadProductCatalog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StokOverviewViewModel(
    private val loadProductCatalog: LoadProductCatalog,
) : ViewModel() {

    private val _produkList = MutableStateFlow<List<Produk>>(emptyList())
    val produkList: StateFlow<List<Produk>> = _produkList.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        viewModelScope.launch {
            loadProductCatalog.eksekusi().collect { list ->
                _produkList.value = list
                _isLoading.value = false
            }
        }
    }
}
