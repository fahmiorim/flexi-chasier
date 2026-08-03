package id.flexi.kasir.domain.identity

import java.util.UUID

/**
 * Pembangkit identitas internal untuk sesi kas.
 */
object CashKasIdGenerator {

    fun buatIdentitasBaru(): String {
        return "kas-${UUID.randomUUID().toString().take(8)}"
    }

    fun buatIdentitasMutasiBaru(): String {
        return "mutasi-${UUID.randomUUID().toString().take(8)}"
    }

    fun buatIdentitasSetoranBaru(): String {
        return "setoran-${UUID.randomUUID().toString().take(8)}"
    }
}
