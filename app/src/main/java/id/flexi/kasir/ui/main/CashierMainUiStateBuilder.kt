package id.flexi.kasir.ui.main

import id.flexi.kasir.domain.util.cariProduk
import id.flexi.kasir.domain.util.sebagaiRupiah
import id.flexi.kasir.domain.usecase.HasilCalculateTotalPurchase
import id.flexi.kasir.domain.usecase.CalculateTotalPurchase
import id.flexi.kasir.domain.usecase.PurchaseTotalSummary
import id.flexi.kasir.domain.model.TaxRule
import id.flexi.kasir.domain.model.Meja
import id.flexi.kasir.domain.model.PaymentMethod
import id.flexi.kasir.domain.model.StoreSetting
import id.flexi.kasir.domain.model.StorePreference
import id.flexi.kasir.domain.model.Produk
import id.flexi.kasir.domain.model.TransactionCostBreakdown
import id.flexi.kasir.domain.model.SyncStatus
import id.flexi.kasir.domain.model.Transaction
import id.flexi.kasir.domain.model.Uang
import id.flexi.kasir.ui.SinkronMesinStatus
import id.flexi.kasir.data.auth.KategoriUrutanStore
import java.time.format.DateTimeFormatter

/**
 * Pembentuk model tampilan untuk layar utama kasir.
 *
 * File ini berada di layer antarmuka karena menghasilkan state presentasi
 * yang langsung dipakai oleh Compose.
 */
class BentukCashierMainUiState {
    private val CalculateTotalPurchase = CalculateTotalPurchase()

