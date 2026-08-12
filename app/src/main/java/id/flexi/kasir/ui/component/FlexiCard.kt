package id.flexi.kasir.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

/**
 * Premium card dengan border, shadow, dan shape yang konsisten.
 *
 * Saat [onClick] diberikan, kartu menjadi interaktif dengan animasi
 * "tekan" (skala mengecil halus) dan elevation naik — memberi kesan
 * responsif dan modern.
 */
@Composable
fun FlexiCard(
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(16.dp)
    val border = BorderStroke(
        width = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
    )
    val colors = CardDefaults.elevatedCardColors(
        containerColor = MaterialTheme.colorScheme.surface,
    )

    if (onClick != null) {
        val interactionSource = remember { MutableInteractionSource() }
        val ditekan by interactionSource.collectIsPressedAsState()
        val skala by animateFloatAsState(
            targetValue = if (ditekan) 0.985f else 1f,
            animationSpec = tween(durationMillis = 120),
            label = "skalaKartu",
        )
        val elevasi by animateFloatAsState(
            targetValue = if (ditekan) 0.5f else 3f,
            animationSpec = tween(durationMillis = 160),
            label = "elevasiKartu",
        )

        ElevatedCard(
            onClick = onClick,
            modifier = modifier
                .fillMaxWidth()
                .graphicsLayer { this.scaleX = skala; this.scaleY = skala },
            interactionSource = interactionSource,
            shape = shape,
            elevation = CardDefaults.elevatedCardElevation(
                defaultElevation = elevasi.dp,
            ),
            colors = colors,
        ) {
            content()
        }
    } else {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = shape,
            tonalElevation = 1.dp,
            shadowElevation = 2.dp,
            color = MaterialTheme.colorScheme.surface,
            border = border,
        ) {
            content()
        }
    }
}
