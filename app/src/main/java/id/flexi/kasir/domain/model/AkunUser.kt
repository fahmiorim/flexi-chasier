package id.flexi.kasir.domain.model

/**
 * Peran pengguna dalam sistem SaaS. Berpengaruh pada menu yang boleh diakses di aplikasi.
 */
enum class PeranAkun {
    Pemilik,
    Kasir,
}

/**
 * Gerai ringkas milik tenant, dipakai untuk memilih gerai aktif di layar login.
 */
data class GeraiSederhana(
    val id: String,
    val nama: String,
    val alamat: String? = null,
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
