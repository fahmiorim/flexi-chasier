package id.flexi.kasir.domain.usecase

import id.flexi.kasir.domain.model.MutasiRekening
import id.flexi.kasir.domain.model.MutasiRekeningTipe
import id.flexi.kasir.domain.model.Uang
import id.flexi.kasir.domain.repository.StokRekeningRepository
import java.util.UUID

/**
 * Mencatat mutasi rekening pemasukan/penarikan manual.
 *
 * @param tipe Hanya Pemasukan atau Penarikan (SaldoAwal memakai [AturSaldoAwalRekening]).
 * @param nominal Jumlah uang yang dimutasikan.
 * @param catatan Deskripsi mutasi.
 */
class CatatMutasiRekening(
    private val stokRekeningRepository: StokRekeningRepository,
) {
    suspend operator fun invoke(
        tipe: MutasiRekeningTipe,
        nominal: Long,
        catatan: String = "",
    ) {
        if (nominal <= 0L) return
        if (tipe == MutasiRekeningTipe.SaldoAwal) return
        stokRekeningRepository.simpanMutasiRekening(
            MutasiRekening(
                id = UUID.randomUUID().toString(),
                tipe = tipe,
                nominal = Uang(nominal),
                catatan = catatan,
            ),
        )
    }
}
