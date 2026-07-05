package id.cassy.kasir.ranah.contoh

import id.cassy.kasir.ranah.model.Produk

/**
 * Penyedia katalog produk awal untuk mode belajar dan instalasi baru.
 *
 * Data di sini bersifat seed lokal, bukan data harga resmi. Produk dipilih agar
 * terasa dekat dengan UMKM Indonesia seperti warung, kedai kopi, angkringan,
 * toko sembako, dan minuman kemasan.
 */
object KatalogProdukContoh {

    /**
     * Menghasilkan daftar awal produk yang stabil dan siap disimpan ke Room.
     *
     * Identitas produk seed dibuat deterministik agar mudah diuji, mudah
     * ditelusuri, dan tidak berubah antar build.
     */
    fun daftarAwal(): List<Produk> {
        return daftarMinumanKedai() +
            daftarMakananKedai() +
            daftarSembakoWarung() +
            daftarMinumanKemasan() +
            daftarJajananIndonesia() +
            daftarKebutuhanHarian()
    }

    private fun daftarMinumanKedai(): List<Produk> {
        return listOf(
            produk(
                id = "produk-minuman-kopi-tubruk",
                nama = "Kopi Tubruk",
                harga = 8_000,
                stokTersedia = 80,
                kodePindai = "kode-minuman-001",
                deskripsi = "Kopi hitam seduh tradisional yang umum dijual di warung dan kedai kecil.",
                kategori = "Minuman",
            ),
            produk(
                id = "produk-minuman-kopi-susu-gula-aren",
                nama = "Kopi Susu Gula Aren",
                harga = 18_000,
                stokTersedia = 60,
                kodePindai = "kode-minuman-002",
                deskripsi = "Kopi susu dengan gula aren, cocok untuk katalog kedai kopi modern.",
                kategori = "Minuman",
            ),
            produk(
                id = "produk-minuman-es-teh-manis",
                nama = "Es Teh Manis",
                harga = 5_000,
                stokTersedia = 100,
                kodePindai = "kode-minuman-003",
                deskripsi = "Minuman teh manis dingin yang umum tersedia di warung makan Indonesia.",
                kategori = "Minuman",
            ),
            produk(
                id = "produk-minuman-teh-tawar-hangat",
                nama = "Teh Tawar Hangat",
                harga = 3_000,
                stokTersedia = 100,
                kodePindai = "kode-minuman-004",
                deskripsi = "Teh hangat tanpa gula untuk pendamping makanan.",
                kategori = "Minuman",
            ),
            produk(
                id = "produk-minuman-jeruk-peras",
                nama = "Jeruk Peras",
                harga = 10_000,
                stokTersedia = 40,
                kodePindai = "kode-minuman-005",
                deskripsi = "Minuman jeruk segar yang biasa dijual di rumah makan dan kedai.",
                kategori = "Minuman",
            ),
            produk(
                id = "produk-minuman-es-cokelat",
                nama = "Es Cokelat",
                harga = 15_000,
                stokTersedia = 45,
                kodePindai = "kode-minuman-006",
                deskripsi = "Minuman cokelat dingin untuk variasi menu kedai.",
                kategori = "Minuman",
            ),
            produk(
                id = "produk-minuman-susu-jahe",
                nama = "Susu Jahe",
                harga = 12_000,
                stokTersedia = 35,
                kodePindai = "kode-minuman-007",
                deskripsi = "Minuman hangat berbahan susu dan jahe untuk menu malam atau angkringan.",
                kategori = "Minuman",
            ),
            produk(
                id = "produk-minuman-es-kopi-hitam",
                nama = "Es Kopi Hitam",
                harga = 12_000,
                stokTersedia = 50,
                kodePindai = "kode-minuman-008",
                deskripsi = "Kopi hitam dingin tanpa susu untuk pelanggan yang menyukai rasa kopi kuat.",
                kategori = "Minuman",
            ),
        )
    }

