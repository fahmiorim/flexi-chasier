package id.cassy.kasir.data.lokal.basisdata

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Kumpulan migrasi database lokal CassyKasir.
 *
 * Scope ini memperbaiki perbedaan schema saat index baru ditambahkan
 * ke tabel transaksi lokal dan item transaksi lokal.
 */
object MigrasiBasisDataKasir {

    /**
     * Migrasi dari versi 1 ke versi 2.
     *
     * Perubahan:
     * - menambahkan index untuk urutan waktu transaksi
     * - menambahkan index untuk produkId pada item transaksi
     */
    val DARI_1_KE_2: Migration = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE INDEX IF NOT EXISTS index_transaksi_lokal_waktuTransaksiEpochMili
                ON transaksi_lokal(waktuTransaksiEpochMili)
                """.trimIndent(),
            )

            db.execSQL(
                """
                CREATE INDEX IF NOT EXISTS index_item_transaksi_lokal_produkId
                ON item_transaksi_lokal(produkId)
                """.trimIndent(),
            )
        }
    }

    /**
     * Migrasi dari versi 2 ke versi 3.
     *
     * Perubahan:
     * - menambahkan tabel produk untuk mendukung local-first katalog.
     */
    val DARI_2_KE_3: Migration = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `produk` (
                    `id` TEXT NOT NULL,
                    `nama` TEXT NOT NULL,
                    `harga` INTEGER NOT NULL,
                    `stokTersedia` INTEGER NOT NULL,
                    `kodePindai` TEXT,
                    `deskripsi` TEXT NOT NULL,
                    `apakahAktif` INTEGER NOT NULL,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent(),
            )
        }
    }

    /**
     * Migrasi dari versi 3 ke versi 4.
     *
     * Perubahan:
     * - menambahkan kolom `status` pada tabel transaksi_lokal
     *   untuk mendukung status pembayaran (Pending/Lunas).
     */
    val DARI_3_KE_4: Migration = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                ALTER TABLE transaksi_lokal
                ADD COLUMN status TEXT NOT NULL DEFAULT 'Lunas'
                """.trimIndent(),
            )
        }
    }

    /**
     * Migrasi dari versi 4 ke versi 5.
     *
     * Perubahan:
     * - menambahkan kolom `kategori` pada tabel produk
     *   untuk mengelompokkan produk (Minuman, Makanan, Sembako, dll).
     */
    val DARI_4_KE_5: Migration = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            try {
                db.execSQL(
                    """
                    ALTER TABLE produk
                    ADD COLUMN kategori TEXT NOT NULL DEFAULT ''
                    """.trimIndent(),
                )
            } catch (_: Exception) {
                // Kolom sudah ada — tidak perlu migrasi ulang.
            }
        }
    }

    /**
     * Migrasi dari versi 5 ke versi 6.
     *
     * Perubahan:
     * - menambahkan kolom `metodeBayar` pada tabel transaksi_lokal
     *   untuk menyimpan metode pembayaran (Tunai/QRIS).
     */
    val DARI_5_KE_6: Migration = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            try {
                db.execSQL(
                    """
                    ALTER TABLE transaksi_lokal
                    ADD COLUMN metodeBayar TEXT NOT NULL DEFAULT 'Tunai'
                    """.trimIndent(),
                )
            } catch (_: Exception) {
                // Kolom sudah ada — tidak perlu migrasi ulang.
            }
        }
    }

    /**
     * Migrasi dari versi 6 ke versi 7.
     *
     * Perubahan:
     * - menambahkan tabel meja_lokal untuk menyimpan daftar nomor meja.
     */
    val DARI_6_KE_7: Migration = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `meja_lokal` (
                    `id` TEXT NOT NULL,
                    `nomor` TEXT NOT NULL,
                    `aktif` INTEGER NOT NULL DEFAULT 1,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent(),
            )
        }
    }

    val DARI_7_KE_8: Migration = object : Migration(7, 8) {
        override fun migrate(db: SupportSQLiteDatabase) {
            try {
                db.execSQL(
                    """
                    ALTER TABLE transaksi_lokal
                    ADD COLUMN tipeOrder TEXT NOT NULL DEFAULT 'DineIn'
                    """.trimIndent(),
                )
            } catch (_: Exception) {
                // Kolom sudah ada — tidak perlu migrasi ulang.
            }
        }
    }
}
