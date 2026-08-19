package id.flexi.kasir.domain.model

/**
 * Peran pengguna dalam sistem SaaS. Berpengaruh pada menu yang boleh diakses di aplikasi.
 */
enum class PeranAkun {
    Pemilik,
    Kasir,
}

/**
 * Gerai milik tenant — berisi info umum + pengaturan.
 * Field dari server (auto-sync saat login).
 */
data class GeraiSederhana(
    val id: String,
    val nama: String,
    val alamat: String? = null,
    val tagline: String? = null,
    // Struk
    val headerStruk: String? = null,
    val footerStruk: String? = null,
    val ukuranKertas: String? = null,
    // Pajak & Biaya
    val tarifPajak: Double? = null,
    val biayaLayanan: Int? = null,
    // Pembayaran
    val metodePembayaran: String? = null,
    // Printer
    val printerNama: String? = null,
    val printerTipe: String? = null,
    val autoCetak: Boolean? = null,
    // Branding
    val logoUri: String? = null,
    val telepon: String? = null,
    val emailToko: String? = null,
    val instagram: String? = null,
    val whatsapp: String? = null,
)

/**
 * Sesi pengguna yang sedang login: data akun, daftar gerai yang boleh diakses,
 * dan gerai aktif yang dipilih untuk transaksi.
 */
data class AkunUser(
    val id: String,
    val nama: String,
    val email: String,
    val peran: PeranAkun,
    val daftarGerai: List<GeraiSederhana>,
    val geraiAktifId: String? = null,
) {
    val geraiAktif: GeraiSederhana?
        get() = daftarGerai.firstOrNull { it.id == geraiAktifId }

    val dapatMengelolaData: Boolean
        get() = peran == PeranAkun.Pemilik
}
