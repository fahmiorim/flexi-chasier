package id.flexi.kasir.domain.usecase

import id.flexi.kasir.domain.identity.CashKasIdGenerator
import id.flexi.kasir.domain.model.Setoran
import id.flexi.kasir.domain.model.Uang
import id.flexi.kasir.domain.repository.CashRepository
import kotlinx.coroutines.flow.first

class CatatSetoran(
    private val cashRepository: CashRepository,
) {
    suspend operator fun invoke(
        nominal: Long,
        catatan: String,
    ): Setoran {
        // Setoran hanya sah bila ada shift kas yang masih terbuka.
        val kasAktif = cashRepository.amatiKasAktif().first()
            ?: throw IllegalArgumentException("Tidak ada shift kas terbuka. Buka kas dulu sebelum menyetor.")
        val setoran = Setoran(
            id = CashKasIdGenerator.buatIdentitasSetoranBaru(),
            shiftId = kasAktif.id,
            nominal = Uang.dariRupiah(nominal),
            catatan = catatan,
            waktu = System.currentTimeMillis(),
        )
        cashRepository.simpanSetoran(setoran)
        return setoran
    }
}
