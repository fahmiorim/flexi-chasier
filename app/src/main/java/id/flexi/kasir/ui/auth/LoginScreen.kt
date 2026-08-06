package id.flexi.kasir.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold { paddingDalam ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingDalam)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Surface(
                modifier = Modifier.size(72.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primary,
            ) {
                Icon(
                    imageVector = if (state.mode == AuthUiState.Mode.Verifikasi) {
                        Icons.Default.MailOutline
                    } else {
                        Icons.Default.Store
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(18.dp),
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = if (state.mode == AuthUiState.Mode.Verifikasi) {
                    "Verifikasi Email"
                } else {
                    "Flexi Cashier"
                },
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                ),
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = when (state.mode) {
                    AuthUiState.Mode.Login -> "Masuk ke akun Anda untuk mulai berjualan"
                    AuthUiState.Mode.Register -> "Buat akun baru untuk usaha Anda"
                    AuthUiState.Mode.Verifikasi ->
                        "Masukkan kode 6 digit yang dikirim ke email Anda"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(28.dp))

            if (state.mode == AuthUiState.Mode.Register) {
                OutlinedTextField(
                    value = state.namaUsaha,
                    onValueChange = viewModel::perbaruiNamaUsaha,
                    label = { Text("Nama Usaha") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = state.namaUser,
                    onValueChange = viewModel::perbaruiNamaUser,
                    label = { Text("Nama Pengguna") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
            }

            if (state.mode == AuthUiState.Mode.Verifikasi) {
                OutlinedTextField(
                    value = state.email,
                    onValueChange = viewModel::perbaruiEmail,
                    label = { Text("Email") },
                    singleLine = true,
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = state.kodeVerifikasi,
                    onValueChange = viewModel::perbaruiKodeVerifikasi,
                    label = { Text("Kode Verifikasi") },
                    singleLine = true,
                    placeholder = { Text("000000") },
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                OutlinedTextField(
                    value = state.email,
                    onValueChange = viewModel::perbaruiEmail,
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = state.password,
                    onValueChange = viewModel::perbaruiPassword,
                    label = { Text("Kata Sandi") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            state.pesanError?.let { pesan ->
                Spacer(Modifier.height(12.dp))
                Text(
                    text = pesan,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            state.pesanInfo?.let { pesan ->
                Spacer(Modifier.height(12.dp))
                Text(
                    text = pesan,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    if (state.mode == AuthUiState.Mode.Verifikasi) {
                        viewModel.verifikasiKode()
                    } else {
                        viewModel.kirim()
                    }
                },
                enabled = !state.sedangMemuat,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
            ) {
                if (state.sedangMemuat) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text(
                        text = when (state.mode) {
                            AuthUiState.Mode.Login -> "Masuk"
                            AuthUiState.Mode.Register -> "Daftar"
                            AuthUiState.Mode.Verifikasi -> "Verifikasi"
                        },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            when (state.mode) {
                AuthUiState.Mode.Verifikasi -> {
                    TextButton(
                        onClick = viewModel::kirimUlangKode,
                        enabled = !state.sedangMemuat,
                    ) {
                        Text("Kirim ulang kode")
                    }
                    TextButton(
                        onClick = { viewModel.gantiMode(AuthUiState.Mode.Login) },
                        enabled = !state.sedangMemuat,
                    ) {
                        Text("Kembali ke Masuk")
                    }
                }
                AuthUiState.Mode.Login -> {
                    TextButton(onClick = {
                        viewModel.gantiMode(AuthUiState.Mode.Register)
                    }) {
                        Text("Belum punya akun? Daftar di sini")
                    }
                }
                AuthUiState.Mode.Register -> {
                    TextButton(onClick = {
                        viewModel.gantiMode(AuthUiState.Mode.Login)
                    }) {
                        Text("Sudah punya akun? Masuk di sini")
                    }
                }
            }
        }
    }
}
