package id.flexi.kasir.domain.usecase

import id.flexi.kasir.domain.model.BahanResep
import id.flexi.kasir.domain.model.Resep
import id.flexi.kasir.domain.repository.BahanRepository
import java.util.UUID

class SimpanResep(
    private val BahanRepository: BahanRepository,
) {
    /**
     * Menyimpan resep produk beserta komposisi bahannya secara atomik.
     * Urutan: simpan resep dulu (validasi FK), lalu hapus komposisi lama, lalu simpan komposisi baru.
     */
    suspend operator fun invoke(
        id: String?,
        produkId: String,
        varianNama: String?,
        daftarBahan: List<BahanResep>,
    ) {
        val resepId = id ?: UUID.randomUUID().toString()

        val resep = Resep(
            id = resepId,
            produkId = produkId,
            varianNama = varianNama,
            createdAt = System.currentTimeMillis(),
        )

        // 1. Simpan resep dulu (validasi FK produkId)
        BahanRepository.saveResep(resep)

        // 2. Hapus komposisi lama
        BahanRepository.deleteBahanResepByResepId(resepId)

        // 3. Simpan komposisi baru — id = "" biar Room autoGenerate
        val bahanResepBaru = daftarBahan.map { bahan ->
            bahan.copy(
                id = "",
                resepId = resepId,
            )
        }
        BahanRepository.saveBahanResep(bahanResepBaru)
    }
}
