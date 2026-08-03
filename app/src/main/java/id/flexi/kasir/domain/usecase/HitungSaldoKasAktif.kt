package id.flexi.kasir.domain.usecase

import id.flexi.kasir.domain.model.CashKas
import id.flexi.kasir.domain.repository.CashRepository

data class SaldoKasResult(
    val saldoAwal: Long,
    val penjualanTunai: Long,
    val totalPemasukan: Long,
    val totalPengeluaran: Long,
    val saldoSaatIni: Long,
)

/**
 * Use case tidak dipakai — penghitungan saldo kas dilakukan langsung
 * di [CashRegisterViewModel.observeMutasiDanSaldo] via Flow combine.
 *
 * Dipertahankan untuk referensi struktur data [SaldoKasResult].
 */
class HitungSaldoKasAktif(
    private val cashRepository: CashRepository,
) {
    suspend operator fun invoke(kas: CashKas): SaldoKasResult {
        return SaldoKasResult(
            saldoAwal = kas.saldoAwal.nilaiRupiah,
            penjualanTunai = 0L,
            totalPemasukan = 0L,
            totalPengeluaran = 0L,
            saldoSaatIni = kas.saldoAwal.nilaiRupiah,
        )
    }
}
