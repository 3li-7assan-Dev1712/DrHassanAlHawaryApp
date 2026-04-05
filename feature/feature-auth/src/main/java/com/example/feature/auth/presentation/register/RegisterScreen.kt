package com.example.feature.auth.presentation.register

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.ui.R
import com.example.core.ui.theme.CairoTypography
import com.example.core.ui.theme.HassanAlHawaryTheme
import com.example.feature.auth.presentation.AuthScreenState
import com.example.feature.auth.presentation.AuthViewModel
import com.example.feature.auth.presentation.components.LoginRegisterSection
import com.example.feature.auth.presentation.components.LoginWithGoogleComp
import com.example.feature.auth.presentation.components.OutlinedField

@Composable
fun RegisterScreen(
    modifier: Modifier = Modifier,
    onLoginClick: () -> Unit,
    onSuccessfulRegister: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(state.isSignInSuccessful) {
        if (state.isSignInSuccessful) {
            onSuccessfulRegister()
            viewModel.resetState()
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    RegisterScreenContent(
        modifier = modifier,
        state = state,
        onNameChanged = viewModel::userNameChanged,
        onEmailChanged = viewModel::emailChanged,
        onPasswordChanged = viewModel::passwordChanged,
        passwordValidation = viewModel.getPasswordValidation(state.enteredPassword),
        onRegisterClick = viewModel::registerNewUser,
        onGoogleRegisterClick = viewModel::loginWithGoogle,
        onNavigateToLogin = onLoginClick
    )
}

@Composable
fun PremiumWelcomeHeader(title: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier
                .size(140.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = CircleShape,
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                ),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(
                width = 3.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f),
                        MaterialTheme.colorScheme.primaryContainer
                    )
                )
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(6.dp)
                    .clip(CircleShape)
            ) {
                Image(
                    modifier = Modifier.fillMaxSize(),
                    painter = painterResource(id = R.drawable.dr_hassan_image),
                    contentDescription = "Doctor Hassan Logo",
                    contentScale = ContentScale.Crop,
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = "مرحباً بك في تطبيق",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(Modifier.height(4.dp))
        
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            ),
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun RegisterScreenContent(
    modifier: Modifier = Modifier,
    state: AuthScreenState,
    onNameChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    passwordValidation: AuthViewModel.PasswordValidation,
    onRegisterClick: () -> Unit,
    onGoogleRegisterClick: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val isButtonEnabled =
        state.userName.isNotBlank() &&
                state.enteredEmail.isNotBlank() &&
                state.enteredPassword.isNotBlank()

    Scaffold(containerColor = Color.Transparent) { padding ->

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
        ) {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(horizontal = 24.dp)
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .imePadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(Modifier.height(16.dp))

                if (state.showSignInProgressBar) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(Modifier.height(24.dp))

                PremiumWelcomeHeader(title = stringResource(R.string.register))

                Spacer(Modifier.height(32.dp))

                OutlinedField(
                    modifier = Modifier.fillMaxWidth(),
                    label = R.string.enter_name,
                    icon = Icons.Default.Person,
                    value = state.userName,
                    onValueChange = onNameChanged
                )

                Spacer(Modifier.height(16.dp))

                OutlinedField(
                    modifier = Modifier.fillMaxWidth(),
                    label = R.string.enter_email,
                    icon = Icons.Default.Email,
                    value = state.enteredEmail,
                    onValueChange = onEmailChanged,
                    showError = state.enterValidEmailMsg.isNotEmpty(),
                    errorMessage = state.enterValidEmailMsg
                )

                Spacer(Modifier.height(16.dp))

                OutlinedField(
                    modifier = Modifier.fillMaxWidth(),
                    label = R.string.enter_password,
                    icon = Icons.Default.Lock,
                    value = state.enteredPassword,
                    onValueChange = onPasswordChanged,
                    showError = state.enterValidPasswordMsg.isNotEmpty(),
                    errorMessage = state.enterValidPasswordMsg,
                    isPasswordField = true
                )

                if (state.enteredPassword.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))

                    PasswordRequirement(
                        text = stringResource(R.string.at_least_6_digits),
                        isValid = passwordValidation.hasMinLength
                    )

                    PasswordRequirement(
                        text = stringResource(R.string.containt_number),
                        isValid = passwordValidation.hasNumber
                    )

                    PasswordRequirement(
                        text = stringResource(R.string.containt_capital_leter),
                        isValid = passwordValidation.hasUpperCase
                    )
                }

                Spacer(Modifier.height(24.dp))

                LoginRegisterSection(
                    modifier = Modifier.fillMaxWidth(),
                    isLogin = false
                ) {
                    onNavigateToLogin()
                }

                Spacer(Modifier.weight(1f))
                
                Spacer(Modifier.height(24.dp))
                
                Button(
                    onClick = onRegisterClick,
                    enabled = isButtonEnabled && !state.showSignInProgressBar,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    if (state.showSignInProgressBar) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.register),
                            style = CairoTypography.titleMedium
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                LoginWithGoogleComp(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    isLogin = false
                ) {
                    onGoogleRegisterClick()
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun PasswordRequirement(
    text: String,
    isValid: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {

        Text(
            text = if (isValid) "✔" else "○",
            color = if (isValid) Color(0xFF4CAF50) else Color.Gray,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.width(8.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = if (isValid)
                MaterialTheme.colorScheme.onBackground
            else
                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
    }
}

@Preview(
    showBackground = true,
    showSystemUi = false,
    device = Devices.PIXEL_7,
    name = "شاشة إنشاء الحساب"
)
@Composable
fun RegisterScreenArabicPreview() {
    HassanAlHawaryTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Surface(color = MaterialTheme.colorScheme.surface) {
                RegisterScreenContent(
                    state = AuthScreenState(),
                    onNameChanged = {},
                    onEmailChanged = {},
                    onPasswordChanged = {},
                    passwordValidation = AuthViewModel.PasswordValidation(),
                    onRegisterClick = {},
                    onGoogleRegisterClick = {},
                    onNavigateToLogin = {}
                )
            }
        }
    }
}
