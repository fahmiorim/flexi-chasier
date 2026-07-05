package id.cassy.kasir.antarmuka.format

import id.cassy.kasir.ranah.fungsi.hitungKembalian
import id.cassy.kasir.ranah.fungsi.hitungTotalTransaksi
import id.cassy.kasir.ranah.model.Transaksi
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Membentuk label transaksi yang aman untuk antarmuka.
 *
 * Fungsi di file ini berada di layer antarmuka karena hasilnya adalah teks
 * presentasi, bukan aturan bisnis inti.
 */

/**
 * Mengubah identitas UUID transaksi menjadi label pendek yang lebih ramah dibaca.
 *
 * Identitas asli tetap dipakai untuk query, navigasi, dan relasi database.
 */
fun String.sebagaiLabelIdentitasTransaksi(): String {
    val kodeRingkas = filter { karakter ->
        karakter.isLetterOrDigit()
    }.take(8)
        .uppercase(Locale.ROOT)
        .ifBlank {
            "TANPAID"
        }

    return "TRX-$kodeRingkas"
}

/**
 * Mengubah waktu transaksi epoch milidetik menjadi label tanggal dan jam.
 */
fun Long.sebagaiLabelWaktuTransaksi(): String {
    val pembentukFormat = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")
    return Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .format(pembentukFormat)
}

/**
 * Menghitung jumlah seluruh item transaksi.
 */
fun Transaksi.hitungJumlahItemTransaksi(): Int {
    return daftarItemKeranjang.sumOf { itemKeranjang ->
        itemKeranjang.jumlah
    }
}

/**
 * Menghitung subtotal transaksi.
 */
fun Transaksi.hitungSubtotalTransaksi(): Long {
    return daftarItemKeranjang.sumOf { itemKeranjang ->
        itemKeranjang.produk.harga * itemKeranjang.jumlah
    }
}

/**
 * Menghitung total akhir transaksi.
 */
fun Transaksi.hitungTotalAkhirTransaksi(): Long {
    return hitungTotalTransaksi(
        daftarItemKeranjang = daftarItemKeranjang,
        potongan = potongan.nilaiRupiah,
        biayaLayanan = biayaLayanan.nilaiRupiah,
        pajak = pajak.nilaiRupiah,
    )
}

/**
 * Menghitung kembalian transaksi.
 */
fun Transaksi.hitungKembalianTransaksi(): Long {
    return hitungKembalian(
        uangDibayar = uangDibayar.nilaiRupiah,
        totalTransaksi = hitungTotalAkhirTransaksi(),
    )
}
