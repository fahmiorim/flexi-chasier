package id.flexi.kasir.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entitas database untuk menyimpan data sesi kas.
 *
 * @property id Identitas unik sesi kas.
 * @property saldoAwal Uang modal awal di laci saat buka kas (dalam Rupiah).
 * @property saldoAkhir Saldo fisik akhir saat tutup kas (null jika belum tutup).
 * @property waktuBuka Epoch millis saat kas dibuka.
 * @property waktuTutup Epoch millis saat kas ditutup (null jika belum tutup).
 * @property status Status sesi ("Buka" atau "Tutup").
 * @property catatanBuka Catatan saat membuka kas.
 * @property catatanTutup Catatan saat menutup kas.
 */
@Entity(tableName = "shift_kas")
data class LocalCashKasEntity(
    @PrimaryKey
    val id: String,
    val saldoAwal: Long,
    val saldoAkhir: Long? = null,
    val waktuBuka: Long,
    val waktuTutup: Long? = null,
    val status: String = "Buka",
    val catatanBuka: String? = null,
    val catatanTutup: String? = null,
)
