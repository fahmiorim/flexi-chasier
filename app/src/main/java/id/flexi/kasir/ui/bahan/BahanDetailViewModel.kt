package id.flexi.kasir.ui.bahan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.flexi.kasir.domain.model.Bahan
import id.flexi.kasir.domain.model.CashExpenseCategory
import id.flexi.kasir.domain.model.CashKas
import id.flexi.kasir.domain.model.CashMutationType
import id.flexi.kasir.domain.model.PembelianBahan
import id.flexi.kasir.domain.model.PenyesuaianStok
import id.flexi.kasir.domain.model.StokJenis
import id.flexi.kasir.domain.usecase.AmatiKasAktif
import id.flexi.kasir.domain.usecase.AmatiPembelianBahan
import id.flexi.kasir.domain.usecase.AmatiRiwayatPenyesuaian
import id.flexi.kasir.domain.usecase.AturStokBahan
import id.flexi.kasir.domain.usecase.CatatMutasiKas
import id.flexi.kasir.domain.usecase.CatatPembelianBahan
import id.flexi.kasir.domain.usecase.HapusMutasiKas
import id.flexi.kasir.domain.usecase.HapusPembelianBahan
import id.flexi.kasir.domain.usecase.ObserveBahanById
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BahanDetailUiState(
    val apakahSedangMemuat: Boolean = true,
    val bahan: Bahan? = null,
    val daftarPembelian: List<PembelianBahan> = emptyList(),
    val kasAktif: CashKas? = null,
    val apakahDialogTambahPembelianTampil: Boolean = false,
    val statusHapusPembelian: StatusHapusPembelian = StatusHapusPembelian(),
    val apakahDialogAturStokTampil: Boolean = false,
    val stokBaru: String = "",
    val alasanAturStok: String = "",
    val apakahSedangMenyimpanStok: Boolean = false,
    val apakahDialogRiwayatTampil: Boolean = false,
    val riwayatPenyesuaian: List<PenyesuaianStok> = emptyList(),
    val pesanSnackbar: String? = null,
)

data class StatusHapusPembelian(
    val apakahTampil: Boolean = false,
    val id: String = "",
    val bahanId: String = "",
    val jumlah: Double = 0.0,
    val satuan: String = "",
    val totalHarga: Long = 0L,
    val mutasiKasId: String? = null,
)

