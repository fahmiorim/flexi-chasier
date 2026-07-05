package id.cassy.kasir.ranah.fungsi

import id.cassy.kasir.ranah.model.AlasanValidasiCheckout
import id.cassy.kasir.ranah.model.AturanPajak
import id.cassy.kasir.ranah.model.HasilValidasiCheckout
import id.cassy.kasir.ranah.model.ItemKeranjang
import id.cassy.kasir.ranah.model.RincianBiayaTransaksi
import id.cassy.kasir.ranah.model.Uang

/**
 * Membersihkan daftar item keranjang sebelum checkout.
 *
 * Sanitasi di sini tidak mengubah jumlah atau harga karena itu aturan bisnis.
 * Fungsi ini hanya merapikan catatan item agar teks kosong tidak ikut disimpan.
 *
 * @return Daftar item keranjang yang sudah dinormalisasi.
 */
fun List<ItemKeranjang>.sanitasiDaftarItemKeranjangUntukCheckout(): List<ItemKeranjang> {
    return map { itemKeranjang ->
        itemKeranjang.copy(
            catatan = itemKeranjang.catatan
                ?.trim()
                ?.takeIf { catatan -> catatan.isNotBlank() },
        )
    }
}

/**
 * Memvalidasi kelayakan dasar daftar item checkout.
 *
 * Validasi ini tidak menghitung uang sehingga aman dipanggil sebelum subtotal
 * dibentuk. Ini penting agar jumlah negatif tidak sempat masuk ke kalkulasi uang.
 *
 * @param daftarItemKeranjang Daftar item yang akan divalidasi.
 * @return Hasil validasi domain.
 */
fun validasiDaftarItemCheckout(
    daftarItemKeranjang: List<ItemKeranjang>,
): HasilValidasiCheckout {
    if (daftarItemKeranjang.isEmpty()) {
        return HasilValidasiCheckout.TidakSah(
            alasan = AlasanValidasiCheckout.KeranjangKosong,
        )
    }

    val itemDenganJumlahTidakValid = daftarItemKeranjang.firstOrNull { itemKeranjang ->
        itemKeranjang.jumlah <= 0
    }

    if (itemDenganJumlahTidakValid != null) {
        return HasilValidasiCheckout.TidakSah(
            alasan = AlasanValidasiCheckout.JumlahItemTidakValid(
                namaProduk = itemDenganJumlahTidakValid.produk.nama,
                jumlah = itemDenganJumlahTidakValid.jumlah,
            ),
        )
    }

    val itemProdukTidakAktif = daftarItemKeranjang.firstOrNull { itemKeranjang ->
        !itemKeranjang.produk.aktif
    }

    if (itemProdukTidakAktif != null) {
        return HasilValidasiCheckout.TidakSah(
            alasan = AlasanValidasiCheckout.ProdukTidakAktif(
                namaProduk = itemProdukTidakAktif.produk.nama,
            ),
        )
    }

    val itemStokTidakCukup = daftarItemKeranjang.firstOrNull { itemKeranjang ->
        itemKeranjang.jumlah > itemKeranjang.produk.stokTersedia
    }

    if (itemStokTidakCukup != null) {
        return HasilValidasiCheckout.TidakSah(
            alasan = AlasanValidasiCheckout.StokTidakCukup(
                namaProduk = itemStokTidakCukup.produk.nama,
                jumlahDiminta = itemStokTidakCukup.jumlah,
                stokTersedia = itemStokTidakCukup.produk.stokTersedia,
            ),
        )
    }

    return HasilValidasiCheckout.Sah
}

/**
 * Memvalidasi checkout dengan aturan pajak aktif.
 *
 * @param daftarItemKeranjang Daftar item yang akan dibeli.
 * @param uangDibayar Nominal uang dari pelanggan.
 * @param potongan Potongan harga.
 * @param biayaLayanan Biaya layanan.
 * @param aturanPajak Aturan pajak yang dipakai.
 * @return Hasil validasi checkout.
 */
