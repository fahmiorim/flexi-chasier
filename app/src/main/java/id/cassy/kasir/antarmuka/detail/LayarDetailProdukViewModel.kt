package id.cassy.kasir.antarmuka.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import id.cassy.kasir.antarmuka.navigasi.TujuanNavigasiKasir
import id.cassy.kasir.ranah.fungsi.sebagaiRupiah
import id.cassy.kasir.ranah.kasuspenggunaan.AmatiProdukBerdasarkanIdentitas
import id.cassy.kasir.ranah.model.Produk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Pengelola status layar detail produk.
 *
 * Detail produk membaca data dari Room melalui use case, bukan lagi dari data contoh.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LayarDetailProdukViewModel(
    private val amatiProdukBerdasarkanIdentitas: AmatiProdukBerdasarkanIdentitas,
    statusTersimpan: SavedStateHandle,
) : ViewModel() {

    private val identitasProduk: String =
        statusTersimpan.toRoute<TujuanNavigasiKasir.DetailProduk>().identitasProduk

    private val nomorPermintaanMuatUlang = MutableStateFlow(0)

    val modelTampilan = nomorPermintaanMuatUlang
        .flatMapLatest {
            bentukAlurModelTampilan()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ModelTampilanDetailProduk(
                produkId = identitasProduk,
                judulLayar = "Detail Produk",
                statusMuat = StatusMuatDetailProduk.Memuat,
            ),
        )

    private val _efek = MutableSharedFlow<EfekLayarDetailProduk>(
        extraBufferCapacity = 1,
    )

    val efek: SharedFlow<EfekLayarDetailProduk> = _efek.asSharedFlow()

    /**
     * Titik masuk tunggal untuk aksi UI layar detail produk.
     */
    fun tanganiAksi(aksi: AksiLayarDetailProduk) {
        when (aksi) {
            AksiLayarDetailProduk.CobaTambahKeKeranjang -> cobaTambahKeKeranjang()
            AksiLayarDetailProduk.CobaMuatUlang -> muatUlang()
        }
    }

    fun muatUlang() {
        nomorPermintaanMuatUlang.value = nomorPermintaanMuatUlang.value + 1
    }

    private fun bentukAlurModelTampilan(): Flow<ModelTampilanDetailProduk> {
        return flow {
            emit(
                ModelTampilanDetailProduk(
                    produkId = identitasProduk,
                    judulLayar = "Detail Produk",
                    statusMuat = StatusMuatDetailProduk.Memuat,
                ),
            )

            emitAll(
                amatiProdukBerdasarkanIdentitas(
                    identitasProduk = identitasProduk,
                ).map { produk ->
                    produk.keModelTampilanDetailProduk()
                },
            )
        }.catch {
            emit(
                ModelTampilanDetailProduk(
                    produkId = identitasProduk,
                    judulLayar = "Detail Produk",
                    statusMuat = StatusMuatDetailProduk.Gagal(
                        judul = "Gagal memuat detail produk",
                        deskripsi = "Terjadi gangguan saat memuat detail produk. Silakan coba lagi.",
                    ),
                ),
            )
        }
    }

    private fun Produk?.keModelTampilanDetailProduk(): ModelTampilanDetailProduk {
        if (this == null) {
            return ModelTampilanDetailProduk(
                produkId = identitasProduk,
                judulLayar = "Detail Produk",
                statusMuat = StatusMuatDetailProduk.Kosong(
                    judul = "Produk tidak ditemukan",
                    deskripsi = "Produk ini belum tersedia di katalog lokal.",
                ),
            )
        }

        val aksiTambahAktif = aktif && stokTersedia > 0

        return ModelTampilanDetailProduk(
            produkId = id,
            judulLayar = "Detail Produk",
            statusMuat = StatusMuatDetailProduk.Berhasil(
                namaProduk = nama,
                hargaProduk = harga.sebagaiRupiah(),
                stokTersedia = stokTersedia,
                deskripsiProduk = deskripsi.ifBlank {
                    "Belum ada keterangan tambahan untuk produk ini."
                },
                statusAksi = StatusAksiDetailProduk(
                    label = if (aksiTambahAktif) {
                        "Tambah ke Keranjang"
                    } else {
                        "Stok Habis"
                    },
                    aktif = aksiTambahAktif,
                    keterangan = if (aksiTambahAktif) {
                        "Produk siap ditambahkan ke transaksi aktif."
                    } else {
                        "Produk ini tidak bisa ditambahkan karena stok di toko sedang kosong."
                    },
                ),
            ),
        )
    }

    private fun cobaTambahKeKeranjang() {
        val statusMuatSaatIni = modelTampilan.value.statusMuat

        if (statusMuatSaatIni !is StatusMuatDetailProduk.Berhasil) return
        if (!statusMuatSaatIni.statusAksi.aktif) return

        _efek.tryEmit(
            EfekLayarDetailProduk.MintaTambahKeKeranjang(
                produkId = identitasProduk,
                namaProduk = statusMuatSaatIni.namaProduk,
            ),
        )
    }
}
