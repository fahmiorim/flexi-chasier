package id.flexi.kasir.ui.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.flexi.kasir.R
import id.flexi.kasir.ui.component.FlexiGradientButton
import id.flexi.kasir.ui.theme.FlexiGradients

private val TabletBreakpoint = 600.dp
private val FormMaxWidth = 420.dp

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold { paddingDalam ->
        BoxWithConstraints(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingDalam),
        ) {
            val isTablet = maxWidth >= TabletBreakpoint

            if (isTablet) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding()
                        .navigationBarsPadding(),
                ) {
                    HeroPanel(
                        state = state,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(horizontal = 48.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        FormPanel(
                            state = state,
                            viewModel = viewModel,
                            modifier = Modifier
                                .widthIn(max = FormMaxWidth)
                                .verticalScroll(rememberScrollState())
                                .padding(vertical = 36.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .imePadding()
                        .navigationBarsPadding(),
                ) {
                    HeroPanel(
                        state = state,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    FormPanel(
                        state = state,
                        viewModel = viewModel,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 24.dp),
                    )
                }
            }
        }
    }
}

// ── Hero section: gradient background + logo + title ──

@Composable
private fun HeroPanel(
    state: AuthUiState,
    modifier: Modifier = Modifier,
) {
    val judul = when (state.mode) {
        AuthUiState.Mode.Verifikasi -> "Verifikasi Email"
        AuthUiState.Mode.LupaPassword -> "Lupa Password"
        AuthUiState.Mode.AturPasswordBaru -> "Atur Password Baru"
        else -> "Flexi Kasir"
    }
    val subjudul = when (state.mode) {
        AuthUiState.Mode.Login -> "Masuk ke akun Anda untuk mulai berjualan"
        AuthUiState.Mode.Register -> "Buat akun baru untuk usaha Anda"
        AuthUiState.Mode.Verifikasi -> "Masukkan kode 6 digit yang dikirim ke email Anda"
        AuthUiState.Mode.LupaPassword -> "Masukkan email terdaftar untuk menerima kode reset"
        AuthUiState.Mode.AturPasswordBaru -> "Masukkan kode 6 digit dan password baru Anda"
    }

    Box(
        modifier = modifier.background(FlexiGradients.heroDalam()),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 40.dp),
        ) {
            Surface(
                modifier = Modifier
                    .shadow(12.dp, RoundedCornerShape(24.dp), clip = false),
                shape = RoundedCornerShape(24.dp),
                color = Color.White.copy(alpha = 0.15f),
            ) {
                Icon(
                    painter = painterResource(R.drawable.logo_kasir),
                    contentDescription = "Flexi Kasir",
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .size(80.dp)
                        .padding(4.dp),
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = judul,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                ),
                color = Color.White,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = subjudul,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 320.dp),
            )
        }
    }
}

// ── Form section: inputs + button + links ──

