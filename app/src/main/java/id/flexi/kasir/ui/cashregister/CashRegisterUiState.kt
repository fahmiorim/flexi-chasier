package id.flexi.kasir.ui.cashregister

import id.flexi.kasir.domain.model.CashExpenseCategory
import id.flexi.kasir.domain.model.CashKas
import id.flexi.kasir.domain.model.CashKasRingkasan
import id.flexi.kasir.domain.model.CashMutation
import id.flexi.kasir.domain.model.CashMutationType
import id.flexi.kasir.domain.model.Setoran
import id.flexi.kasir.domain.model.Transaction
import java.util.Calendar

/**
 * Status tampilan layar kas.
 */
sealed interface CashRegisterUiState {
    data object Memuat : CashRegisterUiState

    /** Belum ada sesi kas aktif — harus buka kas dulu. */
    data class BelumBuka(
        val daftarKasTertutup: List<CashKas> = emptyList(),
        val daftarSetoran: List<Setoran> = emptyList(),
        val totalSetoran: String = "Rp0",
        val kasTerpilih: CashKas? = null,
        // Ringkasan keuangan per shift (termasuk tertutup)
        val ringkasanShift: Map<String, CashKasRingkasan> = emptyMap(),
        // Financial data dari shift terakhir (disimpan saat transisi dari KasAktif)
        val penjualanTunaiTerakhir: String = "Rp0",
        val penjualanQRISTerakhir: String = "Rp0",
        val penjualanTotalTerakhir: String = "Rp0",
        val totalPemasukanTerakhir: String = "Rp0",
        val totalPengeluaranTerakhir: String = "Rp0",
        val daftarMutasiTerakhir: List<CashMutation> = emptyList(),
        val saldoSaatIniTerakhir: String = "Rp0",
        // Data spesifik untuk shift yang dipilih (bukan "terakhir")
        val kasTerpilihPenjualanTunai: String? = null,
        val kasTerpilihPenjualanQRIS: String? = null,
        val kasTerpilihPenjualanTotal: String? = null,
        val kasTerpilihTotalPemasukan: String? = null,
        val kasTerpilihTotalPengeluaran: String? = null,
        val kasTerpilihDaftarMutasi: List<CashMutation>? = null,
        val kasTerpilihDaftarTransaksi: List<Transaction>? = null,
        val kasTerpilihSaldoSaatIni: String? = null,
        // Dialog buka kas
        val apakahDialogBukaTerbuka: Boolean = false,
        val tanggalBukaEpochMili: Long = System.currentTimeMillis(),
        val jamBukaJam: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
        val jamBukaMenit: Int = Calendar.getInstance().get(Calendar.MINUTE),
        val nominalBuka: String = "",
        val catatanBuka: String = "",
        val apakahSedangMemproses: Boolean = false,
        val pesanError: String? = null,
        // Dialog setoran
        val apakahDialogSetoranTerbuka: Boolean = false,
        val nominalSetoran: String = "",
        val catatanSetoran: String = "",
        val apakahSedangSimpanSetoran: Boolean = false,
        val pesanErrorSetoran: String? = null,
        // Dialog edit setoran
        val apakahDialogEditSetoranTerbuka: Boolean = false,
        val setoranYangDiedit: Setoran? = null,
        val catatanEditSetoran: String = "",
        val apakahSedangSimpanEditSetoran: Boolean = false,
        val pesanErrorEditSetoran: String? = null,
    ) : CashRegisterUiState

    /** Kas sedang aktif. */
    data class KasAktif(
        val kas: CashKas,
        val daftarKasTertutup: List<CashKas> = emptyList(),
        val daftarSetoran: List<Setoran> = emptyList(),
        val totalSetoran: String = "Rp0",
        val kasTerpilih: CashKas? = null,
        // Ringkasan keuangan per shift (termasuk tertutup)
        val ringkasanShift: Map<String, CashKasRingkasan> = emptyMap(),
        // Financial data untuk shift tertutup yang dipilih dari riwayat
        val kasTerpilihPenjualanTunai: String? = null,
        val kasTerpilihPenjualanQRIS: String? = null,
        val kasTerpilihPenjualanTotal: String? = null,
        val kasTerpilihTotalPemasukan: String? = null,
        val kasTerpilihTotalPengeluaran: String? = null,
        val kasTerpilihDaftarMutasi: List<CashMutation>? = null,
        val kasTerpilihDaftarTransaksi: List<Transaction>? = null,
        val kasTerpilihSaldoSaatIni: String? = null,
        val saldoSaatIni: String,
        val akumulasiProfitShiftTertutup: Long = 0L,
        val penjualanTunai: String,
        val penjualanQRIS: String = "Rp0",
        val penjualanTotal: String = "Rp0",
        val totalPemasukan: String,
        val totalPengeluaran: String,
        val daftarMutasi: List<CashMutation>,
        // Dialog tambah mutasi
        val apakahDialogMutasiTerbuka: Boolean = false,
        val tipeMutasi: CashMutationType = CashMutationType.Pengeluaran,
        val kategoriMutasi: CashExpenseCategory = CashExpenseCategory.Lainnya,
        val nominalMutasi: String = "",
        val catatanMutasi: String = "",
        val apakahSedangSimpanMutasi: Boolean = false,
        val pesanErrorMutasi: String? = null,
        // Dialog tutup kas
        val apakahDialogTutupTerbuka: Boolean = false,
        val saldoFisikInput: String = "",
        val apakahSedangTutup: Boolean = false,
        val tutupBerhasil: Boolean = false,
        val pesanErrorTutup: String? = null,
        // Dialog setoran
        val apakahDialogSetoranTerbuka: Boolean = false,
        val nominalSetoran: String = "",
        val catatanSetoran: String = "",
        val apakahSedangSimpanSetoran: Boolean = false,
        val pesanErrorSetoran: String? = null,
        // Dialog edit setoran
        val apakahDialogEditSetoranTerbuka: Boolean = false,
        val setoranYangDiedit: Setoran? = null,
        val catatanEditSetoran: String = "",
        val apakahSedangSimpanEditSetoran: Boolean = false,
        val pesanErrorEditSetoran: String? = null,
    ) : CashRegisterUiState
}
