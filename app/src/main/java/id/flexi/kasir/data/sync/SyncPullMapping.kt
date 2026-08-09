package id.flexi.kasir.data.sync

import id.flexi.kasir.data.local.entity.LocalBahanEntity
import id.flexi.kasir.data.local.entity.LocalBahanResepEntity
import id.flexi.kasir.data.local.entity.LocalCashKasEntity
import id.flexi.kasir.data.local.entity.LocalCashMutationEntity
import id.flexi.kasir.data.local.entity.LocalMutasiRekeningEntity
import id.flexi.kasir.data.local.entity.LocalPembelianBahanEntity
import id.flexi.kasir.data.local.entity.LocalPenyesuaianStokEntity
import id.flexi.kasir.data.local.entity.LocalProductEntity
import id.flexi.kasir.data.local.entity.LocalResepEntity
import id.flexi.kasir.data.local.entity.LocalSetoranEntity
import id.flexi.kasir.data.local.entity.LocalTableEntity
import id.flexi.kasir.data.local.entity.LocalTransactionEntity
import id.flexi.kasir.data.local.entity.LocalTransactionItemEntity
import id.flexi.kasir.data.network.model.PengaturanTokoSinkron
import id.flexi.kasir.data.network.model.PerubahanResponse
import id.flexi.kasir.domain.model.StoreSetting

/**
 * Hasil pemetaan respons pull menjadi entitas lokal siap simpan +
 * daftar id yang harus dihapus (soft-delete server `dihapus = true`).
 */
data class PerubahanLokal(
    val produk: List<LocalProductEntity>,
    val produkDihapus: List<String>,
    val bahan: List<LocalBahanEntity>,
    val bahanDihapus: List<String>,
    val meja: List<LocalTableEntity>,
    val mejaDihapus: List<String>,
    val shift: List<LocalCashKasEntity>,
    val shiftDihapus: List<String>,
    val transaksi: List<LocalTransactionEntity>,
    val itemTransaksi: List<LocalTransactionItemEntity>,
    val transaksiDihapus: List<String>,
    val mutasi: List<LocalCashMutationEntity>,
    val mutasiDihapus: List<String>,
    val setoran: List<LocalSetoranEntity>,
    val setoranDihapus: List<String>,
    val pembelian: List<LocalPembelianBahanEntity>,
    val pembelianDihapus: List<String>,
    val resep: List<LocalResepEntity>,
    val resepBahan: List<LocalBahanResepEntity>,
    val resepDihapus: List<String>,
    val pengaturanToko: List<PengaturanTokoSinkron> = emptyList(),
    val penyesuaianStok: List<LocalPenyesuaianStokEntity> = emptyList(),
    val penyesuaianStokDihapus: List<String> = emptyList(),
    val mutasiRekening: List<LocalMutasiRekeningEntity> = emptyList(),
    val mutasiRekeningDihapus: List<String> = emptyList(),
)

/**
 * Memetakan respons pull `/api/sync/perubahan` menjadi entitas lokal (murni).
 *
 * Aturan:
 * - Item `dihapus = true` dari server TIDAK disimpan; id-nya masuk daftar
 *   `*Dihapus` agar baris lokal ikut dihapus.
 * - Item transaksi & bahan resep mengikuti induknya (dikirim terpisah oleh server).
 * - Seluruh tanggal epoch mili dipetakan langsung ke kolom lokal.
 */
