package id.cassy.kasir.antarmuka.pengaturan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.cassy.kasir.ranah.kasuspenggunaan.AmbilPengaturanToko
import id.cassy.kasir.ranah.kasuspenggunaan.SimpanPengaturanToko
import id.cassy.kasir.ranah.model.FormatCetakStruk
import id.cassy.kasir.ranah.model.TampilanKatalog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LayarPengaturanViewModel(
    private val ambilPengaturanToko: AmbilPengaturanToko,
    private val simpanPengaturanToko: SimpanPengaturanToko,
) : ViewModel() {

    private val _state = MutableStateFlow(ModelTampilanPengaturan())
    val state: StateFlow<ModelTampilanPengaturan> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            ambilPengaturanToko().collect { pengaturan ->
                _state.update {
                    it.copy(
                        namaUsaha = pengaturan.namaUsaha,
                        logoUri = pengaturan.logoUri,
                        alamat = pengaturan.alamat,
                        tampilanKatalog = pengaturan.tampilanKatalog,
                        metodeBayarTunaiAktif = pengaturan.metodeBayarTunaiAktif,
                        metodeBayarQrisAktif = pengaturan.metodeBayarQrisAktif,
                        formatCetakStruk = pengaturan.formatCetakStruk,
                        suaraNotifikasiAktif = pengaturan.suaraNotifikasiAktif,
                        satuanStokDefault = pengaturan.satuanStokDefault,
                        apakahSedangMemuat = false,
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

    fun perbaruiTampilanKatalog(tampilan: TampilanKatalog) {
        _state.update { it.copy(tampilanKatalog = tampilan) }
    }

    fun perbaruiMetodeBayarTunai(aktif: Boolean) {
        _state.update { it.copy(metodeBayarTunaiAktif = aktif) }
    }

    fun perbaruiMetodeBayarQris(aktif: Boolean) {
        _state.update { it.copy(metodeBayarQrisAktif = aktif) }
    }

    fun perbaruiFormatCetakStruk(format: FormatCetakStruk) {
        _state.update { it.copy(formatCetakStruk = format) }
    }

    fun perbaruiSuaraNotifikasi(aktif: Boolean) {
        _state.update { it.copy(suaraNotifikasiAktif = aktif) }
    }

    fun perbaruiSatuanStokDefault(satuan: String) {
        _state.update { it.copy(satuanStokDefault = satuan) }
    }

    fun simpan() {
        val s = _state.value
        _state.update { it.copy(apakahSedangMenyimpan = true, pesanBerhasil = null) }
        viewModelScope.launch {
            try {
                simpanPengaturanToko(
                    id.cassy.kasir.ranah.model.PengaturanToko(
                        namaUsaha = s.namaUsaha,
                        logoUri = s.logoUri,
                        alamat = s.alamat,
                        tampilanKatalog = s.tampilanKatalog,
                        metodeBayarTunaiAktif = s.metodeBayarTunaiAktif,
                        metodeBayarQrisAktif = s.metodeBayarQrisAktif,
                        formatCetakStruk = s.formatCetakStruk,
                        suaraNotifikasiAktif = s.suaraNotifikasiAktif,
                        satuanStokDefault = s.satuanStokDefault,
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
}
