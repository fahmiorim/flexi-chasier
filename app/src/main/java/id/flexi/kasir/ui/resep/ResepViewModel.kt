package id.flexi.kasir.ui.resep

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.flexi.kasir.domain.model.Bahan
import id.flexi.kasir.domain.model.BahanResep
import id.flexi.kasir.domain.model.Produk
import id.flexi.kasir.domain.model.Resep
import id.flexi.kasir.domain.repository.BahanRepository
import id.flexi.kasir.domain.usecase.AmatiResepByProduk
import id.flexi.kasir.domain.usecase.HapusResep
import id.flexi.kasir.domain.usecase.LoadBahanCatalog
import id.flexi.kasir.domain.usecase.LoadProductCatalog
import id.flexi.kasir.domain.usecase.SimpanResep
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProdukWithResep(
    val produk: Produk,
    val apakahPunyaResep: Boolean,
)

data class ResepUiState(
    val daftarProduk: List<ProdukWithResep> = emptyList(),
    val apakahSedangMemuat: Boolean = true,
    val kataKunciPencarian: String = "",
    val produkTerpilih: Produk? = null,
    val resepSaatIni: Resep? = null,
    val daftarBahan: List<Bahan> = emptyList(),
    val apakahDialogEditTampil: Boolean = false,
    val pesanSnackbar: String? = null,
)

class ResepViewModel(
    private val LoadProductCatalog: LoadProductCatalog,
    private val LoadBahanCatalog: LoadBahanCatalog,
    private val SimpanResep: SimpanResep,
    private val HapusResep: HapusResep,
    private val AmatiResepByProduk: AmatiResepByProduk,
    private val BahanRepository: BahanRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ResepUiState())
    val amatiState: StateFlow<ResepUiState> = _state.asStateFlow()

    private val _kataKunci = MutableStateFlow("")
    private val _resepProdukIds = MutableStateFlow<Set<String>>(emptySet())

    init {
        // Load bahan list & resep status
        viewModelScope.launch {
            val bahanList = LoadBahanCatalog().first()
            _state.update { it.copy(daftarBahan = bahanList) }

            try {
                val semuaResep = BahanRepository.ambilSemuaResepWithBahan()
                _resepProdukIds.value = semuaResep.map { it.produkId }.toSet()
            } catch (_: Exception) { }
        }

        combine(
            LoadProductCatalog.eksekusi(),
            _kataKunci,
            _resepProdukIds,
        ) { produkList, kataKunci, resepIds ->
            val hasilFilter = if (kataKunci.isBlank()) produkList
            else produkList.filter { it.nama.contains(kataKunci, ignoreCase = true) }

            _state.update {
                it.copy(
                    daftarProduk = hasilFilter.map { p ->
                        ProdukWithResep(p, p.id in resepIds)
                    },
                    kataKunciPencarian = kataKunci,
                    apakahSedangMemuat = false,
                )
            }
        }.launchIn(viewModelScope)
    }

    fun perbaruiKataKunci(kataKunci: String) {
        _kataKunci.value = kataKunci
    }

    fun resetPencarian() {
        _kataKunci.value = ""
    }

    fun bukaEditResep(produk: Produk) {
        viewModelScope.launch {
            val resep = AmatiResepByProduk(produk.id).first()
            _state.update {
                it.copy(
                    produkTerpilih = produk,
                    resepSaatIni = resep,
                    apakahDialogEditTampil = true,
                )
            }
        }
    }

    fun tutupDialogEdit() {
        _state.update {
            it.copy(
                apakahDialogEditTampil = false,
                produkTerpilih = null,
                resepSaatIni = null,
            )
        }
    }

    fun simpanResep(daftarBahanResep: List<BahanResep>) {
        val produk = _state.value.produkTerpilih ?: return
        val resep = _state.value.resepSaatIni

        viewModelScope.launch {
            try {
                SimpanResep(
                    id = resep?.id,
                    produkId = produk.id,
                    varianNama = null,
                    daftarBahan = daftarBahanResep,
                )
                // Refresh status
                val resepBaru = AmatiResepByProduk(produk.id).first()
                _resepProdukIds.value = _resepProdukIds.value + produk.id
                _state.update {
                    it.copy(
                        resepSaatIni = resepBaru,
                        apakahDialogEditTampil = false,
                        pesanSnackbar = "Resep untuk '${produk.nama}' berhasil disimpan.",
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(pesanSnackbar = "Gagal menyimpan: ${e.message}") }
            }
        }
    }

    fun hapusResep() {
        val resep = _state.value.resepSaatIni ?: return
        val produk = _state.value.produkTerpilih ?: return

        viewModelScope.launch {
            try {
                HapusResep(resep.id)
                _resepProdukIds.value = _resepProdukIds.value - produk.id
                _state.update {
                    it.copy(
                        resepSaatIni = null,
                        apakahDialogEditTampil = false,
                        pesanSnackbar = "Resep untuk '${produk.nama}' berhasil dihapus.",
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(pesanSnackbar = "Gagal menghapus: ${e.message}") }
            }
        }
    }

    fun bersihkanPesan() {
        _state.update { it.copy(pesanSnackbar = null) }
    }
}
