package id.cassy.kasir.data.lokal.relasi

import androidx.room.Embedded
import androidx.room.Relation
import id.cassy.kasir.data.lokal.entitas.EntitasItemTransaksiLokal
import id.cassy.kasir.data.lokal.entitas.EntitasTransaksiLokal

/**
 * Model relasi data (POJO) untuk menggabungkan transaksi dengan seluruh itemnya.
 * Digunakan untuk mengambil data transaksi lengkap dalam satu query Room.
 *
 * @property transaksi Data utama transaksi.
 * @property daftarItem Daftar item yang termasuk dalam transaksi ini.
 */
data class TransaksiDenganItemLokal(
    @Embedded
    val transaksi: EntitasTransaksiLokal,
    @Relation(
        parentColumn = "id",
        entityColumn = "transaksiId",
    )
    val daftarItem: List<EntitasItemTransaksiLokal>,
)
