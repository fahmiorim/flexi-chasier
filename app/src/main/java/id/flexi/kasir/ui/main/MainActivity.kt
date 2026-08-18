package id.flexi.kasir.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import id.flexi.kasir.ui.FlexiKasirApp

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
            // Minta izin Bluetooth saat pertama kali dibuka (Android 12+)
            val bluetoothPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                )
            } else {
                emptyArray()
            }

            val launcherPermission = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions(),
            ) { /* Hasil izin ditangani oleh ThermalPrinterManager via SecurityException */ }

            LaunchedEffect(Unit) {
                if (bluetoothPermissions.isNotEmpty()) {
                    val belumDiberikan = bluetoothPermissions.any { izin ->
                        ContextCompat.checkSelfPermission(this@MainActivity, izin) !=
                            PackageManager.PERMISSION_GRANTED
                    }
                    if (belumDiberikan) {
                        launcherPermission.launch(bluetoothPermissions)
                    }
                }
            }

            // Memulai fungsi komposabel utama aplikasi
            FlexiKasirApp()
        }
    }
}
