package id.flexi.kasir.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Kumpulan migrasi database lokal FlexiKasir.
 *
 * Scope ini memperbaiki perbedaan schema saat index baru ditambahkan
 * ke tabel Transaction lokal dan item Transaction lokal.
 */
object CashierDatabaseMigration {

    /**
     * Migrasi dari versi 1 ke versi 2.
     *
     * Perubahan:
     * - menambahkan index untuk urutan waktu Transaction
     * - menambahkan index untuk produkId pada item Transaction
     */
    val DARI_1_KE_2: Migration = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE INDEX IF NOT EXISTS index_Transaction_lokal_waktuTransactionEpochMili
                ON Transaction_lokal(waktuTransactionEpochMili)
                """.trimIndent(),
            )

            db.execSQL(
                """
                CREATE INDEX IF NOT EXISTS index_item_Transaction_lokal_produkId
                ON item_Transaction_lokal(produkId)
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
     * - menambahkan kolom `status` pada tabel Transaction_lokal
     *   untuk mendukung status Payment (Pending/Lunas).
     */
    val DARI_3_KE_4: Migration = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                ALTER TABLE Transaction_lokal
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
     * - menambahkan kolom `PaymentMethod` pada tabel Transaction_lokal
     *   untuk menyimpan metode Payment (Tunai/QRIS).
     */
    val DARI_5_KE_6: Migration = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            try {
                db.execSQL(
                    """
                    ALTER TABLE Transaction_lokal
                    ADD COLUMN PaymentMethod TEXT NOT NULL DEFAULT 'Tunai'
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
                    ALTER TABLE Transaction_lokal
                    ADD COLUMN OrderType TEXT NOT NULL DEFAULT 'DineIn'
                    """.trimIndent(),
                )
            } catch (_: Exception) {
                // Kolom sudah ada — tidak perlu migrasi ulang.
            }
        }
    }

    val DARI_8_KE_9: Migration = object : Migration(8, 9) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Tidak ada perubahan skema — hanya bump versi database
        }
    }

    val DARI_9_KE_10: Migration = object : Migration(9, 10) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Tidak ada perubahan skema — hanya bump versi database
        }
    }

    /**
     * Migrasi dari versi 10 ke versi 11.
     *
     * Perubahan:
     * - menambahkan kolom `fotoUri` pada tabel produk
     *   untuk menyimpan URI gambar produk.
     */
    val DARI_10_KE_11: Migration = object : Migration(10, 11) {
        override fun migrate(db: SupportSQLiteDatabase) {
            try {
                db.execSQL(
                    """
                    ALTER TABLE produk
                    ADD COLUMN fotoUri TEXT
                    """.trimIndent(),
                )
            } catch (_: Exception) {
                // Kolom sudah ada — tidak perlu migrasi ulang.
            }
        }
    }

    /**
     * Migrasi dari versi 11 ke versi 12.
     *
     * Perubahan:
     * - menambahkan kolom `favorit` pada tabel produk
     * - menambahkan kolom `hargaModal` pada tabel produk
     */
    val DARI_11_KE_12: Migration = object : Migration(11, 12) {
        override fun migrate(db: SupportSQLiteDatabase) {
            try {
                db.execSQL("ALTER TABLE produk ADD COLUMN favorit INTEGER NOT NULL DEFAULT 0")
            } catch (_: Exception) { }
            try {
                db.execSQL("ALTER TABLE produk ADD COLUMN hargaModal INTEGER")
            } catch (_: Exception) { }
        }
    }

    /**
     * Migrasi dari versi 12 ke versi 13.
     *
     * Perubahan:
     * - menambahkan kolom `varianJson` pada tabel produk
     *   untuk menyimpan daftar varian (nama + harga) dalam format JSON.
     */
    val DARI_12_KE_13: Migration = object : Migration(12, 13) {
        override fun migrate(db: SupportSQLiteDatabase) {
            try {
                db.execSQL("ALTER TABLE produk ADD COLUMN varianJson TEXT")
            } catch (_: Exception) { }
        }
    }

    /**
     * Migrasi dari versi 13 ke versi 14.
     *
     * Perubahan:
     * - menambahkan kolom `apakahStokDiaktifkan` pada tabel produk
     *   untuk menandai apakah stok dikelola (toggle ON) atau tidak.
     */
    val DARI_13_KE_14: Migration = object : Migration(13, 14) {
        override fun migrate(db: SupportSQLiteDatabase) {
            try {
                db.execSQL("ALTER TABLE produk ADD COLUMN apakahStokDiaktifkan INTEGER NOT NULL DEFAULT 0")
            } catch (_: Exception) { }
        }
    }

    /**
     * Migrasi dari versi 14 ke versi 15.
     *
     * Perubahan:
     * - menambahkan kolom waktuDiprosesEpochMili, waktuSelesaiEpochMili, waktuDibayarEpochMili
     *   pada tabel Transaction_lokal untuk mencatat waktu di setiap tahapan.
     * - menambahkan kolom varianNama, apakahSelesai pada tabel item_Transaction_lokal
     *   untuk mendukung tracking per-item dan penyimpanan varian.
     */
    val DARI_14_KE_15: Migration = object : Migration(14, 15) {
        override fun migrate(db: SupportSQLiteDatabase) {
            try {
                db.execSQL("ALTER TABLE Transaction_lokal ADD COLUMN waktuDiprosesEpochMili INTEGER")
            } catch (_: Exception) { }
            try {
                db.execSQL("ALTER TABLE Transaction_lokal ADD COLUMN waktuSelesaiEpochMili INTEGER")
            } catch (_: Exception) { }
            try {
                db.execSQL("ALTER TABLE Transaction_lokal ADD COLUMN waktuDibayarEpochMili INTEGER")
            } catch (_: Exception) { }
            try {
                db.execSQL("ALTER TABLE item_Transaction_lokal ADD COLUMN varianNama TEXT")
            } catch (_: Exception) { }
            try {
                db.execSQL("ALTER TABLE item_Transaction_lokal ADD COLUMN apakahSelesai INTEGER NOT NULL DEFAULT 0")
            } catch (_: Exception) { }
        }
    }

    /**
     * Migrasi dari versi 15 ke versi 16.
     *
     * Perubahan:
     * - menambahkan tabel shift_kas untuk menyimpan data buka/tutup kas
     * - menambahkan tabel mutasi_kas untuk menyimpan pengeluaran dan setoran
     */
    val DARI_15_KE_16: Migration = object : Migration(15, 16) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `shift_kas` (
                    `id` TEXT NOT NULL,
                    `saldoAwal` INTEGER NOT NULL,
                    `saldoAkhir` INTEGER,
                    `waktuBuka` INTEGER NOT NULL,
                    `waktuTutup` INTEGER,
                    `status` TEXT NOT NULL DEFAULT 'Buka',
                    `catatanBuka` TEXT,
                    `catatanTutup` TEXT,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent(),
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `mutasi_kas` (
                    `id` TEXT NOT NULL,
                    `shiftId` TEXT NOT NULL,
                    `tipe` TEXT NOT NULL,
                    `kategori` TEXT NOT NULL DEFAULT 'Lainnya',
                    `nominal` INTEGER NOT NULL,
                    `catatan` TEXT NOT NULL DEFAULT '',
                    `waktu` INTEGER NOT NULL,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`shiftId`) REFERENCES `shift_kas`(`id`)
                        ON DELETE CASCADE
                )
                """.trimIndent(),
            )

            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_mutasi_kas_shiftId ON mutasi_kas(shiftId)"
            )
        }
    }

    val DARI_16_KE_17: Migration = object : Migration(16, 17) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `setoran_kas` (
                    `id` TEXT NOT NULL,
                    `nominal` INTEGER NOT NULL,
                    `catatan` TEXT NOT NULL DEFAULT '',
                    `waktu` INTEGER NOT NULL,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent(),
            )
        }
    }

    val DARI_17_KE_18: Migration = object : Migration(17, 18) {
        override fun migrate(db: SupportSQLiteDatabase) {
            try {
                db.execSQL("ALTER TABLE setoran_kas ADD COLUMN dihapus INTEGER NOT NULL DEFAULT 0")
            } catch (_: Exception) { }
        }
    }

    val DARI_18_KE_19: Migration = object : Migration(18, 19) {
        override fun migrate(db: SupportSQLiteDatabase) {
            try {
                db.execSQL("ALTER TABLE Transaction_lokal ADD COLUMN dibatalkan INTEGER NOT NULL DEFAULT 0")
            } catch (_: Exception) { }
        }
    }

    val DARI_19_KE_20: Migration = object : Migration(19, 20) {
        override fun migrate(db: SupportSQLiteDatabase) {
            try {
                db.execSQL("ALTER TABLE Transaction_lokal ADD COLUMN alasanPembatalan TEXT DEFAULT NULL")
            } catch (_: Exception) { }
        }
    }

    /**
     * Migrasi dari versi 20 ke versi 21.
     *
     * Perubahan:
     * - menambahkan tabel `bahan` untuk menyimpan data bahan baku
     * - menambahkan tabel `pembelian_bahan` untuk mencatat pembelian bahan
     * - menambahkan tabel `resep` untuk menghubungkan produk dengan bahan
     * - menambahkan tabel `bahan_resep` untuk komposisi bahan dalam resep
     */
    /**
     * Migrasi dari versi 21 ke versi 22.
     *
     * Perubahan:
     * - menambahkan index pada kolom status, PaymentMethod, dibatalkan, waktuDibayarEpochMili
     *   di tabel Transaction_lokal untuk optimalisasi query aggregate
     * - menambahkan index pada kolom tipe di tabel mutasi_kas
     * - menambahkan index pada kolom dihapus di tabel setoran_kas
     */
    val DARI_21_KE_22: Migration = object : Migration(21, 22) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Transaction_lokal indices
            db.execSQL("CREATE INDEX IF NOT EXISTS index_transaksi_status ON Transaction_lokal(status)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_transaksi_payment_method ON Transaction_lokal(PaymentMethod)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_transaksi_dibatalkan ON Transaction_lokal(dibatalkan)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_transaksi_waktu_dibayar ON Transaction_lokal(waktuDibayarEpochMili)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_transaksi_status_payment_batal ON Transaction_lokal(status, PaymentMethod, dibatalkan)")

            // mutasi_kas indices
            db.execSQL("CREATE INDEX IF NOT EXISTS index_mutasi_tipe ON mutasi_kas(tipe)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_mutasi_shift_tipe ON mutasi_kas(shiftId, tipe)")

            // setoran_kas indices
            db.execSQL("CREATE INDEX IF NOT EXISTS index_setoran_dihapus ON setoran_kas(dihapus)")
        }
    }

    val DARI_20_KE_21: Migration = object : Migration(20, 21) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `bahan` (
                    `id` TEXT NOT NULL,
                    `nama` TEXT NOT NULL,
                    `satuan` TEXT NOT NULL DEFAULT 'pcs',
                    `stokTersedia` REAL NOT NULL DEFAULT 0.0,
                    `hargaPerSatuan` INTEGER NOT NULL DEFAULT 0,
                    `createdAt` INTEGER NOT NULL,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent(),
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `pembelian_bahan` (
                    `id` TEXT NOT NULL,
                    `bahanId` TEXT NOT NULL,
                    `jumlah` REAL NOT NULL,
                    `satuanBeli` TEXT NOT NULL DEFAULT 'pcs',
                    `totalHarga` INTEGER NOT NULL,
                    `tanggalBeli` INTEGER NOT NULL,
                    `catatan` TEXT,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`bahanId`) REFERENCES `bahan`(`id`)
                        ON DELETE CASCADE
                )
                """.trimIndent(),
            )

            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_pembelian_bahan_bahanId ON pembelian_bahan(bahanId)"
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `resep` (
                    `id` TEXT NOT NULL,
                    `produkId` TEXT NOT NULL,
                    `varianNama` TEXT,
                    `createdAt` INTEGER NOT NULL,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`produkId`) REFERENCES `produk`(`id`)
                        ON DELETE CASCADE
                )
                """.trimIndent(),
            )

            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_resep_produkId ON resep(produkId)"
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `bahan_resep` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `resepId` TEXT NOT NULL,
                    `bahanId` TEXT NOT NULL,
                    `jumlah` REAL NOT NULL,
                    `satuan` TEXT NOT NULL DEFAULT 'gram',
                    FOREIGN KEY(`resepId`) REFERENCES `resep`(`id`)
                        ON DELETE CASCADE,
                    FOREIGN KEY(`bahanId`) REFERENCES `bahan`(`id`)
                        ON DELETE CASCADE
                )
                """.trimIndent(),
            )

            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_bahan_resep_resepId ON bahan_resep(resepId)"
            )

            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_bahan_resep_bahanId ON bahan_resep(bahanId)"
            )
        }
    }

    /**
     * Migrasi dari versi 22 ke versi 23.
     *
     * Perubahan:
     * - menambahkan kolom `geraiId` pada tabel produk agar katalog tersimpan
     *   per-gerai (mendukung sinkronisasi katalog multi-gerai).
     */
    val DARI_22_KE_23: Migration = object : Migration(22, 23) {
        override fun migrate(db: SupportSQLiteDatabase) {
            try {
                db.execSQL(
                    "ALTER TABLE produk ADD COLUMN geraiId TEXT NOT NULL DEFAULT ''",
                )
            } catch (_: Exception) {
                // Kolom sudah ada — tidak perlu migrasi ulang.
            }
        }
    }

    /**
     * Migrasi dari versi 23 ke versi 24.
     *
     * Perubahan:
     * - menambahkan tabel `outbox_sinkron` untuk antrian perubahan lokal yang
     *   menunggu dikirim ke server (push sinkronisasi).
     * - menambahkan tabel `meta_sinkron` untuk metadata sinkronisasi
     *   (kursor pull terakhir per gerai).
     */
    val DARI_23_KE_24: Migration = object : Migration(23, 24) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `outbox_sinkron` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `entitas` TEXT NOT NULL,
                    `itemId` TEXT NOT NULL,
                    `geraiId` TEXT NOT NULL,
                    `versi` INTEGER NOT NULL,
                    `payload` TEXT NOT NULL,
                    `status` TEXT NOT NULL DEFAULT 'Antri',
                    `jumlahPercobaan` INTEGER NOT NULL DEFAULT 0,
                    `waktuDibuat` INTEGER NOT NULL,
                    `pesanError` TEXT
                )
                """.trimIndent(),
            )

            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS index_outbox_sinkron_entitas_itemId ON outbox_sinkron(entitas, itemId)",
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_outbox_sinkron_status ON outbox_sinkron(status)",
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `meta_sinkron` (
                    `kunci` TEXT NOT NULL,
                    `nilai` TEXT NOT NULL,
                    PRIMARY KEY(`kunci`)
                )
                """.trimIndent(),
            )
        }
    }

    /**
     * Migrasi dari versi 24 ke versi 25.
     *
     * Perubahan:
     * - menambahkan tabel `penyesuaian_stok` untuk riwayat reset/penyesuaian stok
     *   bahan & produk lintas perangkat.
     * - menambahkan tabel `mutasi_rekening` untuk saldo rekening (saldo awal,
     *   pemasukan, penarikan) lintas perangkat.
     */
    val DARI_24_KE_25: Migration = object : Migration(24, 25) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `penyesuaian_stok` (
                    `id` TEXT NOT NULL,
                    `jenis` TEXT NOT NULL,
                    `entitasId` TEXT NOT NULL,
                    `namaEntitas` TEXT NOT NULL DEFAULT '',
                    `stokSebelum` INTEGER NOT NULL,
                    `stokSesudah` INTEGER NOT NULL,
                    `selisih` INTEGER NOT NULL,
                    `alasan` TEXT NOT NULL DEFAULT '',
                    `waktu` INTEGER NOT NULL,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent(),
            )

            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_penyesuaian_stok_entitas ON penyesuaian_stok(jenis, entitasId)",
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_penyesuaian_stok_waktu ON penyesuaian_stok(waktu)",
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `mutasi_rekening` (
                    `id` TEXT NOT NULL,
                    `tipe` TEXT NOT NULL,
                    `nominal` INTEGER NOT NULL,
                    `catatan` TEXT NOT NULL DEFAULT '',
                    `waktu` INTEGER NOT NULL,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent(),
            )

            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_mutasi_rekening_tipe ON mutasi_rekening(tipe)",
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_mutasi_rekening_waktu ON mutasi_rekening(waktu)",
            )
        }
    }

    /**
     * Migrasi dari versi 25 ke versi 26.
     *
     * Perubahan:
     * - menambahkan kolom `stokMinimum` & `aktif` pada tabel `bahan` agar
     *   sinkronisasi LWW tidak menimpa nilai yang diatur lewat web/server.
     * - menambahkan kolom `shiftId` pada tabel `setoran_kas` agar setoran
     *   dikaitkan ke shift kas (selaras dengan server & web).
     */
    val DARI_25_KE_26: Migration = object : Migration(25, 26) {
        override fun migrate(db: SupportSQLiteDatabase) {
            try {
                db.execSQL("ALTER TABLE bahan ADD COLUMN stokMinimum INTEGER NOT NULL DEFAULT 0")
            } catch (_: Exception) { }
            try {
                db.execSQL("ALTER TABLE bahan ADD COLUMN aktif INTEGER NOT NULL DEFAULT 1")
            } catch (_: Exception) { }
            try {
                db.execSQL("ALTER TABLE setoran_kas ADD COLUMN shiftId TEXT NOT NULL DEFAULT ''")
            } catch (_: Exception) { }
            db.execSQL("CREATE INDEX IF NOT EXISTS index_setoran_shiftId ON setoran_kas(shiftId)")
        }
    }

    /**
     * Migrasi dari versi 26 ke versi 27.
     *
     * Perubahan:
     * - menambahkan kolom `mutasiKasId` pada tabel `pembelian_bahan` agar
     *   mutasi kas BelanjaBahan bisa dibatalkan saat pembelian dihapus
     *   (dihapus dari daftar pembelian tidak lagi meninggalkan pengeluaran kas).
     */
    val DARI_26_KE_27: Migration = object : Migration(26, 27) {
        override fun migrate(db: SupportSQLiteDatabase) {
            try {
                db.execSQL("ALTER TABLE pembelian_bahan ADD COLUMN mutasiKasId TEXT")
            } catch (_: Exception) { }
        }
    }

    /**
     * Migrasi dari versi 27 ke versi 28.
     *
     * Perubahan:
     * - menambahkan kolom `versi` pada tabel Transaction_lokal agar rincian
     *   transaksi (potongan, dibatalkan, catatan, status, dll.) bisa
     *   disinkronkan dengan aturan last-write-wins berbasis versi: server
     *   menang hanya jika versinya lebih baru, edit lokal yang belum ter-push
     *   tidak lagi tertimpa.
     */
    val DARI_27_KE_28: Migration = object : Migration(27, 28) {
        override fun migrate(db: SupportSQLiteDatabase) {
            try {
                db.execSQL("ALTER TABLE Transaction_lokal ADD COLUMN versi INTEGER NOT NULL DEFAULT 0")
            } catch (_: Exception) { }
        }
    }

    /**
     * Migrasi dari versi 28 ke versi 29.
     *
     * Perubahan: no-op — menambahkan @ColumnInfo(defaultValue) pada entity
     * LocalPenyesuaianStokEntity agar cocok dengan schema aktual yang sudah
     * memiliki DEFAULT '' sejak migration 24→25.
     */
    val DARI_28_KE_29: Migration = object : Migration(28, 29) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Tidak ada perubahan DDL — schema sudah benar.
        }
    }

    /**
     * Migrasi dari versi 29 ke versi 30.
     *
     * Perubahan: recreation tabel `penyesuaian_stok` — tabel lama dibuat oleh
     * versi awal dengan DEFAULT 'undefined' di semua kolom. Room tidak bisa
     * memvalidasi schema karena `CREATE TABLE IF NOT EXISTS` melewati tabel
     * yang sudah ada. Solusi: drop + recreate dengan schema yang benar
     * (DEFAULT '' pada namaEntitas & alasan).
     */
    val DARI_29_KE_30: Migration = object : Migration(29, 30) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("DROP TABLE IF EXISTS `penyesuaian_stok`")

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `penyesuaian_stok` (
                    `id` TEXT NOT NULL,
                    `jenis` TEXT NOT NULL,
                    `entitasId` TEXT NOT NULL,
                    `namaEntitas` TEXT NOT NULL DEFAULT '',
                    `stokSebelum` INTEGER NOT NULL,
                    `stokSesudah` INTEGER NOT NULL,
                    `selisih` INTEGER NOT NULL,
                    `alasan` TEXT NOT NULL DEFAULT '',
                    `waktu` INTEGER NOT NULL,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent(),
            )

            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_penyesuaian_stok_entitas ON penyesuaian_stok(jenis, entitasId)",
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_penyesuaian_stok_waktu ON penyesuaian_stok(waktu)",
            )
        }
    }

    /**
     * Ubah FK mutasi_kas dari CASCADE ke NO_ACTION supaya mutasi bisa disimpan
     * sementara sebagai orphan saat shift belum ditarik dari server.
     */
    val DARI_30_KE_31: Migration = object : Migration(30, 31) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // FK constraints tidak bisa diubah langsung di SQLite → recreate table
            db.execSQL("CREATE TABLE IF NOT EXISTS `mutasi_kas_new` (" +
                "`id` TEXT NOT NULL, " +
                "`shiftId` TEXT NOT NULL, " +
                "`tipe` TEXT NOT NULL, " +
                "`kategori` TEXT NOT NULL DEFAULT 'Lainnya', " +
                "`nominal` INTEGER NOT NULL, " +
                "`catatan` TEXT NOT NULL DEFAULT '', " +
                "`waktu` INTEGER NOT NULL, " +
                "PRIMARY KEY(`id`), " +
                "FOREIGN KEY(`shiftId`) REFERENCES `shift_kas`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION" +
                ")")
            db.execSQL("INSERT INTO `mutasi_kas_new` SELECT * FROM `mutasi_kas`")
            db.execSQL("DROP TABLE `mutasi_kas`")
            db.execSQL("ALTER TABLE `mutasi_kas_new` RENAME TO `mutasi_kas`")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_mutasi_kas_shiftId` ON `mutasi_kas` (`shiftId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_mutasi_kas_tipe` ON `mutasi_kas` (`tipe`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_mutasi_kas_shiftId_tipe` ON `mutasi_kas` (`shiftId`, `tipe`)")

            // Reset cursor pull mutasiKas supaya sync berikutnya pull ULANG semua
            // mutasi dari server — memulihkan data yang hilang karena orpin drop.
            db.execSQL("DELETE FROM meta_sinkron WHERE kunci LIKE 'pull_terakhir:%:mutasiKas'")
        }
    }
}
