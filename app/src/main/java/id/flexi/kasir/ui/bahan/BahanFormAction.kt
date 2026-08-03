package id.flexi.kasir.ui.bahan

sealed interface BahanFormAction {
    data class UbahNama(val nama: String) : BahanFormAction
    data class UbahSatuan(val satuan: String) : BahanFormAction
    data class UbahStokTersedia(val stok: String) : BahanFormAction
    data class UbahHargaPerSatuan(val harga: String) : BahanFormAction
    data object Simpan : BahanFormAction
    data object BersihkanPesan : BahanFormAction
}