    /**
     * Membentuk [CashierMainUiState] berdasarkan status terkini.
     *
     * @param daftarProdukPenuh Koleksi seluruh produk yang tersedia di sistem.
     * @param TransactionStatus Data inti Transaction saat ini (keranjang dan sinkronisasi).
     * @param statusElemenLayar Status elemen visual dan interaksi pada layar.
     * @param kataKunciMentah Nilai teks asli dari input pencarian sebelum di-debounce.
     * @param kataKunciEfektif Nilai teks pencarian setelah di-debounce untuk memfilter katalog.
     * @param StorePreference Data preferensi toko (untuk metadata sinkronisasi).
     * @return Model tampilan yang siap dikonsumsi oleh komponen Jetpack Compose.
     */
    operator fun invoke(
        daftarProdukPenuh: List<Produk>,
        TransactionStatus: CashierMainTransactionState,
        statusElemenLayar: CashierMainElementState,
        kataKunciMentah: String,
        kataKunciEfektif: String,
        StorePreference: StorePreference,
        StoreSetting: StoreSetting = StoreSetting(),
        daftarPesananPending: List<Transaction> = emptyList(),
        daftarPesananDiproses: List<Transaction> = emptyList(),
        daftarMeja: List<Meja> = emptyList(),
        tabTransaksi: Int = 1,
        kategoriTerpilih: String = "",
        apakahPerluBukaKas: Boolean = false,
        sinkronMesinStatus: SinkronMesinStatus = SinkronMesinStatus(),
        urutanKategoriKustom: List<String> = emptyList(),
    ): CashierMainUiState {
        val daftarCartItem = TransactionStatus.daftarCartItem

        val daftarKategori = KategoriUrutanStore.urutkan(
            kategori = daftarProdukPenuh.map { it.kategori }
                .filter { it.isNotBlank() }
                .distinct(),
            urutanKustom = urutanKategoriKustom,
        )

        val produkSetelahTab = when (tabTransaksi) {
            0 -> emptyList()
            2 -> daftarProdukPenuh.filter { it.favorit }
            else -> daftarProdukPenuh
        }

        val daftarProdukTersaring = produkSetelahTab
            .cariProduk(kataKunciEfektif)
            .filter { produk ->
                when {
                    tabTransaksi == 2 -> true // Favorit: tanpa filter kategori
                    kataKunciEfektif.isNotBlank() -> true // Sedang mencari: tampilkan semua hasil
                    else -> kategoriTerpilih.isNotBlank() &&
                        produk.kategori.equals(kategoriTerpilih, ignoreCase = true) // Filter kategori
                }
            }

        val TaxRuleTampilan = if (StorePreference.basisPoinPajakDefault > 0) {
            TaxRule(
                nama = "PPN",
                basisPoin = StorePreference.basisPoinPajakDefault,
                aktif = true,
            )
        } else {
            TaxRule.NoTax
        }
        val biayaLayananTampilan = Uang.dariRupiah(StorePreference.biayaLayananDefault)

        val hasilCalculateTotalPurchase = CalculateTotalPurchase(
            daftarCartItem = daftarCartItem,
            biayaLayanan = biayaLayananTampilan,
            taxRule = TaxRuleTampilan,
        )

        val PaymentMethodCheckout = if (StoreSetting.PaymentMethodQrisAktif &&
            !StoreSetting.PaymentMethodTunaiAktif
        ) {
            PaymentMethod.Qris
        } else {
            PaymentMethod.Cash
        }
        val PurchaseTotalSummary = when (hasilCalculateTotalPurchase) {
            is HasilCalculateTotalPurchase.Berhasil -> hasilCalculateTotalPurchase.PurchaseTotalSummary
            is HasilCalculateTotalPurchase.Gagal -> PurchaseTotalSummary(
                daftarCartItemBersih = emptyList(),
                jumlahItem = 0,
                TransactionCostBreakdown = TransactionCostBreakdown(
                    subtotal = Uang.Nol,
                ),
                totalAkhir = Uang.Nol,
                kembalian = Uang.Nol,
            )
        }
        val jumlahItem = PurchaseTotalSummary.jumlahItem
        val TransactionCostBreakdown = PurchaseTotalSummary.TransactionCostBreakdown
        val totalAkhir = PurchaseTotalSummary.totalAkhir

        return CashierMainUiState(
            catalogDisplay = StoreSetting.catalogDisplay,
            statusBeranda = HomeStatus(
                namaAplikasi = StoreSetting.namaUsaha.ifBlank { "Flexi Kasir" },
                sloganAplikasi = StoreSetting.tagline.ifBlank { "Solusi Digital UMKM Modern" },
                jumlahProdukTersedia = daftarProdukPenuh.size,
                jumlahCartItem = jumlahItem,
                totalBelanjaSementara = TransactionCostBreakdown.subtotal.sebagaiRupiah(),
                syncStatus = TransactionStatus.syncStatus,
                labelAksiSinkronisasi = when (TransactionStatus.syncStatus) {
                    SyncStatus.Syncing -> "Memperbarui..."
                    else -> "Perbarui katalog"
                },
                aksiSinkronisasiAktif = TransactionStatus.syncStatus !is SyncStatus.Syncing,
                labelMetadataSinkronisasi = bentukLabelMetadataSinkronisasi(
                    syncStatus = TransactionStatus.syncStatus,
                    StorePreference = StorePreference,
                ),
            ),
            daftarProdukTersaring = daftarProdukTersaring,
            daftarCartItem = daftarCartItem,
            statusKeranjang = CartStatus(
                judul = if (daftarCartItem.isEmpty()) "Keranjang masih kosong" else "Keranjang aktif",
                deskripsi = if (daftarCartItem.isEmpty()) {
                    "Yuk, mulai Transaction dengan memilih produk favorit!"
                } else {
                    "Atur jumlah item sebelum lanjut ke Payment."
                },
                jumlahItem = "$jumlahItem item",
            ),
            ringkasanPayment = PaymentSummary(
                subtotal = TransactionCostBreakdown.subtotal.sebagaiRupiah(),
                potongan = TransactionCostBreakdown.potongan.sebagaiRupiah(),
                pajak = TransactionCostBreakdown.pajak.sebagaiRupiah(),
                totalAkhir = totalAkhir.sebagaiRupiah(),
                labelAksiUtama = if (jumlahItem > 0) "Bayar sekarang" else "Pilih produk",
                aksiUtamaAktif = jumlahItem > 0,
            ),
            statusKonfirmasiCheckout = CheckoutConfirmStatus(
                apakahTampil = statusElemenLayar.apakahDialogKonfirmasiCheckoutTampil && jumlahItem > 0,
                judul = "Konfirmasi Payment",
                deskripsi = "Bayar $jumlahItem item dengan total ${totalAkhir.sebagaiRupiah()} sekarang?",
                labelKonfirmasi = "Bayar sekarang",
                catatan = statusElemenLayar.catatanCheckout,
                paymentMethod = statusElemenLayar.paymentMethod,
                PaymentMethodTunaiTersedia = StoreSetting.PaymentMethodTunaiAktif,
                PaymentMethodQrisTersedia = StoreSetting.PaymentMethodQrisAktif,
                daftarMeja = daftarMeja,
                orderType = statusElemenLayar.orderType,
                mejaId = statusElemenLayar.mejaId,
                modeSimpan = statusElemenLayar.modeSimpan,
                apakahSedangMenggabungkan = statusElemenLayar.apakahSedangMenggabungkan,
                totalAkhirNilai = totalAkhir.nilaiRupiah,
                billLainDiMeja = if (statusElemenLayar.mejaId != null) {
                    daftarPesananPending.filter {
                        it.mejaId == statusElemenLayar.mejaId &&
                            it.id != statusElemenLayar.resumeTransactionId
                    }
                } else {
                    emptyList()
                },
            ),
            statusHasilCheckout = statusElemenLayar.statusHasilCheckout,
            kataKunciPencarian = kataKunciMentah,
            tampilkanAksiResetPencarian = kataKunciMentah.isNotBlank(),
            apakahRingkasanPaymentTampil = statusElemenLayar.apakahRingkasanPaymentTampil,
            daftarMeja = daftarMeja,
            daftarPesananPending = daftarPesananPending,
            daftarPesananDiproses = daftarPesananDiproses,
            apakahPendingOrdersPanelTampil = statusElemenLayar.apakahPendingOrdersPanelTampil,
            apakahAntrianPanelTampil = statusElemenLayar.apakahAntrianPanelTampil,
            tabTransaksi = tabTransaksi,
            daftarKategori = daftarKategori,
            kategoriTerpilih = kategoriTerpilih,
            produkUntukPilihVarian = statusElemenLayar.produkUntukPilihVarian,
            apakahPerluBukaKas = apakahPerluBukaKas,
            sinkronMesinStatus = sinkronMesinStatus,
        )
    }

