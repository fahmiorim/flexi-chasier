package id.cassy.kasir.antarmuka.tema

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

private val SkemaWarnaTerang = lightColorScheme(
    primary = Color(0xFF5D4037),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFF5E6D3),
    onPrimaryContainer = Color(0xFF2D1B14),
    secondary = Color(0xFFD4A373),
    onSecondary = Color(0xFF3E2723),
    secondaryContainer = Color(0xFFFFF0E0),
    onSecondaryContainer = Color(0xFF4E342E),
    tertiary = Color(0xFF8D6E63),
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFFFFFAF5),
    onBackground = Color(0xFF1C1B1D),
    surface = Color(0xFFFFFCF8),
    onSurface = Color(0xFF1C1B1D),
    surfaceVariant = Color(0xFFF5EDE8),
    onSurfaceVariant = Color(0xFF5D4037),
    outline = Color(0xFFC4A89B),
)

private val SkemaWarnaGelap = darkColorScheme(
    primary = Color(0xFFD4A373),
    onPrimary = Color(0xFF2D1B14),
    primaryContainer = Color(0xFF4E342E),
    onPrimaryContainer = Color(0xFFF5E6D3),
    secondary = Color(0xFFD4A373),
    onSecondary = Color(0xFF3E2723),
    secondaryContainer = Color(0xFF6D4C41),
    onSecondaryContainer = Color(0xFFFFF0E0),
    tertiary = Color(0xFFA1887F),
    onTertiary = Color(0xFF2D1B14),
    background = Color(0xFF12100E),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1A1614),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF3E352E),
    onSurfaceVariant = Color(0xFFD4C4BC),
    outline = Color(0xFF8D7E76),
)

/**
 * Kumpulan gaya tulisan (Tipografi) yang digunakan di seluruh aplikasi.
 * Ukuran dan ketebalan huruf diatur sedemikian rupa agar informasi penting seperti
 * nama produk dan total harga bisa terlihat dengan jelas.
 */
private val TipografiCassyKasir = Typography(
    headlineMedium = TextStyle(
        fontSize = 24.sp,
        lineHeight = 30.sp,
        fontWeight = FontWeight.SemiBold,
    ),
    titleLarge = TextStyle(
        fontSize = 20.sp,
        lineHeight = 26.sp,
        fontWeight = FontWeight.SemiBold,
    ),
    titleMedium = TextStyle(
        fontSize = 15.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.SemiBold,
    ),
    bodyLarge = TextStyle(
        fontSize = 13.sp,
        lineHeight = 18.sp,
        fontWeight = FontWeight.Normal,
    ),
    bodyMedium = TextStyle(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Normal,
    ),
    labelLarge = TextStyle(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Medium,
    ),
)

/**
 * Definisi bentuk lengkungan pada komponen seperti kartu dan tombol.
 * Kita menggunakan sudut yang agak membulat agar tampilan aplikasi terasa lebih ramah dan modern.
 */
private val BentukCassyKasir = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
)

/**
 * Fungsi utama untuk membungkus seluruh tampilan aplikasi dengan Tema Cassy Kasir.
 * Fungsi ini otomatis mengatur warna, tulisan, dan bentuk sesuai dengan mode yang dipilih.
 *
 * @param modeGelap Otomatis mengikuti pengaturan sistem, tapi bisa dipaksa manual jika perlu.
 * @param gunakanWarnaDinamis Fitur Android 12+ yang menyesuaikan warna aplikasi dengan wallpaper pengguna.
 * @param konten Isi tampilan aplikasi yang akan diberikan tema ini.
 */
@Composable
fun TemaCassyKasir(
    modeGelap: Boolean = isSystemInDarkTheme(),
    gunakanWarnaDinamis: Boolean = false,
    konten: @Composable () -> Unit,
) {
    val konteks = LocalContext.current
    val tampilan = LocalView.current

    // Tentukan skema warna yang paling pas untuk digunakan
    val skemaWarna = when {
        gunakanWarnaDinamis && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (modeGelap) dynamicDarkColorScheme(konteks) else dynamicLightColorScheme(konteks)
        }

        modeGelap -> SkemaWarnaGelap
        else -> SkemaWarnaTerang
    }

    // Mengatur agar ikon di status bar (baterai, jam, dll) tetap terlihat jelas sesuai tema
    if (!tampilan.isInEditMode) {
        SideEffect {
            val aktivitas = tampilan.context as? Activity ?: return@SideEffect
            val jendela = aktivitas.window
            // Jika mode terang, gunakan ikon gelap. Jika mode gelap, gunakan ikon terang.
            WindowCompat.getInsetsController(jendela, tampilan).isAppearanceLightStatusBars = !modeGelap
        }
    }

    MaterialTheme(
        colorScheme = skemaWarna,
        typography = TipografiCassyKasir,
        shapes = BentukCassyKasir,
        content = konten,
    )
}
