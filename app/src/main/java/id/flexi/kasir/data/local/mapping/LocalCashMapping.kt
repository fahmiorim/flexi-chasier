package id.flexi.kasir.data.local.mapping

import id.flexi.kasir.data.local.entity.LocalCashKasEntity
import id.flexi.kasir.data.local.entity.LocalCashMutationEntity
import id.flexi.kasir.data.local.entity.LocalSetoranEntity
import id.flexi.kasir.domain.model.CashKas
import id.flexi.kasir.domain.model.CashKasStatus
import id.flexi.kasir.domain.model.CashMutation
import id.flexi.kasir.domain.model.CashMutationType
import id.flexi.kasir.domain.model.CashExpenseCategory
import id.flexi.kasir.domain.model.Setoran
import id.flexi.kasir.domain.model.Uang

/**
 * Memetakan entitas basis data lokal ke model ranah (domain).
 */
fun LocalCashKasEntity.toDomain(): CashKas = CashKas(
    id = id,
    saldoAwal = Uang.dariRupiah(saldoAwal),
    saldoAkhir = saldoAkhir?.let { Uang.dariRupiah(it) },
    waktuBuka = waktuBuka,
    waktuTutup = waktuTutup,
    status = if (status == "Tutup") CashKasStatus.Tutup else CashKasStatus.Buka,
    catatanBuka = catatanBuka,
    catatanTutup = catatanTutup,
)

fun CashKas.toEntity(): LocalCashKasEntity = LocalCashKasEntity(
    id = id,
    saldoAwal = saldoAwal.nilaiRupiah,
    saldoAkhir = saldoAkhir?.nilaiRupiah,
    waktuBuka = waktuBuka,
    waktuTutup = waktuTutup,
    status = if (status == CashKasStatus.Tutup) "Tutup" else "Buka",
    catatanBuka = catatanBuka,
    catatanTutup = catatanTutup,
)

/**
 * Memetakan entitas mutasi lokal ke model ranah.
 */
fun LocalCashMutationEntity.toDomain(): CashMutation = CashMutation(
    id = id,
    shiftId = shiftId,
    tipe = if (tipe == "Pemasukan") CashMutationType.Pemasukan else CashMutationType.Pengeluaran,
    kategori = CashExpenseCategory.fromString(kategori),
    nominal = Uang.dariRupiah(nominal),
    catatan = catatan,
    waktu = waktu,
)

fun CashMutation.toEntity(): LocalCashMutationEntity = LocalCashMutationEntity(
    id = id,
    shiftId = shiftId,
    tipe = if (tipe == CashMutationType.Pemasukan) "Pemasukan" else "Pengeluaran",
    kategori = kategori.name,
    nominal = nominal.nilaiRupiah,
    catatan = catatan,
    waktu = waktu,
)

/**
 * Memetakan entitas setoran lokal ke model ranah.
 */
fun LocalSetoranEntity.toDomain(): Setoran = Setoran(
    id = id,
    nominal = Uang.dariRupiah(nominal),
    catatan = catatan,
    waktu = waktu,
    dihapus = dihapus,
)

fun Setoran.toEntity(): LocalSetoranEntity = LocalSetoranEntity(
    id = id,
    nominal = nominal.nilaiRupiah,
    catatan = catatan,
    waktu = waktu,
    dihapus = dihapus,
)
