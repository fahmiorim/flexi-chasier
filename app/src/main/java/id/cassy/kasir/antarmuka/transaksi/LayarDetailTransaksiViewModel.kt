package id.cassy.kasir.antarmuka.transaksi

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import id.cassy.kasir.antarmuka.format.hitungJumlahItemTransaksi
import id.cassy.kasir.antarmuka.format.hitungKembalianTransaksi
import id.cassy.kasir.antarmuka.format.hitungSubtotalTransaksi
import id.cassy.kasir.antarmuka.format.hitungTotalAkhirTransaksi
import id.cassy.kasir.antarmuka.format.sebagaiLabelIdentitasTransaksi
import id.cassy.kasir.antarmuka.format.sebagaiLabelWaktuTransaksi
import id.cassy.kasir.antarmuka.navigasi.TujuanNavigasiKasir
import id.cassy.kasir.ranah.kasuspenggunaan.AmatiTransaksiBerdasarkanIdentitas
import id.cassy.kasir.ranah.fungsi.sebagaiRupiah
import id.cassy.kasir.ranah.model.Transaksi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

private const val kunciIdentitasTransaksi = "identitasTransaksi"

/**
 * Pengelola status untuk layar rincian transaksi tunggal.
 *
 * Pada tahap ini detail transaksi sudah reaktif terhadap perubahan Room
 * dan membaca data melalui use case, bukan langsung ke repository.
 */
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class LayarDetailTransaksiViewModel(
    private val amatiTransaksiBerdasarkanIdentitas: AmatiTransaksiBerdasarkanIdentitas,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val identitasTransaksi: String =
        savedStateHandle.get<String>(kunciIdentitasTransaksi)
            ?: savedStateHandle.toRoute<TujuanNavigasiKasir.DetailTransaksi>().identitasTransaksi

    private val _nomorPermintaanMuatUlang = MutableStateFlow(0)

    /**
     * Aliran status tampilan detail yang diobservasi oleh UI.
     */
    val modelTampilan: StateFlow<ModelTampilanDetailTransaksi> =
        _nomorPermintaanMuatUlang
            .flatMapLatest {
                flow {
                    emit(
                        ModelTampilanDetailTransaksi(
                            judulLayar = "Detail Transaksi",
                            statusMuat = StatusMuatDetailTransaksi.Memuat,
                        ),
                    )

                    emitAll(amatiTransaksiBerdasarkanIdentitas(identitasTransaksi).map { transaksi ->
                        transaksi.keModelTampilanDetailTransaksi(identitasTransaksi)
                    })
                }.catch {
                    emit(
                        ModelTampilanDetailTransaksi(
                            judulLayar = "Detail Transaksi",
                            statusMuat = StatusMuatDetailTransaksi.Gagal(
                                judul = "Gagal memuat detail transaksi",
                                deskripsi = "Terjadi gangguan saat membaca transaksi. Silakan coba lagi.",
                            ),
                        ),
                    )
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = ModelTampilanDetailTransaksi(
                    judulLayar = "Detail Transaksi",
                    statusMuat = StatusMuatDetailTransaksi.Memuat,
                ),
            )

    fun muatUlang() {
        _nomorPermintaanMuatUlang.update { nomorLama ->
            nomorLama + 1
        }
    }
}

private fun Transaksi?.keModelTampilanDetailTransaksi(
    identitasTransaksi: String,
): ModelTampilanDetailTransaksi {
    if (this == null) {
        return ModelTampilanDetailTransaksi(
            judulLayar = "Detail Transaksi",
            statusMuat = StatusMuatDetailTransaksi.Kosong(
                judul = "Transaksi tidak ditemukan",
                deskripsi = "Data untuk ID $identitasTransaksi tidak ada di database lokal.",
            ),
        )
    }

    val subtotal = hitungSubtotalTransaksi()
    val totalAkhir = hitungTotalAkhirTransaksi()
    val kembalian = hitungKembalianTransaksi()

    return ModelTampilanDetailTransaksi(
        judulLayar = "Detail Transaksi",
        statusMuat = StatusMuatDetailTransaksi.Berhasil(
            transaksiId = id,
            labelIdentitasTransaksi = id.sebagaiLabelIdentitasTransaksi(),
            labelWaktu = waktuTransaksiEpochMili.sebagaiLabelWaktuTransaksi(),
            labelJumlahItem = "${hitungJumlahItemTransaksi()} item",
            labelSubtotal = subtotal.sebagaiRupiah(),
            labelPotongan = potongan.sebagaiRupiah(),
            labelBiayaLayanan = biayaLayanan.sebagaiRupiah(),
            labelPajak = pajak.sebagaiRupiah(),
            labelTotalAkhir = totalAkhir.sebagaiRupiah(),
            labelUangDibayar = uangDibayar.sebagaiRupiah(),
            labelKembalian = kembalian.sebagaiRupiah(),
            daftarItem = daftarItemKeranjang.map { itemKeranjang ->
                ItemTampilanDetailTransaksi(
                    namaProduk = itemKeranjang.produk.nama,
                    labelJumlahKaliHarga = "${itemKeranjang.jumlah} x ${itemKeranjang.produk.harga.sebagaiRupiah()}",
                    labelSubtotalItem = (itemKeranjang.produk.harga * itemKeranjang.jumlah)
                        .sebagaiRupiah(),
                )
            },
            catatan = catatan,
        ),
    )
}
