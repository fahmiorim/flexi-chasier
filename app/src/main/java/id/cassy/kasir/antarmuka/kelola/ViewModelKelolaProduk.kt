package id.cassy.kasir.antarmuka.kelola

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.cassy.kasir.ranah.kasuspenggunaan.HapusProduk
import id.cassy.kasir.ranah.kasuspenggunaan.MuatKatalogProduk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel untuk mengelola logika bisnis layar kelola produk.
 */
class ViewModelKelolaProduk(
    private val muatKatalogProduk: MuatKatalogProduk,
    private val hapusProduk: HapusProduk,
) : ViewModel() {

    private val _state = MutableStateFlow(ModelTampilanKelolaProduk())
    val amatiState: StateFlow<ModelTampilanKelolaProduk> = _state.asStateFlow()

    private val _kataKunci = MutableStateFlow("")

    init {
        // Menggabungkan aliran data katalog dengan filter pencarian.
        combine(
            muatKatalogProduk.eksekusi(),
            _kataKunci
        ) { katalog, kataKunci ->
            val hasilFilter = if (kataKunci.isBlank()) {
                katalog
            } else {
                katalog.filter { produk ->
                    produk.nama.contains(kataKunci, ignoreCase = true) ||
                    produk.id.contains(kataKunci, ignoreCase = true)
                }
            }

            _state.update {
                it.copy(
                    daftarProduk = hasilFilter,
                    kataKunciPencarian = kataKunci,
                    apakahSedangMemuat = false
                )
            }
        }.launchIn(viewModelScope)
    }

    /**
     * Menangani aksi pengguna.
     */
    fun tanganiAksi(aksi: AksiLayarKelolaProduk) {
        when (aksi) {
            is AksiLayarKelolaProduk.MintaHapusProduk -> {
                _state.update {
                    it.copy(
                        statusKonfirmasiHapus = StatusKonfirmasiHapusProduk(
                            apakahTampil = true,
                            identitasProduk = aksi.identitasProduk,
                            namaProduk = aksi.namaProduk,
                            deskripsi = "Apakah Anda yakin ingin menghapus produk '${aksi.namaProduk}'? Tindakan ini tidak dapat dibatalkan."
                        )
                    )
                }
            }
            AksiLayarKelolaProduk.BatalkanHapusProduk -> {
                _state.update { it.copy(statusKonfirmasiHapus = StatusKonfirmasiHapusProduk()) }
            }
            AksiLayarKelolaProduk.KonfirmasiHapusProduk -> {
                eksekusiHapus()
            }
            is AksiLayarKelolaProduk.PerbaruiKataKunciPencarian -> {
                _kataKunci.value = aksi.kataKunci
            }
            AksiLayarKelolaProduk.ResetPencarian -> {
                _kataKunci.value = ""
            }
        }
    }

    private fun eksekusiHapus() {
        val id = _state.value.statusKonfirmasiHapus.identitasProduk
        viewModelScope.launch {
            hapusProduk.eksekusi(id)
            _state.update { it.copy(statusKonfirmasiHapus = StatusKonfirmasiHapusProduk()) }
        }
    }
}
