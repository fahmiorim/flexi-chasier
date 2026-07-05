package id.cassy.kasir.antarmuka.kelola

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.cassy.kasir.ranah.identitas.PembangkitIdentitasProduk
import id.cassy.kasir.ranah.kasuspenggunaan.AmatiProdukBerdasarkanIdentitas
import id.cassy.kasir.ranah.kasuspenggunaan.SimpanProduk
import id.cassy.kasir.ranah.model.Produk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel untuk mengelola logika formulir tambah/ubah produk.
 */
class ViewModelFormProduk(
    private val idProduk: String?,
    private val amatiProduk: AmatiProdukBerdasarkanIdentitas,
    private val simpanProduk: SimpanProduk,
) : ViewModel() {

    private val _state = MutableStateFlow(ModelTampilanFormProduk())
    val amatiState: StateFlow<ModelTampilanFormProduk> = _state.asStateFlow()

    private var produkAsli: Produk? = null

    init {
        if (idProduk != null) {
            _state.update { it.copy(judulLayar = "Ubah Produk") }
            muatDataProduk(idProduk)
        }
    }

    private fun muatDataProduk(id: String) {
        amatiProduk(id)
            .filterNotNull()
            .onEach { produk ->
                produkAsli = produk
                _state.update {
                    it.copy(
                        nama = produk.nama,
                        harga = produk.harga.toString(),
                        stok = produk.stokTersedia.toString(),
                        deskripsi = produk.deskripsi,
                        kategori = produk.kategori,
                        apakahBisaSimpan = true
                    )
                }
            }.launchIn(viewModelScope)
    }

    fun tanganiAksi(aksi: AksiLayarFormProduk) {
        when (aksi) {
            is AksiLayarFormProduk.UbahNama -> {
                _state.update {
                    it.copy(
                        nama = aksi.nama,
                        pesanKesalahanNama = if (aksi.nama.isBlank()) "Nama tidak boleh kosong" else null
                    )
                }
            }
            is AksiLayarFormProduk.UbahHarga -> {
                val hargaBersih = aksi.harga.filter { it.isDigit() }
                _state.update { it.copy(harga = hargaBersih) }
            }
            is AksiLayarFormProduk.UbahStok -> {
                val stokBersih = aksi.stok.filter { it.isDigit() }
                _state.update { it.copy(stok = stokBersih) }
            }
            is AksiLayarFormProduk.UbahDeskripsi -> {
                _state.update { it.copy(deskripsi = aksi.deskripsi) }
            }
            is AksiLayarFormProduk.UbahKategori -> {
                _state.update { it.copy(kategori = aksi.kategori) }
            }
            AksiLayarFormProduk.Simpan -> eksekusiSimpan()
        }
        validasiForm()
    }

    private fun validasiForm() {
        _state.update {
            it.copy(
                apakahBisaSimpan = it.nama.isNotBlank() &&
                                  it.harga.isNotBlank() &&
                                  it.stok.isNotBlank()
            )
        }
    }

    private fun eksekusiSimpan() {
        val stateSekarang = _state.value
        if (!stateSekarang.apakahBisaSimpan) return

        _state.update { it.copy(apakahSedangMenyimpan = true) }

        viewModelScope.launch {
            try {
                val produkBaru = Produk(
                    id = idProduk ?: PembangkitIdentitasProduk.buatIdentitasBaru(stateSekarang.nama),
                    nama = stateSekarang.nama,
                    harga = stateSekarang.harga.toLongOrNull() ?: 0L,
                    stokTersedia = stateSekarang.stok.toIntOrNull() ?: 0,
                    kodePindai = produkAsli?.kodePindai,
                    deskripsi = stateSekarang.deskripsi,
                    kategori = stateSekarang.kategori,
                    aktif = produkAsli?.aktif ?: true
                )

                simpanProduk.eksekusi(produkBaru)
                _state.update { it.copy(apakahBerhasilDisimpan = true, apakahSedangMenyimpan = false) }
            } catch (e: Exception) {
                _state.update { it.copy(apakahSedangMenyimpan = false) }
            }
        }
    }
}
