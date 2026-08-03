package id.flexi.kasir.ui.table

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.flexi.kasir.domain.model.Meja
import id.flexi.kasir.domain.usecase.DeleteTable
import id.flexi.kasir.domain.usecase.GetTableList
import id.flexi.kasir.domain.usecase.SaveTable
import id.flexi.kasir.domain.usecase.SimpanStorePreference
import id.flexi.kasir.domain.usecase.AmatiStorePreference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsScreenMejaViewModel(
    private val GetTableList: GetTableList,
    private val SaveTable: SaveTable,
    private val DeleteTable: DeleteTable,
    private val amatiStorePreference: AmatiStorePreference,
    private val simpanStorePreference: SimpanStorePreference,
) : ViewModel() {

    fun resetSemuaStatusMeja() {
        viewModelScope.launch {
            val daftar = _state.value.daftarMeja
            for (meja in daftar) {
                if (meja.tableStatus != id.flexi.kasir.domain.model.TableStatus.Available) {
                    SaveTable(meja.copy(
                        tableStatus = id.flexi.kasir.domain.model.TableStatus.Available,
                        TransactionId = null,
                        waktuDudukEpochMili = null,
                    ))
                }
            }
        }
    }

    private val _state = MutableStateFlow(SettingsUiStateMeja())
    val state: StateFlow<SettingsUiStateMeja> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(amatiStorePreference(), GetTableList()) { pref, daftar ->
                val maxBaris = daftar.maxOfOrNull { meja ->
                    meja.posisiGrid()?.first?.plus(1) ?: 0
                } ?: 0
                val maxKolom = daftar.maxOfOrNull { meja ->
                    meja.posisiGrid()?.second?.plus(1) ?: 0
                } ?: 0
                val baris = when {
                    pref.gridBaris > 0 -> pref.gridBaris
                    maxBaris > 0 -> maxBaris
                    else -> 3
                }
                val kolom = when {
                    pref.gridKolom > 0 -> pref.gridKolom
                    maxKolom > 0 -> maxKolom
                    else -> 4
                }
                Pair(daftar, baris to kolom)
            }.collect { (daftar, barisKolom) ->
                _state.update { st ->
                    st.copy(
                        daftarMeja = daftar,
                        barisStr = barisKolom.first.coerceAtLeast(1).toString(),
                        kolomStr = barisKolom.second.coerceAtLeast(1).toString(),
                    )
                }
            }
        }
    }

    fun simpanGrid() {
        viewModelScope.launch {
            val state = _state.value
            val b = state.jumlahBaris
            val k = state.jumlahKolom
            if (b in 1..12 && k in 1..12) {
                val pref = amatiStorePreference().first()
                simpanStorePreference(pref.copy(gridBaris = b, gridKolom = k))
                _state.update { it.copy(pesanSnackbar = "Grid ${b}×${k} tersimpan") }
            }
        }
    }

    fun perbaruiJumlahBaris(baris: String) {
        val bersih = baris.filter { it.isDigit() }.take(2)
        val lama = _state.value.jumlahBaris
        _state.update { it.copy(barisStr = baris) }
        val angka = bersih.toIntOrNull()
        if (angka != null && angka in 1..12 && angka < lama) {
            hapusMejaDiLuarGrid(angka, _state.value.jumlahKolom)
        }
    }

    fun perbaruiJumlahKolom(kolom: String) {
        val bersih = kolom.filter { it.isDigit() }.take(2)
        val lama = _state.value.jumlahKolom
        _state.update { it.copy(kolomStr = kolom) }
        val angka = bersih.toIntOrNull()
        if (angka != null && angka in 1..12 && angka < lama) {
            hapusMejaDiLuarGrid(_state.value.jumlahBaris, angka)
        }
    }

    fun aturMeja(baris: Int, kolom: Int, nomor: String?) {
        val id = "grid_${baris}_${kolom}"
        viewModelScope.launch {
            if (nomor == null) {
                hapusMeja(id)
            } else {
                val sudahAda = _state.value.daftarMeja.any { it.id == id }
                if (!sudahAda) {
                    val duplikat = _state.value.daftarMeja.any { it.nomor == nomor }
                    if (duplikat) {
                        _state.update { it.copy(pesanError = "Nomor meja $nomor sudah ada.") }
                        return@launch
                    }
                }
                SaveTable(
                    Meja(
                        id = id,
                        nomor = nomor,
                        aktif = true,
                    ),
                )
            }
        }
    }

    fun hapusMeja(id: String) {
        viewModelScope.launch {
            DeleteTable(id)
        }
    }

    fun bersihkanPesanError() {
        _state.update { it.copy(pesanError = null, pesanSnackbar = null) }
    }

    private fun hapusMejaDiLuarGrid(baris: Int, kolom: Int) {
        val hapus = _state.value.daftarMeja.filter { meja ->
            val pos = meja.posisiGrid()
            pos == null || pos.first >= baris || pos.second >= kolom
        }
        viewModelScope.launch {
            hapus.forEach { DeleteTable(it.id) }
        }
    }
}