@Composable
private fun FormPanel(
    state: AuthUiState,
    viewModel: AuthViewModel,
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
) {
    val isiFormPassword = when (state.mode) {
        AuthUiState.Mode.Login,
        AuthUiState.Mode.Register,
        -> true
        else -> false
    }

    val ikonMode = when (state.mode) {
        AuthUiState.Mode.Verifikasi,
        AuthUiState.Mode.LupaPassword,
        -> Icons.Default.MailOutline
        AuthUiState.Mode.AturPasswordBaru -> Icons.Default.Lock
        else -> Icons.Default.Store
    }
    val labelTombol = when (state.mode) {
        AuthUiState.Mode.Login -> "Masuk"
        AuthUiState.Mode.Register -> "Daftar"
        AuthUiState.Mode.Verifikasi -> "Verifikasi"
        AuthUiState.Mode.LupaPassword -> "Kirim Kode Reset"
        AuthUiState.Mode.AturPasswordBaru -> "Simpan Kata Sandi Baru"
    }

    Column(
        modifier = modifier,
        horizontalAlignment = horizontalAlignment,
    ) {
        if (state.mode == AuthUiState.Mode.Register) {
            ModernTextField(
                value = state.namaUsaha,
                onValueChange = viewModel::perbaruiNamaUsaha,
                label = "Nama Usaha",
            )
            Spacer(Modifier.height(14.dp))

            ModernTextField(
                value = state.namaUser,
                onValueChange = viewModel::perbaruiNamaUser,
                label = "Nama Pengguna",
            )
            Spacer(Modifier.height(14.dp))
        }

        if (state.mode == AuthUiState.Mode.Login ||
            state.mode == AuthUiState.Mode.Register ||
            state.mode == AuthUiState.Mode.Verifikasi ||
            state.mode == AuthUiState.Mode.LupaPassword ||
            state.mode == AuthUiState.Mode.AturPasswordBaru
        ) {
            ModernTextField(
                value = state.email,
                onValueChange = viewModel::perbaruiEmail,
                label = "Email",
                readOnly = state.mode == AuthUiState.Mode.Verifikasi ||
                    state.mode == AuthUiState.Mode.AturPasswordBaru,
                leadingIcon = {
                    Icon(
                        Icons.Default.MailOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                },
            )
            Spacer(Modifier.height(14.dp))
        }

        when (state.mode) {
            AuthUiState.Mode.Verifikasi -> {
                ModernTextField(
                    value = state.kodeVerifikasi,
                    onValueChange = viewModel::perbaruiKodeVerifikasi,
                    label = "Kode Verifikasi",
                    placeholder = "000000",
                )
            }
            AuthUiState.Mode.AturPasswordBaru -> {
                ModernTextField(
                    value = state.kodeVerifikasi,
                    onValueChange = viewModel::perbaruiKodeVerifikasi,
                    label = "Kode Reset",
                    placeholder = "000000",
                )
                Spacer(Modifier.height(14.dp))

                var sandiBaruTampak by remember { mutableStateOf(false) }
                ModernTextField(
                    value = state.passwordBaru,
                    onValueChange = viewModel::perbaruiPasswordBaru,
                    label = "Kata Sandi Baru",
                    visualTransformation = if (sandiBaruTampak) VisualTransformation.None else PasswordVisualTransformation(),
                    leadingIcon = {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp),
                        )
                    },
                    trailingIcon = {
                        Icon(
                            if (sandiBaruTampak) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (sandiBaruTampak) "Sembunyikan" else "Tampilkan",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { sandiBaruTampak = !sandiBaruTampak },
                        )
                    },
                )
                Spacer(Modifier.height(14.dp))

                var konfirmasiTampak by remember { mutableStateOf(false) }
                ModernTextField(
                    value = state.konfirmasiPassword,
                    onValueChange = viewModel::perbaruiKonfirmasiPassword,
                    label = "Konfirmasi Kata Sandi",
                    visualTransformation = if (konfirmasiTampak) VisualTransformation.None else PasswordVisualTransformation(),
                    leadingIcon = {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp),
                        )
                    },
                    trailingIcon = {
                        Icon(
                            if (konfirmasiTampak) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (konfirmasiTampak) "Sembunyikan" else "Tampilkan",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { konfirmasiTampak = !konfirmasiTampak },
                        )
                    },
                )
            }
            else -> {
                if (isiFormPassword) {
                    var kataSandiTampak by remember { mutableStateOf(false) }
                    ModernTextField(
                        value = state.password,
                        onValueChange = viewModel::perbaruiPassword,
                        label = "Kata Sandi",
                        visualTransformation = if (kataSandiTampak) VisualTransformation.None else PasswordVisualTransformation(),
                        leadingIcon = {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp),
                            )
                        },
                        trailingIcon = {
                            Icon(
                                if (kataSandiTampak) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (kataSandiTampak) "Sembunyikan" else "Tampilkan",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable { kataSandiTampak = !kataSandiTampak },
                            )
                        },
                    )
                }
            }
        }

        // ── Error / Info messages ──
        AnimatedContent(
            targetState = state.pesanError to state.pesanInfo,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "pesanAnimasi",
        ) { (error, info) ->
            if (error != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                ) {
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    )
                }
            } else if (info != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Text(
                        text = info,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        FlexiGradientButton(
            onClick = {
                when (state.mode) {
                    AuthUiState.Mode.Verifikasi -> viewModel.verifikasiKode()
                    AuthUiState.Mode.AturPasswordBaru -> viewModel.simpanPasswordBaru()
                    else -> viewModel.kirim()
                }
            },
            enabled = !state.sedangMemuat,
            text = labelTombol,
            icon = if (state.sedangMemuat) null else ikonMode,
            loading = state.sedangMemuat,
            modifier = Modifier.height(52.dp),
        )

        Spacer(Modifier.height(8.dp))

        when (state.mode) {
            AuthUiState.Mode.Verifikasi -> {
                AuthLink("Kirim ulang kode", enabled = !state.sedangMemuat) {
                    viewModel.kirimUlangKode()
                }
                AuthLink("Kembali ke Masuk", enabled = !state.sedangMemuat) {
                    viewModel.gantiMode(AuthUiState.Mode.Login)
                }
            }
            AuthUiState.Mode.LupaPassword -> {
                AuthLink("Kembali ke Masuk", enabled = !state.sedangMemuat) {
                    viewModel.gantiMode(AuthUiState.Mode.Login)
                }
            }
            AuthUiState.Mode.AturPasswordBaru -> {
                AuthLink("Kirim ulang kode", enabled = !state.sedangMemuat) {
                    viewModel.kirimUlangKode()
                }
                AuthLink("Kembali ke Masuk", enabled = !state.sedangMemuat) {
                    viewModel.gantiMode(AuthUiState.Mode.Login)
                }
            }
            AuthUiState.Mode.Login -> {
                AuthLink("Belum punya akun? Daftar di sini") {
                    viewModel.gantiMode(AuthUiState.Mode.Register)
                }
                AuthLink("Lupa password?") {
                    viewModel.gantiMode(AuthUiState.Mode.LupaPassword)
                }
            }
            AuthUiState.Mode.Register -> {
                AuthLink("Sudah punya akun? Masuk di sini") {
                    viewModel.gantiMode(AuthUiState.Mode.Login)
                }
            }
        }
    }
}

// ── Reusable styled text field ──

@Composable
private fun ModernTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    placeholder: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation =
        androidx.compose.ui.text.input.VisualTransformation.None,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it, color = MaterialTheme.colorScheme.outline) } },
        readOnly = readOnly,
        singleLine = true,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
        ),
        modifier = modifier.fillMaxWidth(),
    )
}

// ── Reusable auth link button ──

@Composable
private fun AuthLink(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
