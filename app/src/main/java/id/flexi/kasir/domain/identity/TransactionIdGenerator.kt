package id.flexi.kasir.domain.identity

import java.util.UUID

/**
 * Pembangkit identitas internal Transaction.
 *
 * Identitas Transaction memakai UUID agar stabil sebagai primary key lokal,
 * aman untuk relasi item Transaction, dan siap dipakai saat sinkronisasi
 * ke sumber data lain di masa depan.
 */
object TransactionIdGenerator {

    /**
     * Membuat identitas Transaction baru.
     *
     * @return Identitas unik Transaction dalam format UUID string.
     */
    fun buatIdentitasBaru(): String {
        return UUID.randomUUID().toString()
    }
}
