package id.cassy.kasir.antarmuka.utama

import id.cassy.kasir.ranah.fungsi.cariProduk
import id.cassy.kasir.ranah.fungsi.sebagaiRupiah
import id.cassy.kasir.ranah.kasuspenggunaan.HasilHitungTotalBelanja
import id.cassy.kasir.ranah.kasuspenggunaan.HitungTotalBelanja
import id.cassy.kasir.ranah.kasuspenggunaan.RingkasanTotalBelanja
import id.cassy.kasir.ranah.model.AturanPajak
import id.cassy.kasir.ranah.model.Meja
import id.cassy.kasir.ranah.model.MetodeBayar
import id.cassy.kasir.ranah.model.PengaturanToko
import id.cassy.kasir.ranah.model.PreferensiToko
import id.cassy.kasir.ranah.model.Produk
import id.cassy.kasir.ranah.model.RincianBiayaTransaksi
import id.cassy.kasir.ranah.model.StatusSinkronisasi
import id.cassy.kasir.ranah.model.Transaksi
import id.cassy.kasir.ranah.model.Uang
import java.time.format.DateTimeFormatter

/**
 * Pembentuk model tampilan untuk layar utama kasir.
 *
 * File ini berada di layer antarmuka karena menghasilkan state presentasi
 * yang langsung dipakai oleh Compose.
 */
class BentukModelTampilanLayarUtamaKasir {
    private val hitungTotalBelanja = HitungTotalBelanja()

