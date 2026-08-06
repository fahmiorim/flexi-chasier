package id.flexi.kasir.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import id.flexi.kasir.data.local.dao.BahanDao
import id.flexi.kasir.data.local.dao.LocalCashDao
import id.flexi.kasir.data.local.dao.LocalTableDao
import id.flexi.kasir.data.local.dao.LocalProductDao
import id.flexi.kasir.data.local.dao.LocalTransactionDao
import id.flexi.kasir.data.local.dao.MutasiRekeningDao
import id.flexi.kasir.data.local.dao.OutboxDao
import id.flexi.kasir.data.local.dao.PenyesuaianStokDao
import id.flexi.kasir.data.local.dao.SinkronMetaDao
import id.flexi.kasir.data.local.entity.LocalBahanEntity
import id.flexi.kasir.data.local.entity.LocalBahanResepEntity
import id.flexi.kasir.data.local.entity.LocalCashMutationEntity
import id.flexi.kasir.data.local.entity.LocalCashKasEntity
import id.flexi.kasir.data.local.entity.LocalMutasiRekeningEntity
import id.flexi.kasir.data.local.entity.LocalPembelianBahanEntity
import id.flexi.kasir.data.local.entity.LocalPenyesuaianStokEntity
import id.flexi.kasir.data.local.entity.LocalResepEntity
import id.flexi.kasir.data.local.entity.LocalSetoranEntity
import id.flexi.kasir.data.local.entity.LocalTransactionItemEntity
import id.flexi.kasir.data.local.entity.LocalTableEntity
import id.flexi.kasir.data.local.entity.LocalProductEntity
import id.flexi.kasir.data.local.entity.LocalTransactionEntity
import id.flexi.kasir.data.local.entity.OutboxSinkronEntity
import id.flexi.kasir.data.local.entity.SinkronMetaEntity

/**
 * Titik masuk utama database Room aplikasi Flexi Cashier.
 */
@Database(
    entities = [
        LocalTransactionEntity::class,
        LocalTransactionItemEntity::class,
        LocalProductEntity::class,
        LocalTableEntity::class,
        LocalCashKasEntity::class,
        LocalCashMutationEntity::class,
        LocalSetoranEntity::class,
        LocalBahanEntity::class,
        LocalPembelianBahanEntity::class,
        LocalResepEntity::class,
        LocalBahanResepEntity::class,
        LocalPenyesuaianStokEntity::class,
        LocalMutasiRekeningEntity::class,
        OutboxSinkronEntity::class,
        SinkronMetaEntity::class,
    ],
    version = 27,
    exportSchema = true,
)
abstract class FlexiCashierDatabase : RoomDatabase() {
    /**
     * Menyediakan akses ke operasi data Transaction.
     */
    abstract fun LocalTransactionDao(): LocalTransactionDao

    /**
     * Menyediakan akses ke operasi data produk.
     */
    abstract fun LocalProductDao(): LocalProductDao

    /**
     * Menyediakan akses ke operasi data meja.
     */
    abstract fun LocalTableDao(): LocalTableDao

    /**
     * Menyediakan akses ke operasi data kas.
     */
    abstract fun LocalCashDao(): LocalCashDao

    /**
     * Menyediakan akses ke operasi data bahan baku.
     */
    abstract fun BahanDao(): BahanDao

    /**
     * Menyediakan akses ke antrian outbox sinkronisasi.
     */
    abstract fun OutboxDao(): OutboxDao

    /**
     * Menyediakan akses ke metadata sinkronisasi (kursor pull, dsb.).
     */
    abstract fun SinkronMetaDao(): SinkronMetaDao

    /**
     * Menyediakan akses ke riwayat penyesuaian/reset stok.
     */
    abstract fun PenyesuaianStokDao(): PenyesuaianStokDao

    /**
     * Menyediakan akses ke mutasi rekening (saldo awal, pemasukan, penarikan).
     */
    abstract fun MutasiRekeningDao(): MutasiRekeningDao
}
