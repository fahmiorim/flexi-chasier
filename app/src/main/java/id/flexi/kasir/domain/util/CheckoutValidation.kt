package id.flexi.kasir.domain.util

import id.flexi.kasir.domain.model.CheckoutValidationReason
import id.flexi.kasir.domain.model.TaxRule
import id.flexi.kasir.domain.model.CheckoutValidationResult
import id.flexi.kasir.domain.model.CartItem
import id.flexi.kasir.domain.model.TransactionCostBreakdown
import id.flexi.kasir.domain.model.Uang

/**
 * Membersihkan daftar item keranjang sebelum checkout.
 *
 * Sanitasi di sini tidak mengubah jumlah atau harga karena itu aturan bisnis.
 * Fungsi ini hanya merapikan catatan item agar teks kosong tidak ikut disimpan.
 *
 * @return Daftar item keranjang yang sudah dinormalisasi.
 */
fun List<CartItem>.sanitasiDaftarCartItemUntukCheckout(): List<CartItem> {
    return map { CartItem ->
        CartItem.copy(
            catatan = CartItem.catatan
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
 * @param daftarCartItem Daftar item yang akan divalidasi.
 * @return Hasil validasi domain.
 */
fun validasiDaftarItemCheckout(
    daftarCartItem: List<CartItem>,
): CheckoutValidationResult {
    if (daftarCartItem.isEmpty()) {
        return CheckoutValidationResult.TidakSah(
            alasan = CheckoutValidationReason.KeranjangKosong,
        )
    }

    val itemDenganJumlahTidakValid = daftarCartItem.firstOrNull { CartItem ->
        CartItem.jumlah <= 0
    }

    if (itemDenganJumlahTidakValid != null) {
        return CheckoutValidationResult.TidakSah(
            alasan = CheckoutValidationReason.JumlahItemTidakValid(
                namaProduk = itemDenganJumlahTidakValid.produk.nama,
                jumlah = itemDenganJumlahTidakValid.jumlah,
            ),
        )
    }

    val itemProdukTidakAktif = daftarCartItem.firstOrNull { CartItem ->
        !CartItem.produk.aktif
    }

    if (itemProdukTidakAktif != null) {
        return CheckoutValidationResult.TidakSah(
            alasan = CheckoutValidationReason.ProdukTidakAktif(
                namaProduk = itemProdukTidakAktif.produk.nama,
            ),
        )
    }

    val itemStokTidakCukup = daftarCartItem.firstOrNull { CartItem ->
        CartItem.produk.apakahStokDiaktifkan && CartItem.jumlah > CartItem.produk.stokTersedia
    }

    if (itemStokTidakCukup != null) {
        return CheckoutValidationResult.TidakSah(
            alasan = CheckoutValidationReason.StokTidakCukup(
                namaProduk = itemStokTidakCukup.produk.nama,
                jumlahDiminta = itemStokTidakCukup.jumlah,
                stokTersedia = itemStokTidakCukup.produk.stokTersedia,
            ),
        )
    }

    return CheckoutValidationResult.Sah
}

/**
 * Memvalidasi checkout dengan aturan pajak aktif.
 *
 * @param daftarCartItem Daftar item yang akan dibeli.
 * @param uangDibayar Nominal uang dari pelanggan.
 * @param potongan Potongan harga.
 * @param biayaLayanan Biaya layanan.
 * @param TaxRule Aturan pajak yang dipakai.
 * @return Hasil validasi checkout.
 */
fun CheckoutValidation(
    daftarCartItem: List<CartItem>,
    uangDibayar: Uang,
    potongan: Uang = Uang.Nol,
    biayaLayanan: Uang = Uang.Nol,
    taxRule: TaxRule = TaxRule.NoTax,
): CheckoutValidationResult {
    val daftarCartItemBersih = daftarCartItem.sanitasiDaftarCartItemUntukCheckout()
    val hasilValidasiDaftarItem = validasiDaftarItemCheckout(daftarCartItemBersih)

    if (!hasilValidasiDaftarItem.apakahSah) {
        return hasilValidasiDaftarItem
    }

    val subtotal = daftarCartItemBersih.hitungSubtotalKeranjangUang()

    if (potongan.nilaiRupiah > subtotal.nilaiRupiah) {
        return CheckoutValidationResult.TidakSah(
            alasan = CheckoutValidationReason.PotonganMelebihiSubtotal,
        )
    }

    val totalAkhir = hitungTotalTransactionUang(
        daftarCartItem = daftarCartItemBersih,
        potongan = potongan,
        biayaLayanan = biayaLayanan,
        taxRule = taxRule,
    )

    if (uangDibayar.nilaiRupiah < totalAkhir.nilaiRupiah) {
        return CheckoutValidationResult.TidakSah(
            alasan = CheckoutValidationReason.UangDibayarKurang(
                totalAkhir = totalAkhir,
                uangDibayar = uangDibayar,
            ),
        )
    }

    return CheckoutValidationResult.Sah
}

/**
 * Memvalidasi checkout dengan pajak manual.
 *
 * Fungsi ini menjadi jembatan kompatibilitas untuk kode lama yang masih
 * mengirim nilai pajak langsung sebagai nominal Rupiah.
 *
 * @param daftarCartItem Daftar item yang akan dibeli.
 * @param uangDibayar Nominal uang dari pelanggan.
 * @param potongan Potongan harga.
 * @param biayaLayanan Biaya layanan.
 * @param pajak Pajak langsung dalam Rupiah.
 * @return Hasil validasi checkout.
 */
fun CheckoutValidationDenganPajakManual(
    daftarCartItem: List<CartItem>,
    uangDibayar: Uang,
    potongan: Uang = Uang.Nol,
    biayaLayanan: Uang = Uang.Nol,
    pajak: Uang = Uang.Nol,
): CheckoutValidationResult {
    val daftarCartItemBersih = daftarCartItem.sanitasiDaftarCartItemUntukCheckout()
    val hasilValidasiDaftarItem = validasiDaftarItemCheckout(daftarCartItemBersih)

    if (!hasilValidasiDaftarItem.apakahSah) {
        return hasilValidasiDaftarItem
    }

    val subtotal = daftarCartItemBersih.hitungSubtotalKeranjangUang()

    if (potongan.nilaiRupiah > subtotal.nilaiRupiah) {
        return CheckoutValidationResult.TidakSah(
            alasan = CheckoutValidationReason.PotonganMelebihiSubtotal,
        )
    }

    val TransactionCostBreakdown = TransactionCostBreakdown(
        subtotal = subtotal,
        potongan = potongan,
        biayaLayanan = biayaLayanan,
        pajak = pajak,
    )

    if (uangDibayar.nilaiRupiah < TransactionCostBreakdown.totalAkhir.nilaiRupiah) {
        return CheckoutValidationResult.TidakSah(
            alasan = CheckoutValidationReason.UangDibayarKurang(
                totalAkhir = TransactionCostBreakdown.totalAkhir,
                uangDibayar = uangDibayar,
            ),
        )
    }

    return CheckoutValidationResult.Sah
}
