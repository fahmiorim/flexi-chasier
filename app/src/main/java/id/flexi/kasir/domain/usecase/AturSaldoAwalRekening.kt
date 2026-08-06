package id.flexi.kasir.domain.usecase

import id.flexi.kasir.domain.model.MutasiRekening
import id.flexi.kasir.domain.model.MutasiRekeningTipe
import id.flexi.kasir.domain.model.Uang
import id.flexi.kasir.domain.repository.StokRekeningRepository
import java.util.UUID

/**
 * Menetapkan saldo awal rekening (tipe SaldoAwal).
 *
 * Membuat baris mutasi dengan tipe SaldoAwal sehingga saldo rekening
 * bertambah dan tersinkron lintas perangkat. Jika saldo awal sudah pernah
 * ditetapkan, sebaiknya disesuaikan lewat pemasukan/penarikan (SaldoAwal
 * hanya menambah).
 */
class AturSaldoAwalRekening(
    private val stokRekeningRepository: StokRekeningRepository,
) {
    suspend operator fun invoke(
        nominal: Long,
        catatan: String = "",
    ) {
        if (nominal <= 0L) return
        stokRekeningRepository.simpanMutasiRekening(
            MutasiRekening(
                id = UUID.randomUUID().toString(),
                tipe = MutasiRekeningTipe.SaldoAwal,
                nominal = Uang(nominal),
                catatan = catatan,
            ),
        )
    }
}
