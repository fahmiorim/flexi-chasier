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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import id.flexi.kasir.domain.model.PenyesuaianStok
import id.flexi.kasir.ui.component.FlexiCard
import id.flexi.kasir.ui.component.FlexiDialog
import id.flexi.kasir.ui.component.FlexiDialogActions
import id.flexi.kasir.ui.component.FlexiDialogHeader
import id.flexi.kasir.ui.component.FlexiDialogSingleAction
import id.flexi.kasir.ui.component.FlexiTopAppBar
import id.flexi.kasir.ui.component.FlexiInfoRow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.flexi.kasir.ui.component.SimpleEmptyStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    modelTampilan: ProductDetailUiState,
    stateAturStok: ProductAturStokUiState,
    saatKembali: () -> Unit,
    saatAksiDikirim: (ProductDetailAction) -> Unit,
    bukaDialogAturStok: () -> Unit,
    tutupDialogAturStok: () -> Unit,
    perbaruiStokBaru: (String) -> Unit,
    perbaruiAlasanAturStok: (String) -> Unit,
    simpanAturStok: () -> Unit,
    bukaDialogRiwayat: () -> Unit,
    tutupDialogRiwayat: () -> Unit,
    bersihkanPesan: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(stateAturStok.pesanSnackbar) {
        stateAturStok.pesanSnackbar?.let {
            snackbarHostState.showSnackbar(it)
            bersihkanPesan()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                    saatAturStok = bukaDialogAturStok,
                    saatLihatRiwayat = bukaDialogRiwayat,
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

        if (stateAturStok.apakahDialogAturStokTampil) {
            val stokSaatIni = (modelTampilan.statusMuat as? ProductDetailLoadStatus.Berhasil)
                ?.stokTersedia
            DialogAturStokProduk(
                stokBaru = stateAturStok.stokBaru,
                alasan = stateAturStok.alasanAturStok,
                stokSaatIni = stokSaatIni,
                sedangMenyimpan = stateAturStok.apakahSedangMenyimpanStok,
                onStokBerubah = perbaruiStokBaru,
                onAlasanBerubah = perbaruiAlasanAturStok,
                onDismiss = tutupDialogAturStok,
                onSimpan = simpanAturStok,
            )
        }

        if (stateAturStok.apakahDialogRiwayatTampil) {
            DialogRiwayatPenyesuaianProduk(
                daftar = stateAturStok.riwayatPenyesuaian,
                onDismiss = tutupDialogRiwayat,
            )
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
    saatAturStok: () -> Unit,
    saatLihatRiwayat: () -> Unit,
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilledTonalButton(
                        onClick = saatAturStok,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp),
                        shape = MaterialTheme.shapes.small,
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Atur Stok", fontWeight = FontWeight.Medium)
                    }
                    FilledTonalButton(
                        onClick = saatLihatRiwayat,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp),
                        shape = MaterialTheme.shapes.small,
                    ) {
                        Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Riwayat", fontWeight = FontWeight.Medium)
                    }
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

@Composable
private fun DialogAturStokProduk(
    stokBaru: String,
    alasan: String,
    stokSaatIni: Int?,
    sedangMenyimpan: Boolean,
    onStokBerubah: (String) -> Unit,
    onAlasanBerubah: (String) -> Unit,
    onDismiss: () -> Unit,
    onSimpan: () -> Unit,
) {
    val stokBaruInt = stokBaru.toIntOrNull()
    val valid = stokBaruInt != null && stokBaruInt >= 0

    FlexiDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FlexiDialogHeader(
                icon = Icons.Default.Tune,
                title = "Atur Stok Produk",
                subtitle = stokSaatIni?.let { "Stok saat ini: $it item" },
                onClose = onDismiss,
            )

            OutlinedTextField(
                value = stokBaru,
                onValueChange = { onStokBerubah(it) },
                label = { Text("Stok Baru") },
                placeholder = { Text("Contoh: 100") },
                singleLine = true,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = alasan,
                onValueChange = { onAlasanBerubah(it) },
                label = { Text("Alasan (opsional)") },
                placeholder = { Text("Contoh: Opname fisik, reset stok") },
                singleLine = true,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth(),
            )

            FlexiDialogActions(
                onBatal = onDismiss,
                labelBatal = "Batal",
                onKonfirmasi = onSimpan,
                labelKonfirmasi = "Simpan",
                konfirmasiIcon = Icons.Default.Add,
                enabled = valid && !sedangMenyimpan,
            )
        }
    }
}

@Composable
private fun DialogRiwayatPenyesuaianProduk(
    daftar: List<PenyesuaianStok>,
    onDismiss: () -> Unit,
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("id", "ID")) }

    FlexiDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FlexiDialogHeader(
                icon = Icons.Default.History,
                title = "Riwayat Penyesuaian Stok",
                onClose = onDismiss,
            )

            if (daftar.isEmpty()) {
                Text(
                    text = "Belum ada penyesuaian stok untuk produk ini.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    daftar.forEach { penyesuaian ->
                        FlexiCard(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = dateFormat.format(Date(penyesuaian.waktu)),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        text = if (penyesuaian.selisih >= 0) "+${penyesuaian.selisih} item" else "${penyesuaian.selisih} item",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (penyesuaian.selisih >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                    )
                                }
                                Text(
                                    text = "${penyesuaian.stokSebelum} → ${penyesuaian.stokSesudah} item",
                                    style = MaterialTheme.typography.bodySmall,
                                )
                                if (penyesuaian.alasan.isNotBlank()) {
                                    Text(
                                        text = penyesuaian.alasan,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            FlexiDialogSingleAction(
                label = "Tutup",
                onClick = onDismiss,
            )
        }
    }
}
