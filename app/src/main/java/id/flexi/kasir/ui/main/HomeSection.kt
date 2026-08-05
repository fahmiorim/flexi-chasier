package id.flexi.kasir.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import id.flexi.kasir.domain.model.CatalogDisplay
import id.flexi.kasir.domain.model.SyncStatus
import id.flexi.kasir.ui.SinkronMesinStatus
import id.flexi.kasir.ui.labelJudulSinkron
import id.flexi.kasir.ui.labelMetadataSinkron


@Composable
internal fun CashierHomeHeader(
    namaAplikasi: String,
    sloganAplikasi: String,
    saatBukaSidebar: () -> Unit,
    nilaiPencarian: String = "",
    saatNilaiPencarianBerubah: (String) -> Unit = {},
    jumlahHasilPencarian: Int = 0,
    tampilkanAksiResetPencarian: Boolean = false,
    saatResetPencarian: () -> Unit = {},
    catalogDisplay: CatalogDisplay = CatalogDisplay.Grid,
    saatAlihkanCatalogDisplay: () -> Unit = {},
    jumlahPesananPending: Int = 0,
    saatBukaPesananPending: () -> Unit = {},
    jumlahAntrian: Int = 0,
    saatBukaAntrian: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.primary,
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 16.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = saatBukaSidebar) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Buka menu",
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }

            Surface(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
            ) {
                Icon(
                    imageVector = Icons.Default.Store,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(6.dp),
                )
            }

            Column(
                modifier = Modifier.padding(start = 8.dp),
            ) {
                Text(
                    text = namaAplikasi,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                Text(
                    text = sloganAplikasi,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                )
            }

            Spacer(modifier = Modifier.weight(0.5f))

            OutlinedTextField(
                value = nilaiPencarian,
                onValueChange = saatNilaiPencarianBerubah,
                modifier = Modifier.width(320.dp).heightIn(max = 34.dp),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                textStyle = MaterialTheme.typography.labelSmall,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp),
                    )
                },
                trailingIcon = {
                    if (tampilkanAksiResetPencarian) {
                        IconButton(onClick = saatResetPencarian) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Reset pencarian",
                                tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                },
                placeholder = {
                    Text(
                        text = "Cari...",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                    )
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f),
                    focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                    unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
                    focusedBorderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f),
                    unfocusedBorderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f),
                    cursorColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )

            Spacer(modifier = Modifier.weight(0.5f))

            IconButton(onClick = saatAlihkanCatalogDisplay) {
                Icon(
                    imageVector = if (catalogDisplay == CatalogDisplay.Grid)
                        Icons.Default.ViewList else Icons.Default.GridView,
                    contentDescription = "Alihkan tampilan",
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }

            Box {
                IconButton(onClick = saatBukaPesananPending) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Pesanan pending",
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
                if (jumlahPesananPending > 0) {
                    Badge(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(end = 4.dp, top = 4.dp),
                        containerColor = MaterialTheme.colorScheme.error,
                    ) {
                        Text(
                            text = if (jumlahPesananPending > 99) "99+" else "$jumlahPesananPending",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onError,
                        )
                    }
                }
            }

            Box {
                IconButton(onClick = saatBukaAntrian) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.List,
                        contentDescription = "Antrian diproses",
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
                if (jumlahAntrian > 0) {
                    Badge(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(end = 4.dp, top = 4.dp),
                        containerColor = MaterialTheme.colorScheme.tertiary,
                    ) {
                        Text(
                            text = if (jumlahAntrian > 99) "99+" else "$jumlahAntrian",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onTertiary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun SinkronStatusBar(
    status: SinkronMesinStatus,
    saatSinkronkan: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val warnaStatus = when (val s = status.status) {
        SyncStatus.Synced -> MaterialTheme.colorScheme.primary
        SyncStatus.LocalChanges -> MaterialTheme.colorScheme.tertiary
        SyncStatus.Syncing -> MaterialTheme.colorScheme.primary
        is SyncStatus.Gagal -> MaterialTheme.colorScheme.error
        SyncStatus.Never -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = warnaStatus.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = warnaStatus.copy(alpha = 0.25f),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Indikator: spinner saat berjalan, titik berwarna saat diam.
            if (status.apakahSedangBerjalan) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = warnaStatus,
                    strokeWidth = 2.dp,
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(warnaStatus),
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = labelJudulSinkron(status),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = warnaStatus,
                )
                Text(
                    text = labelMetadataSinkron(status),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            TextButton(
                onClick = saatSinkronkan,
                enabled = !status.apakahSedangBerjalan,
            ) {
                Text(
                    text = if (status.apakahSedangBerjalan) "Menyinkron..." else "Sinkronkan",
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
internal fun CheckoutResultOverlay(
    statusHasilCheckout: CheckoutResultStatus,
    saatTutup: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Pilih warna berdasarkan metode bayar
    val apakahQris = statusHasilCheckout.paymentMethod == id.flexi.kasir.domain.model.PaymentMethod.Qris
    val warnaUtama = if (apakahQris) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
    val warnaBg = if (apakahQris)
        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.9f)
    else
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)

    // Animasi slide dari atas
    val slideOffset = remember { Animatable(-1f) }
    val bannerAlpha = remember { Animatable(0f) }

    // Mainkan suara notifikasi checkout
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        try {
            val uri = android.provider.Settings.System.DEFAULT_NOTIFICATION_URI
            val mediaPlayer = android.media.MediaPlayer.create(context, uri)
            mediaPlayer?.setOnCompletionListener { it.release() }
            mediaPlayer?.start()
        } catch (_: Exception) {
            // Gagal mainkan suara — tidak perlu ganggu pengguna
        }
    }

    // Animasi masuk: slide down + fade in
    LaunchedEffect(Unit) {
        bannerAlpha.animateTo(1f, animationSpec = tween(200))
        slideOffset.animateTo(
            0f,
            animationSpec = spring(dampingRatio = 0.5f, stiffness = 400f),
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
            ) { saatTutup() },
        contentAlignment = Alignment.TopCenter,
    ) {
        ElevatedCard(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .graphicsLayer {
                    translationY = size.height * slideOffset.value
                    alpha = bannerAlpha.value
                }
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) { /* prevent dismiss */ },
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = warnaBg,
            ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Icon
                Surface(
                    shape = CircleShape,
                    color = warnaUtama.copy(alpha = 0.15f),
                    modifier = Modifier.size(40.dp),
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = warnaUtama,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }

                // Teks
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = statusHasilCheckout.judul,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = warnaUtama,
                    )
                    Text(
                        text = statusHasilCheckout.deskripsi,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                    )
                }

                // Nomor antrian (jika ada) atau tombol tutup
                if (statusHasilCheckout.nomorAntrian != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = warnaUtama,
                    ) {
                        Text(
                            text = "#${statusHasilCheckout.nomorAntrian}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (apakahQris) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        )
                    }
                } else {
                    IconButton(
                        onClick = saatTutup,
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = warnaUtama.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }
    }
}
