package id.cassy.kasir.data.lokal.repositori

import id.cassy.kasir.data.lokal.basisdata.BasisDataCassyKasir
import id.cassy.kasir.data.lokal.entitas.EntitasMejaLokal
import id.cassy.kasir.ranah.model.Meja
import id.cassy.kasir.ranah.repositori.RepositoriMeja
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RepositoriMejaLokal(
    private val basisData: BasisDataCassyKasir,
) : RepositoriMeja {

    override fun amatiSemuaMeja(): Flow<List<Meja>> {
        return basisData.aksesDataMejaLokal().amatiSemuaMeja().map { daftarEntitas ->
            daftarEntitas.map { it.sebagaiMeja() }
        }
    }

    override suspend fun simpanMeja(meja: Meja) {
        basisData.aksesDataMejaLokal().simpanMeja(
            EntitasMejaLokal(
                id = meja.id,
                nomor = meja.nomor,
                aktif = meja.aktif,
            ),
        )
    }

    override suspend fun hapusMeja(id: String) {
        basisData.aksesDataMejaLokal().hapusMeja(id)
    }

    private fun EntitasMejaLokal.sebagaiMeja(): Meja = Meja(
        id = id,
        nomor = nomor,
        aktif = aktif,
    )
}
