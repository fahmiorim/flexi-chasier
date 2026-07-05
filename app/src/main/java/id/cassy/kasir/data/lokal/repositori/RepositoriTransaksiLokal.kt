package id.cassy.kasir.data.lokal.repositori

import androidx.room.withTransaction
import id.cassy.kasir.data.lokal.basisdata.BasisDataCassyKasir
import id.cassy.kasir.data.lokal.pemetaan.keDomain
import id.cassy.kasir.data.lokal.pemetaan.keLokal
import id.cassy.kasir.data.lokal.relasi.TransaksiDenganItemLokal
import id.cassy.kasir.ranah.model.StatusTransaksi
import id.cassy.kasir.ranah.model.Transaksi
import id.cassy.kasir.ranah.model.Uang
import id.cassy.kasir.ranah.repositori.RepositoriTransaksi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Implementasi repository transaksi berbasis Room.
 *
 * Class ini berada di layer data karena mengetahui detail database lokal.
 * Layer ranah hanya mengenal kontrak [RepositoriTransaksi].
 *
 * @property basisData Instance database lokal Cassy Kasir.
 */
class RepositoriTransaksiLokal(
    private val basisData: BasisDataCassyKasir,
) : RepositoriTransaksi {

    private val aksesDataTransaksi = basisData.aksesDataTransaksiLokal()
    private val aksesDataProduk = basisData.aksesDataProdukLokal()

    /**
     * Menyimpan transaksi baru beserta seluruh item terkait ke database secara atomik.
     *
     * @param transaksi Objek domain transaksi yang akan disimpan.
     */
    override suspend fun simpanTransaksi(
        transaksi: Transaksi,
    ) {
        simpanTransaksiTanpaMengubahStok(
            transaksi = transaksi,
        )
    }

    /**
     * Menyimpan transaksi dan mengurangi stok produk secara atomik.
     *
     * @param transaksi Transaksi yang akan dicatat.
     */
    override suspend fun simpanTransaksiDanKurangiStok(
        transaksi: Transaksi,
    ) {
        val daftarJumlahProduk = transaksi.daftarItemKeranjang
            .groupBy { itemKeranjang -> itemKeranjang.produk.id }
            .mapValues { (_, daftarItemKeranjang) ->
                daftarItemKeranjang.sumOf { itemKeranjang -> itemKeranjang.jumlah }
            }

        basisData.withTransaction {
            val daftarProdukLokal = aksesDataProduk
                .ambilProdukBerdasarkanDaftarIdentitas(
                    daftarIdentitasProduk = daftarJumlahProduk.keys.toList(),
                )
                .associateBy { produkLokal -> produkLokal.id }

            daftarJumlahProduk.forEach { (identitasProduk, jumlahDiminta) ->
                val produkLokal = daftarProdukLokal[identitasProduk]
                    ?: throw IllegalArgumentException("Produk tidak ditemukan.")

                if (!produkLokal.apakahAktif) {
                    throw IllegalArgumentException("Produk ${produkLokal.nama} sedang tidak aktif.")
                }

                if (produkLokal.stokTersedia < jumlahDiminta) {
                    throw IllegalArgumentException("Stok ${produkLokal.nama} tidak cukup.")
                }
            }

            simpanTransaksiTanpaMengubahStok(
                transaksi = transaksi,
            )

            daftarJumlahProduk.forEach { (identitasProduk, jumlahPengurang) ->
                val jumlahBarisBerubah = aksesDataProduk.kurangiStokJikaCukup(
                    identitasProduk = identitasProduk,
                    jumlahPengurang = jumlahPengurang,
                )

                if (jumlahBarisBerubah != 1) {
                    val namaProduk = daftarProdukLokal[identitasProduk]?.nama ?: "produk"
                    throw IllegalArgumentException("Stok $namaProduk tidak cukup.")
                }
            }
        }
    }

    private suspend fun simpanTransaksiTanpaMengubahStok(
        transaksi: Transaksi,
    ) {
        val entitasTransaksi = transaksi.keLokal()
        val daftarEntitasItem = transaksi.daftarItemKeranjang.map { itemKeranjang ->
            itemKeranjang.keLokal(transaksi.id)
        }

        aksesDataTransaksi.simpanTransaksiDenganItem(entitasTransaksi, daftarEntitasItem)
    }

    /**
     * Mengamati seluruh riwayat transaksi dari Room.
     *
     * @return Aliran daftar transaksi domain.
     */
    override fun amatiSemuaTransaksi(): Flow<List<Transaksi>> {
        return aksesDataTransaksi.amatiSemuaTransaksi().map { daftarLokal: List<TransaksiDenganItemLokal> ->
            daftarLokal.map { transaksiLokal ->
                transaksiLokal.keDomain()
            }
        }
    }

    /**
     * Mengamati satu transaksi berdasarkan identitas.
     *
     * @param identitasTransaksi Identitas unik transaksi.
     * @return Aliran transaksi domain, atau null jika tidak ditemukan.
     */
    override fun amatiTransaksiBerdasarkanIdentitas(
        identitasTransaksi: String,
    ): Flow<Transaksi?> {
        return aksesDataTransaksi.amatiTransaksiBerdasarkanId(identitasTransaksi).map { transaksiLokal ->
            transaksiLokal?.keDomain()
        }
    }

    /**
     * Mengambil rincian transaksi tunggal berdasarkan identitas unik.
     *
     * @param identitasTransaksi Identitas unik transaksi.
     * @return Objek domain transaksi jika ditemukan, null jika tidak.
     */
    override suspend fun ambilTransaksiBerdasarkanIdentitas(
        identitasTransaksi: String,
    ): Transaksi? {
        return aksesDataTransaksi.ambilTransaksiBerdasarkanId(identitasTransaksi)?.keDomain()
    }

    override fun amatiTransaksiPending(): Flow<List<Transaksi>> {
        return aksesDataTransaksi.amatiTransaksiPending().map { daftarLokal ->
            daftarLokal.map { it.keDomain() }
        }
    }

    override suspend fun hapusTransaksiDanKembalikanStok(
        identitasTransaksi: String,
    ) {
        val transaksi = aksesDataTransaksi.ambilTransaksiBerdasarkanId(identitasTransaksi)
            ?: return

        basisData.withTransaction {
            transaksi.daftarItem.forEach { item ->
                aksesDataProduk.tambahStok(
                    identitasProduk = item.produkId,
                    jumlahPenambah = item.jumlah,
                )
            }
            aksesDataTransaksi.hapusTransaksiBerdasarkanId(identitasTransaksi)
        }
    }

    override suspend fun perbaruiStatusDanPembayaranTransaksi(
        identitasTransaksi: String,
        status: StatusTransaksi,
        uangDibayar: Uang,
    ) {
        aksesDataTransaksi.perbaruiStatusDanUangDibayarTransaksi(
            id = identitasTransaksi,
            status = status.name,
            uangDibayar = uangDibayar.nilaiRupiah,
        )
    }
}
