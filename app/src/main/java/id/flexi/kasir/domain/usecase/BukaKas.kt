package id.flexi.kasir.domain.usecase

import id.flexi.kasir.domain.identity.CashKasIdGenerator
import id.flexi.kasir.domain.model.CashKas
import id.flexi.kasir.domain.model.CashKasStatus
import id.flexi.kasir.domain.model.Uang
import id.flexi.kasir.domain.repository.CashRepository
import kotlinx.coroutines.flow.first

/**
 * Membuka sesi kas baru.
 *
 * @param saldoAwal Uang modal awal di laci (dalam Rupiah).
 * @param catatan Catatan opsional saat membuka kas.
 * @throws IllegalArgumentException bila sudah ada shift kas yang masih terbuka.
 */
class BukaKas(
    private val cashRepository: CashRepository,
) {
    suspend operator fun invoke(
        saldoAwal: Long,
        waktuBuka: Long = System.currentTimeMillis(),
        catatan: String? = null,
    ): CashKas {
        // Cegah pembukaan ganda: hanya boleh ada SATU shift terbuka.
        val kasAktif = cashRepository.amatiKasAktif().first()
        if (kasAktif != null) {
            throw IllegalArgumentException("Shift kas masih terbuka. Tutup dulu sebelum membuka baru.")
        }
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
