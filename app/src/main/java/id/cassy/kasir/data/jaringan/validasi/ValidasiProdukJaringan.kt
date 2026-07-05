package id.cassy.kasir.data.jaringan.validasi

import id.cassy.kasir.data.jaringan.model.ResponsProdukJaringan

/**
 * Memvalidasi daftar produk dari jaringan sebelum disimpan ke penyimpanan lokal.
 *
 * Validasi ini berada di layer data jaringan karena mengecek kualitas response
 * mentah dari server. Tujuannya agar data buruk tidak masuk ke Room.
 */
fun validasiDaftarProdukJaringan(
    daftarProduk: List<ResponsProdukJaringan>,
) {
    require(daftarProduk.isNotEmpty()) {
        "Katalog dari server masih kosong."
    }

    val daftarIdentitas = daftarProduk.map { produk -> produk.id.trim() }
    require(daftarIdentitas.size == daftarIdentitas.toSet().size) {
        "Katalog dari server memiliki identitas produk duplikat."
    }

    val daftarKodePindai = daftarProduk
        .mapNotNull { produk -> produk.kodePindai?.trim()?.takeIf { it.isNotBlank() } }

    require(daftarKodePindai.size == daftarKodePindai.toSet().size) {
        "Katalog dari server memiliki kode pindai duplikat."
    }

    daftarProduk.forEach { produk ->
        require(produk.id.trim().isNotBlank()) {
            "Katalog dari server memiliki produk tanpa identitas."
        }

        require(produk.nama.trim().isNotBlank()) {
            "Produk ${produk.id} dari server belum memiliki nama."
        }

        require(produk.harga > 0L) {
            "Produk ${produk.id} dari server memiliki harga tidak valid."
        }

        require(produk.stokTersedia >= 0) {
            "Produk ${produk.id} dari server memiliki stok tidak valid."
        }
    }
}
