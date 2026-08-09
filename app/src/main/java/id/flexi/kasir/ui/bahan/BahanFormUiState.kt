package id.flexi.kasir.ui.bahan

import id.flexi.kasir.domain.model.Bahan
import java.util.UUID

data class BahanFormUiState(
    val id: String = "",
    val nama: String = "",
    val satuan: String = "",
    val stokTersedia: String = "",
    val hargaPerSatuan: String = "",
    val stokMinimum: Int = 0,
    val aktif: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val apakahModeEdit: Boolean = false,
    val apakahSedangMemuat: Boolean = false,
    val errorNama: String? = null,
    val errorSatuan: String? = null,
    val pesanSukses: String? = null,
    val pesanError: String? = null,
)

fun BahanFormUiState.toDomain(): Bahan? {
    val namaBersih = nama.trim()
    if (namaBersih.isBlank()) return null
    val stokParsed = (stokTersedia.toDoubleOrNull() ?: 0.0)
    return Bahan(
        id = id.ifBlank { UUID.randomUUID().toString() },
        nama = namaBersih,
        satuan = satuan.trim(),
        stokTersedia = if (stokParsed.isFinite() && stokParsed >= 0.0) stokParsed else 0.0,
        hargaPerSatuan = hargaPerSatuan.toLongOrNull() ?: 0,
        // Field yang tidak tampil di form dipertahankan agar edit tidak
        // menghancurkan stokMinimum, status aktif, maupun urutan createdAt.
        stokMinimum = stokMinimum,
        aktif = aktif,
        createdAt = createdAt,
    )
}

fun Bahan.toFormUiState(): BahanFormUiState = BahanFormUiState(
    id = id,
    nama = nama,
    satuan = satuan,
    stokTersedia = if (stokTersedia > 0) formatStok(stokTersedia) else "",
    hargaPerSatuan = if (hargaPerSatuan > 0) hargaPerSatuan.toString() else "",
    stokMinimum = stokMinimum,
    aktif = aktif,
    createdAt = createdAt,
    apakahModeEdit = true,
)

/** Menampilkan nilai utuh tanpa akhiran ".0" (mis. 1.0 → "1"). */
private fun formatStok(nilai: Double): String =
    if (nilai % 1.0 == 0.0) nilai.toLong().toString() else nilai.toString()
