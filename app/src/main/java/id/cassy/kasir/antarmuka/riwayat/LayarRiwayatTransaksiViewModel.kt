package id.cassy.kasir.antarmuka.riwayat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.cassy.kasir.antarmuka.format.hitungJumlahItemTransaksi
import id.cassy.kasir.antarmuka.format.hitungTotalAkhirTransaksi
import id.cassy.kasir.antarmuka.format.sebagaiLabelIdentitasTransaksi
import id.cassy.kasir.antarmuka.format.sebagaiLabelWaktuTransaksi
import id.cassy.kasir.ranah.kasuspenggunaan.AmatiRiwayatTransaksi
import id.cassy.kasir.ranah.fungsi.sebagaiRupiah
import id.cassy.kasir.ranah.model.StatusTransaksi
import id.cassy.kasir.ranah.model.Transaksi
import java.util.Calendar
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class LayarRiwayatTransaksiViewModel(
    private val amatiRiwayatTransaksi: AmatiRiwayatTransaksi,
) : ViewModel() {

    private val _nomorPermintaanMuatUlang = MutableStateFlow(0)
    private val _kataKunciPencarian = MutableStateFlow("")
    private val _filterTanggal = MutableStateFlow(FilterTanggalRiwayat.Semua)

    private val kataKunciPencarianEfektif =
        _kataKunciPencarian
            .debounce(250)
            .map { kataKunci ->
                kataKunci.trim()
            }
            .distinctUntilChanged()

    private val statusSumberData: StateFlow<StatusSumberDataRiwayat> =
        _nomorPermintaanMuatUlang
            .flatMapLatest {
                flow {
                    emit(StatusSumberDataRiwayat.Memuat)

                    emitAll(
                        amatiRiwayatTransaksi().map { daftarTransaksi ->
                            StatusSumberDataRiwayat.Berhasil(
                                daftarTransaksi = daftarTransaksi,
                            )
                        },
                    )
                }.catch {
                    emit(
                        StatusSumberDataRiwayat.Gagal(
                            pesan = "Terjadi gangguan saat membaca daftar transaksi. Silakan coba lagi.",
                        ),
                    )
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = StatusSumberDataRiwayat.Memuat,
            )

    val modelTampilan: StateFlow<ModelTampilanRiwayatTransaksi> =
        combine(
            _kataKunciPencarian,
            kataKunciPencarianEfektif,
            statusSumberData,
            _filterTanggal,
        ) { kataKunciMentah, kataKunciEfektif, statusSumber, filterTanggal ->
            bentukModelTampilan(
                kataKunciMentah = kataKunciMentah,
                kataKunciEfektif = kataKunciEfektif,
                statusSumber = statusSumber,
                filterTanggal = filterTanggal,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ModelTampilanRiwayatTransaksi(
                judulLayar = "Riwayat Transaksi",
                kataKunciPencarian = "",
                tampilkanAksiResetPencarian = false,
                statusMuat = StatusMuatRiwayatTransaksi.Memuat,
            ),
        )

    init {
        muatUlang()
    }

    fun muatUlang() {
        _nomorPermintaanMuatUlang.update { nomorLama ->
            nomorLama + 1
        }
    }

    fun perbaruiKataKunciPencarian(
        kataKunciBaru: String,
    ) {
        _kataKunciPencarian.value = kataKunciBaru
    }

    fun resetPencarian() {
        _kataKunciPencarian.value = ""
    }

    fun perbaruiFilterTanggal(filter: FilterTanggalRiwayat) {
        _filterTanggal.value = filter
    }

    private fun bentukModelTampilan(
        kataKunciMentah: String,
        kataKunciEfektif: String,
        statusSumber: StatusSumberDataRiwayat,
        filterTanggal: FilterTanggalRiwayat = FilterTanggalRiwayat.Semua,
    ): ModelTampilanRiwayatTransaksi {
        return when (statusSumber) {
            StatusSumberDataRiwayat.Memuat -> {
                ModelTampilanRiwayatTransaksi(
                    judulLayar = "Riwayat Transaksi",
                    kataKunciPencarian = kataKunciMentah,
                    tampilkanAksiResetPencarian = kataKunciMentah.isNotBlank(),
                    statusMuat = StatusMuatRiwayatTransaksi.Memuat,
                    filterTanggal = filterTanggal,
                )
            }

            is StatusSumberDataRiwayat.Gagal -> {
                ModelTampilanRiwayatTransaksi(
                    judulLayar = "Riwayat Transaksi",
                    kataKunciPencarian = kataKunciMentah,
                    tampilkanAksiResetPencarian = kataKunciMentah.isNotBlank(),
                    statusMuat = StatusMuatRiwayatTransaksi.Gagal(
                        judul = "Gagal memuat riwayat transaksi",
                        deskripsi = statusSumber.pesan,
                    ),
                    filterTanggal = filterTanggal,
                )
            }

            is StatusSumberDataRiwayat.Berhasil -> {
                val daftarTersaring = statusSumber.daftarTransaksi.filter { transaksi ->
                    transaksi.cocokDenganKataKunci(
                        kataKunci = kataKunciEfektif,
                    )
                }.filter { transaksi ->
                    transaksi.cocokDenganFilterTanggal(filterTanggal)
                }

                if (daftarTersaring.isEmpty()) {
                    ModelTampilanRiwayatTransaksi(
                        judulLayar = "Riwayat Transaksi",
                        kataKunciPencarian = kataKunciMentah,
                        tampilkanAksiResetPencarian = kataKunciMentah.isNotBlank(),
                        statusMuat = StatusMuatRiwayatTransaksi.Kosong(
                            judul = if (kataKunciEfektif.isBlank()) {
                                "Belum ada riwayat transaksi"
                            } else {
                                "Transaksi tidak ditemukan"
                            },
                            deskripsi = if (kataKunciEfektif.isBlank()) {
                                "Riwayat transaksi akan tampil di sini setelah data transaksi mulai disimpan."
                            } else {
                                "Coba reset pencarian atau gunakan kata kunci yang lebih umum."
                            },
                        ),
                        filterTanggal = filterTanggal,
                    )
                } else {
                    ModelTampilanRiwayatTransaksi(
                        judulLayar = "Riwayat Transaksi",
                        kataKunciPencarian = kataKunciMentah,
                        tampilkanAksiResetPencarian = kataKunciMentah.isNotBlank(),
                        statusMuat = StatusMuatRiwayatTransaksi.Berhasil(
                            judulBagian = "Riwayat penjualan",
                            deskripsiBagian = if (kataKunciEfektif.isBlank()) {
                                "Data diambil dari database lokal."
                            } else {
                                "Menampilkan hasil yang sesuai dengan pencarian aktif."
                            },
                            labelJumlahHasil = "${daftarTersaring.size} transaksi ditemukan",
                            labelKataKunciAktif = kataKunciEfektif.takeIf { it.isNotBlank() }?.let { kataKunci ->
                                "Pencarian aktif: \"$kataKunci\""
                            },
                            daftarRingkasanTransaksi = daftarTersaring.map { transaksi ->
                                transaksi.keRingkasanTransaksiRiwayat()
                            },
                        ),
                        filterTanggal = filterTanggal,
                    )
                }
            }
        }
    }
}

private sealed interface StatusSumberDataRiwayat {
    data object Memuat : StatusSumberDataRiwayat

    data class Berhasil(
        val daftarTransaksi: List<Transaksi>,
    ) : StatusSumberDataRiwayat

    data class Gagal(
        val pesan: String,
    ) : StatusSumberDataRiwayat
}

private fun Transaksi.cocokDenganKataKunci(
    kataKunci: String,
): Boolean {
    if (kataKunci.isBlank()) return true

    return id.contains(kataKunci, ignoreCase = true) ||
        catatan?.contains(kataKunci, ignoreCase = true) == true ||
        daftarItemKeranjang.any { itemKeranjang ->
            itemKeranjang.produk.nama.contains(kataKunci, ignoreCase = true) ||
                itemKeranjang.catatan?.contains(kataKunci, ignoreCase = true) == true
        }
}

private fun Transaksi.cocokDenganFilterTanggal(filter: FilterTanggalRiwayat): Boolean {
    if (filter == FilterTanggalRiwayat.Semua) return true

    val kalender = Calendar.getInstance()
    val sekarangEpochMili = kalender.timeInMillis

    val awalHariIni: Long
    val akhirHariIni: Long

    kalender.set(Calendar.HOUR_OF_DAY, 0)
    kalender.set(Calendar.MINUTE, 0)
    kalender.set(Calendar.SECOND, 0)
    kalender.set(Calendar.MILLISECOND, 0)
    awalHariIni = kalender.timeInMillis
    akhirHariIni = awalHariIni + 24 * 60 * 60 * 1000

    return when (filter) {
        FilterTanggalRiwayat.HariIni -> {
            waktuTransaksiEpochMili >= awalHariIni && waktuTransaksiEpochMili < akhirHariIni
        }
        FilterTanggalRiwayat.Kemarin -> {
            val awalKemarin = awalHariIni - 24 * 60 * 60 * 1000
            waktuTransaksiEpochMili >= awalKemarin && waktuTransaksiEpochMili < awalHariIni
        }
        FilterTanggalRiwayat.BulanIni -> {
            val kalenderBulan = Calendar.getInstance()
            kalenderBulan.set(Calendar.HOUR_OF_DAY, 0)
            kalenderBulan.set(Calendar.MINUTE, 0)
            kalenderBulan.set(Calendar.SECOND, 0)
            kalenderBulan.set(Calendar.MILLISECOND, 0)
            kalenderBulan.set(Calendar.DAY_OF_MONTH, 1)
            val awalBulan = kalenderBulan.timeInMillis
            waktuTransaksiEpochMili >= awalBulan
        }
        FilterTanggalRiwayat.Semua -> true
    }
}

private fun Transaksi.keRingkasanTransaksiRiwayat(): RingkasanTransaksiRiwayat {
    return RingkasanTransaksiRiwayat(
        transaksiId = id,
        labelIdentitasTransaksi = id.sebagaiLabelIdentitasTransaksi(),
        labelWaktu = waktuTransaksiEpochMili.sebagaiLabelWaktuTransaksi(),
        labelJumlahItem = "${hitungJumlahItemTransaksi()} item",
        labelTotalAkhir = hitungTotalAkhirTransaksi().sebagaiRupiah(),
        ringkasanItem = bentukRingkasanItem(),
    )
}

private fun Transaksi.bentukRingkasanItem(): String {
    val daftarNamaProduk = daftarItemKeranjang.map { itemKeranjang ->
        itemKeranjang.produk.nama
    }

    if (daftarNamaProduk.isEmpty()) {
        return "Tanpa item"
    }

    val duaProdukPertama = daftarNamaProduk.take(2).joinToString(separator = ", ")
    val sisaProduk = daftarNamaProduk.size - 2

    return if (sisaProduk > 0) {
        "$duaProdukPertama +$sisaProduk lainnya"
    } else {
        duaProdukPertama
    }
}


