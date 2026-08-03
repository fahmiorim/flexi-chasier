package id.flexi.kasir.ui.bahan

sealed interface BahanAction {
    data class MintaHapus(val idBahan: String, val namaBahan: String) : BahanAction
    data object KonfirmasiHapus : BahanAction
    data object BatalkanHapus : BahanAction
    data class PerbaruiKataKunciPencarian(val kataKunci: String) : BahanAction
    data object ResetPencarian : BahanAction
}
