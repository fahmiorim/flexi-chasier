package id.flexi.kasir.data.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Model jaringan untuk protokol sinkronisasi (push & pull).
 *
 * Bentuk field PERSIS mengikuti kontrak backend `flexi-chasier-server`
 * (lihat rute sinkronisasi di backend):
 * - push: POST `/api/sync/<entitas>` dengan body `{ geraiId, items: [...] }`
 * - pull: GET `/api/sync/perubahan?geraiId=&kursor=<peta keyset per entitas>&batas=`
 *
 * `versi` wajib untuk setiap item (aturan last-write-wins di server).
 * Seluruh tanggal memakai epoch mili.
 */

// ── Item sinkronisasi (dipakai push & pull) ──

@Serializable
data class ProdukSinkron(
    val id: String,
    val versi: Long,
    val nama: String,
    val harga: Long,
    val stok: Int,
    val kategori: String? = null,
    val deskripsi: String? = null,
    @SerialName("fotoUri")
    val fotoUri: String? = null,
    val favorit: Boolean = false,
    val aktif: Boolean = true,
    val dihapus: Boolean = false,
)

@Serializable
data class ItemTransaksiSinkron(
    val id: String,
    val versi: Long,
    @SerialName("transactionId")
    val transactionId: String,
    @SerialName("productId")
    val productId: String,
    @SerialName("namaProduk")
    val namaProduk: String,
    @SerialName("hargaSatuan")
    val hargaSatuan: Long,
    val jumlah: Int,
    val subtotal: Long,
)

@Serializable
data class TransaksiSinkron(
    val id: String,
    val versi: Long,
    val nomor: String,
    @SerialName("waktuEpochMili")
    val waktuEpochMili: Long,
    @SerialName("metodePembayaran")
    val metodePembayaran: String, // "Cash" | "Qris"
    @SerialName("jumlahItem")
    val jumlahItem: Int,
    val total: Long,
    val dibayar: Long,
    val kembalian: Long,
    val potongan: Long = 0L,
    @SerialName("biayaLayanan")
    val biayaLayanan: Long = 0L,
    val pajak: Long = 0L,
    val status: String = "Paid",
    @SerialName("orderType")
    val orderType: String = "DineIn",
    val catatan: String? = null,
    val dibatalkan: Boolean = false,
    @SerialName("dibuatOleh")
    val dibuatOleh: String? = null,
    val dihapus: Boolean = false,
    val items: List<ItemTransaksiSinkron> = emptyList(),
)

@Serializable
data class MejaSinkron(
    val id: String,
    val versi: Long,
    val nomor: String,
    val aktif: Boolean = true,
    val dihapus: Boolean = false,
)

@Serializable
data class ShiftKasSinkron(
    val id: String,
    val versi: Long,
    @SerialName("waktuBukaEpochMili")
    val waktuBukaEpochMili: Long,
    @SerialName("waktuTutupEpochMili")
    val waktuTutupEpochMili: Long? = null,
    @SerialName("saldoAwal")
    val saldoAwal: Long,
    @SerialName("saldoAkhir")
    val saldoAkhir: Long? = null,
    @SerialName("catatanBuka")
    val catatanBuka: String? = null,
    @SerialName("catatanTutup")
    val catatanTutup: String? = null,
    @SerialName("dibuatOleh")
    val dibuatOleh: String? = null,
    val dihapus: Boolean = false,
)

@Serializable
data class MutasiKasSinkron(
    val id: String,
    val versi: Long,
    @SerialName("shiftId")
    val shiftId: String,
    val tipe: String, // "Pemasukan" | "Pengeluaran"
    val kategori: String,
    val nominal: Long,
    val catatan: String? = null,
    @SerialName("waktuEpochMili")
    val waktuEpochMili: Long,
    val dihapus: Boolean = false,
)

@Serializable
data class SetoranSinkron(
    val id: String,
    val versi: Long,
    @SerialName("shiftId")
    val shiftId: String,
    val nominal: Long,
    val catatan: String? = null,
    @SerialName("waktuEpochMili")
    val waktuEpochMili: Long,
    val dihapus: Boolean = false,
)

@Serializable
data class BahanSinkron(
    val id: String,
    val versi: Long,
    val nama: String,
    val satuan: String,
    val stok: Int,
    @SerialName("hargaBeli")
    val hargaBeli: Long,
    @SerialName("stokMinimum")
    val stokMinimum: Long = 0,
    val aktif: Boolean = true,
    val dihapus: Boolean = false,
)

@Serializable
data class PembelianBahanSinkron(
    val id: String,
    val versi: Long,
    @SerialName("bahanId")
    val bahanId: String,
    @SerialName("namaBahan")
    val namaBahan: String,
    val jumlah: Int,
    @SerialName("hargaTotal")
    val hargaTotal: Long,
    @SerialName("waktuEpochMili")
    val waktuEpochMili: Long,
    val dihapus: Boolean = false,
)

@Serializable
data class BahanResepSinkron(
    val id: String,
    val versi: Long,
    @SerialName("resepId")
    val resepId: String,
    @SerialName("bahanId")
    val bahanId: String,
    @SerialName("namaBahan")
    val namaBahan: String,
    val jumlah: Int,
)

@Serializable
data class ResepSinkron(
    val id: String,
    val versi: Long,
    @SerialName("productId")
    val productId: String,
    @SerialName("namaProduk")
    val namaProduk: String,
    val dihapus: Boolean = false,
    val bahan: List<BahanResepSinkron> = emptyList(),
)

@Serializable
data class PengaturanTokoSinkron(
    val id: String,
    val versi: Long,
    @SerialName("namaUsaha")
    val namaUsaha: String,
    val alamat: String? = null,
    val tagline: String? = null,
    @SerialName("logoUri")
    val logoUri: String? = null,
)

