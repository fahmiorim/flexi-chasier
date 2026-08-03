package id.flexi.kasir.ui.bahan

import id.flexi.kasir.domain.model.Bahan
import java.util.UUID

data class BahanFormUiState(
    val id: String = "",
    val nama: String = "",
    val satuan: String = "",
    val stokTersedia: String = "",
    val hargaPerSatuan: String = "",
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
    return Bahan(
        id = id.ifBlank { UUID.randomUUID().toString() },
        nama = namaBersih,
        satuan = satuan.trim(),
        stokTersedia = stokTersedia.toDoubleOrNull() ?: 0.0,
        hargaPerSatuan = hargaPerSatuan.toLongOrNull() ?: 0,
    )
}

fun Bahan.toFormUiState(): BahanFormUiState = BahanFormUiState(
    id = id,
    nama = nama,
    satuan = satuan,
    stokTersedia = if (stokTersedia > 0) stokTersedia.toString() else "",
    hargaPerSatuan = if (hargaPerSatuan > 0) hargaPerSatuan.toString() else "",
    apakahModeEdit = true,
)
