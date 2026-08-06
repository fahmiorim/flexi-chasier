package id.flexi.kasir.data.local.mapping

import id.flexi.kasir.data.local.entity.LocalBahanEntity
import id.flexi.kasir.data.local.entity.LocalBahanResepEntity
import id.flexi.kasir.data.local.entity.LocalPembelianBahanEntity
import id.flexi.kasir.data.local.entity.LocalResepEntity
import id.flexi.kasir.domain.model.Bahan
import id.flexi.kasir.domain.model.BahanResep
import id.flexi.kasir.domain.model.PembelianBahan
import id.flexi.kasir.domain.model.Resep

/** Mengubah entitas database menjadi objek domain Bahan. */
fun LocalBahanEntity.keDomain(): Bahan = Bahan(
    id = id,
    nama = nama,
    satuan = satuan,
    stokTersedia = stokTersedia,
    hargaPerSatuan = hargaPerSatuan,
    stokMinimum = stokMinimum,
    aktif = aktif,
    createdAt = createdAt,
)

/** Mengubah objek domain Bahan menjadi entitas database. */
fun Bahan.keLokal(): LocalBahanEntity = LocalBahanEntity(
    id = id,
    nama = nama,
    satuan = satuan,
    stokTersedia = stokTersedia,
    hargaPerSatuan = hargaPerSatuan,
    stokMinimum = stokMinimum,
    aktif = aktif,
    createdAt = createdAt,
)

/** Mengubah entitas database menjadi objek domain PembelianBahan. */
fun LocalPembelianBahanEntity.keDomain(): PembelianBahan = PembelianBahan(
    id = id,
    bahanId = bahanId,
    jumlah = jumlah,
    satuanBeli = satuanBeli,
    totalHarga = totalHarga,
    tanggalBeli = tanggalBeli,
    catatan = catatan,
    mutasiKasId = mutasiKasId,
)

/** Mengubah objek domain PembelianBahan menjadi entitas database. */
fun PembelianBahan.keLokal(): LocalPembelianBahanEntity = LocalPembelianBahanEntity(
    id = id,
    bahanId = bahanId,
    jumlah = jumlah,
    satuanBeli = satuanBeli,
    totalHarga = totalHarga,
    tanggalBeli = tanggalBeli,
    catatan = catatan,
    mutasiKasId = mutasiKasId,
)

/** Mengubah entitas database menjadi objek domain Resep. */
fun LocalResepEntity.keDomain(
    daftarBahan: List<BahanResep> = emptyList(),
): Resep = Resep(
    id = id,
    produkId = produkId,
    varianNama = varianNama,
    daftarBahan = daftarBahan,
    createdAt = createdAt,
)

/** Mengubah objek domain Resep menjadi entitas database (tanpa daftar bahan). */
fun Resep.keLokal(): LocalResepEntity = LocalResepEntity(
    id = id,
    produkId = produkId,
    varianNama = varianNama,
    createdAt = createdAt,
)

/** Mengubah entitas database menjadi objek domain BahanResep. */
fun LocalBahanResepEntity.keDomain(): BahanResep = BahanResep(
    id = id.toString(),
    resepId = resepId,
    bahanId = bahanId,
    jumlah = jumlah,
    satuan = satuan,
)

/** Mengubah objek domain BahanResep menjadi entitas database. */
fun BahanResep.keLokal(): LocalBahanResepEntity = LocalBahanResepEntity(
    id = id.toLongOrNull() ?: 0,
    resepId = resepId,
    bahanId = bahanId,
    jumlah = jumlah,
    satuan = satuan,
)
