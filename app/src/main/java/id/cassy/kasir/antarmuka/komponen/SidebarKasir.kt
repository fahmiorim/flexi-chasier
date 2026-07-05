@file:OptIn(ExperimentalMaterial3Api::class)

package id.cassy.kasir.antarmuka.komponen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TableRestaurant
import id.cassy.kasir.BuildConfig
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.cassy.kasir.antarmuka.navigasi.TujuanNavigasiKasir

/**
 * Panel samping (sidebar drawer) untuk navigasi utama Cassy Kasir.
 *
 * Menampilkan menu-menu utama aplikasi seperti Dashboard, Kasir, Kelola Produk,
 * dan Riwayat Transaksi menggunakan [ModalDrawerSheet] dari Material 3.
 *
 * @param currentRoute Rute navigasi yang sedang aktif untuk menentukan item terpilih.
 * @param onPilihMenu Callback ketika pengguna memilih item menu navigasi.
 * @param modifier Modifikasi tata letak.
 */
@Composable
fun SidebarKasir(
    currentRoute: TujuanNavigasiKasir?,
    onPilihMenu: (TujuanNavigasiKasir) -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalDrawerSheet(
        modifier = modifier.width(280.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Cassy Kasir",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
            )

            Text(
                text = "Solusi Digital UMKM Modern",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, bottom = 16.dp),
            )

            HorizontalDivider()

            Spacer(modifier = Modifier.height(8.dp))

            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                label = { Text("Dashboard") },
                selected = currentRoute == TujuanNavigasiKasir.Dashboard,
                onClick = { onPilihMenu(TujuanNavigasiKasir.Dashboard) },
                modifier = Modifier.padding(horizontal = 12.dp),
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            )

            NavigationDrawerItem(
                icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Kasir") },
                label = { Text("Kasir") },
                selected = currentRoute == TujuanNavigasiKasir.KasirUtama,
                onClick = { onPilihMenu(TujuanNavigasiKasir.KasirUtama) },
                modifier = Modifier.padding(horizontal = 12.dp),
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            )

            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Inventory2, contentDescription = "Kelola Produk") },
                label = { Text("Kelola Produk") },
                selected = currentRoute == TujuanNavigasiKasir.KelolaProduk,
                onClick = { onPilihMenu(TujuanNavigasiKasir.KelolaProduk) },
                modifier = Modifier.padding(horizontal = 12.dp),
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            )

            NavigationDrawerItem(
                icon = { Icon(Icons.Default.TableRestaurant, contentDescription = "Meja") },
                label = { Text("Meja") },
                selected = currentRoute == TujuanNavigasiKasir.PengaturanMeja,
                onClick = { onPilihMenu(TujuanNavigasiKasir.PengaturanMeja) },
                modifier = Modifier.padding(horizontal = 12.dp),
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            )

            NavigationDrawerItem(
                icon = { Icon(Icons.Default.History, contentDescription = "Riwayat Transaksi") },
                label = { Text("Riwayat Transaksi") },
                selected = currentRoute == TujuanNavigasiKasir.RiwayatTransaksi,
                onClick = { onPilihMenu(TujuanNavigasiKasir.RiwayatTransaksi) },
                modifier = Modifier.padding(horizontal = 12.dp),
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            )

            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Settings, contentDescription = "Pengaturan") },
                label = { Text("Pengaturan") },
                selected = currentRoute == TujuanNavigasiKasir.Pengaturan,
                onClick = { onPilihMenu(TujuanNavigasiKasir.Pengaturan) },
                modifier = Modifier.padding(horizontal = 12.dp),
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                    text = "v${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp),
            )
        }
    }
}
