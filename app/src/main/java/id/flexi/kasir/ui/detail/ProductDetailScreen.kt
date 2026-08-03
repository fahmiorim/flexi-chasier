package id.flexi.kasir.ui.detail

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import id.flexi.kasir.ui.component.FlexiCard
import id.flexi.kasir.ui.component.FlexiTopAppBar
import id.flexi.kasir.ui.component.FlexiInfoRow
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.flexi.kasir.ui.component.SimpleEmptyStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    modelTampilan: ProductDetailUiState,
    saatKembali: () -> Unit,
    saatAksiDikirim: (ProductDetailAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { paddingDalam ->
        when (val statusMuat = modelTampilan.statusMuat) {
            ProductDetailLoadStatus.Memuat -> {
                KontenMemuatDetailProduk(
                    paddingDalam = paddingDalam,
                    saatKembali = saatKembali,
                )
            }

            is ProductDetailLoadStatus.Berhasil -> {
                KontenDetailProduk(
                    paddingDalam = paddingDalam,
                    saatKembali = saatKembali,
                    namaProduk = statusMuat.namaProduk,
                    hargaProduk = statusMuat.hargaProduk,
                    stokTersedia = statusMuat.stokTersedia,
                    apakahStokDiaktifkan = statusMuat.apakahStokDiaktifkan,
                    deskripsiProduk = statusMuat.deskripsiProduk,
                    fotoUri = statusMuat.fotoUri,
                    statusAksi = statusMuat.statusAksi,
                    hppProduk = statusMuat.hppProduk,
                    marginProduk = statusMuat.marginProduk,
                    saatTambahKeKeranjang = {
                        saatAksiDikirim(ProductDetailAction.CobaTambahKeKeranjang)
                    },
                )
            }

            is ProductDetailLoadStatus.Kosong -> {
                KontenProdukTidakDitemukan(
                    paddingDalam = paddingDalam,
                    saatKembali = saatKembali,
                    judulStatusKosong = statusMuat.judul,
                    deskripsiStatusKosong = statusMuat.deskripsi,
                )
            }

            is ProductDetailLoadStatus.Gagal -> {
                KontenGagalMemuatDetailProduk(
                    paddingDalam = paddingDalam,
                    saatKembali = saatKembali,
                    judulStatusGagal = statusMuat.judul,
                    deskripsiStatusGagal = statusMuat.deskripsi,
                    saatCobaMuatUlang = {
                        saatAksiDikirim(ProductDetailAction.CobaMuatUlang)
                    },
                )
            }
        }
    }
}

@Composable
private fun KontenMemuatDetailProduk(
    paddingDalam: PaddingValues,
    saatKembali: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingDalam)
            .padding(16.dp),
    ) {
        FlexiTopAppBar(
            title = "Detail Produk",
            saatKembali = saatKembali,
            modifier = Modifier.padding(bottom = 4.dp),
        )
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Memuat detail produk...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
    }
}

/**
 * Konten utama layar detail produk saat data berhasil dimuat.
 *
 * Menggunakan lebar konten maksimum (720dp) agar tampilan tablet tidak terlalu renggang
 * dan elemen CTA tetap fokus di area yang mudah dijangkau.
 *
 * @param paddingDalam Padding dari Scaffold induk.
 * @param saatKembali Callback saat tombol kembali diketuk.
 * @param namaProduk Nama produk yang ditampilkan.
 * @param hargaProduk Harga produk dalam format Rupiah.
 * @param stokTersedia Jumlah stok tersedia.
 * @param deskripsiProduk Deskripsi lengkap produk.
 * @param statusAksi Status tombol aksi (aktif/nonaktif, label, keterangan).
 * @param saatTambahKeKeranjang Callback saat tombol tambah diketuk.
 */
