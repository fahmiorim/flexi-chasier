package id.flexi.kasir.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Premium card dengan border, shadow, dan shape yang konsisten.
 * Support clickable (onClick) dan non-clickable (modifier saja).
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
        ElevatedCard(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            shape = shape,
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
            colors = colors,
        ) {
            content()
        }
    } else {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = shape,
            tonalElevation = 1.dp,
            shadowElevation = 0.5.dp,
            color = MaterialTheme.colorScheme.surface,
            border = border,
        ) {
            content()
        }
    }
}
