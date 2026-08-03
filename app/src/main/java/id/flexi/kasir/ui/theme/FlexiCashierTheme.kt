package id.flexi.kasir.ui.theme

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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

// ── Premium Color Palette ──

private val PremiumBlue = Color(0xFF2563EB)
private val PremiumBlueDark = Color(0xFF1D4ED8)
private val PremiumSky = Color(0xFF0284C7)
private val PremiumTeal = Color(0xFF0D9488)
private val PremiumEmerald = Color(0xFF059669)
private val PremiumAmber = Color(0xFFD97706)
private val PremiumRose = Color(0xFFE11D48)
private val PremiumIndigo = Color(0xFF6366F1)
private val PremiumViolet = Color(0xFF7C3AED)

private val SkemaWarnaTerang = lightColorScheme(
    primary = PremiumBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = Color(0xFF1E3A8A),
    secondary = PremiumSky,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF0C4A6E),
    tertiary = PremiumTeal,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFCCFBF1),
    onTertiaryContainer = Color(0xFF134E4A),
    error = PremiumRose,
    onError = Color.White,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF7F1D1D),
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    surfaceTint = PremiumBlue,
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0),
    inverseSurface = Color(0xFF1E293B),
    inverseOnSurface = Color(0xFFF1F5F9),
    inversePrimary = Color(0xFF93C5FD),
    scrim = Color.Black,
)

private val SkemaWarnaGelap = darkColorScheme(
    primary = Color(0xFF60A5FA),
    onPrimary = Color(0xFF0F172A),
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = Color(0xFF38BDF8),
    onSecondary = Color(0xFF0C4A6E),
    secondaryContainer = Color(0xFF075985),
    onSecondaryContainer = Color(0xFFE0F2FE),
    tertiary = Color(0xFF2DD4BF),
    onTertiary = Color(0xFF134E4A),
    tertiaryContainer = Color(0xFF115E59),
    onTertiaryContainer = Color(0xFFCCFBF1),
    error = Color(0xFFF87171),
    onError = Color(0xFF7F1D1D),
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFEE2E2),
    background = Color(0xFF0F172A),
    onBackground = Color(0xFFF1F5F9),
    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFF94A3B8),
    surfaceTint = Color(0xFF60A5FA),
    outline = Color(0xFF475569),
    outlineVariant = Color(0xFF334155),
    inverseSurface = Color(0xFFF1F5F9),
    inverseOnSurface = Color(0xFF0F172A),
    inversePrimary = Color(0xFF2563EB),
    scrim = Color.Black,
)

// ── Typography ──

private val FlexiCashierTypography = Typography(
    displayLarge = TextStyle(
        fontSize = 34.sp, lineHeight = 40.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp,
    ),
    displayMedium = TextStyle(
        fontSize = 28.sp, lineHeight = 34.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.3).sp,
    ),
    displaySmall = TextStyle(
        fontSize = 24.sp, lineHeight = 30.sp, fontWeight = FontWeight.Bold,
    ),
    headlineLarge = TextStyle(
        fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.2).sp,
    ),
    headlineMedium = TextStyle(
        fontSize = 20.sp, lineHeight = 26.sp, fontWeight = FontWeight.Bold,
    ),
    headlineSmall = TextStyle(
        fontSize = 18.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold,
    ),
    titleLarge = TextStyle(
        fontSize = 17.sp, lineHeight = 22.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.2).sp,
    ),
    titleMedium = TextStyle(
        fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold,
    ),
    titleSmall = TextStyle(
        fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold,
    ),
    bodyLarge = TextStyle(
        fontSize = 15.sp, lineHeight = 22.sp, fontWeight = FontWeight.Normal,
    ),
    bodyMedium = TextStyle(
        fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Normal,
    ),
    bodySmall = TextStyle(
        fontSize = 11.sp, lineHeight = 16.sp, fontWeight = FontWeight.Normal,
    ),
    labelLarge = TextStyle(
        fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold,
    ),
    labelMedium = TextStyle(
        fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium,
    ),
    labelSmall = TextStyle(
        fontSize = 10.sp, lineHeight = 14.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.3.sp,
    ),
)

// ── Shapes ──

private val FlexiCashierShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(20.dp),
)

// ── Custom Color Tokens ──

object FlexiColors {
    val success get() = PremiumEmerald
    val warning get() = PremiumAmber
    val info get() = PremiumSky
    val danger get() = PremiumRose
    val chartBlue get() = PremiumBlue
    val chartPurple get() = PremiumViolet
    val chartTeal get() = PremiumTeal
    val chartAmber get() = PremiumAmber
    val chartIndigo get() = PremiumIndigo

    val successContainer get() = Color(0xFFD1FAE5)
    val warningContainer get() = Color(0xFFFEF3C7)
    val infoContainer get() = Color(0xFFE0F2FE)
    val dangerContainer get() = Color(0xFFFEE2E2)

    val successContainerDark get() = Color(0xFF064E3B)
    val warningContainerDark get() = Color(0xFF78350F)
    val infoContainerDark get() = Color(0xFF0C4A6E)
    val dangerContainerDark get() = Color(0xFF7F1D1D)
}

@Composable
fun FlexiCashierTheme(
    modeGelap: Boolean = isSystemInDarkTheme(),
    gunakanWarnaDinamis: Boolean = false,
    konten: @Composable () -> Unit,
) {
    val konteks = LocalContext.current
    val tampilan = LocalView.current

    val skemaWarna = when {
        gunakanWarnaDinamis && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (modeGelap) dynamicDarkColorScheme(konteks) else dynamicLightColorScheme(konteks)
        }
        modeGelap -> SkemaWarnaGelap
        else -> SkemaWarnaTerang
    }

    if (!tampilan.isInEditMode) {
        SideEffect {
            val aktivitas = tampilan.context as? Activity ?: return@SideEffect
            val jendela = aktivitas.window
            val pengontrol = WindowCompat.getInsetsController(jendela, tampilan)

            jendela.statusBarColor = if (modeGelap) {
                android.graphics.Color.parseColor("#0F172A")
            } else {
                android.graphics.Color.parseColor("#FFFFFF")
            }
            pengontrol.isAppearanceLightStatusBars = !modeGelap
        }
    }

    MaterialTheme(
        colorScheme = skemaWarna,
        typography = FlexiCashierTypography,
        shapes = FlexiCashierShapes,
        content = konten,
    )
}
