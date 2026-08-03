package id.flexi.kasir.ui.table

import id.flexi.kasir.domain.model.Meja

data class SettingsUiStateMeja(
    val daftarMeja: List<Meja> = emptyList(),
    val barisStr: String = "3",
    val kolomStr: String = "4",
    val apakahSedangMemuat: Boolean = false,
    val pesanError: String? = null,
    val pesanSnackbar: String? = null,
) {
    val jumlahBaris: Int get() = barisStr.filter { it.isDigit() }.take(2).toIntOrNull() ?: 3
    val jumlahKolom: Int get() = kolomStr.filter { it.isDigit() }.take(2).toIntOrNull() ?: 4
    fun gridNomorMeja(): List<List<String?>> {
        val grid = MutableList(jumlahBaris) { MutableList<String?>(jumlahKolom) { null } }
        for (meja in daftarMeja) {
            val (baris, kolom) = meja.posisiGrid() ?: continue
            if (baris in 0..<jumlahBaris && kolom in 0..<jumlahKolom) {
                grid[baris][kolom] = meja.nomor
            }
        }
        return grid
    }
}

fun Meja.posisiGrid(): Pair<Int, Int>? {
    val sisa = id.removePrefix("grid_")
    if (sisa != id) {
        val parts = sisa.split("_")
        if (parts.size == 2) {
            val baris = parts[0].toIntOrNull()
            val kolom = parts[1].toIntOrNull()
            if (baris != null && kolom != null) return Pair(baris, kolom)
        }
    }
    // Fallback: format lama meja_r{baris}_c{kolom}
    val lama = id.removePrefix("meja_")
    val parts = lama.split("_r")
    if (parts.size == 2) {
        val koordinat = parts[1].split("_c")
        if (koordinat.size == 2) {
            val baris = koordinat[0].toIntOrNull()
            val kolom = koordinat[1].toIntOrNull()
            if (baris != null && kolom != null) return Pair(baris, kolom)
        }
    }
    return null
}
