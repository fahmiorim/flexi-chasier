package id.cassy.kasir.antarmuka.meja

import id.cassy.kasir.ranah.model.Meja

data class ModelTampilanPengaturanMeja(
    val daftarMeja: List<Meja> = emptyList(),
    val nomorMejaBaru: String = "",
    val apakahSedangMemuat: Boolean = false,
    val pesanError: String? = null,
)
