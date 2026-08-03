package id.flexi.kasir.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// ═══════════════════════════════════════
// PAYMENT SUMMARY SECTION
// ═══════════════════════════════════════

@Composable
internal fun PaymentSummarySection(
    ringkasanPayment: PaymentSummary,
    apakahRingkasanPaymentTampil: Boolean,
    saatUbahVisibilitasRingkasanPayment: () -> Unit,
    saatSimpanPesanan: () -> Unit,
    saatCheckout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SectionTitle(judul = "Payment")
            TextButton(onClick = saatUbahVisibilitasRingkasanPayment) {
                Text(
                    text = if (apakahRingkasanPaymentTampil) "Sembunyikan" else "Tampilkan",
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        if (apakahRingkasanPaymentTampil) {
            PaymentSummaryPanel(
                ringkasanPayment = ringkasanPayment,
                saatSimpanPesanan = saatSimpanPesanan,
                saatCheckout = saatCheckout,
            )
        }
    }
}

@Composable
internal fun PaymentSummaryPanel(
    ringkasanPayment: PaymentSummary,
    saatSimpanPesanan: () -> Unit,
    saatCheckout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),

    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            PaymentSummaryRow(
                label = "Subtotal",
                nilai = ringkasanPayment.subtotal,
            )
            PaymentSummaryRow(
                label = "Potongan",
                nilai = ringkasanPayment.potongan,
            )
            PaymentSummaryRow(
                label = "Pajak",
                nilai = ringkasanPayment.pajak,
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                thickness = 1.dp,
            )

            PaymentSummaryRow(
                label = "Total",
                nilai = ringkasanPayment.totalAkhir,
                tonjolkan = true,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                FilledTonalButton(
                    onClick = saatSimpanPesanan,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 50.dp),
                    enabled = ringkasanPayment.aksiUtamaAktif,
                    shape = MaterialTheme.shapes.small,
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 1.dp,
                        pressedElevation = 3.dp,
                    ),
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Simpan",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                Button(
                    onClick = saatCheckout,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 50.dp),
                    enabled = ringkasanPayment.aksiUtamaAktif,
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 2.dp,
                        pressedElevation = 4.dp,
                    ),
                ) {
                    Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Bayar",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
internal fun PaymentSummaryRow(
    label: String,
    nilai: String,
    modifier: Modifier = Modifier,
    tonjolkan: Boolean = false,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = if (tonjolkan) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
            fontWeight = if (tonjolkan) FontWeight.SemiBold else FontWeight.Normal,
            color = if (tonjolkan) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Text(
            text = nilai,
            style = if (tonjolkan) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleSmall,
            fontWeight = if (tonjolkan) FontWeight.Bold else FontWeight.SemiBold,
            color = if (tonjolkan) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        )
    }
}

