package id.flexi.kasir.domain.sample

import id.flexi.kasir.domain.model.Produk
import id.flexi.kasir.domain.model.Varian

/**
 * Penyedia katalog produk awal untuk Flexi Cashier.
 *
 * Data seed untuk menu Signature Coffee & Eatery.
 * Produk dengan varian (Ice/Hot) digabung jadi satu produk.
 * Semua produk tanpa stok — stok hanya diaktifkan manual oleh pengguna.
 */
object SampleProductCatalog {

    fun daftarAwal(): List<Produk> {
        return daftarSignatureSeries() +
            daftarNonCoffee() +
            daftarAmericanoSeries() +
            daftarTeaSeries() +
            daftarMilkBase() +
            daftarBiteAndSnacks()
    }

    private fun daftarSignatureSeries(): List<Produk> {
        return listOf(
            produk(
                id = "produk-sig-level-up-coffee",
                nama = "Level Up Coffee (Susu Aren)",
                harga = 20_000,
                deskripsi = "Signature kopi susu dengan gula aren khas.",
                kategori = "Signature Series",
                favorit = true,
            ),
            produk(
                id = "produk-sig-berry-manuka",
                nama = "Berry Manuka",
                harga = 25_000,
                deskripsi = "Signature minuman berry manuka favorit pelanggan.",
                kategori = "Signature Series",
                favorit = true,
            ),
        )
    }

    private fun daftarNonCoffee(): List<Produk> {
        return listOf(
            produkVarian(
                id = "produk-nc-matcha-latte",
                nama = "Matcha Latte",
                harga = 22_000,
                deskripsi = "Matcha latte creamy.",
                kategori = "Non-Coffee",
                favorit = true,
                varian = listOf(
                    Varian(nama = "Ice", harga = 22_000),
                    Varian(nama = "Hot", harga = 20_000),
                ),
            ),
            produkVarian(
                id = "produk-nc-chocolate",
                nama = "Chocolate",
                harga = 20_000,
                deskripsi = "Minuman cokelat creamy.",
                kategori = "Non-Coffee",
                favorit = true,
                varian = listOf(
                    Varian(nama = "Ice", harga = 20_000),
                    Varian(nama = "Hot", harga = 18_000),
                ),
            ),
            produkVarian(
                id = "produk-nc-red-velvet",
                nama = "Red Velvet",
                harga = 20_000,
                deskripsi = "Red velvet creamy.",
                kategori = "Non-Coffee",
                varian = listOf(
                    Varian(nama = "Ice", harga = 20_000),
                    Varian(nama = "Hot", harga = 18_000),
                ),
            ),
            produkVarian(
                id = "produk-nc-taro",
                nama = "Taro",
                harga = 20_000,
                deskripsi = "Minuman taro creamy.",
                kategori = "Non-Coffee",
                varian = listOf(
                    Varian(nama = "Ice", harga = 20_000),
                    Varian(nama = "Hot", harga = 18_000),
                ),
            ),
        )
    }

    private fun daftarAmericanoSeries(): List<Produk> {
        return listOf(
            produkVarian(
                id = "produk-am-americano",
                nama = "Americano",
                harga = 15_000,
                deskripsi = "Americano klasik.",
                kategori = "Americano Series",
                varian = listOf(
                    Varian(nama = "Ice", harga = 15_000),
                    Varian(nama = "Hot", harga = 13_000),
                ),
            ),
            produk(
                id = "produk-am-americano-berry",
                nama = "Americano Berry (Ice Only)",
                harga = 20_000,
                deskripsi = "Americano dengan sentuhan rasa berry.",
                kategori = "Americano Series",
            ),
            produk(
                id = "produk-am-americano-lemon",
                nama = "Americano Lemon (Ice Only)",
                harga = 20_000,
                deskripsi = "Americano segar dengan lemon.",
                kategori = "Americano Series",
            ),
            produk(
                id = "produk-am-triple-lychee",
                nama = "Triple Lychee (Ice Only)",
                harga = 20_000,
                deskripsi = "Minuman lychee segar tiga lapis.",
                kategori = "Americano Series",
            ),
            produk(
                id = "produk-am-triple-peach",
                nama = "Triple Peach (Ice Only)",
                harga = 20_000,
                deskripsi = "Minuman peach segar tiga lapis.",
                kategori = "Americano Series",
            ),
        )
    }

