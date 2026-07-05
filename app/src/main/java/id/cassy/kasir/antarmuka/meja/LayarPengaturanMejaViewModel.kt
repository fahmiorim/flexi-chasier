package id.cassy.kasir.antarmuka.meja

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.cassy.kasir.ranah.kasuspenggunaan.AmbilDaftarMeja
import id.cassy.kasir.ranah.kasuspenggunaan.HapusMeja
import id.cassy.kasir.ranah.kasuspenggunaan.SimpanMeja
import id.cassy.kasir.ranah.model.Meja
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LayarPengaturanMejaViewModel(
    private val ambilDaftarMeja: AmbilDaftarMeja,
    private val simpanMeja: SimpanMeja,
    private val hapusMeja: HapusMeja,
) : ViewModel() {

    private val _state = MutableStateFlow(ModelTampilanPengaturanMeja())
    val state: StateFlow<ModelTampilanPengaturanMeja> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            ambilDaftarMeja().collect { daftar ->
                _state.update { it.copy(daftarMeja = daftar) }
            }
        }
    }

    fun perbaruiNomorMejaBaru(nomor: String) {
        _state.update { it.copy(nomorMejaBaru = nomor.filter { c -> c.isDigit() }) }
    }

    fun tambahMeja() {
        val nomor = _state.value.nomorMejaBaru
        if (nomor.isBlank()) return

        val sudahAda = _state.value.daftarMeja.any { it.nomor == nomor }
        if (sudahAda) {
            _state.update { it.copy(pesanError = "Meja $nomor sudah ada.", nomorMejaBaru = "") }
            return
        }

        viewModelScope.launch {
            simpanMeja(
                Meja(
                    id = "meja_$nomor",
                    nomor = nomor,
                    aktif = true,
                ),
            )
            _state.update { it.copy(nomorMejaBaru = "", pesanError = null) }
        }
    }

    fun hapusMeja(id: String) {
        viewModelScope.launch {
            hapusMeja(id)
        }
    }

    fun bersihkanPesanError() {
        _state.update { it.copy(pesanError = null) }
    }
}
