package id.flexi.kasir.domain.usecase

import id.flexi.kasir.domain.identity.CashKasIdGenerator
import id.flexi.kasir.domain.model.CashKas
import id.flexi.kasir.domain.model.CashKasStatus
import id.flexi.kasir.domain.model.Uang
import id.flexi.kasir.domain.repository.CashRepository

/**
 * Membuka sesi kas baru.
 *
 * @param saldoAwal Uang modal awal di laci (dalam Rupiah).
 * @param catatan Catatan opsional saat membuka kas.
 */
class BukaKas(
    private val cashRepository: CashRepository,
) {
    suspend operator fun invoke(
        saldoAwal: Long,
        waktuBuka: Long = System.currentTimeMillis(),
        catatan: String? = null,
    ): CashKas {
        val kas = CashKas(
            id = CashKasIdGenerator.buatIdentitasBaru(),
            saldoAwal = Uang.dariRupiah(saldoAwal),
            waktuBuka = waktuBuka,
            status = CashKasStatus.Buka,
            catatanBuka = catatan,
        )
        cashRepository.simpanKas(kas)
        return kas
    }
}