    private fun daftarTeaSeries(): List<Produk> {
        return listOf(
            produkVarian(
                id = "produk-tea-lemon",
                nama = "Lemon Tea",
                harga = 13_000,
                deskripsi = "Teh lemon segar.",
                kategori = "Tea Series",
                varian = listOf(
                    Varian(nama = "Ice", harga = 13_000),
                    Varian(nama = "Hot", harga = 10_000),
                ),
            ),
            produkVarian(
                id = "produk-tea-lychee",
                nama = "Lychee Tea",
                harga = 13_000,
                deskripsi = "Teh lychee segar.",
                kategori = "Tea Series",
                varian = listOf(
                    Varian(nama = "Ice", harga = 13_000),
                    Varian(nama = "Hot", harga = 10_000),
                ),
            ),
            produkVarian(
                id = "produk-tea-peach",
                nama = "Peach Tea",
                harga = 13_000,
                deskripsi = "Teh peach segar.",
                kategori = "Tea Series",
                varian = listOf(
                    Varian(nama = "Ice", harga = 13_000),
                    Varian(nama = "Hot", harga = 10_000),
                ),
            ),
            produkVarian(
                id = "produk-tea-strawberry",
                nama = "Strawberry Tea",
                harga = 13_000,
                deskripsi = "Teh strawberry segar.",
                kategori = "Tea Series",
                varian = listOf(
                    Varian(nama = "Ice", harga = 13_000),
                    Varian(nama = "Hot", harga = 10_000),
                ),
            ),
            produkVarian(
                id = "produk-tea",
                nama = "Tea",
                harga = 13_000,
                deskripsi = "Teh klasik.",
                kategori = "Tea Series",
                varian = listOf(
                    Varian(nama = "Ice", harga = 13_000),
                    Varian(nama = "Hot", harga = 10_000),
                ),
            ),
        )
    }

    private fun daftarMilkBase(): List<Produk> {
        return listOf(
            produkVarian(
                id = "produk-mb-sanger",
                nama = "Sanger",
                harga = 18_000,
                deskripsi = "Kopi susu sanger khas Aceh.",
                kategori = "Milk Base",
                favorit = true,
                varian = listOf(
                    Varian(nama = "Ice", harga = 18_000),
                    Varian(nama = "Hot", harga = 15_000),
                ),
            ),
            produkVarian(
                id = "produk-mb-caffe-latte",
                nama = "Caffe Latte",
                harga = 20_000,
                deskripsi = "Caffe latte klasik.",
                kategori = "Milk Base",
                varian = listOf(
                    Varian(nama = "Ice", harga = 20_000),
                    Varian(nama = "Hot", harga = 18_000),
                ),
            ),
            produkVarian(
                id = "produk-mb-butterscotch",
                nama = "Butterscotch",
                harga = 22_000,
                deskripsi = "Minuman butterscotch creamy.",
                kategori = "Milk Base",
                varian = listOf(
                    Varian(nama = "Ice", harga = 22_000),
                    Varian(nama = "Hot", harga = 20_000),
                ),
            ),
            produkVarian(
                id = "produk-mb-moccachino",
                nama = "Moccachino",
                harga = 20_000,
                deskripsi = "Moccachino creamy.",
                kategori = "Milk Base",
                varian = listOf(
                    Varian(nama = "Ice", harga = 20_000),
                    Varian(nama = "Hot", harga = 18_000),
                ),
            ),
        )
    }

    private fun daftarBiteAndSnacks(): List<Produk> {
        return listOf(
            produk(
                id = "produk-snack-kentang-goreng",
                nama = "Kentang Goreng",
                harga = 15_000,
                deskripsi = "Kentang goreng renyah.",
                kategori = "Bite & Snacks",
            ),
            produk(
                id = "produk-snack-mix-platters",
                nama = "Mix Platters",
                harga = 18_000,
                deskripsi = "Aneka camilan platter.",
                kategori = "Bite & Snacks",
            ),
            produk(
                id = "produk-snack-nugget",
                nama = "Nugget",
                harga = 10_000,
                deskripsi = "Nugget ayam crispy.",
                kategori = "Bite & Snacks",
            ),
            produk(
                id = "produk-snack-risol",
                nama = "Risol",
                harga = 10_000,
                deskripsi = "Risol isi sayur dan daging.",
                kategori = "Bite & Snacks",
            ),
            produk(
                id = "produk-snack-sosis",
                nama = "Sosis",
                harga = 10_000,
                deskripsi = "Sosis goreng.",
                kategori = "Bite & Snacks",
            ),
        )
    }

    private fun produk(
        id: String,
        nama: String,
        harga: Long,
        deskripsi: String,
        kategori: String,
        favorit: Boolean = false,
    ): Produk {
        return Produk(
            id = id,
            nama = nama,
            harga = harga,
            stokTersedia = 0,
            kodePindai = null,
            deskripsi = deskripsi,
            kategori = kategori,
            aktif = true,
            favorit = favorit,
            apakahStokDiaktifkan = false,
        )
    }

    private fun produkVarian(
        id: String,
        nama: String,
        harga: Long,
        deskripsi: String,
        kategori: String,
        favorit: Boolean = false,
        varian: List<Varian> = emptyList(),
    ): Produk {
        return Produk(
            id = id,
            nama = nama,
            harga = harga,
            stokTersedia = 0,
            kodePindai = null,
            deskripsi = deskripsi,
            kategori = kategori,
            aktif = true,
            favorit = favorit,
            varian = varian,
            apakahStokDiaktifkan = false,
        )
    }
}