class BahanDetailViewModel(
    private val idBahan: String,
    private val ObserveBahanById: ObserveBahanById,
    private val CatatPembelianBahan: CatatPembelianBahan,
    private val AmatiPembelianBahan: AmatiPembelianBahan,
    private val HapusPembelianBahan: HapusPembelianBahan,
    private val hapusMutasiKas: HapusMutasiKas,
    private val aturStokBahan: AturStokBahan,
    private val amatiRiwayatPenyesuaian: AmatiRiwayatPenyesuaian,
    private val amatiKasAktif: AmatiKasAktif,
    private val catatMutasiKas: CatatMutasiKas,
) : ViewModel() {

    private val _state = MutableStateFlow(BahanDetailUiState())
    val amatiState: StateFlow<BahanDetailUiState> = _state.asStateFlow()

    init {
        combine(
            ObserveBahanById(idBahan),
            AmatiPembelianBahan(idBahan),
            amatiRiwayatPenyesuaian(StokJenis.Bahan, idBahan),
            amatiKasAktif(),
        ) { bahan, pembelian, riwayat, kasAktif ->
            _state.update {
                it.copy(
                    apakahSedangMemuat = false,
                    bahan = bahan,
                    daftarPembelian = pembelian,
                    riwayatPenyesuaian = riwayat,
                    kasAktif = kasAktif,
                )
            }
        }.launchIn(viewModelScope)
    }

    fun bukaDialogTambahPembelian() {
        _state.update { it.copy(apakahDialogTambahPembelianTampil = true) }
    }

    fun tutupDialogTambahPembelian() {
        _state.update { it.copy(apakahDialogTambahPembelianTampil = false) }
    }

    fun simpanPembelian(
        jumlah: String,
        satuanBeli: String,
        totalHarga: String,
        catatan: String,
        bayarPakaiLaci: Boolean,
    ) {
        val jumlahDouble = jumlah.toDoubleOrNull() ?: return
        val totalLong = totalHarga.toLongOrNull() ?: return
        if (jumlahDouble <= 0 || totalLong <= 0) return

        val kasAktif = _state.value.kasAktif
        if (bayarPakaiLaci && kasAktif == null) {
            _state.update {
                it.copy(pesanSnackbar = "Belum ada shift kas aktif. Buka shift di menu Kas sebelum bayar pakai uang laci.")
            }
            return
        }

        viewModelScope.launch {
            try {
                val satuan = satuanBeli.ifBlank { "pcs" }
                // Buat mutasi kas BelanjaBahan lebih dulu agar id-nya tersimpan
                // pada pembelian — saat pembelian dihapus, mutasi ikut dibatalkan.
                val mutasiKasId = if (bayarPakaiLaci && kasAktif != null) {
                    val namaBahan = _state.value.bahan?.nama ?: "Bahan"
                    catatMutasiKas(
                        shiftId = kasAktif.id,
                        tipe = CashMutationType.Pengeluaran,
                        nominal = totalLong,
                        catatan = "Pembelian $namaBahan (+${formatJumlah(jumlahDouble)} $satuan)",
                        kategori = CashExpenseCategory.BelanjaBahan,
                    ).id
                } else {
                    null
                }
                CatatPembelianBahan(
                    bahanId = idBahan,
                    jumlah = jumlahDouble,
                    satuanBeli = satuan,
                    totalHarga = totalLong,
                    catatan = catatan.ifBlank { null },
                    mutasiKasId = mutasiKasId,
                )
                _state.update {
                    it.copy(
                        apakahDialogTambahPembelianTampil = false,
                        pesanSnackbar = if (bayarPakaiLaci) {
                            "Pembelian dicatat dari uang laci."
                        } else {
                            "Pembelian berhasil dicatat."
                        },
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(pesanSnackbar = "Gagal: ${e.message}") }
            }
        }
    }

    private fun formatJumlah(jumlah: Double): String =
        if (jumlah == jumlah.toLong().toDouble()) {
            jumlah.toLong().toString()
        } else {
            jumlah.toString()
        }

    fun mintaHapusPembelian(pembelian: PembelianBahan) {
        _state.update {
            it.copy(
                statusHapusPembelian = StatusHapusPembelian(
                    apakahTampil = true,
                    id = pembelian.id,
                    bahanId = pembelian.bahanId,
                    jumlah = pembelian.jumlah,
                    satuan = pembelian.satuanBeli,
                    totalHarga = pembelian.totalHarga,
                    mutasiKasId = pembelian.mutasiKasId,
                ),
            )
        }
    }

    fun batalkanHapusPembelian() {
        _state.update { it.copy(statusHapusPembelian = StatusHapusPembelian()) }
    }

    fun konfirmasiHapusPembelian() {
        val status = _state.value.statusHapusPembelian
        viewModelScope.launch {
            try {
                HapusPembelianBahan(
                    id = status.id,
                    bahanId = status.bahanId,
                    jumlah = status.jumlah,
                )
                // Batalkan mutasi kas BelanjaBahan agar kas tidak selisih
                // (pembelian dibayar dari laci, bahan sudah dihapus).
                status.mutasiKasId?.let { hapusMutasiKas(it) }
                _state.update {
                    it.copy(
                        statusHapusPembelian = StatusHapusPembelian(),
                        pesanSnackbar = "Pembelian berhasil dihapus.",
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(pesanSnackbar = "Gagal: ${e.message}") }
            }
        }
    }

    fun bersihkanPesan() {
        _state.update { it.copy(pesanSnackbar = null) }
    }

    // ── Atur Stok ──

    fun bukaDialogAturStok() {
        _state.update {
            it.copy(
                apakahDialogAturStokTampil = true,
                stokBaru = "",
                alasanAturStok = "",
                apakahSedangMenyimpanStok = false,
            )
        }
    }

    fun tutupDialogAturStok() {
        _state.update { it.copy(apakahDialogAturStokTampil = false) }
    }

    fun perbaruiStokBaru(value: String) {
        _state.update { it.copy(stokBaru = value.filter { c -> c.isDigit() }) }
    }

    fun perbaruiAlasanAturStok(value: String) {
        _state.update { it.copy(alasanAturStok = value) }
    }

    fun simpanAturStok() {
        if (_state.value.apakahSedangMenyimpanStok) return
        val stokBaru = _state.value.stokBaru.toIntOrNull()
        if (stokBaru == null || stokBaru < 0) {
            _state.update { it.copy(pesanSnackbar = "Masukkan jumlah stok yang valid.") }
            return
        }

        _state.update { it.copy(apakahSedangMenyimpanStok = true) }
        viewModelScope.launch {
            try {
                aturStokBahan(
                    bahanId = idBahan,
                    stokBaru = stokBaru,
                    alasan = _state.value.alasanAturStok.trim(),
                )
                _state.update {
                    it.copy(
                        apakahDialogAturStokTampil = false,
                        apakahSedangMenyimpanStok = false,
                        pesanSnackbar = "Stok bahan berhasil diatur.",
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        apakahSedangMenyimpanStok = false,
                        pesanSnackbar = "Gagal mengatur stok: ${e.message}",
                    )
                }
            }
        }
    }

    // ── Riwayat Penyesuaian ──

    fun bukaDialogRiwayat() {
        _state.update { it.copy(apakahDialogRiwayatTampil = true) }
    }

    fun tutupDialogRiwayat() {
        _state.update { it.copy(apakahDialogRiwayatTampil = false) }
    }
}