    private fun daftarMakananKedai(): List<Produk> {
        return listOf(
            produk(
                id = "produk-makanan-nasi-goreng-telur",
                nama = "Nasi Goreng Telur",
                harga = 18_000,
                stokTersedia = 30,
                kodePindai = "kode-makanan-001",
                deskripsi = "Nasi goreng sederhana dengan telur, cocok untuk warung makan.",
                kategori = "Makanan",
            ),
            produk(
                id = "produk-makanan-mie-goreng-telur",
                nama = "Mie Goreng Telur",
                harga = 16_000,
                stokTersedia = 30,
                kodePindai = "kode-makanan-002",
                deskripsi = "Mie goreng dengan telur dan sayuran sederhana.",
                kategori = "Makanan",
            ),
            produk(
                id = "produk-makanan-ayam-geprek",
                nama = "Ayam Geprek",
                harga = 20_000,
                stokTersedia = 25,
                kodePindai = "kode-makanan-003",
                deskripsi = "Ayam goreng tepung dengan sambal geprek khas Indonesia.",
                kategori = "Makanan",
            ),
            produk(
                id = "produk-makanan-pecel-ayam",
                nama = "Pecel Ayam",
                harga = 24_000,
                stokTersedia = 20,
                kodePindai = "kode-makanan-004",
                deskripsi = "Ayam goreng dengan sambal, lalapan, dan nasi.",
                kategori = "Makanan",
            ),
            produk(
                id = "produk-makanan-soto-ayam",
                nama = "Soto Ayam",
                harga = 18_000,
                stokTersedia = 25,
                kodePindai = "kode-makanan-005",
                deskripsi = "Soto ayam kuah kuning dengan isian ayam dan bihun.",
                kategori = "Makanan",
            ),
            produk(
                id = "produk-makanan-bakso-kuah",
                nama = "Bakso Kuah",
                harga = 18_000,
                stokTersedia = 25,
                kodePindai = "kode-makanan-006",
                deskripsi = "Bakso kuah dengan mie dan pelengkap sederhana.",
                kategori = "Makanan",
            ),
            produk(
                id = "produk-makanan-nasi-uduk",
                nama = "Nasi Uduk",
                harga = 12_000,
                stokTersedia = 35,
                kodePindai = "kode-makanan-007",
                deskripsi = "Nasi gurih dengan lauk sederhana untuk menu pagi.",
                kategori = "Makanan",
            ),
            produk(
                id = "produk-makanan-bubur-ayam",
                nama = "Bubur Ayam",
                harga = 15_000,
                stokTersedia = 30,
                kodePindai = "kode-makanan-008",
                deskripsi = "Bubur ayam dengan suwiran ayam dan kerupuk.",
                kategori = "Makanan",
            ),
        )
    }

    private fun daftarSembakoWarung(): List<Produk> {
        return listOf(
            produk(
                id = "produk-sembako-beras-premium-lima-kilogram",
                nama = "Beras Premium 5 Kilogram",
                harga = 78_000,
                stokTersedia = 20,
                kodePindai = "kode-sembako-001",
                deskripsi = "Beras kemasan lima kilogram untuk kebutuhan rumah tangga.",
                kategori = "Sembako",
            ),
            produk(
                id = "produk-sembako-gula-pasir-satu-kilogram",
                nama = "Gula Pasir 1 Kilogram",
                harga = 18_000,
                stokTersedia = 40,
                kodePindai = "kode-sembako-002",
                deskripsi = "Gula pasir kemasan satu kilogram untuk kebutuhan harian.",
                kategori = "Sembako",
            ),
            produk(
                id = "produk-sembako-minyak-goreng-satu-liter",
                nama = "Minyak Goreng 1 Liter",
                harga = 19_000,
                stokTersedia = 35,
                kodePindai = "kode-sembako-003",
                deskripsi = "Minyak goreng kemasan satu liter untuk warung sembako.",
                kategori = "Sembako",
            ),
            produk(
                id = "produk-sembako-telur-ayam-satu-kilogram",
                nama = "Telur Ayam 1 Kilogram",
                harga = 30_000,
                stokTersedia = 25,
                kodePindai = "kode-sembako-004",
                deskripsi = "Telur ayam curah satu kilogram.",
                kategori = "Sembako",
            ),
            produk(
                id = "produk-sembako-tepung-terigu-satu-kilogram",
                nama = "Tepung Terigu 1 Kilogram",
                harga = 13_000,
                stokTersedia = 30,
                kodePindai = "kode-sembako-005",
                deskripsi = "Tepung terigu kemasan satu kilogram.",
                kategori = "Sembako",
            ),
            produk(
                id = "produk-sembako-garam-dapur",
                nama = "Garam Dapur",
                harga = 5_000,
                stokTersedia = 50,
                kodePindai = "kode-sembako-006",
                deskripsi = "Garam dapur kemasan kecil untuk kebutuhan rumah tangga.",
                kategori = "Sembako",
            ),
            produk(
                id = "produk-sembako-kecap-manis",
                nama = "Kecap Manis",
                harga = 12_000,
                stokTersedia = 35,
                kodePindai = "kode-sembako-007",
                deskripsi = "Kecap manis botol kecil untuk pelengkap masakan.",
                kategori = "Sembako",
            ),
            produk(
                id = "produk-sembako-saus-sambal",
                nama = "Saus Sambal",
                harga = 11_000,
                stokTersedia = 35,
                kodePindai = "kode-sembako-008",
                deskripsi = "Saus sambal botol kecil untuk kebutuhan warung.",
                kategori = "Sembako",
            ),
        )
    }

