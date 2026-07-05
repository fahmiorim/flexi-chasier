package id.cassy.kasir.ranah.fungsi

import id.cassy.kasir.ranah.model.ItemKeranjang
import id.cassy.kasir.ranah.model.Produk
import id.cassy.kasir.ranah.model.Uang
import java.text.NumberFormat
import java.util.Locale

/**
 * Menghitung nilai subtotal untuk satu baris item keranjang dalam bentuk Long.
 *
 * Fungsi ini dipertahankan sebagai kompatibilitas untuk UI dan mapper lama.
 */
fun ItemKeranjang.hitungSubTotal(): Long {
    return hitungSubtotalUang().nilaiRupiah
}

/**
 * Menghitung nilai subtotal untuk satu baris item keranjang dalam bentuk [Uang].
 *
 * @return Subtotal item berdasarkan harga produk dikali jumlah.
 */
fun ItemKeranjang.hitungSubtotalUang(): Uang {
    return Uang.dariRupiah(
        nilaiRupiah = produk.harga * jumlah,
    )
}

/**
 * Menghitung total seluruh jumlah item dalam keranjang.
 */
fun List<ItemKeranjang>.hitungJumlahItem(): Int {
    return sumOf { itemKeranjang ->
        itemKeranjang.jumlah
    }
}

/**
 * Menghitung total nilai uang seluruh item dalam keranjang sebelum potongan,
 * pajak, atau biaya layanan dalam bentuk Long.
 *
 * Fungsi ini dipertahankan agar kode lama tetap aman selama migrasi bertahap.
 */
fun List<ItemKeranjang>.hitungSubtotalKeranjang(): Long {
    return hitungSubtotalKeranjangUang().nilaiRupiah
}

/**
 * Menghitung total nilai uang seluruh item dalam keranjang sebelum potongan,
 * pajak, atau biaya layanan dalam bentuk [Uang].
 *
 * @return Subtotal keranjang sebagai value object uang.
 */
fun List<ItemKeranjang>.hitungSubtotalKeranjangUang(): Uang {
    val subtotal = sumOf { itemKeranjang ->
        itemKeranjang.hitungSubtotalUang().nilaiRupiah
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
 * Nilai uang tetap disimpan sebagai Long agar aman untuk transaksi kasir.
 */
fun Long.sebagaiRupiah(): String {
    val pembuatFormatRupiah = NumberFormat.getCurrencyInstance(
        Locale.forLanguageTag("id-ID"),
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
