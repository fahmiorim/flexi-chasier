package id.flexi.kasir.ui.manage

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import id.flexi.kasir.ui.component.FlexiDialog
import id.flexi.kasir.ui.component.FlexiDialogActions
import id.flexi.kasir.ui.component.FlexiDialogHeader
import id.flexi.kasir.ui.component.FlexiDialogWarningPanel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

// ═══════════════════════════════════════
// REUSABLE FORM CARD
// ═══════════════════════════════════════

@Composable
internal fun FormCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp),
            ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

// ═══════════════════════════════════════
// IMAGE LABELED CARD WRAPPER
// ═══════════════════════════════════════

@Composable
internal fun CardLabelDenganSwitch(
    label: String,
    deskripsi: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(text = deskripsi, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

// ═══════════════════════════════════════
// SECTION: FOTO PRODUK
// ═══════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BagianFotoProduk(
    fotoUri: String?,
    nama: String,
    harga: String,
    kategori: String,
    kodePindai: String,
    daftarKategori: List<String>,
    pesanKesalahanNama: String?,
    pesanKesalahanHarga: String?,
    apakahTampilVarian: Boolean,
    varianDraft: List<VarianDraft>,
    onUbahNama: (String) -> Unit,
    onUbahHarga: (String) -> Unit,
    onUbahKategori: (String) -> Unit,
    onUbahKodePindai: (String) -> Unit,
    onPilihFoto: (String) -> Unit,
    onHapusFoto: () -> Unit,
) {
    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? -> uri?.let { onPilihFoto(it.toString()) } }

    FormCard {
        // Foto upload area
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
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
                Box {
                    bitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(), contentDescription = "Foto produk",
                            modifier = Modifier.size(120.dp).clip(MaterialTheme.shapes.medium),
                            contentScale = ContentScale.Crop,
                        )
                    }
                    IconButton(
                        onClick = onHapusFoto,
                        modifier = Modifier.align(Alignment.TopEnd).size(24.dp),
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.size(24.dp),
                        ) {
                            Icon(
                                Icons.Default.Close, contentDescription = "Hapus foto",
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(4.dp),
                            )
                        }
                    }
                }
            } else {
                Surface(
                    onClick = { pickerLauncher.launch("image/*") },
                    modifier = Modifier.size(120.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Tambah foto",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Tambah Foto", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = nama, onValueChange = onUbahNama,
            label = { Text("Nama Produk") }, modifier = Modifier.fillMaxWidth(),
            isError = pesanKesalahanNama != null,
            supportingText = { pesanKesalahanNama?.let { Text(it) } },
            singleLine = true, shape = RoundedCornerShape(12.dp),
        )

        Spacer(modifier = Modifier.height(12.dp))

        val hargaKosongDenganVarian = harga.isBlank() && apakahTampilVarian && varianDraft.any { it.nama.isNotBlank() }
        OutlinedTextField(
            value = harga, onValueChange = onUbahHarga,
            label = { Text("Harga Jual (Rp)") }, modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = pesanKesalahanHarga != null,
            supportingText = {
                when {
                    pesanKesalahanHarga != null -> Text(pesanKesalahanHarga)
                    hargaKosongDenganVarian -> Text("Opsional — harga diambil dari varian",
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            singleLine = true, shape = RoundedCornerShape(12.dp),
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Kategori dropdown
        val ekspansiKategori = remember { androidx.compose.runtime.mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = ekspansiKategori.value,
            onExpandedChange = { ekspansiKategori.value = it },
        ) {
            OutlinedTextField(
                value = kategori,
                onValueChange = { ekspansiKategori.value = true; onUbahKategori(it) },
                label = { Text("Kategori") },
                modifier = Modifier.fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true),
                singleLine = true, shape = RoundedCornerShape(12.dp),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = ekspansiKategori.value) },
            )
            ExposedDropdownMenu(
                expanded = ekspansiKategori.value,
                onDismissRequest = { ekspansiKategori.value = false },
            ) {
                daftarKategori.forEach { kat ->
                    DropdownMenuItem(
                        text = { Text(kat) },
                        onClick = { onUbahKategori(kat); ekspansiKategori.value = false },
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = kodePindai, onValueChange = onUbahKodePindai,
            label = { Text("Barcode / Kode") }, modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Ketuk atau pindai kode produk") },
            singleLine = true, shape = RoundedCornerShape(12.dp),
        )
    }
}

// ═══════════════════════════════════════
// SECTION: FAVORIT
// ═══════════════════════════════════════

@Composable
internal fun BagianFavorit(
    favorit: Boolean,
    onToggle: () -> Unit,
) {
    FormCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (favorit) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = null,
                tint = if (favorit) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Favorit", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text("Tandai sebagai produk favorit", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = favorit, onCheckedChange = { onToggle() })
        }
    }
}

// ═══════════════════════════════════════
// SECTION: HARGA MODAL
// ═══════════════════════════════════════

@Composable
internal fun BagianHargaModal(
    apakahTampil: Boolean,
    hargaModal: String,
    onToggle: () -> Unit,
    onUbahHargaModal: (String) -> Unit,
) {
    FormCard {
        CardLabelDenganSwitch(
            label = "Harga Modal",
            deskripsi = "Catat harga modal untuk lihat laba",
            checked = apakahTampil,
            onCheckedChange = { onToggle() },
        )
        if (apakahTampil) {
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = hargaModal, onValueChange = onUbahHargaModal,
                label = { Text("Harga Modal (Rp)") }, modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true, shape = RoundedCornerShape(12.dp),
            )
        }
    }
}

// ═══════════════════════════════════════
// SECTION: KELOLA STOK
// ═══════════════════════════════════════

@Composable
internal fun BagianKelolaStok(
    apakahTampil: Boolean,
    stok: String,
    pesanKesalahanStok: String?,
    apakahTampilVarian: Boolean,
    varianDraft: List<VarianDraft>,
    onToggle: () -> Unit,
    onUbahStok: (String) -> Unit,
    onToggleVarian: () -> Unit,
    onUbahNamaVarian: (Int, String) -> Unit,
    onUbahHargaVarian: (Int, String) -> Unit,
    onTambahVarian: () -> Unit,
    onHapusVarianTerakhir: () -> Unit,
) {
    FormCard {
        CardLabelDenganSwitch(
            label = "Kelola Stok",
            deskripsi = "Aktifkan untuk produk yang butuh stok",
            checked = apakahTampil,
            onCheckedChange = { onToggle() },
        )
        if (apakahTampil) {
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = stok, onValueChange = onUbahStok,
                label = { Text("Stok Tersedia") }, modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = pesanKesalahanStok != null,
                supportingText = { pesanKesalahanStok?.let { Text(it) } },
                singleLine = true, shape = RoundedCornerShape(12.dp),
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            VarianSection(
                apakahTampil = apakahTampilVarian,
                varianDraft = varianDraft,
                onToggle = onToggleVarian,
                onUbahNamaVarian = onUbahNamaVarian,
                onUbahHargaVarian = onUbahHargaVarian,
                onTambahVarian = onTambahVarian,
                onHapusVarianTerakhir = onHapusVarianTerakhir,
            )
        }
    }
}

// ═══════════════════════════════════════
// SECTION: VARIAN
// ═══════════════════════════════════════

@Composable
internal fun VarianSection(
    apakahTampil: Boolean,
    varianDraft: List<VarianDraft>,
    onToggle: () -> Unit,
    onUbahNamaVarian: (Int, String) -> Unit,
    onUbahHargaVarian: (Int, String) -> Unit,
    onTambahVarian: () -> Unit,
    onHapusVarianTerakhir: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        CardLabelDenganSwitch(
            label = "Varian", deskripsi = "Tambah varian seperti HOT, ICE, Large",
            checked = apakahTampil, onCheckedChange = { onToggle() },
        )
        if (apakahTampil) {
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            varianDraft.forEachIndexed { indeks, varian ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = varian.nama, onValueChange = { onUbahNamaVarian(indeks, it) },
                        label = { Text("Nama") }, placeholder = { Text("HOT") },
                        modifier = Modifier.weight(1f), singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                    )
                    OutlinedTextField(
                        value = varian.harga, onValueChange = { onUbahHargaVarian(indeks, it) },
                        label = { Text("Harga") }, placeholder = { Text("15000") },
                        modifier = Modifier.weight(1f), singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                    IconButton(onClick = { onHapusVarianTerakhir() }, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus varian",
                            tint = MaterialTheme.colorScheme.error)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            OutlinedButton(onClick = onTambahVarian, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Tambah Varian")
            }
        }
    }
}

// ═══════════════════════════════════════
// DIALOG: HAPUS PRODUK
// ═══════════════════════════════════════

@Composable
internal fun DialogHapusProduk(
    namaProduk: String,
    onDismiss: () -> Unit,
    onConfirmHapus: () -> Unit,
) {
    FlexiDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            FlexiDialogHeader(
                icon = Icons.Default.Delete, title = "Hapus Produk?",
                subtitle = "Tindakan ini tidak dapat dibatalkan",
                iconTint = MaterialTheme.colorScheme.error,
                onClose = onDismiss,
            )
            FlexiDialogWarningPanel(
                message = "Apakah Anda yakin ingin menghapus produk \"$namaProduk\"? Tindakan ini tidak dapat dibatalkan.",
            )
            FlexiDialogActions(
                onBatal = onDismiss, onKonfirmasi = onConfirmHapus,
                labelKonfirmasi = "Hapus", konfirmasiIcon = Icons.Default.Delete,
                konfirmasiColor = MaterialTheme.colorScheme.error,
            )
        }
    }
}
