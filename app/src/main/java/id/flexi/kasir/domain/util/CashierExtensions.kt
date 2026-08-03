package id.flexi.kasir.domain.util

import id.flexi.kasir.domain.model.CartItem
import id.flexi.kasir.domain.model.Produk
import id.flexi.kasir.domain.model.Transaction
import id.flexi.kasir.domain.model.Uang
import java.text.NumberFormat
import java.util.Locale

/**
 * Menghitung nilai subtotal untuk satu baris item keranjang dalam bentuk Long.
 *
 * Fungsi ini dipertahankan sebagai kompatibilitas untuk UI dan mapper lama.
 */
fun CartItem.hitungSubTotal(): Long {
    return hitungSubtotalUang().nilaiRupiah
}

/**
 * Menghitung nilai subtotal untuk satu baris item keranjang dalam bentuk [Uang].
 *
 * @return Subtotal item berdasarkan harga produk dikali jumlah.
 */
fun CartItem.hitungSubtotalUang(): Uang {
    val hargaPakai = varian?.harga ?: produk.harga
    return Uang.dariRupiah(
        nilaiRupiah = hargaPakai * jumlah,
    )
}

/**
 * Menghitung total seluruh jumlah item dalam keranjang.
 */
fun List<CartItem>.hitungJumlahItem(): Int {
    return sumOf { CartItem ->
        CartItem.jumlah
    }
}

/**
 * Menghitung total nilai uang seluruh item dalam keranjang sebelum potongan,
 * pajak, atau biaya layanan dalam bentuk Long.
 *
 * Fungsi ini dipertahankan agar kode lama tetap aman selama migrasi bertahap.
 */
fun List<CartItem>.hitungSubtotalKeranjang(): Long {
    return hitungSubtotalKeranjangUang().nilaiRupiah
}

/**
 * Menghitung total nilai uang seluruh item dalam keranjang sebelum potongan,
 * pajak, atau biaya layanan dalam bentuk [Uang].
 *
 * @return Subtotal keranjang sebagai value object uang.
 */
fun List<CartItem>.hitungSubtotalKeranjangUang(): Uang {
    val subtotal = sumOf { CartItem ->
        CartItem.hitungSubtotalUang().nilaiRupiah
    }

    return Uang.dariRupiah(
        nilaiRupiah = subtotal,
    )
}

/**
 * Menyaring daftar produk berdasarkan nama produk atau kode pindai.
 */
fun List<Produk>.cariProduk(kataKunci: String): List<Produk> {
    val kataKunciBersih = kataKunci.trim()

    if (kataKunciBersih.isBlank()) {
        return this
    }

    return filter { produk ->
        produk.nama.contains(kataKunciBersih, ignoreCase = true) ||
            produk.kodePindai?.contains(kataKunciBersih, ignoreCase = true) == true
    }
}

/**
 * Memastikan nilai teks tidak kosong untuk tampilan.
 */
fun String?.ambilAtauStrip(): String {
    return if (isNullOrBlank()) "-" else this
}

/**
 * Mengubah nilai uang berbasis Long menjadi format Rupiah.
 *
 * Nilai uang tetap disimpan sebagai Long agar aman untuk Transaction kasir.
 */
fun Long.sebagaiRupiah(): String {
    val pembuatFormatRupiah = NumberFormat.getCurrencyInstance(
        Locale("id", "ID"),
    )

    pembuatFormatRupiah.maximumFractionDigits = 0

    return pembuatFormatRupiah.format(this)
}

/**
 * Mengubah value object [Uang] menjadi format Rupiah.
 *
 * Fungsi ini menjadi jembatan aman agar UI tidak perlu membaca nilai mentah
 * terlalu sering saat domain sudah mulai memakai [Uang].
 */
fun Uang.sebagaiRupiah(): String {
    return nilaiRupiah.sebagaiRupiah()
}

/**
 * Menghitung total akhir Transaction.
 */
fun Transaction.hitungTotalAkhirTransaction(): Long {
    return hitungTotalTransaction(
        daftarCartItem = daftarCartItem,
        potongan = potongan.nilaiRupiah,
        biayaLayanan = biayaLayanan.nilaiRupiah,
        pajak = pajak.nilaiRupiah,
    )
}
