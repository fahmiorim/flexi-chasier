package id.flexi.kasir.data.local.mapping

import id.flexi.kasir.data.local.entity.LocalTransactionItemEntity
import id.flexi.kasir.data.local.entity.LocalTransactionEntity
import id.flexi.kasir.data.local.relation.TransactionWithLocalItems
import id.flexi.kasir.domain.model.CartItem
import id.flexi.kasir.domain.model.PaymentMethod
import id.flexi.kasir.domain.model.Produk
import id.flexi.kasir.domain.model.TransactionStatus
import id.flexi.kasir.domain.model.OrderType
import id.flexi.kasir.domain.model.Transaction
import id.flexi.kasir.domain.model.Uang
import id.flexi.kasir.domain.model.Varian

/**
 * Mengonversi model domain [Transaction] menjadi entitas database utama [LocalTransactionEntity].
 *
 * @return Entitas Transaction yang siap disimpan ke database Room.
 */
fun Transaction.keLokal(): LocalTransactionEntity {
    return LocalTransactionEntity(
        id = id,
        uangDibayar = uangDibayar.nilaiRupiah,
        potongan = potongan.nilaiRupiah,
        biayaLayanan = biayaLayanan.nilaiRupiah,
        pajak = pajak.nilaiRupiah,
        waktuTransactionEpochMili = waktuTransactionEpochMili,
        catatan = catatan,
        status = status.name,
        PaymentMethod = paymentMethod.name,
        OrderType = orderType.name,
        nomorAntrian = nomorAntrian,
        mejaId = mejaId,
        waktuDiprosesEpochMili = waktuDiprosesEpochMili,
        waktuSelesaiEpochMili = waktuSelesaiEpochMili,
        waktuDibayarEpochMili = waktuDibayarEpochMili,
        dibatalkan = dibatalkan,
        alasanPembatalan = alasanPembatalan,
    )
}

/**
 * Mengonversi [CartItem] menjadi entitas database [LocalTransactionItemEntity].
 *
 * @param TransactionId ID Transaction induk untuk menghubungkan item dengan Transaction terkait.
 * @return Entitas item Transaction yang siap disimpan ke database Room.
 */
fun CartItem.keLokal(TransactionId: String): LocalTransactionItemEntity {
    return LocalTransactionItemEntity(
        TransactionId = TransactionId,
        produkId = produk.id,
        namaProduk = produk.nama,
        hargaProduk = varian?.harga ?: produk.harga,
        jumlah = jumlah,
        catatanItem = catatan,
        kodePindai = produk.kodePindai,
        deskripsiProduk = produk.deskripsi,
        varianNama = varian?.nama,
        apakahSelesai = apakahSelesai,
    )
}

/**
 * Mengonversi data database [TransactionWithLocalItems] kembali menjadi model domain [Transaction].
 * Melakukan pemetaan rekursif untuk setiap item Transaction ke dalam model domain.
 *
 * @return Objek domain Transaction yang lengkap dengan daftar itemnya.
 */
fun TransactionWithLocalItems.keDomain(): Transaction {
    return Transaction(
        id = Transaction.id,
        daftarCartItem = daftarItem.map { itemLokal ->
            CartItem(
                produk = Produk(
                    id = itemLokal.produkId,
                    nama = itemLokal.namaProduk,
                    harga = itemLokal.hargaProduk,
                    stokTersedia = 0,
                    kodePindai = itemLokal.kodePindai,
                    deskripsi = itemLokal.deskripsiProduk,
                    aktif = true,
                ),
                jumlah = itemLokal.jumlah,
                catatan = itemLokal.catatanItem,
                varian = itemLokal.varianNama?.let { nama ->
                    Varian(nama = nama, harga = itemLokal.hargaProduk)
                },
                apakahSelesai = itemLokal.apakahSelesai,
            )
        },
        uangDibayar = Uang.dariRupiah(Transaction.uangDibayar),
        potongan = Uang.dariRupiah(Transaction.potongan),
        biayaLayanan = Uang.dariRupiah(Transaction.biayaLayanan),
        pajak = Uang.dariRupiah(Transaction.pajak),
        waktuTransactionEpochMili = Transaction.waktuTransactionEpochMili,
        catatan = Transaction.catatan,
        status = TransactionStatus.valueOf(Transaction.status),
        paymentMethod = try {
            PaymentMethod.valueOf(Transaction.PaymentMethod)
        } catch (_: IllegalArgumentException) {
            PaymentMethod.Cash
        },
        orderType = try {
            OrderType.valueOf(Transaction.OrderType)
        } catch (_: IllegalArgumentException) {
            OrderType.DineIn
        },
        nomorAntrian = Transaction.nomorAntrian,
        mejaId = Transaction.mejaId,
        waktuDiprosesEpochMili = Transaction.waktuDiprosesEpochMili,
        waktuSelesaiEpochMili = Transaction.waktuSelesaiEpochMili,
        waktuDibayarEpochMili = Transaction.waktuDibayarEpochMili,
        dibatalkan = Transaction.dibatalkan,
    )
}
