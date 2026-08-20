package id.flexi.kasir.ui.cashregister

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.flexi.kasir.domain.model.CashExpenseCategory
import id.flexi.kasir.domain.model.CashMutation
import id.flexi.kasir.domain.model.CashMutationType
import id.flexi.kasir.domain.model.CashKas
import id.flexi.kasir.domain.model.CashKasStatus
import id.flexi.kasir.domain.model.MutasiRekening
import id.flexi.kasir.domain.model.MutasiRekeningTipe
import id.flexi.kasir.domain.model.Setoran
import id.flexi.kasir.domain.usecase.AmatiMutasiKas
import id.flexi.kasir.domain.usecase.AmatiMutasiRekening
import id.flexi.kasir.domain.usecase.AmatiSemuaKas
import id.flexi.kasir.domain.usecase.AmatiSetoran
import id.flexi.kasir.domain.usecase.AmatiKasAktif
import id.flexi.kasir.domain.usecase.AturSaldoAwalRekening
import id.flexi.kasir.domain.usecase.BukaKas
import id.flexi.kasir.domain.usecase.CatatMutasiKas
import id.flexi.kasir.domain.usecase.CatatMutasiRekening
import id.flexi.kasir.domain.usecase.CatatSetoran
import id.flexi.kasir.domain.usecase.HapusMutasiKas
import id.flexi.kasir.domain.usecase.HapusSetoran
import id.flexi.kasir.domain.usecase.HitungSaldoRekening
import id.flexi.kasir.domain.usecase.PerbaruiSetoran
import id.flexi.kasir.domain.usecase.TutupKas
import id.flexi.kasir.domain.util.sebagaiRupiah
import id.flexi.kasir.domain.repository.TransactionRepository
import id.flexi.kasir.domain.model.PaymentMethod
import id.flexi.kasir.domain.util.hitungTotalAkhirTransaction
import id.flexi.kasir.ui.history.StatusExportPdf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** Status seksi Rekening di layar kas (terpisah dari state utama). */
data class RekeningUiState(
    val saldoAkhir: Long = 0L,
    val daftarMutasi: List<MutasiRekening> = emptyList(),
    val apakahDialogSaldoAwalTampil: Boolean = false,
    val nominalSaldoAwal: String = "",
    val catatanSaldoAwal: String = "",
    val apakahDialogMutasiTampil: Boolean = false,
    val tipeMutasi: MutasiRekeningTipe = MutasiRekeningTipe.Pemasukan,
    val nominalMutasi: String = "",
    val catatanMutasi: String = "",
    val apakahSedangMenyimpan: Boolean = false,
    val pesanSnackbar: String? = null,
)

