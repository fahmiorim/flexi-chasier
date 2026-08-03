@file:OptIn(ExperimentalMaterial3Api::class)

package id.flexi.kasir.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.TableRestaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.flexi.kasir.BuildConfig
import id.flexi.kasir.ui.navigation.CashierNavigationDestination

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun SidebarKasir(
    currentRoute: CashierNavigationDestination?,
    onPilihMenu: (CashierNavigationDestination) -> Unit,
    apakahManajemenKasAktif: Boolean = true,
    namaUsaha: String = "",
    alamat: String = "",
    tagline: String = "",
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val primaryGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
            MaterialTheme.colorScheme.surface,
        )
    )

    ModalDrawerSheet(
        modifier = modifier.width(280.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            // Header gradasi premium
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(primaryGradient)
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Surface(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        shadowElevation = 4.dp
                    ) {
                        Icon(
                            imageVector = Icons.Default.Store,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                                .padding(10.dp)
                                .size(24.dp),
                        )
                    }

                    Column {
                        // Nama usaha sebagai header utama
                        Text(
                            text = namaUsaha.ifBlank { "Flexi Cashier" },
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )

                        // App branding sebagai subtitle
                        Text(
                            text = if (namaUsaha.isBlank()) {
                                "Versi ${BuildConfig.VERSION_NAME}"
                            } else {
                                "Flexi Cashier — v${BuildConfig.VERSION_NAME}"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        )

                        // Tagline (jika ada)
                        if (tagline.isNotBlank()) {
                            Text(
                                text = tagline,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }

                        // Alamat usaha (jika ada)
                        if (alamat.isNotBlank()) {
                            Text(
                                text = alamat,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Daftar Menu scrollable
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(vertical = 12.dp)
            ) {
                val itemsMenu = listOf(
                    Triple(Icons.Default.Dashboard, "Dashboard", CashierNavigationDestination.Dashboard),
                    Triple(Icons.Default.ShoppingCart, "Transaksi", CashierNavigationDestination.KasirUtama),
                    Triple(Icons.Default.Inventory2, "Kelola Produk", CashierNavigationDestination.KelolaProduk),
                    Triple(Icons.Default.TableRestaurant, "Pengaturan Meja", CashierNavigationDestination.PengaturanMeja),
                    Triple(Icons.Default.History, "Riwayat Transaksi", CashierNavigationDestination.RiwayatTransaction),
                )

                itemsMenu.forEach { (icon, label, tujuan) ->
                    val apakahDipilih = when (tujuan) {
                        CashierNavigationDestination.KelolaProduk -> currentRoute == CashierNavigationDestination.KelolaProduk || currentRoute == CashierNavigationDestination.DaftarProduk
                        else -> currentRoute == tujuan
                    }

                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = if (apakahDipilih) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        label = {
                            Text(
                                text = label,
                                fontWeight = if (apakahDipilih) FontWeight.Bold else FontWeight.Medium,
                                style = MaterialTheme.typography.labelLarge
                            )
                        },
                        selected = apakahDipilih,
                        onClick = { onPilihMenu(tujuan) },
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 2.dp)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            unselectedContainerColor = Color.Transparent,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                    )
                }

                if (apakahManajemenKasAktif) {
                    val rekapKasDipilih = currentRoute == CashierNavigationDestination.Kasir
                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = "Rekap Kas",
                                tint = if (rekapKasDipilih) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        label = {
                            Text(
                                text = "Rekap Kas",
                                fontWeight = if (rekapKasDipilih) FontWeight.Bold else FontWeight.Medium,
                                style = MaterialTheme.typography.labelLarge
                            )
                        },
                        selected = rekapKasDipilih,
                        onClick = { onPilihMenu(CashierNavigationDestination.Kasir) },
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 2.dp)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            unselectedContainerColor = Color.Transparent
                        ),
                    )
                }

                val laporanDipilih = currentRoute == CashierNavigationDestination.Laporan
                NavigationDrawerItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Assessment,
                            contentDescription = "Laporan",
                            tint = if (laporanDipilih) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    label = {
                        Text(
                            text = "Laporan",
                            fontWeight = if (laporanDipilih) FontWeight.Bold else FontWeight.Medium,
                            style = MaterialTheme.typography.labelLarge
                        )
                    },
                    selected = laporanDipilih,
                    onClick = { onPilihMenu(CashierNavigationDestination.Laporan) },
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 2.dp)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        unselectedContainerColor = Color.Transparent
                    ),
                )

                val pengaturanDipilih = currentRoute == CashierNavigationDestination.Pengaturan
                NavigationDrawerItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Pengaturan",
                            tint = if (pengaturanDipilih) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    label = {
                        Text(
                            text = "Pengaturan",
                            fontWeight = if (pengaturanDipilih) FontWeight.Bold else FontWeight.Medium,
                            style = MaterialTheme.typography.labelLarge
                        )
                    },
                    selected = pengaturanDipilih,
                    onClick = { onPilihMenu(CashierNavigationDestination.Pengaturan) },
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 2.dp)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        unselectedContainerColor = Color.Transparent
                    ),
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Footer versi aplikasi yang rapi
            Text(
                text = "v${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 16.dp),
            )
        }
    }
}
