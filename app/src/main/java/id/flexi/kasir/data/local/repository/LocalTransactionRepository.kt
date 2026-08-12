package id.flexi.kasir.data.local.repository

import androidx.room.withTransaction
import id.flexi.kasir.data.local.database.FlexiKasirDatabase
import id.flexi.kasir.data.local.entity.LocalTransactionItemEntity
import id.flexi.kasir.data.local.mapping.keDomain
import id.flexi.kasir.data.local.mapping.keLokal
import id.flexi.kasir.data.local.relation.TransactionWithLocalItems
import id.flexi.kasir.domain.model.PaymentMethod
import id.flexi.kasir.domain.model.TransactionStatus
import id.flexi.kasir.domain.model.Transaction
import id.flexi.kasir.domain.model.Uang
import id.flexi.kasir.domain.model.OrderType
import id.flexi.kasir.data.sync.OutboxPencatat
import id.flexi.kasir.data.sync.PayloadSinkron
import id.flexi.kasir.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map

/**
 * Implementasi repository Transaction berbasis Room.
 *
 * Class ini berada di layer data karena mengetahui detail database lokal.
 * Layer ranah hanya mengenal kontrak [TransactionRepository].
 *
 * @property basisData Instance database lokal Flexi Kasir.
 */
class TransactionRepositoryLokal(
    private val basisData: FlexiKasirDatabase,
    private val pencatatOutbox: OutboxPencatat? = null,
) : TransactionRepository {

    private val aksesDataTransaction = basisData.LocalTransactionDao()
    private val aksesDataProduk = basisData.LocalProductDao()

    /**
     * Menyimpan Transaction baru beserta seluruh item terkait ke database secara atomik.
     *
     * @param Transaction Objek domain Transaction yang akan disimpan.
     */
    override suspend fun simpanTransaction(
        Transaction: Transaction,
    ) {
        simpanTransactionTanpaMengubahStok(
            Transaction = Transaction,
        )
    }

    /**
     * Menyimpan Transaction dengan penyesuaian stok berdasarkan delta.
     *
     * Membandingkan item lama dengan item baru:
     * - Item baru/ditambah jumlahnya → kurangi stok
     * - Item dihapus/dikurangi jumlahnya → tambah (restore) stok
     * - Item tidak berubah → tidak ada perubahan stok
     *
     * @param Transaction Transaction baru yang akan disimpan.
     * @param oldTransaction Transaction lama (untuk perbandingan), null jika baru.
     */
    override suspend fun simpanTransactionDenganDeltaStok(
        Transaction: Transaction,
        oldTransaction: Transaction?,
    ) {
        if (oldTransaction == null) {
            // Tidak ada data lama → kurangi stok seperti biasa
            simpanTransactionDanKurangiStok(Transaction)
            return
        }

        val oldMap = oldTransaction.daftarCartItem
            .groupBy { it.produk.id }
            .mapValues { (_, items) -> items.sumOf { it.jumlah } }
        val newMap = Transaction.daftarCartItem
            .groupBy { it.produk.id }
            .mapValues { (_, items) -> items.sumOf { it.jumlah } }

        basisData.withTransaction {
            // Identifikasi produk yang perlu validasi (bertambah jumlahnya).
            // Item manual (id "manual_*") tanpa baris produk lokal dilewati.
            val produkPerluValidasi = newMap.filter { (produkId, newQty) ->
                !apakahItemManual(produkId) && newQty > (oldMap[produkId] ?: 0)
            }.keys.toList()

            // Muat data produk untuk SEMUA item terdampak (bertambah ATAU
            // berkurang). Produk yang berkurang juga butuh datanya agar restore
            // stok bisa memeriksa apakah stok produk diaktifkan — tanpa ini,
            // produk dengan stok non-aktif (tak pernah dikurangi saat jual)
            // akan "menggembung" saat jumlahnya dikurangi/dibatalkan.
            val allIds = (oldMap.keys + newMap.keys).filterNot { apakahItemManual(it) }.toSet()
            val daftarProdukLokal = if (allIds.isNotEmpty()) {
                aksesDataProduk.ambilProdukBerdasarkanDaftarIdentitas(allIds.toList())
                    .associateBy { it.id }
            } else {
                emptyMap()
            }

            // Validasi produk untuk item baru/bertambah
            produkPerluValidasi.forEach { produkId ->
                val newQty = newMap[produkId] ?: 0
                val oldQty = oldMap[produkId] ?: 0
                val delta = newQty - oldQty
                val produkLokal = daftarProdukLokal[produkId]
                    ?: throw IllegalArgumentException("Produk tidak ditemukan.")

                if (!produkLokal.apakahAktif) {
                    throw IllegalArgumentException("Produk ${produkLokal.nama} sedang tidak aktif.")
                }
                if (produkLokal.apakahStokDiaktifkan && produkLokal.stokTersedia < delta) {
                    throw IllegalArgumentException("Stok ${produkLokal.nama} tidak cukup.")
                }
            }

            // Terapkan delta stok
            allIds.forEach { produkId ->
                val oldQty = oldMap[produkId] ?: 0
                val newQty = newMap[produkId] ?: 0
                val delta = newQty - oldQty

                when {
                    delta < 0 -> {
                        // Item dihapus/dikurangi → restore stok (hanya jika stok
                        // diaktifkan — simetris dengan pengurangan saat jual).
                        val produkLokal = daftarProdukLokal[produkId]
                        if (produkLokal?.apakahStokDiaktifkan == true) {
                            aksesDataProduk.tambahStok(produkId, -delta)
                        }
                    }
                    delta > 0 -> {
                        // Item baru/ditambah → kurangi stok (hanya jika stok diaktifkan)
                        val produkLokal = daftarProdukLokal[produkId]
                        if (produkLokal?.apakahStokDiaktifkan == true) {
                            val jumlahBarisBerubah = aksesDataProduk.kurangiStokJikaCukup(produkId, delta)
                            if (jumlahBarisBerubah != 1) {
                                throw IllegalArgumentException("Stok ${produkLokal.nama} tidak cukup.")
                            }
                        }
                    }
                    // delta == 0 → tidak ada perubahan stok
                }
            }

            // Simpan transaksi tanpa mengubah stok (stock sudah dihandle oleh delta)
            simpanTransactionTanpaMengubahStok(Transaction)

            // Catat stok terbaru produk terdampak ke outbox (best-effort).
            catatPerubahanStokKeOutbox(allIds)
        }
    }

    /**
     * Menyimpan Transaction dan mengurangi stok produk secara atomik.
     *
     * @param Transaction Transaction yang akan dicatat.
     */
    override suspend fun simpanTransactionDanKurangiStok(
        Transaction: Transaction,
    ) {
        val daftarJumlahProduk = Transaction.daftarCartItem
            .groupBy { CartItem -> CartItem.produk.id }
            .mapValues { (_, daftarCartItem) ->
                daftarCartItem.sumOf { CartItem -> CartItem.jumlah }
            }
            // Item manual ("manual_*") tanpa baris produk lokal dilewati.
            .filterKeys { identitasProduk -> !apakahItemManual(identitasProduk) }

        basisData.withTransaction {
            val daftarProdukLokal = aksesDataProduk
                .ambilProdukBerdasarkanDaftarIdentitas(
                    daftarIdentitasProduk = daftarJumlahProduk.keys.toList(),
                )
                .associateBy { produkLokal -> produkLokal.id }

            daftarJumlahProduk.forEach { (identitasProduk, jumlahDiminta) ->
                val produkLokal = daftarProdukLokal[identitasProduk]
                    ?: throw IllegalArgumentException("Produk tidak ditemukan.")

                if (!produkLokal.apakahAktif) {
                    throw IllegalArgumentException("Produk ${produkLokal.nama} sedang tidak aktif.")
                }

                if (produkLokal.apakahStokDiaktifkan && produkLokal.stokTersedia < jumlahDiminta) {
                    throw IllegalArgumentException("Stok ${produkLokal.nama} tidak cukup.")
                }
            }

            simpanTransactionTanpaMengubahStok(
                Transaction = Transaction,
            )

            // Kurangi stok hanya untuk produk yang stoknya diaktifkan
            daftarJumlahProduk.forEach { (identitasProduk, jumlahPengurang) ->
                val produkLokal = daftarProdukLokal[identitasProduk]
                if (produkLokal?.apakahStokDiaktifkan == true) {
                    val jumlahBarisBerubah = aksesDataProduk.kurangiStokJikaCukup(
                        identitasProduk = identitasProduk,
                        jumlahPengurang = jumlahPengurang,
                    )

                    if (jumlahBarisBerubah != 1) {
                        val namaProduk = produkLokal.nama
                        throw IllegalArgumentException("Stok $namaProduk tidak cukup.")
                    }
                }
            }

            // Catat stok terbaru produk terdampak ke outbox (best-effort).
            catatPerubahanStokKeOutbox(daftarJumlahProduk.keys)
        }
    }

    /**
     * Menghitung versi transaksi baru (monotonik) dari versi tersimpan saat ini:
     * pasti lebih besar dari versi terakhir yang diketahui — termasuk versi
     * dari server saat pull — sehingga edit lokal menang LWW saat pull berikutnya.
     */
    private suspend fun hitungVersiTransaksiBaru(identitasTransaction: String): Long =
        PayloadSinkron.hitungVersiBaru(
            versiTersimpan = aksesDataTransaction
                .ambilTransactionBerdasarkanId(identitasTransaction)
                ?.Transaction
                ?.versi,
            waktuSekarang = System.currentTimeMillis(),
        )

    private suspend fun simpanTransactionTanpaMengubahStok(
        Transaction: Transaction,
    ) {
        // LWW berbasis versi: setiap perubahan lokal menaikkan versi transaksi.
        val versiBaru = hitungVersiTransaksiBaru(Transaction.id)
        val transactionDenganVersi = Transaction.copy(versi = versiBaru)
        val entitasTransaction = transactionDenganVersi.keLokal()
        val daftarEntitasItem = transactionDenganVersi.daftarCartItem.map { CartItem ->
            CartItem.keLokal(transactionDenganVersi.id)
        }

        aksesDataTransaction.simpanTransactionDenganItem(entitasTransaction, daftarEntitasItem)

        // Catat ke outbox agar perubahan ini ter-push ke server (best-effort).
        runCatching { pencatatOutbox?.catatTransaksi(transactionDenganVersi) }
    }

    /** Item manual (id "manual_*") dibuat tanpa baris Produk lokal. */
    private fun apakahItemManual(produkId: String): Boolean = produkId.startsWith("manual_")

    /**
     * Mengembalikan stok produk yang stoknya DIKETIK diaktifkan
     * ([LocalProductEntity.apakahStokDiaktifkan] = true) untuk daftar item.
     *
     * Simetris dengan pengurangan stok saat penjualan: produk dengan stok
     * non-aktif tidak pernah dikurangi saat jual, jadi tidak boleh ditambah
     * saat dibatalkan/dihapus — bila tidak, stok akan "menggembung".
     * Item manual ("manual_*") tanpa baris produk lokal dilewati.
     *
     * Keterbatasan: keputusan memakai flag [LocalProductEntity.apakahStokDiaktifkan]
     * SAAT INI (sama seperti logika pengurangan saat jual). Bila flag di-toggle
     * antara saat jual dan saat batal, hasilnya tidak persis: false→true masih
     * bisa menambah stok yang tak pernah dikurangi; true→false tidak
     * mengembalikan stok yang pernah dikurangi. Solusi ideal: simpan snapshot
     * flag per item transaksi — di luar cakupan perbaikan ini.
     */
    private suspend fun kembalikanStokProduk(daftarItem: List<LocalTransactionItemEntity>) {
        val idBukanManual = daftarItem
            .map { it.produkId }
            .filterNot { apakahItemManual(it) }
            .toSet()
        if (idBukanManual.isEmpty()) return

        val produkLokal = aksesDataProduk
            .ambilProdukBerdasarkanDaftarIdentitas(idBukanManual.toList())
            .associateBy { it.id }

        daftarItem.forEach { item ->
            if (produkLokal[item.produkId]?.apakahStokDiaktifkan == true) {
                aksesDataProduk.tambahStok(item.produkId, item.jumlah)
            }
        }
    }

    /**
     * Mencatat stok terbaru produk terdampak ke outbox (best-effort) agar
     * perubahan stok akibat transaksi/restore ikut ter-push ke server.
     */
    private suspend fun catatPerubahanStokKeOutbox(daftarIdProduk: Collection<String>) {
        if (pencatatOutbox == null) return
        val idBukanManual = daftarIdProduk.filterNot { apakahItemManual(it) }
        if (idBukanManual.isEmpty()) return
        aksesDataProduk
            .ambilProdukBerdasarkanDaftarIdentitas(idBukanManual)
            .map { it.keDomain() }
            .forEach { produk ->
                runCatching { pencatatOutbox?.catatProduk(produk) }
            }
    }

    /**
     * Mengamati seluruh riwayat Transaction dari Room.
     *
     * @return Aliran daftar Transaction domain.
     */
    override fun amatiSemuaTransaction(): Flow<List<Transaction>> {
        return aksesDataTransaction.amatiSemuaTransaction().map { daftarLokal: List<TransactionWithLocalItems> ->
            daftarLokal.map { TransactionLokal ->
                TransactionLokal.keDomain()
            }
        }
    }

    /**
     * Mengamati satu Transaction berdasarkan identitas.
     *
     * @param identitasTransaction Identitas unik Transaction.
     * @return Aliran Transaction domain, atau null jika tidak ditemukan.
     */
    override fun ObserveTransactionById(
        identitasTransaction: String,
    ): Flow<Transaction?> {
        return aksesDataTransaction.amatiTransactionBerdasarkanId(identitasTransaction).map { TransactionLokal ->
            TransactionLokal?.keDomain()
        }
    }

    /**
     * Mengambil rincian Transaction tunggal berdasarkan identitas unik.
     *
     * @param identitasTransaction Identitas unik Transaction.
     * @return Objek domain Transaction jika ditemukan, null jika tidak.
     */
    override suspend fun ambilTransactionBerdasarkanIdentitas(
        identitasTransaction: String,
    ): Transaction? {
        return aksesDataTransaction.ambilTransactionBerdasarkanId(identitasTransaction)?.keDomain()
    }

    override fun amatiTransactionPending(): Flow<List<Transaction>> {
        return aksesDataTransaction.amatiTransactionPending().map { daftarLokal ->
            daftarLokal.map { it.keDomain() }
        }
    }

    override fun amatiTransactionDiproses(): Flow<List<Transaction>> {
        return aksesDataTransaction.amatiTransactionDiproses().map { daftarLokal ->
            daftarLokal.map { it.keDomain() }
        }
    }

    override fun amatiTransactionLunas(): Flow<List<Transaction>> {
        return aksesDataTransaction.amatiTransactionLunas().map { daftarLokal ->
            daftarLokal.map { it.keDomain() }
        }
    }

    override suspend fun perbaruiStatusTransaction(
        identitasTransaction: String,
        status: TransactionStatus,
    ) {
        aksesDataTransaction.perbaruiStatusTransaction(
            id = identitasTransaction,
            status = status.name,
        )
    }

    override suspend fun hapusTransactionDanKembalikanStok(
        identitasTransaction: String,
    ) {
        val Transaction = aksesDataTransaction.ambilTransactionBerdasarkanId(identitasTransaction)
            ?: return

        basisData.withTransaction {
            // Kembalikan stok hanya untuk produk yang stoknya diaktifkan
            // (simetris dengan pengurangan stok saat transaksi dijual).
            kembalikanStokProduk(Transaction.daftarItem)
            aksesDataTransaction.hapusTransactionBerdasarkanId(identitasTransaction)
        }

        // Catat stok hasil restore ke outbox (best-effort).
        catatPerubahanStokKeOutbox(Transaction.daftarItem.map { it.produkId })

        // Beri tahu server bahwa transaksi ini dihapus (soft-delete).
        runCatching { pencatatOutbox?.catatTransaksi(Transaction.keDomain(), dihapus = true) }
    }

    override suspend fun batalkanTransaction(
        identitasTransaction: String,
        alasan: String?,
    ) {
        val transactionLokal = aksesDataTransaction.ambilTransactionBerdasarkanId(identitasTransaction)
            ?: return
        val Transaction = transactionLokal.keDomain()
        // Transaksi yang sudah dibatalkan tidak boleh dibatalkan lagi — bila
        // tidak, stok akan dikembalikan dua kali.
        if (Transaction.dibatalkan) return

        basisData.withTransaction {
            // Transaksi dibatalkan → barang tidak jadi terjual → stok dikembalikan
            // (hanya produk yang stoknya diaktifkan — simetris dengan saat jual).
            kembalikanStokProduk(transactionLokal.daftarItem)
            aksesDataTransaction.tandaiDibatalkan(identitasTransaction, alasan)
        }

        // Catat stok hasil restore ke outbox (best-effort).
        catatPerubahanStokKeOutbox(transactionLokal.daftarItem.map { it.produkId })

        // Beri tahu server bahwa transaksi ini dibatalkan.
        runCatching {
            pencatatOutbox?.catatTransaksi(
                Transaction.copy(dibatalkan = true, alasanPembatalan = alasan),
            )
        }
    }

    override suspend fun perbaruiStatusDanPaymentTransaction(
        identitasTransaction: String,
        status: TransactionStatus,
        uangDibayar: Uang,
        paymentMethod: PaymentMethod,
        waktuDibayarEpochMili: Long?,
    ) {
        basisData.withTransaction {
            // Update parsial mengubah field BERSAMA (status/dibayar/metode) →
            // versi entity juga dinaikkan (monotonik dari versi lama) agar
            // perubahan ini dianggap lebih baru dari data server saat pull,
            // meski push-nya belum berhasil.
            val versiBaru = hitungVersiTransaksiBaru(identitasTransaction)
            aksesDataTransaction.perbaruiStatusDanUangDibayarTransaction(
                id = identitasTransaction,
                status = status.name,
                uangDibayar = uangDibayar.nilaiRupiah,
                paymentMethod = paymentMethod.name,
                waktuDibayar = waktuDibayarEpochMili ?: System.currentTimeMillis(),
            )
            aksesDataTransaction.perbaruiVersiTransaction(identitasTransaction, versiBaru)
        }

        // Data pembayaran berubah → perbarui payload outbox agar server tidak
        // menyimpan versi lama (dibayar = 0 untuk pesanan yang baru dibayar).
        val transaction = aksesDataTransaction
            .ambilTransactionBerdasarkanId(identitasTransaction)
            ?.keDomain()
        if (transaction != null) {
            runCatching { pencatatOutbox?.catatTransaksi(transaction) }
        }
    }

    override suspend fun perbaruiStatusDanWaktuTransaction(
        identitasTransaction: String,
        status: TransactionStatus,
        waktuDiprosesEpochMili: Long?,
        waktuSelesaiEpochMili: Long?,
        waktuDibayarEpochMili: Long?,
    ) {
        basisData.withTransaction {
            // Status dikirim server (LWW) → versi entity ikut dinaikkan agar
            // perubahan lokal yang belum ter-push tidak tertimpa saat pull.
            val versiBaru = hitungVersiTransaksiBaru(identitasTransaction)
            aksesDataTransaction.perbaruiStatusTransaction(
                id = identitasTransaction,
                status = status.name,
            )
            waktuDiprosesEpochMili?.let {
                aksesDataTransaction.perbaruiWaktuDiproses(identitasTransaction, it)
            }
            waktuSelesaiEpochMili?.let {
                aksesDataTransaction.perbaruiWaktuSelesai(identitasTransaction, it)
            }
            waktuDibayarEpochMili?.let {
                aksesDataTransaction.perbaruiWaktuDibayar(identitasTransaction, it)
            }
            aksesDataTransaction.perbaruiVersiTransaction(identitasTransaction, versiBaru)
        }

        // Bump versi di outbox agar status terbaru ikut ter-push.
        val transaction = aksesDataTransaction
            .ambilTransactionBerdasarkanId(identitasTransaction)
            ?.keDomain()
        if (transaction != null) {
            runCatching { pencatatOutbox?.catatTransaksi(transaction) }
        }
    }

    override suspend fun perbaruiWaktuSelesai(
        identitasTransaction: String,
        waktuSelesaiEpochMili: Long,
    ) {
        aksesDataTransaction.perbaruiWaktuSelesai(identitasTransaction, waktuSelesaiEpochMili)
    }

    override suspend fun perbaruiWaktuDibayar(
        identitasTransaction: String,
        waktuDibayarEpochMili: Long,
    ) {
        aksesDataTransaction.perbaruiWaktuDibayar(identitasTransaction, waktuDibayarEpochMili)
    }

    override suspend fun tandaiItemSelesai(identitasTransaction: String) {
        aksesDataTransaction.tandaiItemSelesai(identitasTransaction)
    }

    override suspend fun ambilIdProdukTerpopuler(
        batasJumlah: Int,
    ): List<String> {
        return aksesDataTransaction.ambilIdProdukTerpopuler(batasJumlah)
            .map { it.produkId }
    }

    override suspend fun pastikanDataAwalTersedia() {
        // Data penjualan murni dari transaksi nyata pengguna, bukan sample data.
        // Tidak perlu melakukan apa pun di sini.
    }

    override suspend fun ambilNomorAntrianBerikutnya(): Int {
        val kalender = java.util.Calendar.getInstance()
        kalender.set(java.util.Calendar.HOUR_OF_DAY, 0)
        kalender.set(java.util.Calendar.MINUTE, 0)
        kalender.set(java.util.Calendar.SECOND, 0)
        kalender.set(java.util.Calendar.MILLISECOND, 0)
        val awalHari = kalender.timeInMillis

        kalender.add(java.util.Calendar.DAY_OF_MONTH, 1)
        val akhirHari = kalender.timeInMillis

        return aksesDataTransaction.ambilNomorAntrianMaksHariIni(awalHari, akhirHari) + 1
    }

    // ═══════════════════════════════════════
    // AGGREGATE — SQL SUM langsung, bukan load semua
    // ═══════════════════════════════════════

    override fun hitungTotalQRISSemua(): Flow<Long> =
        aksesDataTransaction.hitungTotalQRISSemua()

    override fun hitungTotalTunaiSejak(sejak: Long): Flow<Long> =
        aksesDataTransaction.hitungTotalTunaiSejak(sejak)

    override fun hitungTotalQRISSejak(sejak: Long): Flow<Long> =
        aksesDataTransaction.hitungTotalQRISSejak(sejak)

    override suspend fun hitungTotalTunaiRentang(sejak: Long, sampai: Long): Long =
        aksesDataTransaction.hitungTotalTunaiRentang(sejak, sampai)

    override suspend fun hitungTotalQRISRentang(sejak: Long, sampai: Long): Long =
        aksesDataTransaction.hitungTotalQRISRentang(sejak, sampai)

    override suspend fun ambilTransactionRentang(sejak: Long, sampai: Long): List<Transaction> =
        aksesDataTransaction.ambilTransactionRentang(sejak, sampai).map { it.keDomain() }

    override fun amatiTransactionSejak(sejak: Long): Flow<List<Transaction>> =
        aksesDataTransaction.amatiTransactionSejak(sejak).map { daftarLokal ->
            daftarLokal.map { it.keDomain() }
        }

    // ═══════════════════════════════════════
    // PAGING 3 — Data bertahap
    // ═══════════════════════════════════════

    override fun amatiTransactionPaged(
        sejak: Long?,
        sampai: Long?,
    ): Flow<PagingData<Transaction>> {
        return Pager(
            config = PagingConfig(pageSize = 30, enablePlaceholders = false),
            pagingSourceFactory = {
                aksesDataTransaction.amatiTransactionPaged(sejak, sampai)
            },
        ).flow.map { pagingData ->
            pagingData.map { it.keDomain() }
        }
    }
}
