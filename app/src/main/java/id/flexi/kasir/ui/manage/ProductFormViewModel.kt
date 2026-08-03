package id.flexi.kasir.ui.manage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.flexi.kasir.domain.identity.ProductIdGenerator
import id.flexi.kasir.domain.usecase.LoadProductCatalog
import id.flexi.kasir.domain.usecase.ObserveProductById
import id.flexi.kasir.domain.usecase.SaveProduct
import id.flexi.kasir.domain.usecase.DeleteProduct
import id.flexi.kasir.domain.model.Produk
import id.flexi.kasir.domain.model.Varian
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel untuk mengelola logika formulir tambah/ubah produk.
 */
class ProductFormViewModel(
    val idProduk: String?,
    private val amatiProduk: ObserveProductById,
    private val SaveProduct: SaveProduct,
    private val muatKatalog: LoadProductCatalog,
    private val deleteProduct: DeleteProduct,
) : ViewModel() {

    private val _state = MutableStateFlow(ProductFormUiState())
    val amatiState: StateFlow<ProductFormUiState> = _state.asStateFlow()

    private var produkAsli: Produk? = null

    init {
        if (idProduk != null) {
            _state.update { it.copy(judulLayar = "Ubah Produk") }
            muatDataProduk(idProduk)
        }
        muatKatalog.eksekusi().map { katalog ->
            katalog.map { it.kategori }
                .filter { it.isNotBlank() }
                .distinct()
                .sorted()
        }.onEach { kategori ->
            _state.update { it.copy(daftarKategori = kategori) }
        }.launchIn(viewModelScope)
    }

    private fun muatDataProduk(id: String) {
        amatiProduk(id)
            .filterNotNull()
            .onEach { produk ->
                produkAsli = produk
                val varianDraft = produk.varian.map { v ->
                    VarianDraft(nama = v.nama, harga = v.harga.toString())
                }
                _state.update {
                    it.copy(
                        nama = produk.nama,
                        harga = produk.harga.toString(),
                        stok = produk.stokTersedia.toString(),
                        deskripsi = produk.deskripsi,
                        kategori = produk.kategori,
                        fotoUri = produk.fotoUri,
                        favorit = produk.favorit,
                        hargaModal = produk.hargaModal?.toString() ?: "",
                        apakahTampilHargaModal = produk.hargaModal != null,
                        apakahTampilKelolaStok = produk.apakahStokDiaktifkan,
                        apakahTampilVarian = produk.varian.isNotEmpty(),
                        varianDraft = varianDraft,
                        apakahBisaSimpan = true
                    )
                }
            }.launchIn(viewModelScope)
    }

    fun tanganiAksi(aksi: ProductFormAction) {
        when (aksi) {
            is ProductFormAction.UbahNama -> {
                _state.update {
                    it.copy(
                        nama = aksi.nama,
                        pesanKesalahanNama = if (aksi.nama.isBlank()) "Nama tidak boleh kosong" else null
                    )
                }
            }
            is ProductFormAction.UbahHarga -> {
                val hargaBersih = aksi.harga.filter { it.isDigit() }
                _state.update { it.copy(harga = hargaBersih) }
            }
            is ProductFormAction.UbahStok -> {
                val stokBersih = aksi.stok.filter { it.isDigit() }
                _state.update { it.copy(stok = stokBersih) }
            }
            is ProductFormAction.UbahDeskripsi -> {
                _state.update { it.copy(deskripsi = aksi.deskripsi) }
            }
            is ProductFormAction.UbahKategori -> {
                _state.update { it.copy(kategori = aksi.kategori) }
            }
            is ProductFormAction.PilihFoto -> {
                _state.update { it.copy(fotoUri = aksi.uri) }
            }
            ProductFormAction.HapusFoto -> {
                _state.update { it.copy(fotoUri = null) }
            }
            is ProductFormAction.UbahHargaModal -> {
                val hargaModalBersih = aksi.hargaModal.filter { it.isDigit() }
                _state.update { it.copy(hargaModal = hargaModalBersih) }
            }
            ProductFormAction.ToggleFavorit -> {
                _state.update { it.copy(favorit = !it.favorit) }
            }
            ProductFormAction.ToggleTampilHargaModal -> {
                _state.update { it.copy(apakahTampilHargaModal = !it.apakahTampilHargaModal) }
            }
            ProductFormAction.ToggleTampilKelolaStok -> {
                _state.update { it.copy(apakahTampilKelolaStok = !it.apakahTampilKelolaStok) }
            }
            ProductFormAction.ToggleTampilVarian -> {
                val tampil = !_state.value.apakahTampilVarian
                _state.update {
                    it.copy(
                        apakahTampilVarian = tampil,
                        varianDraft = if (tampil && it.varianDraft.isEmpty()) {
                            listOf(VarianDraft())
                        } else if (!tampil) {
                            emptyList()
                        } else {
                            it.varianDraft
                        }
                    )
                }
            }
            is ProductFormAction.TambahVarian -> {
                _state.update {
                    val draftBaru = it.varianDraft + VarianDraft(nama = aksi.nama, harga = aksi.harga)
                    it.copy(varianDraft = draftBaru)
                }
            }
            is ProductFormAction.UbahNamaVarian -> {
                _state.update {
                    val draft = it.varianDraft.toMutableList()
                    if (aksi.indeks in draft.indices) {
                        draft[aksi.indeks] = draft[aksi.indeks].copy(nama = aksi.nama)
                    }
                    it.copy(varianDraft = draft)
                }
            }
            is ProductFormAction.UbahHargaVarian -> {
                _state.update {
                    val draft = it.varianDraft.toMutableList()
                    if (aksi.indeks in draft.indices) {
                        val hargaBersih = aksi.harga.filter { c -> c.isDigit() }
                        draft[aksi.indeks] = draft[aksi.indeks].copy(harga = hargaBersih)
                    }
                    it.copy(varianDraft = draft)
                }
            }
            ProductFormAction.HapusVarianTerakhir -> {
                _state.update {
                    val draft = it.varianDraft.toMutableList()
                    if (draft.isNotEmpty()) {
                        draft.removeAt(draft.lastIndex)
                    }
                    it.copy(
                        varianDraft = draft,
                        apakahTampilVarian = draft.isNotEmpty(),
                    )
                }
            }
            ProductFormAction.MintaHapusProduk -> {
                _state.update { it.copy(apakahTampilDialogHapus = true) }
            }
            ProductFormAction.KonfirmasiHapusProduk -> eksekusiHapus()
            ProductFormAction.BatalkanHapusProduk -> {
                _state.update { it.copy(apakahTampilDialogHapus = false) }
            }
            ProductFormAction.Simpan -> eksekusiSimpan()
        }
        validasiForm()
    }

    private fun validasiForm() {
        val state = _state.value
        val punyaVarianValid = state.varianDraft.any { it.nama.isNotBlank() && it.harga.isNotBlank() }
        _state.update {
            it.copy(
                apakahBisaSimpan = it.nama.isNotBlank() &&
                    (it.harga.isNotBlank() || punyaVarianValid) &&
                    (it.stok.isNotBlank() || !it.apakahTampilKelolaStok)
            )
        }
    }

    private fun eksekusiSimpan() {
        val stateSekarang = _state.value
        if (!stateSekarang.apakahBisaSimpan) return

        _state.update { it.copy(apakahSedangMenyimpan = true) }

        viewModelScope.launch {
            try {
                val varian = stateSekarang.varianDraft
                    .filter { it.nama.isNotBlank() && it.harga.isNotBlank() }
                    .map { Varian(nama = it.nama.trim(), harga = it.harga.toLongOrNull() ?: 0L) }
                    .filter { it.harga > 0 }

                val produkBaru = Produk(
                    id = idProduk ?: ProductIdGenerator.buatIdentitasBaru(stateSekarang.nama),
                    nama = stateSekarang.nama,
                    harga = stateSekarang.harga.toLongOrNull() ?: 0L,
                    stokTersedia = if (stateSekarang.apakahTampilKelolaStok) stateSekarang.stok.toIntOrNull() ?: 0 else 0,
                    kodePindai = produkAsli?.kodePindai,
                    deskripsi = stateSekarang.deskripsi,
                    kategori = stateSekarang.kategori,
                    fotoUri = stateSekarang.fotoUri,
                    favorit = stateSekarang.favorit,
                    hargaModal = if (stateSekarang.apakahTampilHargaModal) stateSekarang.hargaModal.toLongOrNull() else null,
                    aktif = produkAsli?.aktif ?: true,
                    varian = varian,
                    apakahStokDiaktifkan = stateSekarang.apakahTampilKelolaStok,
                )

                SaveProduct.eksekusi(produkBaru)
                _state.update { it.copy(apakahBerhasilDisimpan = true, apakahSedangMenyimpan = false) }
            } catch (e: Exception) {
                _state.update { it.copy(apakahSedangMenyimpan = false) }
            }
        }
    }

    private fun eksekusiHapus() {
        val id = idProduk ?: return
        _state.update { it.copy(apakahSedangMenyimpan = true) }

        viewModelScope.launch {
            try {
                deleteProduct.eksekusi(id)
                _state.update { it.copy(apakahBerhasilDisimpan = true, apakahSedangMenyimpan = false) }
            } catch (_: Exception) {
                _state.update { it.copy(apakahSedangMenyimpan = false, apakahTampilDialogHapus = false) }
            }
        }
    }
}
