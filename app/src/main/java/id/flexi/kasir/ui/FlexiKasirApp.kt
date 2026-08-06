package id.flexi.kasir.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
 *
 * Desain: gradien navy (selaras tema), logo monogram K + nota, tagline,
 * serta animasi masuk (fade + scale) dan progress bar emas.
 */
@Composable
private fun SplashScreenKustom(
    setelahSplash: () -> Unit,
) {
    var tampil by remember { mutableStateOf(false) }
    val alfa by animateFloatAsState(
        targetValue = if (tampil) 1f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "splashAlpha",
    )
    val skala by animateFloatAsState(
        targetValue = if (tampil) 1f else 0.85f,
        animationSpec = tween(durationMillis = 600),
        label = "splashScale",
    )

    LaunchedEffect(Unit) {
        tampil = true
        delay(1800L)
        setelahSplash()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF1E3A8A),
                        Color(0xFF17479E),
                        Color(0xFF0D47A1),
                    ),
                ),
            ),
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
            // Logo monogram K + nota (fade-in + scale)
            Image(
                painter = painterResource(id = R.drawable.logo_kasir_mark),
                contentDescription = "Flexi Kasir Logo",
                modifier = Modifier
                    .size(132.dp)
                    .alpha(alfa)
                    .scale(skala),
                contentScale = ContentScale.Fit,
            )

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Flexi Kasir",
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.alpha(alfa),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Kasir Digital • Multi Gerai • Sinkron Otomatis",
                color = Color(0xFFBFDBFE),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.alpha(alfa),
            )

            Spacer(modifier = Modifier.height(48.dp))

            CircularProgressIndicator(
                modifier = Modifier.size(26.dp),
                color = Color(0xFFF59E0B),
                strokeWidth = 3.dp,
            )
        }
    }
}