class CashRegisterViewModel(
    private val bukaKas: BukaKas,
    private val tutupKas: TutupKas,
    private val amatiKasAktif: AmatiKasAktif,
    private val amatiSemuaKas: AmatiSemuaKas,
    private val catatMutasiKas: CatatMutasiKas,
    private val amatiMutasiKas: AmatiMutasiKas,
    private val hapusMutasiKas: HapusMutasiKas,
    private val catatSetoran: CatatSetoran,
    private val amatiSetoran: AmatiSetoran,
    private val hapusSetoran: HapusSetoran,
    private val perbaruiSetoran: PerbaruiSetoran,
    private val aturSaldoAwalRekening: AturSaldoAwalRekening,
    private val catatMutasiRekening: CatatMutasiRekening,
    private val amatiMutasiRekening: AmatiMutasiRekening,
    private val hitungSaldoRekening: HitungSaldoRekening,
    private val transactionRepository: TransactionRepository,
    private val cashRepository: id.flexi.kasir.domain.repository.CashRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<CashRegisterUiState>(CashRegisterUiState.Memuat)
    val state: StateFlow<CashRegisterUiState> = _state

    private val _stateRekening = MutableStateFlow(RekeningUiState())
    val stateRekening: StateFlow<RekeningUiState> = _stateRekening.asStateFlow()

    private var shiftIdAktif: String? = null
    private var observasiMutasiJob: Job? = null
    private var profitCalculationJob: Job? = null
    private var daftarKasTertutup: List<CashKas> = emptyList()
    private var daftarSetoran: List<Setoran> = emptyList()
    private var totalSetoran: Long = 0L
    private val _tutupBerhasil = MutableStateFlow(false)
    val tutupBerhasil: StateFlow<Boolean> = _tutupBerhasil.asStateFlow()
    // Last financial data saved for transition to BelumBuka
    private var lastPenjualanTunai: String = "Rp0"
    private var lastPenjualanQRIS: String = "Rp0"
    private var lastPenjualanTotal: String = "Rp0"
    private var lastPemasukan: String = "Rp0"
    private var lastPengeluaran: String = "Rp0"
    private var lastMutasiList: List<CashMutation> = emptyList()
    private var lastSaldoSaatIni: String = "Rp0"
    // Cache akumulasi profit dari shift tertutup (dihitung async, dipakai di BelumBuka & KasAktif)
    private var cachedAkumulasiProfit: Long = 0L
    // Flag anti-dobel-klik: dipisah dari state agar tidak dioverwrite oleh combine emissions
    @Volatile
    private var sedangTutup = false

    init {
        viewModelScope.launch {
            combine(
                amatiMutasiRekening(),
                hitungSaldoRekening(),
            ) { daftar, saldo ->
                _stateRekening.update {
                    it.copy(
                        saldoAkhir = saldo,
                        daftarMutasi = daftar.sortedByDescending { mutasi -> mutasi.waktu },
                    )
                }
            }.collectLatest { }
        }


        viewModelScope.launch {
            amatiSetoran().collect { setoranList ->
                daftarSetoran = setoranList
                totalSetoran = setoranList.filter { !it.dihapus }.sumOf { it.nominal.nilaiRupiah }
                val current = _state.value
                when (current) {
                    is CashRegisterUiState.BelumBuka -> _state.value = current.copy(
                        daftarSetoran = daftarSetoran,
                        totalSetoran = totalSetoran.sebagaiRupiah(),
                    )
                    is CashRegisterUiState.KasAktif -> _state.value = current.copy(
                        daftarSetoran = daftarSetoran,
                        totalSetoran = totalSetoran.sebagaiRupiah(),
                    )
                    else -> {}
                }
            }
        }

        viewModelScope.launch {
            combine(
                amatiKasAktif(),
                amatiSemuaKas(),
            ) { shift, daftarSemuaKas ->
                val currentState = _state.value

                // Jangan overwrite state tutup berhasil — biarkan user lihat dulu
                if (currentState is CashRegisterUiState.KasAktif && currentState.tutupBerhasil) {
                    return@combine
                }

                daftarKasTertutup = daftarSemuaKas
                    .filter { it.status == CashKasStatus.Tutup }
                    .sortedByDescending { it.waktuTutup ?: it.waktuBuka }

                if (shift == null) {
                    val existing = if (currentState is CashRegisterUiState.BelumBuka) currentState else null

                    // Set state awal dengan cached value (sinkron, cepat)
                    _state.value = CashRegisterUiState.BelumBuka(
                        daftarKasTertutup = daftarKasTertutup,
                        daftarSetoran = daftarSetoran,
                        totalSetoran = totalSetoran.sebagaiRupiah(),
                        kasTerpilih = existing?.kasTerpilih,
                        penjualanTunaiTerakhir = lastPenjualanTunai,
                        penjualanQRISTerakhir = lastPenjualanQRIS,
                        penjualanTotalTerakhir = lastPenjualanTotal,
                        totalPemasukanTerakhir = lastPemasukan,
                        totalPengeluaranTerakhir = lastPengeluaran,
                        daftarMutasiTerakhir = lastMutasiList,
                        saldoSaatIniTerakhir = cachedAkumulasiProfit.sebagaiRupiah(),
                        tanggalBukaEpochMili = existing?.tanggalBukaEpochMili ?: System.currentTimeMillis(),
                        jamBukaJam = existing?.jamBukaJam ?: Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                        jamBukaMenit = existing?.jamBukaMenit ?: Calendar.getInstance().get(Calendar.MINUTE),
                        nominalBuka = existing?.nominalBuka ?: "",
                        catatanBuka = existing?.catatanBuka ?: "",
                        apakahDialogSetoranTerbuka = existing?.apakahDialogSetoranTerbuka ?: false,
                        nominalSetoran = existing?.nominalSetoran ?: "",
                        catatanSetoran = existing?.catatanSetoran ?: "",
                    )

                    // Hitung akumulasi profit dari SEMUA shift tertutup secara async
                    // Batalkan kalkulasi sebelumnya jika ada
                    profitCalculationJob?.cancel()
                    profitCalculationJob = viewModelScope.launch {
                        try {
                            var totalProfit = 0L
                            for (s in daftarKasTertutup) {
                                val sampai = s.waktuTutup ?: Long.MAX_VALUE
                                val tunai = transactionRepository.hitungTotalTunaiRentang(s.waktuBuka, sampai)
                                val pemasukanS = cashRepository.ambilTotalMutasiBerdasarkanTipe(s.id, CashMutationType.Pemasukan.name)
                                val pengeluaranS = cashRepository.ambilTotalMutasiBerdasarkanTipe(s.id, CashMutationType.Pengeluaran.name)
                                val setoranS = cashRepository.hitungTotalSetoranBerdasarkanKas(s.id).first()
                                totalProfit += tunai + pemasukanS - pengeluaranS - setoranS
                            }
                            cachedAkumulasiProfit = totalProfit
                            // Update state hanya jika masih di BelumBuka dan shift masih null
                            val cur = _state.value
                            if (cur is CashRegisterUiState.BelumBuka && shiftIdAktif == null) {
                                _state.value = cur.copy(
                                    saldoSaatIniTerakhir = totalProfit.sebagaiRupiah(),
                                )
                            }
                        } catch (_: Exception) { }
                    }

                    observasiMutasiJob?.cancel()
                    observasiMutasiJob = null
                    shiftIdAktif = null
                } else if (shiftIdAktif != shift.id) {
                    shiftIdAktif = shift.id
                    observeMutasiDanSaldo(shift)
                }
            }.collect { }
        }
    }

    private fun observeMutasiDanSaldo(shift: id.flexi.kasir.domain.model.CashKas) {
        observasiMutasiJob?.cancel()
        observasiMutasiJob = viewModelScope.launch {
            combine(
                amatiMutasiKas(shift.id),
                transactionRepository.hitungTotalTunaiSejak(shift.waktuBuka),
                transactionRepository.hitungTotalQRISSejak(shift.waktuBuka),
                cashRepository.hitungTotalMutasiBerdasarkanTipe(shift.id, CashMutationType.Pemasukan.name),
                cashRepository.hitungTotalMutasiBerdasarkanTipe(shift.id, CashMutationType.Pengeluaran.name),
                cashRepository.hitungTotalSetoranBerdasarkanKas(shift.id),
            ) { values ->
                val daftarMutasi = values[0] as List<CashMutation>
                val penjualanTunaiHariIni = values[1] as Long
                val penjualanQrisHariIni = values[2] as Long
                val totalPemasukan = values[3] as Long
                val totalPengeluaran = values[4] as Long
                val totalSetoranShift = values[5] as Long

                // Saldo Kas = Saldo Awal + Penjualan Tunai + Pemasukan - Pengeluaran - Setoran
                val saldoSaatIni = shift.saldoAwal.nilaiRupiah + penjualanTunaiHariIni + totalPemasukan - totalPengeluaran - totalSetoranShift

                // Simpan profit shift ini
                lastSaldoSaatIni = saldoSaatIni.sebagaiRupiah()

                val currentState = _state.value
                val existingAktif = if (currentState is CashRegisterUiState.KasAktif) currentState else null

                _state.value = CashRegisterUiState.KasAktif(
                    kas = shift,
                    daftarKasTertutup = daftarKasTertutup,
                    akumulasiProfitShiftTertutup = cachedAkumulasiProfit,
                    daftarSetoran = daftarSetoran.filter { it.shiftId == shift.id },
                    totalSetoran = totalSetoranShift.sebagaiRupiah(),
                    kasTerpilih = existingAktif?.kasTerpilih,
                    saldoSaatIni = saldoSaatIni.sebagaiRupiah(),
                    penjualanTunai = penjualanTunaiHariIni.sebagaiRupiah(),
                    penjualanQRIS = penjualanQrisHariIni.sebagaiRupiah(),
                    penjualanTotal = (penjualanTunaiHariIni + penjualanQrisHariIni).sebagaiRupiah(),
                    totalPemasukan = totalPemasukan.sebagaiRupiah(),
                    totalPengeluaran = totalPengeluaran.sebagaiRupiah(),
                    daftarMutasi = daftarMutasi.sortedByDescending { it.waktu },
                    apakahDialogMutasiTerbuka = existingAktif?.apakahDialogMutasiTerbuka ?: false,
                    tipeMutasi = existingAktif?.tipeMutasi ?: CashMutationType.Pengeluaran,
                    kategoriMutasi = existingAktif?.kategoriMutasi ?: CashExpenseCategory.Lainnya,
                    nominalMutasi = existingAktif?.nominalMutasi ?: "",
                    catatanMutasi = existingAktif?.catatanMutasi ?: "",
                    apakahDialogTutupTerbuka = existingAktif?.apakahDialogTutupTerbuka ?: false,
                    saldoFisikInput = existingAktif?.saldoFisikInput ?: "",
                    apakahSedangTutup = existingAktif?.apakahSedangTutup ?: false,
                    tutupBerhasil = existingAktif?.tutupBerhasil ?: false,
                    apakahDialogSetoranTerbuka = existingAktif?.apakahDialogSetoranTerbuka ?: false,
                    nominalSetoran = existingAktif?.nominalSetoran ?: "",
                    catatanSetoran = existingAktif?.catatanSetoran ?: "",
                    apakahSedangSimpanSetoran = existingAktif?.apakahSedangSimpanSetoran ?: false,
                    pesanErrorSetoran = existingAktif?.pesanErrorSetoran,
                    // Preserve selected closed shift's financial data
                    kasTerpilihPenjualanTunai = existingAktif?.kasTerpilihPenjualanTunai,
                    kasTerpilihPenjualanQRIS = existingAktif?.kasTerpilihPenjualanQRIS,
                    kasTerpilihPenjualanTotal = existingAktif?.kasTerpilihPenjualanTotal,
                    kasTerpilihTotalPemasukan = existingAktif?.kasTerpilihTotalPemasukan,
                    kasTerpilihTotalPengeluaran = existingAktif?.kasTerpilihTotalPengeluaran,
                    kasTerpilihDaftarMutasi = existingAktif?.kasTerpilihDaftarMutasi,
                    kasTerpilihSaldoSaatIni = existingAktif?.kasTerpilihSaldoSaatIni,
                )

                // Save for transition to BelumBuka
                lastPenjualanTunai = penjualanTunaiHariIni.sebagaiRupiah()
                lastPenjualanQRIS = penjualanQrisHariIni.sebagaiRupiah()
                lastPenjualanTotal = (penjualanTunaiHariIni + penjualanQrisHariIni).sebagaiRupiah()
                lastPemasukan = totalPemasukan.sebagaiRupiah()
                lastPengeluaran = totalPengeluaran.sebagaiRupiah()
                lastMutasiList = daftarMutasi.sortedByDescending { it.waktu }
                lastSaldoSaatIni = saldoSaatIni.sebagaiRupiah()
            }.collect { }
        }
    }

    // ── Aksi Buka Kas (Dialog) ──

    fun bukaDialogBuka() {
        val state = _state.value
        if (state is CashRegisterUiState.BelumBuka) {
            val kal = Calendar.getInstance()
            _state.value = state.copy(
                apakahDialogBukaTerbuka = true,
                tanggalBukaEpochMili = kal.timeInMillis,
                jamBukaJam = kal.get(Calendar.HOUR_OF_DAY),
                jamBukaMenit = kal.get(Calendar.MINUTE),
                nominalBuka = "",
                catatanBuka = "",
                pesanError = null,
            )
        }
    }

    fun tutupDialogBuka() {
        val state = _state.value
        if (state is CashRegisterUiState.BelumBuka) {
            _state.value = state.copy(apakahDialogBukaTerbuka = false, pesanError = null)
        }
    }

    fun perbaruiTanggalBuka(epochMili: Long) {
        val state = _state.value
        if (state is CashRegisterUiState.BelumBuka) {
            _state.value = state.copy(tanggalBukaEpochMili = epochMili)
        }
    }

    fun perbaruiJamBuka(jam: Int, menit: Int) {
        val state = _state.value
        if (state is CashRegisterUiState.BelumBuka) {
            _state.value = state.copy(jamBukaJam = jam.coerceIn(0, 23), jamBukaMenit = menit.coerceIn(0, 59))
        }
    }

    fun perbaruiNominalBuka(value: String) {
        val state = _state.value
        if (state is CashRegisterUiState.BelumBuka) {
            _state.value = state.copy(nominalBuka = value.filter { it.isDigit() }, pesanError = null)
        }
    }

    fun perbaruiCatatanBuka(value: String) {
        val state = _state.value
        if (state is CashRegisterUiState.BelumBuka) {
            _state.value = state.copy(catatanBuka = value)
        }
    }

    fun bukaKas() {
        val state = _state.value
        if (state !is CashRegisterUiState.BelumBuka || state.apakahSedangMemproses) return

        val nominal = state.nominalBuka.toLongOrNull()
        if (nominal == null || nominal < 0L) {
            _state.value = state.copy(pesanError = "Masukkan jumlah uang yang valid")
            return
        }

        // Gabungkan tanggal + jam untuk waktuBuka
        val kal = Calendar.getInstance()
        kal.timeInMillis = state.tanggalBukaEpochMili
        kal.set(Calendar.HOUR_OF_DAY, state.jamBukaJam)
        kal.set(Calendar.MINUTE, state.jamBukaMenit)
        kal.set(Calendar.SECOND, 0)
        kal.set(Calendar.MILLISECOND, 0)
        val waktuBuka = kal.timeInMillis

        _state.value = state.copy(apakahSedangMemproses = true, pesanError = null)
        viewModelScope.launch {
            try {
                bukaKas(
                    saldoAwal = nominal,
                    waktuBuka = waktuBuka,
                    catatan = state.catatanBuka.ifBlank { null },
                )
            } catch (e: Exception) {
                _state.value = state.copy(
                    apakahSedangMemproses = false,
                    pesanError = "Gagal membuka kas: ${e.message}"
                )
            }
        }
    }

    // ── Aksi Uang Masuk / Uang Keluar ──

    fun bukaDialogUangMasuk() {
        val state = _state.value
        if (state is CashRegisterUiState.KasAktif) {
            _state.value = state.copy(
                apakahDialogMutasiTerbuka = true,
                tipeMutasi = CashMutationType.Pemasukan,
                kategoriMutasi = CashExpenseCategory.Lainnya,
                nominalMutasi = "",
                catatanMutasi = "",
                pesanErrorMutasi = null,
            )
        }
    }

    fun bukaDialogUangKeluar() {
        val state = _state.value
        if (state is CashRegisterUiState.KasAktif) {
            _state.value = state.copy(
                apakahDialogMutasiTerbuka = true,
                tipeMutasi = CashMutationType.Pengeluaran,
                kategoriMutasi = CashExpenseCategory.Lainnya,
                nominalMutasi = "",
                catatanMutasi = "",
                pesanErrorMutasi = null,
            )
        }
    }

    fun tutupDialogMutasi() {
        val state = _state.value
        if (state is CashRegisterUiState.KasAktif) {
            _state.value = state.copy(apakahDialogMutasiTerbuka = false, pesanErrorMutasi = null)
        }
    }

    fun perbaruiTipeMutasi(tipe: CashMutationType) {
        val state = _state.value
        if (state is CashRegisterUiState.KasAktif) {
            _state.value = state.copy(tipeMutasi = tipe)
        }
    }

    fun perbaruiKategoriMutasi(kategori: CashExpenseCategory) {
        val state = _state.value
        if (state is CashRegisterUiState.KasAktif) {
            _state.value = state.copy(kategoriMutasi = kategori)
        }
    }

    fun perbaruiNominalMutasi(value: String) {
        val state = _state.value
        if (state is CashRegisterUiState.KasAktif) {
            _state.value = state.copy(
                nominalMutasi = value.filter { it.isDigit() },
                pesanErrorMutasi = null,
            )
        }
    }

    fun perbaruiCatatanMutasi(value: String) {
        val state = _state.value
        if (state is CashRegisterUiState.KasAktif) {
            _state.value = state.copy(catatanMutasi = value)
        }
    }

    fun simpanMutasi() {
        val state = _state.value
        if (state !is CashRegisterUiState.KasAktif || state.apakahSedangSimpanMutasi) return
        val shiftId = shiftIdAktif ?: return

        val nominal = state.nominalMutasi.toLongOrNull()
        if (nominal == null || nominal <= 0L) {
            _state.value = state.copy(pesanErrorMutasi = "Masukkan nominal yang valid")
            return
        }

        _state.value = state.copy(apakahSedangSimpanMutasi = true, pesanErrorMutasi = null)
        viewModelScope.launch {
            try {
                catatMutasiKas(
                    shiftId = shiftId,
                    tipe = state.tipeMutasi,
                    nominal = nominal,
                    catatan = state.catatanMutasi,
                    kategori = state.kategoriMutasi,
                )
                _state.value = state.copy(
                    apakahDialogMutasiTerbuka = false,
                    apakahSedangSimpanMutasi = false,
                    nominalMutasi = "",
                    catatanMutasi = "",
                )
            } catch (e: Exception) {
                _state.value = state.copy(
                    apakahSedangSimpanMutasi = false,
                    pesanErrorMutasi = "Gagal menyimpan: ${e.message}",
                )
            }
        }
    }

    fun hapusMutasi(id: String) {
        viewModelScope.launch {
            try {
                hapusMutasiKas(id)
            } catch (_: Exception) { }
        }
    }

    /**
     * Membersihkan flag tutup berhasil agar state kembali normal
     * saat user navigasi kembali setelah sukses tutup kas.
     */
    fun clearTutupBerhasil() {
        _tutupBerhasil.value = false
        // Tutup dialog juga, jaga-jaga kalau combine belum transisi ke BelumBuka
        val s = _state.value
        if (s is CashRegisterUiState.KasAktif) {
            _state.value = s.copy(
                apakahDialogTutupTerbuka = false,
                apakahSedangTutup = false,
            )
        }
    }

    // ── Detail Kas ──

    fun pilihKas(shift: CashKas) {
        val state = _state.value
        when (state) {
            is CashRegisterUiState.BelumBuka -> {
                _state.value = state.copy(
                    kasTerpilih = shift,
                    kasTerpilihPenjualanTunai = null,
                    kasTerpilihPenjualanQRIS = null,
                    kasTerpilihPenjualanTotal = null,
                    kasTerpilihTotalPemasukan = null,
                    kasTerpilihTotalPengeluaran = null,
                    kasTerpilihDaftarMutasi = null,
                    kasTerpilihDaftarTransaksi = null,
                    kasTerpilihSaldoSaatIni = null,
                )
                muatDataKasTerpilih(shift)
            }
            is CashRegisterUiState.KasAktif -> {
                _state.value = state.copy(kasTerpilih = shift)
                muatDataKasTerpilih(shift)
            }
            else -> {}
        }
    }

    private fun muatDataKasTerpilih(shift: CashKas) {
        viewModelScope.launch {
            val sejakBukaShift = shift.waktuBuka
            val sampaiTutupShift = shift.waktuTutup ?: Long.MAX_VALUE

            val penjualanTunai = transactionRepository.hitungTotalTunaiRentang(sejakBukaShift, sampaiTutupShift)
            val penjualanQRIS = transactionRepository.hitungTotalQRISRentang(sejakBukaShift, sampaiTutupShift)
            val totalPemasukan = cashRepository.ambilTotalMutasiBerdasarkanTipe(shift.id, CashMutationType.Pemasukan.name)
            val totalPengeluaran = cashRepository.ambilTotalMutasiBerdasarkanTipe(shift.id, CashMutationType.Pengeluaran.name)
            val totalSetoran = cashRepository.hitungTotalSetoranBerdasarkanKas(shift.id).first()
            val daftarMutasi = amatiMutasiKas(shift.id).first()
            val daftarTransaksi = transactionRepository.ambilTransactionRentang(sejakBukaShift, sampaiTutupShift)

            val saldoSaatIni = shift.saldoAwal.nilaiRupiah + penjualanTunai + totalPemasukan - totalPengeluaran - totalSetoran

            val current = _state.value
            when {
                current is CashRegisterUiState.BelumBuka && current.kasTerpilih?.id == shift.id -> {
                    _state.value = current.copy(
                        kasTerpilihPenjualanTunai = penjualanTunai.sebagaiRupiah(),
                        kasTerpilihPenjualanQRIS = penjualanQRIS.sebagaiRupiah(),
                        kasTerpilihPenjualanTotal = (penjualanTunai + penjualanQRIS).sebagaiRupiah(),
                        kasTerpilihTotalPemasukan = totalPemasukan.sebagaiRupiah(),
                        kasTerpilihTotalPengeluaran = totalPengeluaran.sebagaiRupiah(),
                        kasTerpilihDaftarMutasi = daftarMutasi.sortedByDescending { it.waktu },
                        kasTerpilihDaftarTransaksi = daftarTransaksi,
                        kasTerpilihSaldoSaatIni = saldoSaatIni.sebagaiRupiah(),
                    )
                }
                current is CashRegisterUiState.KasAktif && current.kasTerpilih?.id == shift.id -> {
                    val saldoAkhirShift = shift.saldoAkhir
                    _state.value = current.copy(
                        kasTerpilihPenjualanTunai = penjualanTunai.sebagaiRupiah(),
                        kasTerpilihPenjualanQRIS = penjualanQRIS.sebagaiRupiah(),
                        kasTerpilihPenjualanTotal = (penjualanTunai + penjualanQRIS).sebagaiRupiah(),
                        kasTerpilihTotalPemasukan = totalPemasukan.sebagaiRupiah(),
                        kasTerpilihTotalPengeluaran = totalPengeluaran.sebagaiRupiah(),
                        kasTerpilihDaftarMutasi = daftarMutasi.sortedByDescending { it.waktu },
                        kasTerpilihDaftarTransaksi = daftarTransaksi,
                        kasTerpilihSaldoSaatIni = saldoSaatIni.sebagaiRupiah(),
                    )
                }
            }
        }
    }

    fun tutupDetailKas() {
        val state = _state.value
        when (state) {
            is CashRegisterUiState.BelumBuka ->
                _state.value = state.copy(kasTerpilih = null)
            is CashRegisterUiState.KasAktif ->
                _state.value = state.copy(kasTerpilih = null)
            else -> {}
        }
    }

    // ── Aksi Tutup Kas ──

    fun bukaDialogTutup() {
        val state = _state.value
        if (state is CashRegisterUiState.KasAktif) {
            _state.value = state.copy(
                apakahDialogTutupTerbuka = true,
                saldoFisikInput = "",
                pesanErrorTutup = null,
                tutupBerhasil = false,
            )
        }
    }

    fun tutupDialogTutup() {
        val state = _state.value
        if (state is CashRegisterUiState.KasAktif) {
            _state.value = state.copy(apakahDialogTutupTerbuka = false, pesanErrorTutup = null)
        }
    }

    fun perbaruiSaldoFisik(value: String) {
        val state = _state.value
        if (state is CashRegisterUiState.KasAktif) {
            _state.value = state.copy(
                saldoFisikInput = value.filter { it.isDigit() },
                pesanErrorTutup = null,
            )
        }
    }

    fun tutupKas() {
        if (sedangTutup) return
        val state = _state.value
        if (state !is CashRegisterUiState.KasAktif) return
        val shiftId = shiftIdAktif ?: return

        val saldoFisik = state.saldoFisikInput.toLongOrNull()
        if (saldoFisik == null) {
            _state.value = state.copy(pesanErrorTutup = "Masukkan saldo fisik yang valid")
            return
        }

        sedangTutup = true
        _state.value = state.copy(apakahSedangTutup = true, pesanErrorTutup = null)
        viewModelScope.launch {
            try {
                tutupKas(shiftId, saldoFisik)
                // Cuma set success flag — biarkan combine transisi ke BelumBuka secara natural
                _tutupBerhasil.value = true
            } catch (e: Exception) {
                val s = _state.value
                if (s is CashRegisterUiState.KasAktif) {
                    _state.value = s.copy(
                        apakahSedangTutup = false,
                        pesanErrorTutup = "Gagal tutup kas: ${e.message}",
                    )
                }
            } finally {
                sedangTutup = false
            }
        }
    }

    // ── Aksi Setoran ──

    fun bukaDialogSetoran() {
        val state = _state.value
        when (state) {
            is CashRegisterUiState.BelumBuka -> _state.value = state.copy(
                apakahDialogSetoranTerbuka = true, nominalSetoran = "", catatanSetoran = "", pesanErrorSetoran = null,
            )
            is CashRegisterUiState.KasAktif -> _state.value = state.copy(
                apakahDialogSetoranTerbuka = true, nominalSetoran = "", catatanSetoran = "", pesanErrorSetoran = null,
            )
            else -> {}
        }
    }

    fun tutupDialogSetoran() {
        val state = _state.value
        when (state) {
            is CashRegisterUiState.BelumBuka -> _state.value = state.copy(apakahDialogSetoranTerbuka = false, pesanErrorSetoran = null)
            is CashRegisterUiState.KasAktif -> _state.value = state.copy(apakahDialogSetoranTerbuka = false, pesanErrorSetoran = null)
            else -> {}
        }
    }

    fun perbaruiNominalSetoran(value: String) {
        val state = _state.value
        when (state) {
            is CashRegisterUiState.BelumBuka -> _state.value = state.copy(nominalSetoran = value.filter { it.isDigit() }, pesanErrorSetoran = null)
            is CashRegisterUiState.KasAktif -> _state.value = state.copy(nominalSetoran = value.filter { it.isDigit() }, pesanErrorSetoran = null)
            else -> {}
        }
    }

    fun perbaruiCatatanSetoran(value: String) {
        val state = _state.value
        when (state) {
            is CashRegisterUiState.BelumBuka -> _state.value = state.copy(catatanSetoran = value)
            is CashRegisterUiState.KasAktif -> _state.value = state.copy(catatanSetoran = value)
            else -> {}
        }
    }

    fun simpanSetoran() {
        val state = _state.value
        val (nominalSetoranVal, catatanSetoranVal) = when (state) {
            is CashRegisterUiState.BelumBuka -> {
                if (state.apakahSedangSimpanSetoran) return
                Pair(state.nominalSetoran, state.catatanSetoran)
            }
            is CashRegisterUiState.KasAktif -> {
                if (state.apakahSedangSimpanSetoran) return
                Pair(state.nominalSetoran, state.catatanSetoran)
            }
            else -> return
        }

        val nominal = nominalSetoranVal.toLongOrNull()
        if (nominal == null || nominal <= 0L) {
            val eState = _state.value
            when (eState) {
                is CashRegisterUiState.BelumBuka -> _state.value = eState.copy(pesanErrorSetoran = "Masukkan nominal yang valid")
                is CashRegisterUiState.KasAktif -> _state.value = eState.copy(pesanErrorSetoran = "Masukkan nominal yang valid")
                else -> {}
            }
            return
        }

        val loadingState = _state.value
        when (loadingState) {
            is CashRegisterUiState.BelumBuka -> _state.value = loadingState.copy(apakahSedangSimpanSetoran = true, pesanErrorSetoran = null)
            is CashRegisterUiState.KasAktif -> _state.value = loadingState.copy(apakahSedangSimpanSetoran = true, pesanErrorSetoran = null)
            else -> return
        }

        viewModelScope.launch {
            try {
                catatSetoran(nominal = nominal, catatan = catatanSetoranVal)
                val s = _state.value
                when (s) {
                    is CashRegisterUiState.BelumBuka -> _state.value = s.copy(apakahDialogSetoranTerbuka = false, apakahSedangSimpanSetoran = false, nominalSetoran = "", catatanSetoran = "")
                    is CashRegisterUiState.KasAktif -> _state.value = s.copy(apakahDialogSetoranTerbuka = false, apakahSedangSimpanSetoran = false, nominalSetoran = "", catatanSetoran = "")
                    else -> {}
                }
            } catch (e: Exception) {
                val s = _state.value
                when (s) {
                    is CashRegisterUiState.BelumBuka -> _state.value = s.copy(apakahSedangSimpanSetoran = false, pesanErrorSetoran = "Gagal menyimpan: ${e.message}")
                    is CashRegisterUiState.KasAktif -> _state.value = s.copy(apakahSedangSimpanSetoran = false, pesanErrorSetoran = "Gagal menyimpan: ${e.message}")
                    else -> {}
                }
            }
        }
    }

    // ── Dialog Edit Setoran ──

    fun bukaDialogEditSetoran(setoran: Setoran) {
        val state = _state.value
        when (state) {
            is CashRegisterUiState.BelumBuka -> _state.value = state.copy(
                apakahDialogEditSetoranTerbuka = true,
                setoranYangDiedit = setoran,
                catatanEditSetoran = setoran.catatan,
                pesanErrorEditSetoran = null,
            )
            is CashRegisterUiState.KasAktif -> _state.value = state.copy(
                apakahDialogEditSetoranTerbuka = true,
                setoranYangDiedit = setoran,
                catatanEditSetoran = setoran.catatan,
                pesanErrorEditSetoran = null,
            )
            else -> {}
        }
    }

    fun tutupDialogEditSetoran() {
        val state = _state.value
        when (state) {
            is CashRegisterUiState.BelumBuka -> _state.value = state.copy(
                apakahDialogEditSetoranTerbuka = false,
                setoranYangDiedit = null,
                pesanErrorEditSetoran = null,
            )
            is CashRegisterUiState.KasAktif -> _state.value = state.copy(
                apakahDialogEditSetoranTerbuka = false,
                setoranYangDiedit = null,
                pesanErrorEditSetoran = null,
            )
            else -> {}
        }
    }

    fun perbaruiCatatanEditSetoran(value: String) {
        val state = _state.value
        when (state) {
            is CashRegisterUiState.BelumBuka -> _state.value = state.copy(catatanEditSetoran = value)
            is CashRegisterUiState.KasAktif -> _state.value = state.copy(catatanEditSetoran = value)
            else -> {}
        }
    }

    fun simpanEditSetoran() {
        val state = _state.value
        val setoran = when (state) {
            is CashRegisterUiState.BelumBuka -> if (!state.apakahSedangSimpanEditSetoran) state.setoranYangDiedit else null
            is CashRegisterUiState.KasAktif -> if (!state.apakahSedangSimpanEditSetoran) state.setoranYangDiedit else null
            else -> null
        } ?: return

        val catatanBaru = when (state) {
            is CashRegisterUiState.BelumBuka -> state.catatanEditSetoran
            is CashRegisterUiState.KasAktif -> state.catatanEditSetoran
            else -> return
        }

        when (state) {
            is CashRegisterUiState.BelumBuka -> _state.value = state.copy(apakahSedangSimpanEditSetoran = true, pesanErrorEditSetoran = null)
            is CashRegisterUiState.KasAktif -> _state.value = state.copy(apakahSedangSimpanEditSetoran = true, pesanErrorEditSetoran = null)
            else -> return
        }

        viewModelScope.launch {
            try {
                perbaruiSetoran(setoran.id, catatanBaru)
                val s = _state.value
                when (s) {
                    is CashRegisterUiState.BelumBuka -> _state.value = s.copy(
                        apakahDialogEditSetoranTerbuka = false,
                        setoranYangDiedit = null,
                        apakahSedangSimpanEditSetoran = false,
                    )
                    is CashRegisterUiState.KasAktif -> _state.value = s.copy(
                        apakahDialogEditSetoranTerbuka = false,
                        setoranYangDiedit = null,
                        apakahSedangSimpanEditSetoran = false,
                    )
                    else -> {}
                }
            } catch (e: Exception) {
                val s = _state.value
                when (s) {
                    is CashRegisterUiState.BelumBuka -> _state.value = s.copy(
                        apakahSedangSimpanEditSetoran = false,
                        pesanErrorEditSetoran = "Gagal menyimpan: ${e.message}",
                    )
                    is CashRegisterUiState.KasAktif -> _state.value = s.copy(
                        apakahSedangSimpanEditSetoran = false,
                        pesanErrorEditSetoran = "Gagal menyimpan: ${e.message}",
                    )
                    else -> {}
                }
            }
        }
    }

    fun hapusSetoran(id: String) {
        val useCase = hapusSetoran
        viewModelScope.launch {
            try {
                useCase(id)
                tutupDialogEditSetoran()
            } catch (_: Exception) { }
        }
    }

    // ── Export PDF ──

    private val _statusExport = MutableStateFlow<StatusExportPdf?>(null)
    val statusExport: StateFlow<StatusExportPdf?> = _statusExport.asStateFlow()

    fun bersihkanStatusExport() {
        _statusExport.value = null
    }

    fun exportPdfDetailKas(
        context: Context,
        uri: Uri,
        shiftId: String,
        namaUsaha: String = "",
        alamat: String = "",
        tagline: String = "",
        logoUri: String = "",
    ) {
        _statusExport.value = StatusExportPdf.SedangMengexport
        viewModelScope.launch {
            try {
                val shift = cashRepository.ambilKasBerdasarkanId(shiftId)
                    ?: run {
                        _statusExport.value = StatusExportPdf.Gagal("Kas tidak ditemukan")
                        return@launch
                    }

                val sampai = shift.waktuTutup ?: Long.MAX_VALUE
                val daftarTransaksi = transactionRepository.ambilTransactionRentang(shift.waktuBuka, sampai)
                    .filter { !it.dibatalkan }
                    .sortedBy { it.waktuTransactionEpochMili }
                val daftarMutasi = cashRepository.amatiMutasiBerdasarkanKas(shift.id).first()
                    .sortedBy { it.waktu }

                val penjualanTunai = daftarTransaksi.filter { it.paymentMethod == PaymentMethod.Cash }
                    .sumOf { it.hitungTotalAkhirTransaction() }
                val penjualanQRIS = daftarTransaksi.filter { it.paymentMethod == PaymentMethod.Qris }
                    .sumOf { it.hitungTotalAkhirTransaction() }
                val totalPemasukan = daftarMutasi.filter { it.tipe == CashMutationType.Pemasukan }
                    .sumOf { it.nominal.nilaiRupiah }
                val totalPengeluaran = daftarMutasi.filter { it.tipe == CashMutationType.Pengeluaran }
                    .sumOf { it.nominal.nilaiRupiah }
                val totalSetoran = cashRepository.hitungTotalSetoranBerdasarkanKas(shift.id).first()
                val saldoSaatIni = shift.saldoAwal.nilaiRupiah + penjualanTunai + totalPemasukan - totalPengeluaran - totalSetoran

                val logoBitmap = muatLogoKas(context, logoUri)
                val waktuCetak = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID")).format(Date())

                val pdfHasil = withContext(Dispatchers.IO) {
                    CashReportPdfGenerator().generateDetailKas(
                        namaToko = namaUsaha.ifBlank { "FLEXI KASIR" },
                        alamat = alamat,
                        tagline = tagline,
                        logoBitmap = logoBitmap,
                        shift = shift,
                        saldoSaatIni = saldoSaatIni,
                        penjualanTunai = penjualanTunai,
                        penjualanQRIS = penjualanQRIS,
                        totalPemasukan = totalPemasukan,
                        totalPengeluaran = totalPengeluaran,
                        daftarTransaksi = daftarTransaksi,
                        daftarMutasi = daftarMutasi,
                        waktuCetak = waktuCetak,
                    )
                }

                withContext(Dispatchers.IO) {
                    context.contentResolver.openFileDescriptor(uri, "w")?.use { pfd ->
                        FileOutputStream(pfd.fileDescriptor).use { output ->
                            pdfHasil.writeTo(output)
                        }
                        pdfHasil.close()
                    } ?: throw Exception("Gagal membuka file")
                }

                _statusExport.value = StatusExportPdf.Berhasil
            } catch (e: Exception) {
                _statusExport.value = StatusExportPdf.Gagal("Gagal: ${e.message}")
            }
        }
    }

    fun exportPdfRekapKas(
        context: Context,
        uri: Uri,
        mulai: Long,
        selesai: Long,
        namaUsaha: String = "",
        alamat: String = "",
        tagline: String = "",
        logoUri: String = "",
    ) {
        _statusExport.value = StatusExportPdf.SedangMengexport
        viewModelScope.launch {
            try {
                val semuaKas = cashRepository.amatiSemuaKas().first()
                val semuaSetoran = cashRepository.amatiSetoran().first()

                val daftarKas = semuaKas
                    .filter { it.waktuBuka >= mulai && it.waktuBuka < selesai }
                    .sortedBy { it.waktuBuka }
                val daftarSetoran = semuaSetoran
                    .filter { !it.dihapus && it.waktu >= mulai && it.waktu < selesai }
                    .sortedBy { it.waktu }

                if (daftarKas.isEmpty() && daftarSetoran.isEmpty()) {
                    _statusExport.value = StatusExportPdf.Gagal("Tidak ada data untuk diexport")
                    return@launch
                }

                val barisKas = daftarKas.map { shift ->
                    val sampai = shift.waktuTutup ?: Long.MAX_VALUE
                    BarisKasExport(
                        shift = shift,
                        penjualanTunai = transactionRepository.hitungTotalTunaiRentang(shift.waktuBuka, sampai),
                        penjualanQRIS = transactionRepository.hitungTotalQRISRentang(shift.waktuBuka, sampai),
                        totalPemasukan = cashRepository.ambilTotalMutasiBerdasarkanTipe(shift.id, CashMutationType.Pemasukan.name),
                        totalPengeluaran = cashRepository.ambilTotalMutasiBerdasarkanTipe(shift.id, CashMutationType.Pengeluaran.name),
                    )
                }

                val logoBitmap = muatLogoKas(context, logoUri)
                val waktuCetak = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID")).format(Date())
                val fmtP = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
                val periodeLabel = "${fmtP.format(Date(mulai))} - ${fmtP.format(Date(selesai - 86400000L))}"

                val pdfHasil = withContext(Dispatchers.IO) {
                    CashReportPdfGenerator().generateRekapKas(
                        namaToko = namaUsaha.ifBlank { "FLEXI KASIR" },
                        alamat = alamat,
                        tagline = tagline,
                        logoBitmap = logoBitmap,
                        periodeLabel = periodeLabel,
                        barisKas = barisKas,
                        daftarSetoran = daftarSetoran,
                        waktuCetak = waktuCetak,
                    )
                }

                withContext(Dispatchers.IO) {
                    context.contentResolver.openFileDescriptor(uri, "w")?.use { pfd ->
                        FileOutputStream(pfd.fileDescriptor).use { output ->
                            pdfHasil.writeTo(output)
                        }
                        pdfHasil.close()
                    } ?: throw Exception("Gagal membuka file")
                }

                _statusExport.value = StatusExportPdf.Berhasil
            } catch (e: Exception) {
                _statusExport.value = StatusExportPdf.Gagal("Gagal: ${e.message}")
            }
        }
    }

    private suspend fun muatLogoKas(context: Context, logoUri: String): Bitmap? {
        if (logoUri.isBlank()) return null
        return try {
            val cacheFile = File(context.cacheDir, "logo_cached.png")
            if (cacheFile.exists()) {
                BitmapFactory.decodeFile(cacheFile.absolutePath)
            } else {
                context.contentResolver.openInputStream(Uri.parse(logoUri))?.use { input ->
                    BitmapFactory.decodeStream(input)
                }
            }
        } catch (_: Exception) { null }
    }

    // ── Rekening ──

    fun bukaDialogSaldoAwalRekening() {
        _stateRekening.update {
            it.copy(
                apakahDialogSaldoAwalTampil = true,
                nominalSaldoAwal = "",
                catatanSaldoAwal = "",
                apakahSedangMenyimpan = false,
                pesanSnackbar = null,
            )
        }
    }

    fun tutupDialogSaldoAwalRekening() {
        _stateRekening.update { it.copy(apakahDialogSaldoAwalTampil = false) }
    }

    fun perbaruiNominalSaldoAwalRekening(value: String) {
        _stateRekening.update { it.copy(nominalSaldoAwal = value.filter { c -> c.isDigit() }) }
    }

    fun perbaruiCatatanSaldoAwalRekening(value: String) {
        _stateRekening.update { it.copy(catatanSaldoAwal = value) }
    }

    fun simpanSaldoAwalRekening() {
        if (_stateRekening.value.apakahSedangMenyimpan) return
        val nominal = _stateRekening.value.nominalSaldoAwal.toLongOrNull()
        if (nominal == null || nominal <= 0L) {
            _stateRekening.update { it.copy(pesanSnackbar = "Masukkan nominal yang valid.") }
            return
        }

        _stateRekening.update { it.copy(apakahSedangMenyimpan = true) }
        viewModelScope.launch {
            try {
                aturSaldoAwalRekening(
                    nominal = nominal,
                    catatan = _stateRekening.value.catatanSaldoAwal.trim(),
                )
                _stateRekening.update {
                    it.copy(
                        apakahDialogSaldoAwalTampil = false,
                        apakahSedangMenyimpan = false,
                        pesanSnackbar = "Saldo awal rekening berhasil diatur.",
                    )
                }
            } catch (e: Exception) {
                _stateRekening.update {
                    it.copy(
                        apakahSedangMenyimpan = false,
                        pesanSnackbar = "Gagal mengatur saldo awal: ${e.message}",
                    )
                }
            }
        }
    }

    fun bukaDialogMutasiRekening(tipe: MutasiRekeningTipe) {
        _stateRekening.update {
            it.copy(
                apakahDialogMutasiTampil = true,
                tipeMutasi = tipe,
                nominalMutasi = "",
                catatanMutasi = "",
                apakahSedangMenyimpan = false,
                pesanSnackbar = null,
            )
        }
    }

    fun tutupDialogMutasiRekening() {
        _stateRekening.update { it.copy(apakahDialogMutasiTampil = false) }
    }

    fun perbaruiNominalMutasiRekening(value: String) {
        _stateRekening.update { it.copy(nominalMutasi = value.filter { c -> c.isDigit() }) }
    }

    fun perbaruiCatatanMutasiRekening(value: String) {
        _stateRekening.update { it.copy(catatanMutasi = value) }
    }

    fun simpanMutasiRekening() {
        if (_stateRekening.value.apakahSedangMenyimpan) return
        val nominal = _stateRekening.value.nominalMutasi.toLongOrNull()
        if (nominal == null || nominal <= 0L) {
            _stateRekening.update { it.copy(pesanSnackbar = "Masukkan nominal yang valid.") }
            return
        }

        _stateRekening.update { it.copy(apakahSedangMenyimpan = true) }
        viewModelScope.launch {
            try {
                catatMutasiRekening(
                    tipe = _stateRekening.value.tipeMutasi,
                    nominal = nominal,
                    catatan = _stateRekening.value.catatanMutasi.trim(),
                )
                _stateRekening.update {
                    it.copy(
                        apakahDialogMutasiTampil = false,
                        apakahSedangMenyimpan = false,
                        pesanSnackbar = "Mutasi rekening berhasil dicatat.",
                    )
                }
            } catch (e: Exception) {
                _stateRekening.update {
                    it.copy(
                        apakahSedangMenyimpan = false,
                        pesanSnackbar = "Gagal mencatat mutasi: ${e.message}",
                    )
                }
            }
        }
    }

    fun bersihkanPesanRekening() {
        _stateRekening.update { it.copy(pesanSnackbar = null) }
    }

}
