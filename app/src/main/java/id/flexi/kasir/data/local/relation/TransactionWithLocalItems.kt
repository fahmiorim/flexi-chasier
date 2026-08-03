package id.flexi.kasir.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import id.flexi.kasir.data.local.entity.LocalTransactionItemEntity
import id.flexi.kasir.data.local.entity.LocalTransactionEntity

/**
 * Model relasi data (POJO) untuk menggabungkan Transaction dengan seluruh itemnya.
 * Digunakan untuk mengambil data Transaction lengkap dalam satu query Room.
 *
 * @property Transaction Data utama Transaction.
 * @property daftarItem Daftar item yang termasuk dalam Transaction ini.
 */
data class TransactionWithLocalItems(
    @Embedded
    val Transaction: LocalTransactionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "TransactionId",
    )
    val daftarItem: List<LocalTransactionItemEntity>,
)
