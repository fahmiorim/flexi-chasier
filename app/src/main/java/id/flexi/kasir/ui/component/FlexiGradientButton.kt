package id.flexi.kasir.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.flexi.kasir.ui.theme.FlexiGradients

/**
 * Tombol CTA utama dengan latar gradien brand dan animasi tekan halus.
 *
 * @param text Label tombol.
 * @param icon Ikon opsional di sisi kiri label.
 * @param trailingIcon Ikon opsional di sisi kanan label.
 * @param loading Jika true, spinner ditampilkan menggantikan ikon (label tetap).
 * @param fillWidth Jika true (default), tombol mengisi lebar penuh; jika false,
 *   ukurannya menyesuaikan isi (cocok di dalam Row/dialog).
 * @param gradient Gradien opsional (default [FlexiGradients.heroDalam]).
 * @param contentColor Warna teks/ikon (default [onPrimary]).
 */
@Composable
fun FlexiGradientButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: String,
    icon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    loading: Boolean = false,
    fillWidth: Boolean = true,
    gradient: Brush? = null,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
) {
    val brush = gradient ?: FlexiGradients.heroDalam()
    val interactionSource = remember { MutableInteractionSource() }
    val ditekan by interactionSource.collectIsPressedAsState()
    val skala by animateFloatAsState(
        targetValue = if (ditekan) 0.97f else 1f,
        animationSpec = tween(durationMillis = 120),
        label = "skalaTombol",
    )
    val shape = RoundedCornerShape(14.dp)
    val warnaKonten = if (enabled) contentColor else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        modifier = modifier
            .then(if (fillWidth) Modifier.fillMaxWidth() else Modifier)
            .graphicsLayer { this.scaleX = skala; this.scaleY = skala },
        shape = shape,
        color = Color.Transparent,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .then(
                    if (enabled) {
                        Modifier.background(brush)
                    } else {
                        Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                    },
                )
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = warnaKonten,
                        strokeWidth = 2.dp,
                    )
                } else if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = warnaKonten,
                    )
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = warnaKonten,
                )
                if (!loading && trailingIcon != null) {
                    Icon(
                        imageVector = trailingIcon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = warnaKonten,
                    )
                }
            }
        }
    }
}
