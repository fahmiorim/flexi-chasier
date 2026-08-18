package id.flexi.kasir.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.flexi.kasir.domain.usecase.AmbilStoreSetting
import id.flexi.kasir.domain.usecase.SimpanStoreSetting
import id.flexi.kasir.domain.usecase.AmatiStorePreference
import id.flexi.kasir.domain.usecase.SimpanStorePreference
import id.flexi.kasir.domain.model.CatalogDisplay
import id.flexi.kasir.domain.model.LebarStruk
import id.flexi.kasir.domain.model.PrinterType
import id.flexi.kasir.domain.model.ReceiptPrintFormat
import id.flexi.kasir.data.sync.SinkronStatusPengamat
import id.flexi.kasir.data.sync.SinkronStatusLokal
import id.flexi.kasir.ui.keSinkronMesinStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val ambilStoreSetting: AmbilStoreSetting,
    private val simpanStoreSetting: SimpanStoreSetting,
    private val amatiStorePreference: AmatiStorePreference,
    private val simpanStorePreference: SimpanStorePreference,
    private val sinkronStatusPengamat: SinkronStatusPengamat? = null,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        // Pantau status mesin sinkronisasi (antrian outbox + hasil terakhir).
        viewModelScope.launch {
            (sinkronStatusPengamat?.status ?: flowOf(SinkronStatusLokal())).collect { statusLokal ->
                _state.update {
                    it.copy(sinkronMesinStatus = statusLokal.keSinkronMesinStatus())
                }
            }
        }

        viewModelScope.launch {
            ambilStoreSetting().collect { pengaturan ->
                _state.update {
                    it.copy(
                        namaUsaha = pengaturan.namaUsaha,
                        logoUri = pengaturan.logoUri,
                        alamat = pengaturan.alamat,
                        tagline = pengaturan.tagline,
                        catalogDisplay = pengaturan.catalogDisplay,
                        PaymentMethodTunaiAktif = pengaturan.PaymentMethodTunaiAktif,
                        PaymentMethodQrisAktif = pengaturan.PaymentMethodQrisAktif,
                        receiptPrintFormat = pengaturan.receiptPrintFormat,
                        printerType = pengaturan.printerType,
                        printerAddress = pengaturan.printerAddress,
                        printerName = pengaturan.printerName,
                        suaraNotifikasiAktif = pengaturan.suaraNotifikasiAktif,
                        satuanStokDefault = pengaturan.satuanStokDefault,
                        jumlahTopFavorit = pengaturan.jumlahTopFavorit.toString(),
                        manajemenKasAktif = pengaturan.manajemenKasAktif,
                        strukHeader = pengaturan.strukHeader,
                        strukFooter = pengaturan.strukFooter,
                        lebarStruk = pengaturan.lebarStruk,
                        jumlahCopyCetak = pengaturan.jumlahCopyCetak.toString(),
                        tampilkanLogoDiStruk = pengaturan.tampilkanLogoDiStruk,
                        tampilkanPajakDiStruk = pengaturan.tampilkanPajakDiStruk,
                        printerDapurAktif = pengaturan.printerDapurAktif,
                        printerDapurType = pengaturan.printerDapurType,
                        printerDapurAddress = pengaturan.printerDapurAddress,
                        printerDapurName = pengaturan.printerDapurName,
                        apakahSedangMemuat = false,
                    )
                }
            }
        }

        viewModelScope.launch {
            amatiStorePreference().collect { preferensi ->
                _state.update {
                    it.copy(
                        basisPoinPajak = preferensi.basisPoinPajakDefault.toString(),
                        biayaLayanan = preferensi.biayaLayananDefault.toString(),
                    )
                }
            }
        }
    }

    fun perbaruiLogoUri(uri: String) {
        _state.update { it.copy(logoUri = uri) }
    }

    fun perbaruiNamaUsaha(nama: String) {
        _state.update { it.copy(namaUsaha = nama) }
    }

    fun perbaruiAlamat(alamat: String) {
        _state.update { it.copy(alamat = alamat) }
    }

    fun perbaruiTagline(tagline: String) {
        _state.update { it.copy(tagline = tagline) }
    }

    fun perbaruiCatalogDisplay(tampilan: CatalogDisplay) {
        _state.update { it.copy(catalogDisplay = tampilan) }
    }

    fun perbaruiPaymentMethodTunai(aktif: Boolean) {
        _state.update { it.copy(PaymentMethodTunaiAktif = aktif) }
    }

    fun perbaruiPaymentMethodQris(aktif: Boolean) {
        _state.update { it.copy(PaymentMethodQrisAktif = aktif) }
    }

    fun perbaruiReceiptPrintFormat(format: ReceiptPrintFormat) {
        _state.update { it.copy(receiptPrintFormat = format) }
    }

    fun perbaruiPrinterType(tipe: PrinterType) {
        _state.update { it.copy(printerType = tipe) }
    }

    fun perbaruiPrinter(alamat: String, nama: String) {
        _state.update { it.copy(printerAddress = alamat, printerName = nama) }
    }

    fun perbaruiPrinterDapurAktif(aktif: Boolean) {
        _state.update { it.copy(printerDapurAktif = aktif) }
    }

    fun perbaruiPrinterDapurType(tipe: PrinterType) {
        _state.update { it.copy(printerDapurType = tipe) }
    }

    fun perbaruiPrinterDapur(alamat: String, nama: String) {
        _state.update { it.copy(printerDapurAddress = alamat, printerDapurName = nama) }
    }

    fun perbaruiSuaraNotifikasi(aktif: Boolean) {
        _state.update { it.copy(suaraNotifikasiAktif = aktif) }
    }

    fun perbaruiSatuanStokDefault(satuan: String) {
        _state.update { it.copy(satuanStokDefault = satuan) }
    }

    fun perbaruiJumlahTopFavorit(jumlah: String) {
        _state.update { it.copy(jumlahTopFavorit = jumlah.filter { c -> c.isDigit() }.take(2)) }
    }

    fun perbaruiManajemenKas(aktif: Boolean) {
        _state.update { it.copy(manajemenKasAktif = aktif) }
    }

    fun perbaruiStrukHeader(header: String) {
        _state.update { it.copy(strukHeader = header) }
    }

    fun perbaruiStrukFooter(footer: String) {
        _state.update { it.copy(strukFooter = footer) }
    }

    fun perbaruiLebarStruk(lebar: LebarStruk) {
        _state.update { it.copy(lebarStruk = lebar) }
    }

    fun perbaruiJumlahCopyCetak(jumlah: String) {
        val bersih = jumlah.filter { it.isDigit() }.take(1)
        _state.update { it.copy(jumlahCopyCetak = bersih) }
    }

    fun perbaruiTampilkanLogoDiStruk(tampil: Boolean) {
        _state.update { it.copy(tampilkanLogoDiStruk = tampil) }
    }

    fun perbaruiTampilkanPajakDiStruk(tampil: Boolean) {
        _state.update { it.copy(tampilkanPajakDiStruk = tampil) }
    }

    fun perbaruiBasisPoinPajak(basisPoin: String) {
        _state.update { it.copy(basisPoinPajak = basisPoin.filter { c -> c.isDigit() }.take(4)) }
    }

    fun perbaruiBiayaLayanan(nominal: String) {
        _state.update { it.copy(biayaLayanan = nominal.filter { c -> c.isDigit() }.take(10)) }
    }

    fun simpan() {
        val s = _state.value
        _state.update { it.copy(apakahSedangMenyimpan = true, pesanBerhasil = null) }
        viewModelScope.launch {
            try {                    simpanStoreSetting(
                        id.flexi.kasir.domain.model.StoreSetting(
                            namaUsaha = s.namaUsaha,
                            logoUri = s.logoUri,
                            alamat = s.alamat,
                            tagline = s.tagline,
                            catalogDisplay = s.catalogDisplay,
                            PaymentMethodTunaiAktif = s.PaymentMethodTunaiAktif,
                            PaymentMethodQrisAktif = s.PaymentMethodQrisAktif,
                            receiptPrintFormat = s.receiptPrintFormat,
                            printerType = s.printerType,
                            printerAddress = s.printerAddress,
                            printerName = s.printerName,
                            suaraNotifikasiAktif = s.suaraNotifikasiAktif,
                            satuanStokDefault = s.satuanStokDefault,
                            jumlahTopFavorit = s.jumlahTopFavorit.filter { it.isDigit() }.take(2).toIntOrNull() ?: 10,
                            manajemenKasAktif = s.manajemenKasAktif,
                            strukHeader = s.strukHeader,
                            strukFooter = s.strukFooter,
                            lebarStruk = s.lebarStruk,
                            jumlahCopyCetak = s.jumlahCopyCetak.filter { it.isDigit() }.take(1).toIntOrNull()?.coerceIn(1, 5) ?: 1,
                            tampilkanLogoDiStruk = s.tampilkanLogoDiStruk,
                            tampilkanPajakDiStruk = s.tampilkanPajakDiStruk,
                            printerDapurAktif = s.printerDapurAktif,
                            printerDapurType = s.printerDapurType,
                            printerDapurAddress = s.printerDapurAddress,
                            printerDapurName = s.printerDapurName,
                        ),
                    )
                // Pajak & biaya layanan disimpan terpisah di StorePreference
                // (dipakai mesin kasir saat menghitung transaksi).
                val preferensiSekarang = amatiStorePreference().first()
                simpanStorePreference(
                    preferensiSekarang.copy(
                        basisPoinPajakDefault = s.basisPoinPajak.filter { it.isDigit() }.take(4).toIntOrNull() ?: 0,
                        biayaLayananDefault = s.biayaLayanan.filter { it.isDigit() }.take(10).toLongOrNull() ?: 0L,
                    ),
                )
                _state.update { it.copy(apakahSedangMenyimpan = false, pesanBerhasil = "Pengaturan berhasil disimpan") }
            } catch (_: Exception) {
                _state.update { it.copy(apakahSedangMenyimpan = false) }
            }
        }
    }

    fun bersihkanPesan() {
        _state.update { it.copy(pesanBerhasil = null) }
    }

    /**
     * Menjalankan satu siklus sinkronisasi penuh (push outbox + pull perubahan).
     * Hasil ditampilkan lewat snackbar; bar status terbarui otomatis dari flow.
     */
    fun sinkronkanSekarang() {
        val pengamat = sinkronStatusPengamat ?: run {
            _state.update { it.copy(pesanSinkronisasi = "Sinkronisasi belum tersedia.") }
            return
        }
        viewModelScope.launch {
            val hasil = pengamat.sinkronkanSekarang()
            val pesan = when {
                hasil.geraiId.isNullOrBlank() -> "Belum ada gerai aktif untuk disinkronkan."
                hasil.berhasil -> "Sinkronisasi selesai."
                else -> hasil.pesanError ?: "Sinkronisasi gagal."
            }
            _state.update { it.copy(pesanSinkronisasi = pesan) }
        }
    }

    fun bersihkanPesanSinkronisasi() {
        _state.update { it.copy(pesanSinkronisasi = null) }
    }
}
