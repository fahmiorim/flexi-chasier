package id.flexi.kasir.data.local.mapping

import id.flexi.kasir.data.local.entity.LocalMutasiRekeningEntity
import id.flexi.kasir.data.local.entity.LocalPenyesuaianStokEntity
import id.flexi.kasir.domain.model.MutasiRekening
import id.flexi.kasir.domain.model.MutasiRekeningTipe
import id.flexi.kasir.domain.model.PenyesuaianStok
import id.flexi.kasir.domain.model.StokJenis
import id.flexi.kasir.domain.model.Uang

/** Mengubah entitas database menjadi objek domain PenyesuaianStok. */
fun LocalPenyesuaianStokEntity.keDomain(): PenyesuaianStok = PenyesuaianStok(
    id = id,
    jenis = StokJenis.entries.firstOrNull { it.name == jenis } ?: StokJenis.Bahan,
    entitasId = entitasId,
    namaEntitas = namaEntitas,
    stokSebelum = stokSebelum,
    stokSesudah = stokSesudah,
    alasan = alasan,
    waktu = waktu,
)

/** Mengubah objek domain PenyesuaianStok menjadi entitas database. */
fun PenyesuaianStok.keLokal(): LocalPenyesuaianStokEntity = LocalPenyesuaianStokEntity(
    id = id,
    jenis = jenis.name,
    entitasId = entitasId,
    namaEntitas = namaEntitas,
    stokSebelum = stokSebelum,
    stokSesudah = stokSesudah,
    selisih = selisih,
    alasan = alasan,
    waktu = waktu,
)

/** Mengubah entitas database menjadi objek domain MutasiRekening. */
fun LocalMutasiRekeningEntity.keDomain(): MutasiRekening = MutasiRekening(
    id = id,
    tipe = MutasiRekeningTipe.entries.firstOrNull { it.name == tipe } ?: MutasiRekeningTipe.Pemasukan,
    nominal = Uang(nominal),
    catatan = catatan,
    waktu = waktu,
)

/** Mengubah objek domain MutasiRekening menjadi entitas database. */
fun MutasiRekening.keLokal(): LocalMutasiRekeningEntity = LocalMutasiRekeningEntity(
    id = id,
    tipe = tipe.name,
    nominal = nominal.nilaiRupiah,
    catatan = catatan,
    waktu = waktu,
)