    private fun daftarMinumanKemasan(): List<Produk> {
        return listOf(
            produk(
                id = "produk-kemasan-air-mineral-enam-ratus-mililiter",
                nama = "Air Mineral 600 Mililiter",
                harga = 5_000,
                stokTersedia = 120,
                kodePindai = "kode-kemasan-001",
                deskripsi = "Air mineral botol ukuran sedang untuk minimarket dan warung.",
                kategori = "Minuman",
            ),
            produk(
                id = "produk-kemasan-air-mineral-seribu-lima-ratus-mililiter",
                nama = "Air Mineral 1500 Mililiter",
                harga = 8_000,
                stokTersedia = 80,
                kodePindai = "kode-kemasan-002",
                deskripsi = "Air mineral botol besar untuk kebutuhan perjalanan atau rumah.",
                kategori = "Minuman",
            ),
            produk(
                id = "produk-kemasan-teh-melati-kotak",
                nama = "Teh Melati Kotak",
                harga = 6_000,
                stokTersedia = 60,
                kodePindai = "kode-kemasan-003",
                deskripsi = "Minuman teh melati kemasan kotak.",
                kategori = "Minuman",
            ),
            produk(
                id = "produk-kemasan-minuman-isotonik",
                nama = "Minuman Isotonik",
                harga = 8_000,
                stokTersedia = 50,
                kodePindai = "kode-kemasan-004",
                deskripsi = "Minuman kemasan untuk aktivitas harian dan olahraga ringan.",
                kategori = "Minuman",
            ),
            produk(
                id = "produk-kemasan-susu-cokelat-kotak",
                nama = "Susu Cokelat Kotak",
                harga = 7_000,
                stokTersedia = 50,
                kodePindai = "kode-kemasan-005",
                deskripsi = "Susu rasa cokelat kemasan kotak kecil.",
                kategori = "Minuman",
            ),
            produk(
                id = "produk-kemasan-kopi-susu-kaleng",
                nama = "Kopi Susu Kaleng",
                harga = 10_000,
                stokTersedia = 45,
                kodePindai = "kode-kemasan-006",
                deskripsi = "Kopi susu siap minum dalam kemasan kaleng.",
                kategori = "Minuman",
            ),
        )
    }

