package id.flexi.kasir.ui.bahan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.flexi.kasir.domain.model.Bahan
import id.flexi.kasir.domain.model.PembelianBahan
import id.flexi.kasir.domain.usecase.AmatiPembelianBahan
import id.flexi.kasir.domain.usecase.CatatPembelianBahan
import id.flexi.kasir.domain.usecase.HapusPembelianBahan
import id.flexi.kasir.domain.usecase.ObserveBahanById
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BahanDetailUiState(
    val apakahSedangMemuat: Boolean = true,
    val bahan: Bahan? = null,
    val daftarPembelian: List<PembelianBahan> = emptyList(),
    val apakahDialogTambahPembelianTampil: Boolean = false,
    val statusHapusPembelian: StatusHapusPembelian = StatusHapusPembelian(),
    val pesanSnackbar: String? = null,
)

data class StatusHapusPembelian(
    val apakahTampil: Boolean = false,
    val id: String = "",
    val bahanId: String = "",
    val jumlah: Double = 0.0,
    val satuan: String = "",
    val totalHarga: Long = 0L,
)

class BahanDetailViewModel(
    private val idBahan: String,
    private val ObserveBahanById: ObserveBahanById,
    private val CatatPembelianBahan: CatatPembelianBahan,
    private val AmatiPembelianBahan: AmatiPembelianBahan,
    private val HapusPembelianBahan: HapusPembelianBahan,
) : ViewModel() {

    private val _state = MutableStateFlow(BahanDetailUiState())
    val amatiState: StateFlow<BahanDetailUiState> = _state.asStateFlow()

    init {
        combine(
            ObserveBahanById(idBahan),
            AmatiPembelianBahan(idBahan),
        ) { bahan, pembelian ->
            _state.update {
                it.copy(
                    apakahSedangMemuat = false,
                    bahan = bahan,
                    daftarPembelian = pembelian,
                )
            }
        }.launchIn(viewModelScope)
    }

    fun bukaDialogTambahPembelian() {
        _state.update { it.copy(apakahDialogTambahPembelianTampil = true) }
    }

    fun tutupDialogTambahPembelian() {
        _state.update { it.copy(apakahDialogTambahPembelianTampil = false) }
    }

    fun simpanPembelian(jumlah: String, satuanBeli: String, totalHarga: String, catatan: String) {
        val jumlahDouble = jumlah.toDoubleOrNull() ?: return
        val totalLong = totalHarga.toLongOrNull() ?: return
        if (jumlahDouble <= 0 || totalLong <= 0) return

        viewModelScope.launch {
            try {
                CatatPembelianBahan(
                    bahanId = idBahan,
                    jumlah = jumlahDouble,
                    satuanBeli = satuanBeli.ifBlank { "pcs" },
                    totalHarga = totalLong,
                    catatan = catatan.ifBlank { null },
                )
                _state.update {
                    it.copy(
                        apakahDialogTambahPembelianTampil = false,
                        pesanSnackbar = "Pembelian berhasil dicatat.",
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(pesanSnackbar = "Gagal: ${e.message}") }
            }
        }
    }

    fun mintaHapusPembelian(pembelian: PembelianBahan) {
        _state.update {
            it.copy(
                statusHapusPembelian = StatusHapusPembelian(
                    apakahTampil = true,
                    id = pembelian.id,
                    bahanId = pembelian.bahanId,
                    jumlah = pembelian.jumlah,
                    satuan = pembelian.satuanBeli,
                    totalHarga = pembelian.totalHarga,
                ),
            )
        }
    }

    fun batalkanHapusPembelian() {
        _state.update { it.copy(statusHapusPembelian = StatusHapusPembelian()) }
    }

    fun konfirmasiHapusPembelian() {
        val status = _state.value.statusHapusPembelian
        viewModelScope.launch {
            try {
                HapusPembelianBahan(
                    id = status.id,
                    bahanId = status.bahanId,
                    jumlah = status.jumlah,
                )
                _state.update {
                    it.copy(
                        statusHapusPembelian = StatusHapusPembelian(),
                        pesanSnackbar = "Pembelian berhasil dihapus.",
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(pesanSnackbar = "Gagal: ${e.message}") }
            }
        }
    }

    fun bersihkanPesan() {
        _state.update { it.copy(pesanSnackbar = null) }
    }
}
