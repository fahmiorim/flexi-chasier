package id.flexi.kasir.ui.top

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.flexi.kasir.domain.model.PaymentStatus
import id.flexi.kasir.domain.model.Transaction
import id.flexi.kasir.domain.usecase.ObserveTransactionHistory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class TopProductsViewModel(
    private val observeTransactionHistory: ObserveTransactionHistory,
) : ViewModel() {

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        viewModelScope.launch {
            val sejak90Hari = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_MONTH, -90)
            }.timeInMillis
            observeTransactionHistory().collect { list ->
                _transactions.value = list.filter {
                    it.paymentStatus == PaymentStatus.SudahDibayar && !it.dibatalkan
                }
                _isLoading.value = false
            }
        }
    }
}