    /**
     * Membentuk [ModelTampilanLayarUtamaKasir] berdasarkan status terkini.
     *
     * @param daftarProdukPenuh Koleksi seluruh produk yang tersedia di sistem.
     * @param statusTransaksi Data inti transaksi saat ini (keranjang dan sinkronisasi).
     * @param statusElemenLayar Status elemen visual dan interaksi pada layar.
     * @param kataKunciMentah Nilai teks asli dari input pencarian sebelum di-debounce.
     * @param kataKunciEfektif Nilai teks pencarian setelah di-debounce untuk memfilter katalog.
     * @param preferensiToko Data preferensi toko (untuk metadata sinkronisasi).
     * @return Model tampilan yang siap dikonsumsi oleh komponen Jetpack Compose.
     */
    operator fun invoke(
        daftarProdukPenuh: List<Produk>,
        statusTransaksi: StatusTransaksiLayarUtamaKasir,
        statusElemenLayar: StatusElemenLayarUtamaKasir,
        kataKunciMentah: String,
        kataKunciEfektif: String,
        preferensiToko: PreferensiToko,
        pengaturanToko: PengaturanToko = PengaturanToko(),
        daftarPesananPending: List<Transaksi> = emptyList(),
        daftarMeja: List<Meja> = emptyList(),
    ): ModelTampilanLayarUtamaKasir {
        val daftarItemKeranjang = statusTransaksi.daftarItemKeranjang

        val daftarProdukTersaring = daftarProdukPenuh.cariProduk(kataKunciEfektif)

        val aturanPajakTampilan = if (preferensiToko.basisPoinPajakDefault > 0) {
            AturanPajak(
                nama = "PPN",
                basisPoin = preferensiToko.basisPoinPajakDefault,
                aktif = true,
            )
        } else {
            AturanPajak.TanpaPajak
        }
        val biayaLayananTampilan = Uang.dariRupiah(preferensiToko.biayaLayananDefault)

        val hasilHitungTotalBelanja = hitungTotalBelanja(
            daftarItemKeranjang = daftarItemKeranjang,
            biayaLayanan = biayaLayananTampilan,
            aturanPajak = aturanPajakTampilan,
        )

        val metodeBayarCheckout = if (pengaturanToko.metodeBayarQrisAktif &&
            !pengaturanToko.metodeBayarTunaiAktif
        ) {
            MetodeBayar.Qris
        } else {
            MetodeBayar.Tunai
        }
        val ringkasanTotalBelanja = when (hasilHitungTotalBelanja) {
            is HasilHitungTotalBelanja.Berhasil -> hasilHitungTotalBelanja.ringkasanTotalBelanja
            is HasilHitungTotalBelanja.Gagal -> RingkasanTotalBelanja(
                daftarItemKeranjangBersih = emptyList(),
                jumlahItem = 0,
                rincianBiayaTransaksi = RincianBiayaTransaksi(
                    subtotal = Uang.Nol,
                ),
                totalAkhir = Uang.Nol,
                kembalian = Uang.Nol,
            )
        }
        val jumlahItem = ringkasanTotalBelanja.jumlahItem
        val rincianBiayaTransaksi = ringkasanTotalBelanja.rincianBiayaTransaksi
        val totalAkhir = ringkasanTotalBelanja.totalAkhir

        return ModelTampilanLayarUtamaKasir(
            tampilanKatalog = pengaturanToko.tampilanKatalog,
            statusBeranda = StatusBerandaKasir(
                namaAplikasi = pengaturanToko.namaUsaha.ifBlank { "Cassy Kasir" },
                sloganAplikasi = "Solusi Digital UMKM Modern",
                jumlahProdukTersedia = daftarProdukPenuh.size,
                jumlahItemKeranjang = jumlahItem,
                totalBelanjaSementara = rincianBiayaTransaksi.subtotal.sebagaiRupiah(),
                statusSinkronisasi = statusTransaksi.statusSinkronisasi,
                labelAksiSinkronisasi = when (statusTransaksi.statusSinkronisasi) {
                    StatusSinkronisasi.SedangSinkron -> "Memperbarui..."
                    else -> "Perbarui katalog"
                },
                aksiSinkronisasiAktif = statusTransaksi.statusSinkronisasi !is StatusSinkronisasi.SedangSinkron,
                labelMetadataSinkronisasi = bentukLabelMetadataSinkronisasi(
                    statusSinkronisasi = statusTransaksi.statusSinkronisasi,
                    preferensiToko = preferensiToko,
                ),
            ),
            daftarProdukTersaring = daftarProdukTersaring,
            daftarItemKeranjang = daftarItemKeranjang,
            statusKeranjang = StatusKeranjangKasir(
                judul = if (daftarItemKeranjang.isEmpty()) "Keranjang masih kosong" else "Keranjang aktif",
                deskripsi = if (daftarItemKeranjang.isEmpty()) {
                    "Yuk, mulai transaksi dengan memilih produk favorit!"
                } else {
                    "Atur jumlah item sebelum lanjut ke pembayaran."
                },
                jumlahItem = "$jumlahItem item",
            ),
            ringkasanPembayaran = RingkasanPembayaranKasir(
                subtotal = rincianBiayaTransaksi.subtotal.sebagaiRupiah(),
                potongan = rincianBiayaTransaksi.potongan.sebagaiRupiah(),
                pajak = rincianBiayaTransaksi.pajak.sebagaiRupiah(),
                totalAkhir = totalAkhir.sebagaiRupiah(),
                labelAksiUtama = if (jumlahItem > 0) "Bayar sekarang" else "Pilih produk",
                aksiUtamaAktif = jumlahItem > 0,
            ),
            statusKonfirmasiCheckout = StatusKonfirmasiCheckoutKasir(
                apakahTampil = statusElemenLayar.apakahDialogKonfirmasiCheckoutTampil && jumlahItem > 0,
                judul = "Konfirmasi pembayaran",
                deskripsi = "Bayar $jumlahItem item dengan total ${totalAkhir.sebagaiRupiah()} sekarang?",
                labelKonfirmasi = "Bayar sekarang",
                catatan = statusElemenLayar.catatanCheckout,
                metodeBayar = metodeBayarCheckout,
                metodeBayarTunaiTersedia = pengaturanToko.metodeBayarTunaiAktif,
                metodeBayarQrisTersedia = pengaturanToko.metodeBayarQrisAktif,
                daftarMeja = daftarMeja,
                tipeOrder = statusElemenLayar.tipeOrder,
            ),
            statusHasilCheckout = statusElemenLayar.statusHasilCheckout,
            kataKunciPencarian = kataKunciMentah,
            tampilkanAksiResetPencarian = kataKunciMentah.isNotBlank(),
            apakahRingkasanPembayaranTampil = statusElemenLayar.apakahRingkasanPembayaranTampil,
            daftarPesananPending = daftarPesananPending,
            apakahPanelPesananPendingTampil = statusElemenLayar.apakahPanelPesananPendingTampil,
        )
    }

    private fun bentukLabelMetadataSinkronisasi(
        statusSinkronisasi: StatusSinkronisasi,
        preferensiToko: PreferensiToko,
    ): String {
        val waktuTerakhir = preferensiToko.waktuSinkronisasiKatalogTerakhirEpochMili
        val pesanGagalTerakhir = preferensiToko.pesanGagalSinkronisasiKatalogTerakhir
            ?.trim()
            ?.takeIf { pesan -> pesan.isNotBlank() }

        return when (statusSinkronisasi) {
            StatusSinkronisasi.SedangSinkron -> "Sedang memperbarui katalog..."
            is StatusSinkronisasi.Gagal -> {
                pesanGagalTerakhir?.let {
                    "Gagal sinkron: $it"
                } ?: "Gagal sinkron. Katalog lokal tetap digunakan."
            }
            StatusSinkronisasi.Berhasil -> {
                if (waktuTerakhir == null) {
                    "Katalog baru saja diperbarui."
                } else {
                    "Terakhir diperbarui ${waktuTerakhir.sebagaiLabelWaktuSinkronisasi()}."
                }
            }
            StatusSinkronisasi.SinkronLokal -> {
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
            StatusSinkronisasi.BelumPernah -> "Katalog belum pernah diperbarui dari server."
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
