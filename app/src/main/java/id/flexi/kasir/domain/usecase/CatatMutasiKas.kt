package id.flexi.kasir.domain.usecase

import id.flexi.kasir.domain.identity.CashKasIdGenerator
import id.flexi.kasir.domain.model.CashExpenseCategory
import id.flexi.kasir.domain.model.CashMutation
import id.flexi.kasir.domain.model.CashMutationType
import id.flexi.kasir.domain.model.Uang
import id.flexi.kasir.domain.repository.CashRepository

class CatatMutasiKas(
    private val cashRepository: CashRepository,
) {
    suspend operator fun invoke(
        shiftId: String,
        tipe: CashMutationType,
        nominal: Long,
        catatan: String,
        kategori: CashExpenseCategory = CashExpenseCategory.Lainnya,
    ): CashMutation {
        val mutasi = CashMutation(
            id = CashKasIdGenerator.buatIdentitasMutasiBaru(),
            shiftId = shiftId,
            tipe = tipe,
            kategori = kategori,
            nominal = Uang.dariRupiah(nominal),
            catatan = catatan,
            waktu = System.currentTimeMillis(),
        )
        cashRepository.simpanMutasi(mutasi)
        return mutasi
    }
}
