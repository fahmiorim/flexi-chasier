package id.flexi.kasir.ui.manage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.flexi.kasir.domain.model.Produk
import id.flexi.kasir.domain.repository.BahanRepository
import id.flexi.kasir.domain.usecase.DeleteProduct
import id.flexi.kasir.domain.usecase.LoadBahanCatalog
import id.flexi.kasir.domain.usecase.LoadProductCatalog
import id.flexi.kasir.domain.usecase.SaveProduct
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToLong

class ManageProductsViewModel(
    private val LoadProductCatalog: LoadProductCatalog,
    private val DeleteProduct: DeleteProduct,
    private val SaveProduct: SaveProduct,
    private val BahanRepository: BahanRepository,
    private val LoadBahanCatalog: LoadBahanCatalog,
) : ViewModel() {

    private val _state = MutableStateFlow(ManageProductsUiState())
    val amatiState: StateFlow<ManageProductsUiState> = _state.asStateFlow()

    private val _kataKunci = MutableStateFlow("")
    private val _kategoriFilter = MutableStateFlow("")
    private val _hppMap = MutableStateFlow<Map<String, Long>>(emptyMap())

    private var semuaProduk: List<Produk> = emptyList()

    init {
        // Muat HPP untuk semua produk
        muatHpp()

        combine(
            LoadProductCatalog.eksekusi(),
            _kataKunci,
            _kategoriFilter,
            _hppMap,
        ) { katalog, kataKunci, kategoriFilter, hppMap ->
            semuaProduk = katalog

            val hasilFilter = katalog.filter { produk ->
                val cocokKataKunci = kataKunci.isBlank() ||
                    produk.nama.contains(kataKunci, ignoreCase = true) ||
                    produk.id.contains(kataKunci, ignoreCase = true)
                val cocokKategori = kategoriFilter.isBlank() ||
                    produk.kategori.equals(kategoriFilter, ignoreCase = true)
                cocokKataKunci && cocokKategori
            }

            _state.update {
                it.copy(
                    daftarProduk = hasilFilter,
                    daftarKategori = katalog.map { p -> p.kategori }
                        .filter { it.isNotBlank() }
                        .distinct()
                        .sorted(),
                    kataKunciPencarian = kataKunci,
                    kategoriFilter = kategoriFilter,
                    hppMap = hppMap,
                    apakahSedangMemuat = false,
                )
            }
        }.launchIn(viewModelScope)
    }

    private fun muatHpp() {
        viewModelScope.launch {
            try {
                val bahanList = LoadBahanCatalog().first()
                val semuaResep = BahanRepository.ambilSemuaResepWithBahan()
                val hppMap = mutableMapOf<String, Long>()

                for (resep in semuaResep) {
                    val hpp = resep.daftarBahan.sumOf { bahanResep ->
                        val hargaSatuan = bahanList.firstOrNull { it.id == bahanResep.bahanId }?.hargaPerSatuan ?: 0L
                        (bahanResep.jumlah * hargaSatuan).roundToLong()
                    }
                    if (hpp > 0) {
                        hppMap[resep.produkId] = hpp
                    }
                }
                _hppMap.value = hppMap
            } catch (_: Exception) {
                // Gagal muat HPP — tidak kritis
            }
        }
    }

    fun tanganiAksi(aksi: ManageProductsAction) {
        when (aksi) {
            is ManageProductsAction.MintaDeleteProduct -> {
                _state.update {
                    it.copy(
                        statusKonfirmasiHapus = StatusKonfirmasiDeleteProduct(
                            apakahTampil = true,
                            identitasProduk = aksi.identitasProduk,
                            namaProduk = aksi.namaProduk,
                            deskripsi = "Apakah Anda yakin ingin menghapus produk '${aksi.namaProduk}'? Tindakan ini tidak dapat dibatalkan."
                        )
                    )
                }
            }
            ManageProductsAction.BatalkanDeleteProduct -> {
                _state.update { it.copy(statusKonfirmasiHapus = StatusKonfirmasiDeleteProduct()) }
            }
            ManageProductsAction.KonfirmasiDeleteProduct -> {
                eksekusiHapus()
            }
            is ManageProductsAction.PerbaruiKataKunciPencarian -> {
                _kataKunci.value = aksi.kataKunci
            }
            ManageProductsAction.ResetPencarian -> {
                _kataKunci.value = ""
            }
            is ManageProductsAction.TambahKategori -> {
                val kategoriBaru = aksi.nama.trim()
                if (kategoriBaru.isNotBlank()) {
                    _state.update {
                        val sudahAda = it.daftarKategori.any { k -> k.equals(kategoriBaru, ignoreCase = true) }
                        if (sudahAda) it
                        else it.copy(daftarKategori = (it.daftarKategori + kategoriBaru).sorted())
                    }
                }
            }
            is ManageProductsAction.HapusKategori -> {
                eksekusiHapusKategori(aksi.nama)
            }
            is ManageProductsAction.PerbaruiKategoriFilter -> {
                _kategoriFilter.value = aksi.kategori
            }
        }
    }

    private fun eksekusiHapus() {
        val id = _state.value.statusKonfirmasiHapus.identitasProduk
        viewModelScope.launch {
            DeleteProduct.eksekusi(id)
            _state.update { it.copy(statusKonfirmasiHapus = StatusKonfirmasiDeleteProduct()) }
        }
    }

    private fun eksekusiHapusKategori(namaKategori: String) {
        viewModelScope.launch {
            semuaProduk
                .filter { it.kategori.equals(namaKategori, ignoreCase = true) }
                .forEach { produk ->
                    SaveProduct.eksekusi(produk.copy(kategori = ""))
                }
        }
    }
}
