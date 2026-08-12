package id.flexi.kasir.ui.bahan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.flexi.kasir.domain.usecase.ObserveBahanById
import id.flexi.kasir.domain.usecase.SimpanBahan
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BahanFormViewModel(
    private val idBahan: String?,
    private val ObserveBahanById: ObserveBahanById,
    private val SimpanBahan: SimpanBahan,
) : ViewModel() {

    private val _state = MutableStateFlow(BahanFormUiState())
    val amatiState: StateFlow<BahanFormUiState> = _state.asStateFlow()

    init {
        if (idBahan != null) {
            muatDataBahan(idBahan)
        }
    }

    private fun muatDataBahan(id: String) {
        viewModelScope.launch {
            _state.update { it.copy(apakahSedangMemuat = true) }
            val bahan = ObserveBahanById(id).first()
            if (bahan != null) {
                _state.update { bahan.toFormUiState().copy(apakahSedangMemuat = false) }
            } else {
                _state.update {
                    it.copy(
                        apakahSedangMemuat = false,
                        pesanError = "Bahan tidak ditemukan.",
                    )
                }
            }
        }
    }

    fun tanganiAksi(aksi: BahanFormAction) {
        when (aksi) {
            is BahanFormAction.UbahNama -> _state.update { it.copy(nama = aksi.nama, errorNama = null) }
            is BahanFormAction.UbahSatuan -> _state.update { it.copy(satuan = aksi.satuan, errorSatuan = null) }
            is BahanFormAction.UbahStokTersedia -> {
                val bersih = aksi.stok.filter { it.isDigit() || it == '.' }
                // Hanya izinkan maksimal 1 titik desimal
                val valid = bersih.count { it == '.' } <= 1
                _state.update { it.copy(stokTersedia = if (valid) bersih else _state.value.stokTersedia) }
            }
            is BahanFormAction.UbahHargaPerSatuan -> {
                val bersih = aksi.harga.filter { it.isDigit() }
                _state.update { it.copy(hargaPerSatuan = bersih) }
            }
            BahanFormAction.Simpan -> simpan()
            BahanFormAction.BersihkanPesan -> _state.update { it.copy(pesanSukses = null, pesanError = null) }
        }
    }

    private fun simpan() {
        val state = _state.value

        var error = false
        if (state.nama.trim().isBlank()) {
            _state.update { it.copy(errorNama = "Nama bahan harus diisi.") }
            error = true
        }
        if (state.satuan.trim().isBlank()) {
            _state.update { it.copy(errorSatuan = "Satuan harus diisi.") }
            error = true
        }
        if (error) return

        viewModelScope.launch {
            try {
                var bahan = state.toDomain() ?: return@launch
                if (state.apakahModeEdit) {
                    // Stok, stok minimum, dan status aktif hanya berubah lewat
                    // aksi khusus (Atur Stok / Catat Pembelian / web), bukan
                    // dari form ini. Ambil nilai TERBARU dari DB agar edit
                    // nama/satuan/harga tidak menimpa nilai yang diubah
                    // perangkat lain sejak form dibuka.
                    val terkini = ObserveBahanById(state.id).first()
                    if (terkini != null) {
                        bahan = bahan.copy(
                            stokTersedia = terkini.stokTersedia,
                            stokMinimum = terkini.stokMinimum,
                            aktif = terkini.aktif,
                        )
                    }
                }
                SimpanBahan(bahan)
                _state.update {
                    it.copy(
                        pesanSukses = "Bahan berhasil disimpan.",
                        errorNama = null,
                        errorSatuan = null,
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(pesanError = "Gagal menyimpan: ${e.message}") }
            }
        }
    }
}
