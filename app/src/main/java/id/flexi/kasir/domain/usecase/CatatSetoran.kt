package id.flexi.kasir.domain.usecase

import id.flexi.kasir.domain.identity.CashKasIdGenerator
import id.flexi.kasir.domain.model.Setoran
import id.flexi.kasir.domain.model.Uang
import id.flexi.kasir.domain.repository.CashRepository

class CatatSetoran(
    private val cashRepository: CashRepository,
) {
    suspend operator fun invoke(
        nominal: Long,
        catatan: String,
    ): Setoran {
        val setoran = Setoran(
            id = CashKasIdGenerator.buatIdentitasSetoranBaru(),
            nominal = Uang.dariRupiah(nominal),
            catatan = catatan,
            waktu = System.currentTimeMillis(),
        )
        cashRepository.simpanSetoran(setoran)
        return setoran
    }
}