fun validasiCheckout(
    daftarItemKeranjang: List<ItemKeranjang>,
    uangDibayar: Uang,
    potongan: Uang = Uang.Nol,
    biayaLayanan: Uang = Uang.Nol,
    aturanPajak: AturanPajak = AturanPajak.TanpaPajak,
): HasilValidasiCheckout {
    val daftarItemKeranjangBersih = daftarItemKeranjang.sanitasiDaftarItemKeranjangUntukCheckout()
    val hasilValidasiDaftarItem = validasiDaftarItemCheckout(daftarItemKeranjangBersih)

    if (!hasilValidasiDaftarItem.apakahSah) {
        return hasilValidasiDaftarItem
    }

    val subtotal = daftarItemKeranjangBersih.hitungSubtotalKeranjangUang()

    if (potongan.nilaiRupiah > subtotal.nilaiRupiah) {
        return HasilValidasiCheckout.TidakSah(
            alasan = AlasanValidasiCheckout.PotonganMelebihiSubtotal,
        )
    }

    val totalAkhir = hitungTotalTransaksiUang(
        daftarItemKeranjang = daftarItemKeranjangBersih,
        potongan = potongan,
        biayaLayanan = biayaLayanan,
        aturanPajak = aturanPajak,
    )

    if (uangDibayar.nilaiRupiah < totalAkhir.nilaiRupiah) {
        return HasilValidasiCheckout.TidakSah(
            alasan = AlasanValidasiCheckout.UangDibayarKurang(
                totalAkhir = totalAkhir,
                uangDibayar = uangDibayar,
            ),
        )
    }

    return HasilValidasiCheckout.Sah
}

/**
 * Memvalidasi checkout dengan pajak manual.
 *
 * Fungsi ini menjadi jembatan kompatibilitas untuk kode lama yang masih
 * mengirim nilai pajak langsung sebagai nominal Rupiah.
 *
 * @param daftarItemKeranjang Daftar item yang akan dibeli.
 * @param uangDibayar Nominal uang dari pelanggan.
 * @param potongan Potongan harga.
 * @param biayaLayanan Biaya layanan.
 * @param pajak Pajak langsung dalam Rupiah.
 * @return Hasil validasi checkout.
 */
fun validasiCheckoutDenganPajakManual(
    daftarItemKeranjang: List<ItemKeranjang>,
    uangDibayar: Uang,
    potongan: Uang = Uang.Nol,
    biayaLayanan: Uang = Uang.Nol,
    pajak: Uang = Uang.Nol,
): HasilValidasiCheckout {
    val daftarItemKeranjangBersih = daftarItemKeranjang.sanitasiDaftarItemKeranjangUntukCheckout()
    val hasilValidasiDaftarItem = validasiDaftarItemCheckout(daftarItemKeranjangBersih)

    if (!hasilValidasiDaftarItem.apakahSah) {
        return hasilValidasiDaftarItem
    }

    val subtotal = daftarItemKeranjangBersih.hitungSubtotalKeranjangUang()

    if (potongan.nilaiRupiah > subtotal.nilaiRupiah) {
        return HasilValidasiCheckout.TidakSah(
            alasan = AlasanValidasiCheckout.PotonganMelebihiSubtotal,
        )
    }

    val rincianBiayaTransaksi = RincianBiayaTransaksi(
        subtotal = subtotal,
        potongan = potongan,
        biayaLayanan = biayaLayanan,
        pajak = pajak,
    )

    if (uangDibayar.nilaiRupiah < rincianBiayaTransaksi.totalAkhir.nilaiRupiah) {
        return HasilValidasiCheckout.TidakSah(
            alasan = AlasanValidasiCheckout.UangDibayarKurang(
                totalAkhir = rincianBiayaTransaksi.totalAkhir,
                uangDibayar = uangDibayar,
            ),
        )
    }

    return HasilValidasiCheckout.Sah
}