@Composable
private fun KontenDetailProduk(
    paddingDalam: PaddingValues,
    saatKembali: () -> Unit,
    namaProduk: String,
    hargaProduk: String,
    stokTersedia: Int,
    apakahStokDiaktifkan: Boolean = false,
    deskripsiProduk: String,
    fotoUri: String? = null,
    hppProduk: String? = null,
    marginProduk: String? = null,
    statusAksi: ProductDetailActionStatus,
    saatTambahKeKeranjang: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingDalam)
            .padding(16.dp),
    ) {
        FlexiTopAppBar(
            title = "Detail Produk",
            saatKembali = saatKembali,
            modifier = Modifier.padding(bottom = 4.dp),
        )
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 720.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                FlexiCard {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        if (fotoUri != null) {
                            val ctx = LocalContext.current
                            val bitmap = remember(fotoUri) {
                                try {
                                    val uri = Uri.parse(fotoUri)
                                    ctx.contentResolver.openInputStream(uri)?.use { stream ->
                                        BitmapFactory.decodeStream(stream)
                                    }
                                } catch (_: Exception) { null }
                            }
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center,
                            ) {
                                bitmap?.let {
                                    Image(
                                        bitmap = it.asImageBitmap(),
                                        contentDescription = namaProduk,
                                        modifier = Modifier
                                            .size(160.dp)
                                            .clip(MaterialTheme.shapes.medium),
                                        contentScale = ContentScale.Crop,
                                    )
                                }
                            }
                        }

                        Text(
                            text = namaProduk,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )

                        Text(
                            text = hargaProduk,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )

                        // HPP & Margin
                        if (hppProduk != null || marginProduk != null) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    hppProduk?.let { hpp ->
                                        FlexiInfoRow(label = "HPP", value = hpp, divider = true)
                                    }
                                    marginProduk?.let { margin ->
                                        FlexiInfoRow(
                                            label = "Margin Laba",
                                            value = margin,
                                            valueColor = MaterialTheme.colorScheme.primary,
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Stok tersedia",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            if (apakahStokDiaktifkan) {
                                Text(
                                    text = "$stokTersedia item",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (stokTersedia > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                )
                            } else {
                                Text(
                                    text = "Tidak dibatasi",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        if (deskripsiProduk.isNotBlank()) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                            Text(
                                text = deskripsiProduk,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                statusAksi.keterangan?.let { keterangan ->
                    Text(
                        text = keterangan,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp),
                    )
                }

                Button(
                    onClick = saatTambahKeKeranjang,
                    enabled = statusAksi.aktif,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp),
                    shape = MaterialTheme.shapes.small,
                ) {
                    Text(
                        text = statusAksi.label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun KontenProdukTidakDitemukan(
    paddingDalam: PaddingValues,
    saatKembali: () -> Unit,
    judulStatusKosong: String,
    deskripsiStatusKosong: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingDalam)
            .padding(16.dp),
    ) {
        FlexiTopAppBar(
            title = "Detail Produk",
            saatKembali = saatKembali,
            modifier = Modifier.padding(bottom = 4.dp),
        )
        SimpleEmptyStatus(
            judul = judulStatusKosong,
            deskripsi = deskripsiStatusKosong,
        )
    }
}

@Composable
private fun KontenGagalMemuatDetailProduk(
    paddingDalam: PaddingValues,
    saatKembali: () -> Unit,
    judulStatusGagal: String,
    deskripsiStatusGagal: String,
    saatCobaMuatUlang: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingDalam)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        FlexiTopAppBar(
            title = "Detail Produk",
            saatKembali = saatKembali,
            modifier = Modifier.padding(bottom = 4.dp),
        )
        SimpleEmptyStatus(
            judul = judulStatusGagal,
            deskripsi = deskripsiStatusGagal,
        )

        Button(
            onClick = saatCobaMuatUlang,
            modifier = Modifier.heightIn(min = 48.dp),
            shape = MaterialTheme.shapes.small,
        ) {
            Text(
                text = "Coba Lagi",
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}
