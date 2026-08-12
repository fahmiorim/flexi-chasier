package id.flexi.kasir.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.flexi.kasir.domain.model.AkunUser
import id.flexi.kasir.ui.component.FlexiGradientButton
import id.flexi.kasir.ui.theme.FlexiGradients

/**
 * Layar pemilihan gerai aktif saat pengguna memiliki lebih dari satu gerai.
 */
@Composable
fun PilihGeraiScreen(
    akun: AkunUser,
    onPilihGerai: (String) -> Unit,
    onKeluar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var idTerpilih by remember { mutableStateOf(akun.daftarGerai.firstOrNull()?.id ?: "") }

    Scaffold { paddingDalam ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingDalam)
                .verticalScroll(rememberScrollState()),
        ) {
            // ── Hero gradien brand ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(FlexiGradients.heroDalam())
                    .padding(horizontal = 28.dp, vertical = 40.dp),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Surface(
                        modifier = Modifier
                            .size(64.dp)
                            .shadow(10.dp, RoundedCornerShape(20.dp), clip = false),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.16f),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Store,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(15.dp),
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = "Pilih Gerai",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        color = MaterialTheme.colorScheme.onPrimary,
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = "Halo ${akun.nama}, pilih gerai tempat Anda bertugas.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                    )
                }
            }

            // ── Daftar gerai ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
            ) {
                akun.daftarGerai.forEach { gerai ->
                    val dipilih = gerai.id == idTerpilih
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable { idTerpilih = gerai.id },
                        shape = RoundedCornerShape(16.dp),
                        color = if (dipilih) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        },
                        border = BorderStroke(
                            width = if (dipilih) 2.dp else 1.dp,
                            color = if (dipilih) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                            },
                        ),
                        shadowElevation = if (dipilih) 4.dp else 0.dp,
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .shadow(4.dp, RoundedCornerShape(13.dp), clip = false)
                                    .then(
                                        if (dipilih) {
                                            Modifier.background(
                                                FlexiGradients.tile(MaterialTheme.colorScheme.primary),
                                                RoundedCornerShape(13.dp),
                                            )
                                        } else {
                                            Modifier.background(
                                                MaterialTheme.colorScheme.surfaceVariant,
                                                RoundedCornerShape(13.dp),
                                            )
                                        },
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Store,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp),
                                )
                            }
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 12.dp),
                            ) {
                                Text(
                                    text = gerai.nama,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                    ),
                                )
                                gerai.alamat?.takeIf { it.isNotBlank() }?.let { alamat ->
                                    Text(
                                        text = alamat,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = if (dipilih) "Dipilih" else null,
                                tint = if (dipilih) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                },
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                FlexiGradientButton(
                    onClick = { onPilihGerai(idTerpilih) },
                    enabled = idTerpilih.isNotBlank(),
                    text = "Gunakan Gerai Ini",
                    modifier = Modifier.height(52.dp),
                )

                Spacer(Modifier.height(4.dp))

                TextButton(
                    onClick = onKeluar,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                ) {
                    Text("Keluar dari akun")
                }
            }
        }
    }
}