fun PerubahanResponse.kePerubahanLokal(geraiId: String): PerubahanLokal {

    // ── Produk ──
    val daftarProdukAktif = products.filter { item -> !item.dihapus }
    val idProdukDihapus = products.filter { item -> item.dihapus }.map { item -> item.id }
    val hasilProduk: List<LocalProductEntity> = daftarProdukAktif.map { p ->
        LocalProductEntity(
            id = p.id,
            geraiId = geraiId,
            nama = p.nama,
            harga = p.harga,
            stokTersedia = p.stok,
            kodePindai = p.kodePindai,
            deskripsi = p.deskripsi ?: "",
            apakahAktif = p.aktif,
            kategori = p.kategori ?: "",
            fotoUri = p.fotoUri,
            favorit = p.favorit,
            hargaModal = null,
            varianJson = null,
            apakahStokDiaktifkan = true,
        )
    }

    // ── Bahan ──
    val daftarBahanAktif = bahan.filter { item -> !item.dihapus }
    val idBahanDihapus = bahan.filter { item -> item.dihapus }.map { item -> item.id }
    val hasilBahan: List<LocalBahanEntity> = daftarBahanAktif.map { b ->
        LocalBahanEntity(
            id = b.id,
            nama = b.nama,
            satuan = b.satuan,
            stokTersedia = b.stok.toDouble(),
            hargaPerSatuan = b.hargaBeli,
            stokMinimum = b.stokMinimum.toInt(),
            aktif = b.aktif,
            createdAt = 0L,
        )
    }

    // ── Meja ──
    val daftarMejaAktif = tables.filter { item -> !item.dihapus }
    val idMejaDihapus = tables.filter { item -> item.dihapus }.map { item -> item.id }
    val hasilMeja: List<LocalTableEntity> = daftarMejaAktif.map { t ->
        LocalTableEntity(
            id = t.id,
            nomor = t.nomor,
            aktif = t.aktif,
        )
    }

    // ── Shift kas ──
    val daftarShiftAktif = cashShifts.filter { item -> !item.dihapus }
    val idShiftDihapus = cashShifts.filter { item -> item.dihapus }.map { item -> item.id }
    val hasilShift: List<LocalCashKasEntity> = daftarShiftAktif.map { s ->
        LocalCashKasEntity(
            id = s.id,
            saldoAwal = s.saldoAwal,
            saldoAkhir = s.saldoAkhir,
            waktuBuka = s.waktuBukaEpochMili,
            waktuTutup = s.waktuTutupEpochMili,
            status = if (s.waktuTutupEpochMili != null) "Tutup" else "Buka",
            catatanBuka = s.catatanBuka,
            catatanTutup = s.catatanTutup,
        )
    }

    // ── Transaksi + item ──
    val daftarTransaksiAktif = transactions.filter { item -> !item.dihapus }
    val idTransaksiDihapus = transactions.filter { item -> item.dihapus }.map { item -> item.id }
    val hasilTransaksi: List<LocalTransactionEntity> = daftarTransaksiAktif.map { t ->
        LocalTransactionEntity(
            id = t.id,
            uangDibayar = t.dibayar,
            potongan = t.potongan,
            biayaLayanan = t.biayaLayanan,
            pajak = t.pajak,
            waktuTransactionEpochMili = t.waktuEpochMili,
            catatan = t.catatan,
            status = t.status,
            PaymentMethod = t.metodePembayaran,
            OrderType = t.orderType,
            nomorAntrian = null,
            mejaId = null,
            waktuDiprosesEpochMili = null,
            waktuSelesaiEpochMili = null,
            waktuDibayarEpochMili = if (t.dibatalkan) null else t.waktuEpochMili,
            dibatalkan = t.dibatalkan,
            alasanPembatalan = null,
        )
    }
    val itemPerTransaksi = transactionItems.groupBy { item -> item.transactionId }
    val hasilItemTransaksi: List<LocalTransactionItemEntity> = daftarTransaksiAktif.flatMap { t ->
        itemPerTransaksi[t.id].orEmpty().map { item ->
            LocalTransactionItemEntity(
                TransactionId = item.transactionId,
                produkId = item.productId,
                namaProduk = item.namaProduk,
                hargaProduk = item.hargaSatuan,
                jumlah = item.jumlah,
                catatanItem = null,
                kodePindai = null,
                deskripsiProduk = "",
                varianNama = null,
                apakahSelesai = false,
            )
        }
    }

    // ── Mutasi ──
    val daftarMutasiAktif = cashMutations.filter { item -> !item.dihapus }
    val idMutasiDihapus = cashMutations.filter { item -> item.dihapus }.map { item -> item.id }
    val hasilMutasi: List<LocalCashMutationEntity> = daftarMutasiAktif.map { m ->
        LocalCashMutationEntity(
            id = m.id,
            shiftId = m.shiftId,
            tipe = m.tipe,
            kategori = m.kategori,
            nominal = m.nominal,
            catatan = m.catatan ?: "",
            waktu = m.waktuEpochMili,
        )
    }

    // ── Setoran ──
    val daftarSetoranAktif = setoran.filter { item -> !item.dihapus }
    val idSetoranDihapus = setoran.filter { item -> item.dihapus }.map { item -> item.id }
    val hasilSetoran: List<LocalSetoranEntity> = daftarSetoranAktif.map { st ->
        LocalSetoranEntity(
            id = st.id,
            shiftId = st.shiftId,
            nominal = st.nominal,
            catatan = st.catatan ?: "",
            waktu = st.waktuEpochMili,
            dihapus = false,
        )
    }

    // ── Pembelian bahan ──
    val daftarPembelianAktif = pembelianBahan.filter { item -> !item.dihapus }
    val idPembelianDihapus = pembelianBahan.filter { item -> item.dihapus }.map { item -> item.id }
    val hasilPembelian: List<LocalPembelianBahanEntity> = daftarPembelianAktif.map { pb ->
        LocalPembelianBahanEntity(
            id = pb.id,
            bahanId = pb.bahanId,
            jumlah = pb.jumlah.toDouble(),
            satuanBeli = "pcs",
            totalHarga = pb.hargaTotal,
            tanggalBeli = pb.waktuEpochMili,
            catatan = null,
        )
    }

    // ── Resep + bahan resep ──
    val daftarResepAktif = resep.filter { item -> !item.dihapus }
    val idResepDihapus = resep.filter { item -> item.dihapus }.map { item -> item.id }
    val hasilResep: List<LocalResepEntity> = daftarResepAktif.map { r ->
        LocalResepEntity(
            id = r.id,
            produkId = r.productId,
            varianNama = null,
            createdAt = 0L,
        )
    }
    val bahanPerResep = resepBahan.groupBy { item -> item.resepId }
    val hasilResepBahan: List<LocalBahanResepEntity> = daftarResepAktif.flatMap { r ->
        bahanPerResep[r.id].orEmpty().map { rb ->
            LocalBahanResepEntity(
                resepId = rb.resepId,
                bahanId = rb.bahanId,
                jumlah = rb.jumlah.toDouble(),
                satuan = "gram",
            )
        }
    }

    // ── Penyesuaian stok ──
    val daftarPenyesuaianAktif = penyesuaianStok.filter { item -> !item.dihapus }
    val idPenyesuaianDihapus = penyesuaianStok.filter { item -> item.dihapus }.map { item -> item.id }
    val hasilPenyesuaian: List<LocalPenyesuaianStokEntity> = daftarPenyesuaianAktif.map { ps ->
        LocalPenyesuaianStokEntity(
            id = ps.id,
            jenis = ps.jenis,
            entitasId = ps.entitasId,
            namaEntitas = ps.namaEntitas ?: "",
            stokSebelum = ps.stokSebelum,
            stokSesudah = ps.stokSesudah,
            selisih = ps.selisih,
            alasan = ps.alasan ?: "",
            waktu = ps.waktuEpochMili,
        )
    }

    // ── Mutasi rekening ──
    val daftarMutasiRekeningAktif = mutasiRekening.filter { item -> !item.dihapus }
    val idMutasiRekeningDihapus = mutasiRekening.filter { item -> item.dihapus }.map { item -> item.id }
    val hasilMutasiRekening: List<LocalMutasiRekeningEntity> = daftarMutasiRekeningAktif.map { mr ->
        LocalMutasiRekeningEntity(
            id = mr.id,
            tipe = mr.tipe,
            nominal = mr.nominal,
            catatan = mr.catatan ?: "",
            waktu = mr.waktuEpochMili,
        )
    }

    return PerubahanLokal(
        produk = hasilProduk,
        produkDihapus = idProdukDihapus,
        bahan = hasilBahan,
        bahanDihapus = idBahanDihapus,
        meja = hasilMeja,
        mejaDihapus = idMejaDihapus,
        shift = hasilShift,
        shiftDihapus = idShiftDihapus,
        transaksi = hasilTransaksi,
        itemTransaksi = hasilItemTransaksi,
        transaksiDihapus = idTransaksiDihapus,
        mutasi = hasilMutasi,
        mutasiDihapus = idMutasiDihapus,
        setoran = hasilSetoran,
        setoranDihapus = idSetoranDihapus,
        pembelian = hasilPembelian,
        pembelianDihapus = idPembelianDihapus,
        resep = hasilResep,
        resepBahan = hasilResepBahan,
        resepDihapus = idResepDihapus,
        pengaturanToko = storeSettings,
        penyesuaianStok = hasilPenyesuaian,
        penyesuaianStokDihapus = idPenyesuaianDihapus,
        mutasiRekening = hasilMutasiRekening,
        mutasiRekeningDihapus = idMutasiRekeningDihapus,
    )
}

/**
 * Menggabungkan pengaturan toko dari server ke [StoreSetting] lokal saat ini.
 *
 * Hanya field yang dikirim server (nama usaha, alamat, tagline, logo) yang
 * ditimpa; field spesifik perangkat (printer, struk, tampilan, dsb.) tetap
 * dipertahankan agar pengaturan lokal tidak hilang saat pull.
 */
fun PengaturanTokoSinkron.terapkanKe(saatIni: StoreSetting): StoreSetting = saatIni.copy(
    namaUsaha = namaUsaha,
    alamat = alamat ?: saatIni.alamat,
    tagline = tagline ?: saatIni.tagline,
    logoUri = logoUri ?: saatIni.logoUri,
)
