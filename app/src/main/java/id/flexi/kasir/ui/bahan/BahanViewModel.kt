package id.flexi.kasir.ui.bahan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.flexi.kasir.domain.usecase.HapusBahan
import id.flexi.kasir.domain.usecase.LoadBahanCatalog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BahanViewModel(
    private val LoadBahanCatalog: LoadBahanCatalog,
    private val HapusBahan: HapusBahan,
) : ViewModel() {

    private val _state = MutableStateFlow(BahanUiState())
    val amatiState: StateFlow<BahanUiState> = _state.asStateFlow()

    private val _kataKunci = MutableStateFlow("")

    init {
        combine(
            LoadBahanCatalog(),
            _kataKunci,
        ) { daftar, kataKunci ->
            val hasilFilter = if (kataKunci.isBlank()) {
                daftar
            } else {
                daftar.filter { bahan ->
                    bahan.nama.contains(kataKunci, ignoreCase = true) ||
                        bahan.id.contains(kataKunci, ignoreCase = true)
                }
            }

            _state.update {
                it.copy(
                    daftarBahan = hasilFilter,
                    kataKunciPencarian = kataKunci,
                    apakahSedangMemuat = false,
                )
            }
        }.launchIn(viewModelScope)
    }

    fun tanganiAksi(aksi: BahanAction) {
        when (aksi) {
            is BahanAction.MintaHapus -> {
                _state.update {
                    it.copy(
                        statusKonfirmasiHapus = StatusKonfirmasiHapusBahan(
                            apakahTampil = true,
                            idBahan = aksi.idBahan,
                            namaBahan = aksi.namaBahan,
                        ),
                    )
                }
            }
            BahanAction.BatalkanHapus -> {
                _state.update { it.copy(statusKonfirmasiHapus = StatusKonfirmasiHapusBahan()) }
            }
            BahanAction.KonfirmasiHapus -> {
                val id = _state.value.statusKonfirmasiHapus.idBahan
                if (id.isNotBlank()) {
                    viewModelScope.launch {
                        HapusBahan(id)
                        _state.update { it.copy(statusKonfirmasiHapus = StatusKonfirmasiHapusBahan()) }
                    }
                }
            }
            is BahanAction.PerbaruiKataKunciPencarian -> {
                _kataKunci.value = aksi.kataKunci
            }
            BahanAction.ResetPencarian -> {
                _kataKunci.value = ""
            }
        }
    }
}
