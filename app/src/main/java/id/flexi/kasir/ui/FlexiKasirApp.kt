package id.flexi.kasir.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import id.flexi.kasir.R
import id.flexi.kasir.ui.navigation.NavigasiFlexiKasirApp
import id.flexi.kasir.ui.theme.FlexiKasirTheme
import kotlinx.coroutines.delay

/**
 * Komposabel akar aplikasi.
 *
 * Tanggung jawab file ini:
 * - menerapkan tema aplikasi
 * - menyiapkan surface dasar
 * - memasang root navigation aplikasi
 */
@Composable
fun FlexiKasirApp() {
    var splashTampil by remember { mutableStateOf(true) }

    if (splashTampil) {
        SplashScreenKustom(
            setelahSplash = { splashTampil = false },
        )
    } else {
        FlexiKasirTheme(
            gunakanWarnaDinamis = false,
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                NavigasiFlexiKasirApp()
            }
        }
    }
}

/**
 * Layar splash kustom dengan logo dan teks Flexi Kasir.
 * Tidak menggunakan SplashScreen API agar teks tidak terpotong lingkaran.
 */
@Composable
private fun SplashScreenKustom(
    setelahSplash: () -> Unit,
) {
    LaunchedEffect(Unit) {
        delay(1500L)
        setelahSplash()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 360.dp)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(id = R.drawable.splash),
                contentDescription = "Flexi Kasir Logo",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp),
                contentScale = ContentScale.Fit,
            )
        }
    }
}
