package id.flexi.kasir.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entitas database untuk mutasi rekening (saldo awal, pemasukan, penarikan).
 *
 * Satu akun per gerai; saldo akhir dihitung kumulatif:
 * saldoAkhir = Σ SaldoAwal + Σ Pemasukan − Σ Penarikan.
 * Mendukung offline-first: mutasi lokal tetap tersimpan meski belum ter-push.
 */
@Entity(tableName = "mutasi_rekening")
data class LocalMutasiRekeningEntity(
    @PrimaryKey
    val id: String,
    val tipe: String, // "SaldoAwal" | "Pemasukan" | "Penarikan"
    val nominal: Long,
    val catatan: String = "",
    val waktu: Long,
)
