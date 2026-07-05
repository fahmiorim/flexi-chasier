package id.cassy.kasir.data.lokal.pemetaan

import id.cassy.kasir.data.lokal.entitas.EntitasItemTransaksiLokal
import id.cassy.kasir.data.lokal.entitas.EntitasTransaksiLokal
import id.cassy.kasir.data.lokal.relasi.TransaksiDenganItemLokal
import id.cassy.kasir.ranah.model.ItemKeranjang
import id.cassy.kasir.ranah.model.MetodeBayar
import id.cassy.kasir.ranah.model.Produk
import id.cassy.kasir.ranah.model.StatusTransaksi
import id.cassy.kasir.ranah.model.TipeOrder
import id.cassy.kasir.ranah.model.Transaksi
import id.cassy.kasir.ranah.model.Uang

/**
 * Mengonversi model domain [Transaksi] menjadi entitas database utama [EntitasTransaksiLokal].
 *
 * @return Entitas transaksi yang siap disimpan ke database Room.
 */
fun Transaksi.keLokal(): EntitasTransaksiLokal {
    return EntitasTransaksiLokal(
        id = id,
        uangDibayar = uangDibayar.nilaiRupiah,
        potongan = potongan.nilaiRupiah,
        biayaLayanan = biayaLayanan.nilaiRupiah,
        pajak = pajak.nilaiRupiah,
        waktuTransaksiEpochMili = waktuTransaksiEpochMili,
        catatan = catatan,
        status = status.name,
        metodeBayar = metodeBayar.name,
        tipeOrder = tipeOrder.name,
    )
}

/**
 * Mengonversi [ItemKeranjang] menjadi entitas database [EntitasItemTransaksiLokal].
 *
 * @param transaksiId ID transaksi induk untuk menghubungkan item dengan transaksi terkait.
 * @return Entitas item transaksi yang siap disimpan ke database Room.
 */
fun ItemKeranjang.keLokal(transaksiId: String): EntitasItemTransaksiLokal {
    return EntitasItemTransaksiLokal(
        transaksiId = transaksiId,
        produkId = produk.id,
        namaProduk = produk.nama,
        hargaProduk = produk.harga,
        jumlah = jumlah,
        catatanItem = catatan,
        kodePindai = produk.kodePindai,
        deskripsiProduk = produk.deskripsi,
    )
}

/**
 * Mengonversi data database [TransaksiDenganItemLokal] kembali menjadi model domain [Transaksi].
 * Melakukan pemetaan rekursif untuk setiap item transaksi ke dalam model domain.
 *
 * @return Objek domain Transaksi yang lengkap dengan daftar itemnya.
 */
fun TransaksiDenganItemLokal.keDomain(): Transaksi {
    return Transaksi(
        id = transaksi.id,
        daftarItemKeranjang = daftarItem.map { itemLokal ->
            ItemKeranjang(
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
            )
        },
        uangDibayar = Uang.dariRupiah(transaksi.uangDibayar),
        potongan = Uang.dariRupiah(transaksi.potongan),
        biayaLayanan = Uang.dariRupiah(transaksi.biayaLayanan),
        pajak = Uang.dariRupiah(transaksi.pajak),
        waktuTransaksiEpochMili = transaksi.waktuTransaksiEpochMili,
        catatan = transaksi.catatan,
        status = StatusTransaksi.valueOf(transaksi.status),
        metodeBayar = try {
            MetodeBayar.valueOf(transaksi.metodeBayar)
        } catch (_: IllegalArgumentException) {
            MetodeBayar.Tunai
        },
        tipeOrder = try {
            TipeOrder.valueOf(transaksi.tipeOrder)
        } catch (_: IllegalArgumentException) {
            TipeOrder.DineIn
        },
    )
}
