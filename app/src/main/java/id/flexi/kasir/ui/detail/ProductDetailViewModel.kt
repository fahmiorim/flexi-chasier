package id.flexi.kasir.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import id.flexi.kasir.ui.navigation.CashierNavigationDestination
import id.flexi.kasir.domain.util.sebagaiRupiah
import id.flexi.kasir.domain.usecase.AmatiResepByProduk
import id.flexi.kasir.domain.usecase.LoadBahanCatalog
import id.flexi.kasir.domain.usecase.ObserveProductById
import id.flexi.kasir.domain.model.Bahan
import id.flexi.kasir.domain.model.Produk
import id.flexi.kasir.domain.model.Resep
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlin.math.roundToLong

/**
 * Pengelola status layar detail produk.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProductDetailViewModel(
    private val ObserveProductById: ObserveProductById,
    private val AmatiResepByProduk: AmatiResepByProduk,
    private val LoadBahanCatalog: LoadBahanCatalog,
    statusTersimpan: SavedStateHandle,
) : ViewModel() {

    private val identitasProduk: String =
        statusTersimpan.toRoute<CashierNavigationDestination.DetailProduk>().identitasProduk

    private val nomorPermintaanMuatUlang = MutableStateFlow(0)

    /** Semua bahan baku (untuk cari harga per satuan). */
    private val daftarBahan = LoadBahanCatalog()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val modelTampilan = nomorPermintaanMuatUlang
        .flatMapLatest {
            bentukAlurModelTampilan()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ProductDetailUiState(
                produkId = identitasProduk,
                judulLayar = "Detail Produk",
                statusMuat = ProductDetailLoadStatus.Memuat,
            ),
        )

    private val _efek = MutableSharedFlow<ProductDetailEffect>(
        extraBufferCapacity = 1,
    )

    val efek: SharedFlow<ProductDetailEffect> = _efek.asSharedFlow()

    fun tanganiAksi(aksi: ProductDetailAction) {
        when (aksi) {
            ProductDetailAction.CobaTambahKeKeranjang -> cobaTambahKeKeranjang()
            ProductDetailAction.CobaMuatUlang -> muatUlang()
        }
    }

    fun muatUlang() {
        nomorPermintaanMuatUlang.value = nomorPermintaanMuatUlang.value + 1
    }

    private fun bentukAlurModelTampilan(): Flow<ProductDetailUiState> {
        return flow {
            emit(
                ProductDetailUiState(
                    produkId = identitasProduk,
                    judulLayar = "Detail Produk",
                    statusMuat = ProductDetailLoadStatus.Memuat,
                ),
            )

            emitAll(
                combine(
                    ObserveProductById(identitasProduk = identitasProduk),
                    AmatiResepByProduk(identitasProduk),
                ) { produk, resep ->
                    produk.keProductDetailUiState(resep, daftarBahan.value)
                },
            )
        }.catch {
            emit(
                ProductDetailUiState(
                    produkId = identitasProduk,
                    judulLayar = "Detail Produk",
                    statusMuat = ProductDetailLoadStatus.Gagal(
                        judul = "Gagal memuat detail produk",
                        deskripsi = "Terjadi gangguan saat memuat detail produk. Silakan coba lagi.",
                    ),
                ),
            )
        }
    }

    private fun Produk?.keProductDetailUiState(
        resep: Resep?,
        bahanList: List<Bahan>,
    ): ProductDetailUiState {
        if (this == null) {
            return ProductDetailUiState(
                produkId = identitasProduk,
                judulLayar = "Detail Produk",
                statusMuat = ProductDetailLoadStatus.Kosong(
                    judul = "Produk tidak ditemukan",
                    deskripsi = "Produk ini belum tersedia di katalog lokal.",
                ),
            )
        }

        val aksiTambahAktif = aktif && (!apakahStokDiaktifkan || stokTersedia > 0)

        // Hitung HPP dari resep
        val hppPerUnit = if (resep != null && resep.daftarBahan.isNotEmpty()) {
            resep.daftarBahan.sumOf { bahanResep ->
                val hargaSatuan = bahanList.firstOrNull { it.id == bahanResep.bahanId }?.hargaPerSatuan ?: 0L
                (bahanResep.jumlah * hargaSatuan).roundToLong()
            }
        } else {
            0L
        }

        val margin = if (hppPerUnit > 0 && harga > 0) {
            harga - hppPerUnit
        } else {
            0L
        }

        val persenMargin = if (hppPerUnit > 0 && harga > 0) {
            ((margin.toDouble() / harga) * 100).roundToLong()
        } else {
            0L
        }

        return ProductDetailUiState(
            produkId = id,
            judulLayar = "Detail Produk",
            statusMuat = ProductDetailLoadStatus.Berhasil(
                namaProduk = nama,
                hargaProduk = harga.sebagaiRupiah(),
                hppProduk = if (hppPerUnit > 0) hppPerUnit.sebagaiRupiah() else null,
                marginProduk = if (hppPerUnit > 0) "${
                    margin.sebagaiRupiah()
                } ($persenMargin%)" else null,
                stokTersedia = stokTersedia,
                apakahStokDiaktifkan = apakahStokDiaktifkan,
                deskripsiProduk = deskripsi.ifBlank {
                    "Belum ada keterangan tambahan untuk produk ini."
                },
                fotoUri = fotoUri,
                statusAksi = ProductDetailActionStatus(
                    label = if (aksiTambahAktif) {
                        "Tambah ke Keranjang"
                    } else {
                        "Stok Habis"
                    },
                    aktif = aksiTambahAktif,
                    keterangan = if (aksiTambahAktif) {
                        "Produk siap ditambahkan ke Transaction aktif."
                    } else {
                        "Produk ini tidak bisa ditambahkan karena stok di toko sedang kosong."
                    },
                ),
            ),
        )
    }

    private fun cobaTambahKeKeranjang() {
        val statusMuatSaatIni = modelTampilan.value.statusMuat

        if (statusMuatSaatIni !is ProductDetailLoadStatus.Berhasil) return
        if (!statusMuatSaatIni.statusAksi.aktif) return

        _efek.tryEmit(
            ProductDetailEffect.MintaTambahKeKeranjang(
                produkId = identitasProduk,
                namaProduk = statusMuatSaatIni.namaProduk,
            ),
        )
    }
}
