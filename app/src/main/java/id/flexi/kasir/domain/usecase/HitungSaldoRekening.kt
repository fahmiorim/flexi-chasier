package id.flexi.kasir.domain.usecase

import id.flexi.kasir.domain.model.MutasiRekeningTipe
import id.flexi.kasir.domain.repository.CashRepository
import id.flexi.kasir.domain.repository.StokRekeningRepository
import id.flexi.kasir.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Menghitung saldo rekening kumulatif sesuai aturan backend:
 * saldoAkhir = SaldoAwal (catatan TERBARU) + Σ Setoran + Σ Penjualan QRIS + Σ Pemasukan − Σ Penarikan.
 *
 * Backend memakai catatan SaldoAwal terbaru saja (bukan menjumlahkan semua),
 * sehingga set ulang saldo awal tidak menggandakan saldo. Setoran & penjualan
 * QRIS ikut menambah saldo rekening agar selaras dengan laporan rekening di
 * server/web.
 *
 * Mengabaikan baris bernilai tidak valid; nominal tidak negatif dijamin
 * oleh [id.flexi.kasir.domain.model.Uang].
 */
class HitungSaldoRekening(
    private val stokRekeningRepository: StokRekeningRepository,
    private val transactionRepository: TransactionRepository,
    private val cashRepository: CashRepository,
) {
    operator fun invoke(): Flow<Long> = combine(
        stokRekeningRepository.amatiMutasiRekening(),
        transactionRepository.hitungTotalQRISSemua(),
        cashRepository.hitungTotalSetoranAktif(),
    ) { daftar, totalQris, totalSetoran ->
        val saldoAwal = daftar
            .filter { it.tipe == MutasiRekeningTipe.SaldoAwal }
            .maxByOrNull { it.waktu }
            ?.nominal?.nilaiRupiah ?: 0L
        val totalPemasukan = daftar
            .filter { it.tipe == MutasiRekeningTipe.Pemasukan }
            .sumOf { it.nominal.nilaiRupiah }
        val totalPenarikan = daftar
            .filter { it.tipe == MutasiRekeningTipe.Penarikan }
            .sumOf { it.nominal.nilaiRupiah }
        saldoAwal + totalSetoran + totalQris + totalPemasukan - totalPenarikan
    }
}
