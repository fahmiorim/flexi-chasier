package id.flexi.kasir.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import id.flexi.kasir.ui.FlexiCashierApp

/**
 * Titik masuk utama aplikasi FlexiKasir.
 * Mengelola siklus hidup aktivitas utama dan menginisialisasi tampilan Compose.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Memasang Splash Screen sebelum setContentView / setContent
        installSplashScreen()

        // Mengaktifkan fitur Edge-to-Edge untuk tampilan yang lebih imersif dan modern
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            // Memulai fungsi komposabel utama aplikasi
            FlexiCashierApp()
        }
    }
}
