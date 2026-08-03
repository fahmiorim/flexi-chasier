package id.flexi.kasir.ui.format

import id.flexi.kasir.domain.util.hitungKembalian
import id.flexi.kasir.domain.util.hitungTotalAkhirTransaction
import id.flexi.kasir.domain.model.Transaction
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Membentuk label Transaction yang aman untuk antarmuka.
 *
 * Fungsi di file ini berada di layer antarmuka karena hasilnya adalah teks
 * presentasi, bukan aturan bisnis inti.
 */

/**
 * Mengubah identitas UUID Transaction menjadi label pendek yang lebih ramah dibaca.
 *
 * Identitas asli tetap dipakai untuk query, navigasi, dan relasi database.
 */
fun String.sebagaiLabelIdentitasTransaction(): String {
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
 * Mengubah waktu Transaction epoch milidetik menjadi label tanggal dan jam.
 */
fun Long.sebagaiLabelWaktuTransaction(): String {
    val pembentukFormat = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")
    return Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .format(pembentukFormat)
}

/**
 * Menghitung jumlah seluruh item Transaction.
 */
fun Transaction.hitungJumlahItemTransaction(): Int {
    return daftarCartItem.sumOf { CartItem ->
        CartItem.jumlah
    }
}

/**
 * Menghitung subtotal Transaction.
 */
fun Transaction.hitungSubtotalTransaction(): Long {
    return daftarCartItem.sumOf { CartItem ->
        CartItem.produk.harga * CartItem.jumlah
    }
}

/**
 * Menghitung kembalian Transaction.
 */
fun Transaction.hitungKembalianTransaction(): Long {
    return hitungKembalian(
        uangDibayar = uangDibayar.nilaiRupiah,
        totalTransaction = hitungTotalAkhirTransaction(),
    )
}
