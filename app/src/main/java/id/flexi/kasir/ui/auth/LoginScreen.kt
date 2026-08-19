package id.flexi.kasir.ui.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.flexi.kasir.R

private val TabletBreakpoint = 600.dp
private val FormMaxWidth = 420.dp

// Modern color palette
private val PrimaryGreen = Color(0xFF2E7D32)
private val PrimaryGreenLight = Color(0xFF4CAF50)
private val PrimaryGreenDark = Color(0xFF1B5E20)
private val AccentGreen = Color(0xFF81C784)
private val BackgroundLight = Color(0xFFF8FAF8)
private val CardWhite = Color.White
private val TextPrimary = Color(0xFF1A1A1A)
private val TextSecondary = Color(0xFF6B7280)
private val BorderColor = Color(0xFFE5E7EB)

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
                .padding(paddingDalam)
                .background(BackgroundLight),
        ) {
            val isTablet = maxWidth >= TabletBreakpoint

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .navigationBarsPadding(),
            ) {
                HeroSection(
                    state = state,
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(if (isTablet) Modifier.height(200.dp) else Modifier),
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = if (isTablet) 48.dp else 24.dp),
                    contentAlignment = if (isTablet) Alignment.TopCenter else Alignment.TopCenter,
                ) {
                    FormSection(
                        state = state,
                        viewModel = viewModel,
                        modifier = Modifier
                            .widthIn(max = FormMaxWidth)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = if (isTablet) 24.dp else 16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroSection(
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

    // Animated gradient background
    val infiniteTransition = rememberInfiniteTransition(label = "gradient")
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "offset",
    )

    Box(
        modifier = modifier
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        PrimaryGreenDark,
                        PrimaryGreen,
                        PrimaryGreenLight,
                    ),
                    start = androidx.compose.ui.geometry.Offset(
                        x = animatedOffset * 500f,
                        y = 0f,
                    ),
                    end = androidx.compose.ui.geometry.Offset(
                        x = animatedOffset * 500f + 800f,
                        y = 800f,
                    ),
                )
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 48.dp),
        ) {
            // Logo with subtle shadow
            Surface(
                modifier = Modifier
                    .shadow(20.dp, CircleShape, clip = false)
                    .size(100.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.2f),
            ) {
                Icon(
                    painter = painterResource(R.drawable.logo_kasir),
                    contentDescription = "Flexi Kasir",
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .size(100.dp)
                        .padding(16.dp),
                )
            }

            Spacer(Modifier.height(24.dp))

            // App name with modern typography
            Text(
                text = judul,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp,
                ),
                color = Color.White,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(12.dp))

            // Subtitle
            Text(
                text = subjudul,
                style = MaterialTheme.typography.bodyLarge.copy(
                    lineHeight = 24.sp,
                ),
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 320.dp),
            )
        }
    }
}

@Composable
private fun FormSection(
    state: AuthUiState,
    viewModel: AuthViewModel,
    modifier: Modifier = Modifier,
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
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Welcome text
        Text(
            text = when (state.mode) {
                AuthUiState.Mode.Login -> "Selamat datang kembali!"
                AuthUiState.Mode.Register -> "Buat akun baru"
                else -> ""
            },
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.SemiBold,
            ),
            color = TextPrimary,
            modifier = Modifier.fillMaxWidth(),
        )

        if (state.mode == AuthUiState.Mode.Login || state.mode == AuthUiState.Mode.Register) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = when (state.mode) {
                    AuthUiState.Mode.Login -> "Masukkan kredensial Anda untuk melanjutkan"
                    AuthUiState.Mode.Register -> "Isi data di bawah untuk memulai"
                    else -> ""
                },
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(Modifier.height(24.dp))

        // Form card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                if (state.mode == AuthUiState.Mode.Register) {
                    ModernTextField(
                        value = state.namaUsaha,
                        onValueChange = viewModel::perbaruiNamaUsaha,
                        label = "Nama Usaha",
                        leadingIcon = {
                            Icon(
                                Icons.Default.Store,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp),
                            )
                        },
                    )

                    ModernTextField(
                        value = state.namaUser,
                        onValueChange = viewModel::perbaruiNamaUser,
                        label = "Nama Pengguna",
                        leadingIcon = {
                            Icon(
                                Icons.Default.Store,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp),
                            )
                        },
                    )
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
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp),
                            )
                        },
                    )
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
                                    tint = TextSecondary,
                                    modifier = Modifier.size(20.dp),
                                )
                            },
                            trailingIcon = {
                                Icon(
                                    if (sandiBaruTampak) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (sandiBaruTampak) "Sembunyikan" else "Tampilkan",
                                    tint = TextSecondary,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clickable { sandiBaruTampak = !sandiBaruTampak },
                                )
                            },
                        )

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
                                    tint = TextSecondary,
                                    modifier = Modifier.size(20.dp),
                                )
                            },
                            trailingIcon = {
                                Icon(
                                    if (konfirmasiTampak) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (konfirmasiTampak) "Sembunyikan" else "Tampilkan",
                                    tint = TextSecondary,
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
                                        tint = TextSecondary,
                                        modifier = Modifier.size(20.dp),
                                    )
                                },
                                trailingIcon = {
                                    Icon(
                                        if (kataSandiTampak) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (kataSandiTampak) "Sembunyikan" else "Tampilkan",
                                        tint = TextSecondary,
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clickable { kataSandiTampak = !kataSandiTampak },
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }

        // Error / Info messages
        AnimatedContent(
            targetState = state.pesanError to state.pesanInfo,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "pesanAnimasi",
        ) { (error, info) ->
            if (error != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFEE2E2),
                ) {
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFDC2626),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    )
                }
            } else if (info != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFDBEAFE),
                ) {
                    Text(
                        text = info,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF2563EB),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Primary button
        Button(
            onClick = {
                when (state.mode) {
                    AuthUiState.Mode.Verifikasi -> viewModel.verifikasiKode()
                    AuthUiState.Mode.AturPasswordBaru -> viewModel.simpanPasswordBaru()
                    else -> viewModel.kirim()
                }
            },
            enabled = !state.sedangMemuat,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryGreen,
                contentColor = Color.White,
                disabledContainerColor = PrimaryGreen.copy(alpha = 0.5f),
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
        ) {
            if (state.sedangMemuat) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = Color.White,
                )
            } else {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = labelTombol,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Auth links
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
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it, color = TextSecondary.copy(alpha = 0.5f)) } },
        readOnly = readOnly,
        singleLine = true,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = BorderColor,
            focusedBorderColor = PrimaryGreen,
            unfocusedContainerColor = BackgroundLight,
            focusedContainerColor = Color.White,
        ),
        modifier = modifier.fillMaxWidth(),
    )
}

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
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
            ),
            color = PrimaryGreen,
        )
    }
}