/**
 * Riwayat penyesuaian/reset stok (bahan maupun produk) lintas perangkat.
 *
 * Bentuk field PERSIS kontrak backend `POST /api/sync/penyesuaian-stok` dan
 * bagian `penyesuaianStok` pada `GET /api/sync/perubahan`.
 */
@Serializable
data class PenyesuaianStokSinkron(
    val id: String,
    val versi: Long,
    val jenis: String, // "Bahan" | "Produk"
    @SerialName("entitasId")
    val entitasId: String,
    @SerialName("namaEntitas")
    val namaEntitas: String? = null,
    @SerialName("stokSebelum")
    val stokSebelum: Int,
    @SerialName("stokSesudah")
    val stokSesudah: Int,
    val selisih: Int,
    val alasan: String? = null,
    @SerialName("waktuEpochMili")
    val waktuEpochMili: Long,
    val dihapus: Boolean = false,
)

/**
 * Mutasi rekening (saldo awal, pemasukan, penarikan) lintas perangkat.
 *
 * Bentuk field PERSIS kontrak backend `POST /api/sync/mutasi-rekening` dan
 * bagian `mutasiRekening` pada `GET /api/sync/perubahan`.
 */
@Serializable
data class MutasiRekeningSinkron(
    val id: String,
    val versi: Long,
    val tipe: String, // "SaldoAwal" | "Pemasukan" | "Penarikan"
    val nominal: Long,
    val catatan: String? = null,
    @SerialName("waktuEpochMili")
    val waktuEpochMili: Long,
    val dihapus: Boolean = false,
)

// ── Envelope push (body request) ──

@Serializable
data class PushProdukRequest(
    @SerialName("geraiId")
    val geraiId: String,
    val items: List<ProdukSinkron>,
)

@Serializable
data class PushTransaksiRequest(
    @SerialName("geraiId")
    val geraiId: String,
    val items: List<TransaksiSinkron>,
)

@Serializable
data class PushMejaRequest(
    @SerialName("geraiId")
    val geraiId: String,
    val items: List<MejaSinkron>,
)

@Serializable
data class PushShiftKasRequest(
    @SerialName("geraiId")
    val geraiId: String,
    val items: List<ShiftKasSinkron>,
)

@Serializable
data class PushMutasiKasRequest(
    @SerialName("geraiId")
    val geraiId: String,
    val items: List<MutasiKasSinkron>,
)

@Serializable
data class PushSetoranRequest(
    @SerialName("geraiId")
    val geraiId: String,
    val items: List<SetoranSinkron>,
)

@Serializable
data class PushBahanRequest(
    @SerialName("geraiId")
    val geraiId: String,
    val items: List<BahanSinkron>,
)

@Serializable
data class PushPembelianBahanRequest(
    @SerialName("geraiId")
    val geraiId: String,
    val items: List<PembelianBahanSinkron>,
)

@Serializable
data class PushResepRequest(
    @SerialName("geraiId")
    val geraiId: String,
    val items: List<ResepSinkron>,
)

@Serializable
data class PushPengaturanTokoRequest(
    @SerialName("geraiId")
    val geraiId: String,
    val items: List<PengaturanTokoSinkron>,
)

@Serializable
data class PushPenyesuaianStokRequest(
    @SerialName("geraiId")
    val geraiId: String,
    val items: List<PenyesuaianStokSinkron>,
)

@Serializable
data class PushMutasiRekeningRequest(
    @SerialName("geraiId")
    val geraiId: String,
    val items: List<MutasiRekeningSinkron>,
)

/** Respons push: jumlah item yang diterima server (LWW) vs total yang dikirim. */
@Serializable
data class PushResponse(
    val diterima: Int,
    val total: Int,
)

// ── Respons pull: semua entitas + kursor per entitas + flag terpotong ──

@Serializable
data class PerubahanResponse(
    val terpotong: Boolean = false,
    /**
     * Kursor keyset per entitas (format "<epochMili>:<id>"). Klien memakainya
     * sebagai posisi tarik berikutnya, sehingga baris yang belum terkirim
     * (batch terpotong) tidak pernah terlewat — berbeda dari kursor waktu
     * server lama (`waktuServerEpochMili`) yang bisa melompati data.
     */
    @SerialName("kursorBaru")
    val kursorBaru: Map<String, String> = emptyMap(),
    val products: List<ProdukSinkron> = emptyList(),
    val transactions: List<TransaksiSinkron> = emptyList(),
    @SerialName("transactionItems")
    val transactionItems: List<ItemTransaksiSinkron> = emptyList(),
    val tables: List<MejaSinkron> = emptyList(),
    @SerialName("cashShifts")
    val cashShifts: List<ShiftKasSinkron> = emptyList(),
    @SerialName("cashMutations")
    val cashMutations: List<MutasiKasSinkron> = emptyList(),
    val setoran: List<SetoranSinkron> = emptyList(),
    val bahan: List<BahanSinkron> = emptyList(),
    @SerialName("pembelianBahan")
    val pembelianBahan: List<PembelianBahanSinkron> = emptyList(),
    val resep: List<ResepSinkron> = emptyList(),
    @SerialName("resepBahan")
    val resepBahan: List<BahanResepSinkron> = emptyList(),
    @SerialName("storeSettings")
    val storeSettings: List<PengaturanTokoSinkron> = emptyList(),
    @SerialName("penyesuaianStok")
    val penyesuaianStok: List<PenyesuaianStokSinkron> = emptyList(),
    @SerialName("mutasiRekening")
    val mutasiRekening: List<MutasiRekeningSinkron> = emptyList(),
)
