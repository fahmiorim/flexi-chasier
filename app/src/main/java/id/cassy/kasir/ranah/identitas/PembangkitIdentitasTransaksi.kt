package id.cassy.kasir.ranah.identitas

import java.util.UUID

/**
 * Pembangkit identitas internal transaksi.
 *
 * Identitas transaksi memakai UUID agar stabil sebagai primary key lokal,
 * aman untuk relasi item transaksi, dan siap dipakai saat sinkronisasi
 * ke sumber data lain di masa depan.
 */
object PembangkitIdentitasTransaksi {

    /**
     * Membuat identitas transaksi baru.
     *
     * @return Identitas unik transaksi dalam format UUID string.
     */
    fun buatIdentitasBaru(): String {
        return UUID.randomUUID().toString()
    }
}