    private fun daftarJajananIndonesia(): List<Produk> {
        return listOf(
            produk(
                id = "produk-jajanan-pisang-goreng",
                nama = "Pisang Goreng",
                harga = 3_000,
                stokTersedia = 60,
                kodePindai = "kode-jajanan-001",
                deskripsi = "Gorengan pisang yang umum dijual di warung dan kantin.",
                kategori = "Makanan",
            ),
            produk(
                id = "produk-jajanan-bakwan-sayur",
                nama = "Bakwan Sayur",
                harga = 2_500,
                stokTersedia = 80,
                kodePindai = "kode-jajanan-002",
                deskripsi = "Gorengan bakwan sayur untuk jajanan harian.",
                kategori = "Makanan",
            ),
            produk(
                id = "produk-jajanan-risol-mayo",
                nama = "Risol Mayo",
                harga = 5_000,
                stokTersedia = 40,
                kodePindai = "kode-jajanan-003",
                deskripsi = "Risol isi mayo dan telur, cocok untuk kantin dan kedai.",
                kategori = "Makanan",
            ),
            produk(
                id = "produk-jajanan-martabak-mini",
                nama = "Martabak Mini",
                harga = 6_000,
                stokTersedia = 35,
                kodePindai = "kode-jajanan-004",
                deskripsi = "Martabak ukuran kecil dengan rasa manis.",
                kategori = "Makanan",
            ),
            produk(
                id = "produk-jajanan-klepon",
                nama = "Klepon",
                harga = 5_000,
                stokTersedia = 30,
                kodePindai = "kode-jajanan-005",
                deskripsi = "Jajanan pasar berisi gula merah dengan taburan kelapa.",
                kategori = "Makanan",
            ),
            produk(
                id = "produk-jajanan-lemper-ayam",
                nama = "Lemper Ayam",
                harga = 5_000,
                stokTersedia = 30,
                kodePindai = "kode-jajanan-006",
                deskripsi = "Ketan isi ayam suwir yang cocok untuk jajanan pagi.",
                kategori = "Makanan",
            ),
        )
    }

    private fun daftarKebutuhanHarian(): List<Produk> {
        return listOf(
            produk(
                id = "produk-harian-sabun-mandi-batang",
                nama = "Sabun Mandi Batang",
                harga = 5_000,
                stokTersedia = 50,
                kodePindai = "kode-harian-001",
                deskripsi = "Sabun mandi batang untuk kebutuhan harian.",
                kategori = "Kebutuhan Harian",
            ),
            produk(
                id = "produk-harian-sampo-saset",
                nama = "Sampo Saset",
                harga = 1_500,
                stokTersedia = 100,
                kodePindai = "kode-harian-002",
                deskripsi = "Sampo saset satu kali pakai untuk warung kecil.",
                kategori = "Kebutuhan Harian",
            ),
            produk(
                id = "produk-harian-pasta-gigi-kecil",
                nama = "Pasta Gigi Kecil",
                harga = 7_000,
                stokTersedia = 45,
                kodePindai = "kode-harian-003",
                deskripsi = "Pasta gigi ukuran kecil untuk kebutuhan rumah tangga.",
                kategori = "Kebutuhan Harian",
            ),
            produk(
                id = "produk-harian-tisu-wajah",
                nama = "Tisu Wajah",
                harga = 10_000,
                stokTersedia = 40,
                kodePindai = "kode-harian-004",
                deskripsi = "Tisu wajah kemasan kotak kecil.",
                kategori = "Kebutuhan Harian",
            ),
            produk(
                id = "produk-harian-deterjen-saset",
                nama = "Deterjen Saset",
                harga = 2_000,
                stokTersedia = 100,
                kodePindai = "kode-harian-005",
                deskripsi = "Deterjen saset untuk kebutuhan cuci harian.",
                kategori = "Kebutuhan Harian",
            ),
            produk(
                id = "produk-harian-obat-nyamuk-bakar",
                nama = "Obat Nyamuk Bakar",
                harga = 6_000,
                stokTersedia = 45,
                kodePindai = "kode-harian-006",
                deskripsi = "Obat nyamuk bakar kemasan kecil untuk warung harian.",
                kategori = "Kebutuhan Harian",
            ),
        )
    }

    private fun produk(
        id: String,
        nama: String,
        harga: Long,
        stokTersedia: Int,
        kodePindai: String?,
        deskripsi: String,
        kategori: String,
    ): Produk {
        return Produk(
            id = id,
            nama = nama,
            harga = harga,
            stokTersedia = stokTersedia,
            kodePindai = kodePindai,
            deskripsi = deskripsi,
            kategori = kategori,
            aktif = true,
        )
    }
}
