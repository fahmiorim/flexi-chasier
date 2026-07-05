package id.cassy.kasir.antarmuka

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import id.cassy.kasir.antarmuka.navigasi.NavigasiAplikasiCassyKasir
import id.cassy.kasir.antarmuka.tema.TemaCassyKasir

/**
 * Komposabel akar aplikasi.
 *
 * Tanggung jawab file ini:
 * - menerapkan tema aplikasi
 * - menyiapkan surface dasar
 * - memasang root navigation aplikasi
 */
@Composable
fun AplikasiCassyKasir() {
    TemaCassyKasir(
        gunakanWarnaDinamis = false,
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            NavigasiAplikasiCassyKasir()
        }
    }
}
