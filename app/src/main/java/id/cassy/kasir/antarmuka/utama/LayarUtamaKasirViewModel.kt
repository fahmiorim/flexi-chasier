package id.cassy.kasir.antarmuka.utama

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.cassy.kasir.ranah.fungsi.sebagaiRupiah
import id.cassy.kasir.ranah.kasuspenggunaan.AmbilDaftarMeja
import id.cassy.kasir.ranah.kasuspenggunaan.AmatiPesananPending
import id.cassy.kasir.ranah.kasuspenggunaan.AmatiPreferensiToko
import id.cassy.kasir.ranah.kasuspenggunaan.AmbilPengaturanToko
import id.cassy.kasir.ranah.kasuspenggunaan.BayarPesananPending
import id.cassy.kasir.ranah.kasuspenggunaan.HapusPesananPending
import id.cassy.kasir.ranah.kasuspenggunaan.HapusProdukDariKeranjang
import id.cassy.kasir.ranah.kasuspenggunaan.KurangiProdukDiKeranjang
import id.cassy.kasir.ranah.kasuspenggunaan.LanjutkanPesananPending
import id.cassy.kasir.ranah.kasuspenggunaan.MuatKatalogProduk
import id.cassy.kasir.ranah.kasuspenggunaan.SelesaikanCheckoutLokalKasir
import id.cassy.kasir.ranah.kasuspenggunaan.SimpanPreferensiToko
import id.cassy.kasir.ranah.kasuspenggunaan.TambahProdukKeKeranjang
import id.cassy.kasir.ranah.model.AturanPajak
import id.cassy.kasir.ranah.model.HasilOperasiJaringan
import id.cassy.kasir.ranah.model.Meja
import id.cassy.kasir.ranah.model.MetodeBayar
import id.cassy.kasir.ranah.model.PengaturanToko
import id.cassy.kasir.ranah.model.PreferensiToko
import id.cassy.kasir.ranah.model.Produk
import id.cassy.kasir.ranah.model.StatusSinkronisasi
import id.cassy.kasir.ranah.model.StatusTransaksi
import id.cassy.kasir.ranah.model.Transaksi
import id.cassy.kasir.ranah.model.TipeOrder
import id.cassy.kasir.ranah.model.Uang
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Pengelola status layar utama kasir dengan pola alur data satu arah (UDF).
 * Mengatur keranjang belanja, proses checkout ke penyimpanan lokal,
 * serta fitur pencarian produk reaktif dengan debounce untuk performa optimal.
 */
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class LayarUtamaKasirViewModel(
    private val muatKatalogProduk: MuatKatalogProduk,
    private val selesaikanCheckoutLokalKasir: SelesaikanCheckoutLokalKasir,
    private val amatiPreferensiToko: AmatiPreferensiToko,
    private val simpanPreferensiToko: SimpanPreferensiToko,
    private val ambilPengaturanToko: AmbilPengaturanToko,
    private val amatiPesananPending: AmatiPesananPending,
    private val lanjutkanPesananPending: LanjutkanPesananPending,
    private val bayarPesananPending: BayarPesananPending,
    private val hapusPesananPending: HapusPesananPending,
    private val ambilDaftarMeja: AmbilDaftarMeja,
    private val tambahProdukKeKeranjang: TambahProdukKeKeranjang = TambahProdukKeKeranjang(),
    private val kurangiProdukDiKeranjang: KurangiProdukDiKeranjang = KurangiProdukDiKeranjang(),
    private val hapusProdukDariKeranjang: HapusProdukDariKeranjang = HapusProdukDariKeranjang(),
    private val bentukModelTampilan: BentukModelTampilanLayarUtamaKasir = BentukModelTampilanLayarUtamaKasir(),
) : ViewModel() {

    private val daftarProdukPenuh = muatKatalogProduk.eksekusi()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList(),
        )

    private val preferensiToko = amatiPreferensiToko()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = PreferensiToko(),
        )

    private val pengaturanToko = ambilPengaturanToko()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = PengaturanToko(),
        )

    private val daftarPesananPending = amatiPesananPending()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList(),
        )

    private val daftarMeja = ambilDaftarMeja()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList(),
        )

    private val _statusTransaksi = MutableStateFlow(
        StatusTransaksiLayarUtamaKasir(),
    )

    private val _statusElemenLayar = MutableStateFlow(
        StatusElemenLayarUtamaKasir(),
    )

    private val _efek = MutableSharedFlow<EfekLayarUtamaKasir>(
        extraBufferCapacity = 1,
    )
    val efek: SharedFlow<EfekLayarUtamaKasir> = _efek.asSharedFlow()

    private val _kataKunciPencarian = MutableStateFlow("")

    private val kataKunciPencarianEfektif =
        _kataKunciPencarian
            .debounce(250)
            .map { kataKunci ->
                kataKunci.trim()
            }
            .distinctUntilChanged()

    private val statusDasarModelTampilan = combine(
        daftarProdukPenuh,
        _statusTransaksi,
        _statusElemenLayar,
    ) { daftarProdukPenuh, statusTransaksi, statusElemenLayar ->
        StatusDasarModelTampilan(
            daftarProdukPenuh = daftarProdukPenuh,
            statusTransaksi = statusTransaksi,
            statusElemenLayar = statusElemenLayar,
        )
    }

    private val statusPencarianModelTampilan = combine(
        _kataKunciPencarian,
        kataKunciPencarianEfektif,
    ) { kataKunciMentah, kataKunciEfektif ->
        StatusPencarianModelTampilan(
            kataKunciMentah = kataKunciMentah,
            kataKunciEfektif = kataKunciEfektif,
        )
    }

    /**
     * Aliran status UI publik yang dirender oleh Compose.
     */
    private val statusPengaturanModelTampilan = combine(
        preferensiToko,
        pengaturanToko,
        daftarPesananPending,
        daftarMeja,
    ) { preferensiToko, pengaturanToko, daftarPesananPending, daftarMeja ->
        StatusPengaturanModelTampilan(
            preferensiToko = preferensiToko,
            pengaturanToko = pengaturanToko,
            daftarPesananPending = daftarPesananPending,
            daftarMeja = daftarMeja,
        )
    }

    val modelTampilan = combine(
        statusDasarModelTampilan,
        statusPencarianModelTampilan,
        statusPengaturanModelTampilan,
    ) { statusDasar, statusPencarian, statusPengaturan ->
        bentukModelTampilan(
            daftarProdukPenuh = statusDasar.daftarProdukPenuh,
            statusTransaksi = statusDasar.statusTransaksi,
            statusElemenLayar = statusDasar.statusElemenLayar,
            kataKunciMentah = statusPencarian.kataKunciMentah,
            kataKunciEfektif = statusPencarian.kataKunciEfektif,
            preferensiToko = statusPengaturan.preferensiToko,
            pengaturanToko = statusPengaturan.pengaturanToko,
            daftarPesananPending = statusPengaturan.daftarPesananPending,
            daftarMeja = statusPengaturan.daftarMeja,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = bentukModelTampilan(
            daftarProdukPenuh = emptyList(),
            statusTransaksi = StatusTransaksiLayarUtamaKasir(),
            statusElemenLayar = StatusElemenLayarUtamaKasir(),
            kataKunciMentah = "",
            kataKunciEfektif = "",
            preferensiToko = PreferensiToko(),
        ),
    )

    init {
        pastikanKatalogAwalTersedia()
    }

    /**
     * Titik masuk tunggal untuk semua aksi yang dipicu oleh UI.
     */
    fun tanganiAksi(aksi: AksiLayarUtamaKasir) {
        when (aksi) {
            is AksiLayarUtamaKasir.UbahKataKunciPencarian -> perbaruiPencarian(aksi.kataKunciBaru)
            AksiLayarUtamaKasir.UbahVisibilitasRingkasanPembayaran -> alihkanVisibilitasPembayaran()
            is AksiLayarUtamaKasir.TambahProdukKeKeranjang -> tambahProdukKeKeranjang(aksi.produkId)
            is AksiLayarUtamaKasir.KurangiProdukDiKeranjang -> kurangiProdukDiKeranjang(aksi.produkId)
            is AksiLayarUtamaKasir.HapusProdukDariKeranjang -> hapusProdukDariKeranjang(aksi.produkId)
            AksiLayarUtamaKasir.CobaCheckout -> cobaCheckout()
            AksiLayarUtamaKasir.BatalkanKonfirmasiCheckout -> batalkanKonfirmasiCheckout()
            AksiLayarUtamaKasir.SimpanPesanan -> simpanPesanan()
            AksiLayarUtamaKasir.BayarSekarang -> bayarSekarang()
            AksiLayarUtamaKasir.TutupStatusHasilCheckout -> tutupStatusHasilCheckout()
            AksiLayarUtamaKasir.ResetPencarian -> resetPencarian()
            AksiLayarUtamaKasir.SinkronkanKatalogProduk -> sinkronkanKatalogProduk()
            is AksiLayarUtamaKasir.UbahCatatanCheckout -> perbaruiCatatanCheckout(aksi.catatan)
            is AksiLayarUtamaKasir.UbahMetodeBayar -> perbaruiMetodeBayar(aksi.metodeBayar)
            is AksiLayarUtamaKasir.UbahTipeOrder -> perbaruiTipeOrder(aksi.tipeOrder)
            AksiLayarUtamaKasir.BukaPanelPesananPending -> bukaPanelPesananPending()
            AksiLayarUtamaKasir.TutupPanelPesananPending -> tutupPanelPesananPending()
            is AksiLayarUtamaKasir.LanjutkanPesananPending -> lanjutkanPesananPending(aksi.identitasTransaksi)
            is AksiLayarUtamaKasir.BayarPesananPending -> bayarPesananPending(aksi.identitasTransaksi)
            is AksiLayarUtamaKasir.HapusPesananPending -> hapusPesananPending(aksi.identitasTransaksi)
        }
    }

    private fun perbaruiPencarian(kataKunciBaru: String) {
        _kataKunciPencarian.value = kataKunciBaru
    }

    private fun pastikanKatalogAwalTersedia() {
        viewModelScope.launch {
            try {
                muatKatalogProduk.pastikanKatalogAwalTersedia()
            } catch (_: Exception) {
                kirimPesanSingkat("Katalog awal belum siap. Coba buka ulang aplikasi.")
            }
        }
    }

    private fun resetPencarian() {
        _kataKunciPencarian.value = ""
    }

    private fun sinkronkanKatalogProduk() {
        _statusTransaksi.update { statusLama ->
            statusLama.copy(
                statusSinkronisasi = StatusSinkronisasi.SedangSinkron,
            )
        }

        viewModelScope.launch {
            when (val hasilSinkronisasi = muatKatalogProduk.sinkronkan()) {
                is HasilOperasiJaringan.Berhasil -> {
                    _statusTransaksi.update { statusLama ->
                        statusLama.copy(
                            statusSinkronisasi = StatusSinkronisasi.Berhasil,
                        )
                    }

                    simpanPreferensiToko.simpanSinkronisasiKatalogBerhasil(
                        waktuEpochMili = System.currentTimeMillis(),
                    )

                    kirimPesanSingkat("Katalog produk berhasil diperbarui.")
                }

                is HasilOperasiJaringan.GagalJaringan -> {
                    _statusTransaksi.update { statusLama ->
                        statusLama.copy(
                            statusSinkronisasi = StatusSinkronisasi.Gagal(
                                pesan = hasilSinkronisasi.pesan,
                            ),
                        )
                    }

                    simpanPreferensiToko.simpanSinkronisasiKatalogGagal(
                        pesan = hasilSinkronisasi.pesan,
                    )

                    kirimPesanSingkat(hasilSinkronisasi.pesan)
                }

                is HasilOperasiJaringan.GagalServer -> {
                    _statusTransaksi.update { statusLama ->
                        statusLama.copy(
                            statusSinkronisasi = StatusSinkronisasi.Gagal(
                                pesan = hasilSinkronisasi.pesan,
                            ),
                        )
                    }

                    simpanPreferensiToko.simpanSinkronisasiKatalogGagal(
                        pesan = hasilSinkronisasi.pesan,
                    )

                    kirimPesanSingkat(hasilSinkronisasi.pesan)
                }

                is HasilOperasiJaringan.FallbackLokal -> {
                    _statusTransaksi.update { statusLama ->
                        statusLama.copy(
                            statusSinkronisasi = StatusSinkronisasi.Gagal(
                                pesan = hasilSinkronisasi.alasanGagal,
                            ),
                        )
                    }

                    simpanPreferensiToko.simpanSinkronisasiKatalogGagal(
                        pesan = hasilSinkronisasi.alasanGagal,
                    )

                    kirimPesanSingkat(hasilSinkronisasi.alasanGagal)
                }
            }
        }
    }

    private fun alihkanVisibilitasPembayaran() {
        _statusElemenLayar.update { statusLama ->
            statusLama.copy(
                apakahRingkasanPembayaranTampil = !statusLama.apakahRingkasanPembayaranTampil,
            )
        }
    }

    private fun tambahProdukKeKeranjang(produkId: String) {
        val produk = daftarProdukPenuh.value.firstOrNull { it.id == produkId } ?: return

        var apakahStokPenuh = false

        _statusTransaksi.update { statusLama ->
            val daftarLama = statusLama.daftarItemKeranjang
            val daftarBaru = tambahProdukKeKeranjang(
                daftarItemKeranjang = daftarLama,
                produk = produk,
            )

            apakahStokPenuh = daftarBaru == daftarLama

            statusLama.copy(
                daftarItemKeranjang = daftarBaru,
                statusSinkronisasi = StatusSinkronisasi.SinkronLokal,
            )
        }

        _statusElemenLayar.update { statusLama ->
            statusLama.copy(
                statusHasilCheckout = StatusHasilCheckoutKasir(),
            )
        }

        if (apakahStokPenuh) {
            kirimPesanSingkat("Stok produk sudah mencapai batas maksimum.")
        }
    }

    private fun kurangiProdukDiKeranjang(produkId: String) {
        _statusTransaksi.update { statusLama ->
            statusLama.copy(
                daftarItemKeranjang = kurangiProdukDiKeranjang(
                    daftarItemKeranjang = statusLama.daftarItemKeranjang,
                    produkId = produkId,
                ),
                statusSinkronisasi = StatusSinkronisasi.SinkronLokal,
            )
        }

        resetStatusHasil()
    }

    private fun hapusProdukDariKeranjang(produkId: String) {
        _statusTransaksi.update { statusLama ->
            statusLama.copy(
                daftarItemKeranjang = hapusProdukDariKeranjang(
                    daftarItemKeranjang = statusLama.daftarItemKeranjang,
                    produkId = produkId,
                ),
                statusSinkronisasi = StatusSinkronisasi.SinkronLokal,
            )
        }

        resetStatusHasil()
    }

    private fun cobaCheckout() {
        if (_statusTransaksi.value.daftarItemKeranjang.isEmpty()) {
            kirimPesanSingkat("Keranjang masih kosong. Yuk, tambah produk!")
            return
        }

        _statusElemenLayar.update { statusLama ->
            statusLama.copy(
                apakahDialogKonfirmasiCheckoutTampil = true,
                catatanCheckout = "",
                tipeOrder = TipeOrder.DineIn,
                statusHasilCheckout = StatusHasilCheckoutKasir(),
            )
        }
    }

    private fun batalkanKonfirmasiCheckout() {
        _statusElemenLayar.update { statusLama ->
            statusLama.copy(
                apakahDialogKonfirmasiCheckoutTampil = false,
            )
        }
    }

    private fun perbaruiCatatanCheckout(catatan: String) {
        _statusElemenLayar.update { statusLama ->
            statusLama.copy(
                catatanCheckout = catatan,
            )
        }
    }

    private fun perbaruiMetodeBayar(metodeBayar: MetodeBayar) {
        _statusElemenLayar.update { statusLama ->
            statusLama.copy(
                metodeBayar = metodeBayar,
            )
        }
    }

    private fun perbaruiTipeOrder(tipeOrder: TipeOrder) {
        _statusElemenLayar.update { statusLama ->
            statusLama.copy(
                tipeOrder = tipeOrder,
                catatanCheckout = if (tipeOrder == TipeOrder.TakeAway) "" else statusLama.catatanCheckout,
            )
        }
    }

    private fun simpanPesanan() {
        checkoutDenganStatus(StatusTransaksi.Pending)
    }

    private fun bayarSekarang() {
        checkoutDenganStatus(StatusTransaksi.Lunas)
    }

    private fun checkoutDenganStatus(status: StatusTransaksi) {
        val daftarKeranjangSaatIni = _statusTransaksi.value.daftarItemKeranjang
        if (daftarKeranjangSaatIni.isEmpty()) {
            kirimPesanSingkat("Keranjang masih kosong. Yuk, tambah produk!")
            return
        }

        val catatan = _statusElemenLayar.value.catatanCheckout
        val metodeBayar = _statusElemenLayar.value.metodeBayar
        val tipeOrder = _statusElemenLayar.value.tipeOrder
        val preferensi = preferensiToko.value
        val aturanPajak = if (preferensi.basisPoinPajakDefault > 0) {
            AturanPajak(
                nama = "PPN",
                basisPoin = preferensi.basisPoinPajakDefault,
                aktif = true,
            )
        } else {
            AturanPajak.TanpaPajak
        }
        val biayaLayanan = Uang.dariRupiah(preferensi.biayaLayananDefault)

        val judulHasil = when (status) {
            StatusTransaksi.Pending -> "Pesanan Disimpan"
            StatusTransaksi.Lunas -> "Transaksi Berhasil"
        }

        viewModelScope.launch {
            try {
                val hasilCheckout = selesaikanCheckoutLokalKasir.eksekusi(
                    daftarItemKeranjang = daftarKeranjangSaatIni,
                    catatan = catatan.ifBlank { null },
                    status = status,
                    aturanPajak = aturanPajak,
                    biayaLayanan = biayaLayanan,
                    metodeBayar = metodeBayar,
                    tipeOrder = tipeOrder,
                )

                _statusTransaksi.update {
                    StatusTransaksiLayarUtamaKasir(
                        daftarItemKeranjang = hasilCheckout.daftarItemKeranjangBaru,
                        statusSinkronisasi = hasilCheckout.statusSinkronisasiBaru,
                    )
                }

                _statusElemenLayar.update { statusLama ->
                    statusLama.copy(
                        apakahDialogKonfirmasiCheckoutTampil = false,
                        catatanCheckout = "",
                        statusHasilCheckout = StatusHasilCheckoutKasir(
                            apakahTampil = true,
                            judul = judulHasil,
                            deskripsi = "Sebanyak ${hasilCheckout.jumlahItemCheckout} item dengan total ${hasilCheckout.totalCheckout.sebagaiRupiah()} telah diproses.",
                        ),
                    )
                }
            } catch (kesalahanValidasi: IllegalArgumentException) {
                _statusElemenLayar.update { statusLama ->
                    statusLama.copy(
                        apakahDialogKonfirmasiCheckoutTampil = false,
                    )
                }

                kirimPesanSingkat(
                    pesan = kesalahanValidasi.message
                        ?: "Transaksi belum valid untuk disimpan.",
                )
            } catch (_: Exception) {
                _statusElemenLayar.update { statusLama ->
                    statusLama.copy(
                        apakahDialogKonfirmasiCheckoutTampil = false,
                    )
                }

                kirimPesanSingkat("Transaksi belum tersimpan. Coba lagi.")
            }
        }
    }

    private fun tutupStatusHasilCheckout() {
        _statusElemenLayar.update { statusLama ->
            statusLama.copy(
                statusHasilCheckout = StatusHasilCheckoutKasir(),
            )
        }
    }

    private fun resetStatusHasil() {
        _statusElemenLayar.update { statusLama ->
            statusLama.copy(
                statusHasilCheckout = StatusHasilCheckoutKasir(),
            )
        }
    }

    fun tampilkanPesanOperasional(
        pesan: String,
    ) {
        if (pesan.isBlank()) return
        kirimPesanSingkat(pesan)
    }

    private fun bukaPanelPesananPending() {
        _statusElemenLayar.update { it.copy(apakahPanelPesananPendingTampil = true) }
    }

    private fun tutupPanelPesananPending() {
        _statusElemenLayar.update { it.copy(apakahPanelPesananPendingTampil = false) }
    }

    private fun lanjutkanPesananPending(identitasTransaksi: String) {
        viewModelScope.launch {
            try {
                val daftarItemBaru = lanjutkanPesananPending.eksekusi(
                    identitasTransaksi = identitasTransaksi,
                    daftarProdukSaatIni = daftarProdukPenuh.value,
                )

                _statusTransaksi.update {
                    StatusTransaksiLayarUtamaKasir(
                        daftarItemKeranjang = daftarItemBaru,
                        statusSinkronisasi = StatusSinkronisasi.SinkronLokal,
                    )
                }

                _statusElemenLayar.update {
                    it.copy(
                        apakahPanelPesananPendingTampil = false,
                        statusHasilCheckout = StatusHasilCheckoutKasir(),
                    )
                }

                kirimPesanSingkat("Pesanan dilanjutkan ke keranjang.")
            } catch (kesalahan: IllegalArgumentException) {
                kirimPesanSingkat(kesalahan.message ?: "Pesanan gagal dilanjutkan.")
            }
        }
    }

    private fun bayarPesananPending(identitasTransaksi: String) {
        viewModelScope.launch {
            try {
                bayarPesananPending.eksekusi(identitasTransaksi)

                _statusElemenLayar.update {
                    it.copy(
                        apakahPanelPesananPendingTampil = false,
                        statusHasilCheckout = StatusHasilCheckoutKasir(
                            apakahTampil = true,
                            judul = "Pembayaran Berhasil",
                            deskripsi = "Pesanan pending telah dibayar lunas.",
                        ),
                    )
                }
            } catch (kesalahan: IllegalArgumentException) {
                kirimPesanSingkat(kesalahan.message ?: "Pembayaran gagal.")
            }
        }
    }

    private fun hapusPesananPending(identitasTransaksi: String) {
        viewModelScope.launch {
            try {
                hapusPesananPending.eksekusi(identitasTransaksi)
                kirimPesanSingkat("Pesanan pending dibatalkan.")
            } catch (_: Exception) {
                kirimPesanSingkat("Pesanan gagal dibatalkan.")
            }
        }
    }

    private fun kirimPesanSingkat(pesan: String) {
        _efek.tryEmit(
            EfekLayarUtamaKasir.TampilkanPesanSingkat(
                pesan = pesan,
            ),
        )
    }
}

private data class StatusDasarModelTampilan(
    val daftarProdukPenuh: List<Produk>,
    val statusTransaksi: StatusTransaksiLayarUtamaKasir,
    val statusElemenLayar: StatusElemenLayarUtamaKasir,
)

private data class StatusPencarianModelTampilan(
    val kataKunciMentah: String,
    val kataKunciEfektif: String,
)

private data class StatusPengaturanModelTampilan(
    val preferensiToko: PreferensiToko,
    val pengaturanToko: PengaturanToko,
    val daftarPesananPending: List<Transaksi>,
    val daftarMeja: List<Meja>,
)