    private fun bentukLabelMetadataSinkronisasi(
        syncStatus: SyncStatus,
        StorePreference: StorePreference,
    ): String {
        val waktuTerakhir = StorePreference.waktuSinkronisasiKatalogTerakhirEpochMili
        val pesanGagalTerakhir = StorePreference.pesanGagalSinkronisasiKatalogTerakhir
            ?.trim()
            ?.takeIf { pesan -> pesan.isNotBlank() }

        return when (syncStatus) {
            SyncStatus.Syncing -> "Sedang memperbarui katalog..."
            is SyncStatus.Gagal -> {
                pesanGagalTerakhir?.let {
                    "Gagal sinkron: $it"
                } ?: "Gagal sinkron. Katalog lokal tetap digunakan."
            }
            SyncStatus.Synced -> {
                if (waktuTerakhir == null) {
                    "Katalog baru saja diperbarui."
                } else {
                    "Terakhir diperbarui ${waktuTerakhir.sebagaiLabelWaktuSinkronisasi()}."
                }
            }
            SyncStatus.LocalChanges -> {
                when {
                    waktuTerakhir != null -> {
                        "Terakhir diperbarui ${waktuTerakhir.sebagaiLabelWaktuSinkronisasi()}."
                    }

                    pesanGagalTerakhir != null -> {
                        "Sinkronisasi terakhir gagal. Katalog lokal tetap digunakan."
                    }

                    else -> {
                        "Katalog lokal siap digunakan."
                    }
                }
            }
            SyncStatus.Never -> "Katalog belum pernah diperbarui dari server."
        }
    }

    private fun Long.sebagaiLabelWaktuSinkronisasi(): String {
        return java.time.Instant.ofEpochMilli(this)
            .atZone(java.time.ZoneId.systemDefault())
            .format(PEMBENTUK_FORMAT_WAKTU)
    }

    companion object {
        private val PEMBENTUK_FORMAT_WAKTU = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")
    }
}
