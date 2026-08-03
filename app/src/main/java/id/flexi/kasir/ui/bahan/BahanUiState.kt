package id.flexi.kasir.ui.bahan

import id.flexi.kasir.domain.model.Bahan

data class BahanUiState(
    val daftarBahan: List<Bahan> = emptyList(),
    val kataKunciPencarian: String = "",
    val statusKonfirmasiHapus: StatusKonfirmasiHapusBahan = StatusKonfirmasiHapusBahan(),
    val apakahSedangMemuat: Boolean = true,
)

data class StatusKonfirmasiHapusBahan(
    val apakahTampil: Boolean = false,
    val idBahan: String = "",
    val namaBahan: String = "",
)
