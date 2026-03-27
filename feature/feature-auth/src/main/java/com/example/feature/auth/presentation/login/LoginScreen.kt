package com.example.feature.auth.presentation.login

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.core.ui.R
import com.example.core.ui.theme.CairoTypography
import com.example.feature.auth.presentation.AuthViewModel
import com.example.feature.auth.presentation.components.LoginRegisterSection
import com.example.feature.auth.presentation.components.LoginWithGoogleComp
import com.example.feature.auth.presentation.components.OutlinedField
import com.example.feature.auth.presentation.components.WelcomeScreen


@Composable
fun LoginScreen(
    onRegisterClick: () -> Unit,
    onSuccessfulLogin: () -> Unit
) {

    val viewModel: AuthViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(state.isSignInSuccessful) {
        if (state.isSignInSuccessful) {
            onSuccessfulLogin()
            viewModel.resetState()
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    val isButtonEnabled =
        state.enteredEmail.isNotBlank() &&
                state.enteredPassword.length >= 6

    Scaffold(containerColor = Color.Transparent) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {

            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(horizontal = 20.dp)
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .imePadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(Modifier.height(16.dp))

                if (state.showSignInProgressBar) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(8.dp))

                WelcomeScreen(
                    loginRegister = R.string.login,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(.25f)
                )

                Spacer(Modifier.height(16.dp))

                OutlinedField(
                    modifier = Modifier.fillMaxWidth(),
                    label = R.string.enter_email,
                    icon = Icons.Default.Email,
                    value = state.enteredEmail,
                    onValueChange = { viewModel.emailChanged(it) },
                    showError = state.enterValidEmailMsg.isNotEmpty(),
                    errorMessage = state.enterValidEmailMsg
                )

                Spacer(Modifier.height(12.dp))

                OutlinedField(
                    modifier = Modifier.fillMaxWidth(),
                    label = R.string.enter_password,
                    icon = Icons.Default.Lock,
                    value = state.enteredPassword,
                    onValueChange = { viewModel.passwordChanged(it) },
                    showError = state.enterValidPasswordMsg.isNotEmpty(),
                    errorMessage = state.enterValidPasswordMsg,
                    isPasswordField = true
                )

                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                    TextButton(onClick = { viewModel.sendPasswordResetEmail() }) {
                        Text(
                            text = stringResource(R.string.forgot_password),
                            style = CairoTypography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))


                LoginRegisterSection(
                    modifier = Modifier.fillMaxWidth(),
                    isLogin = true
                ) {
                    onRegisterClick()
                }

                Spacer(Modifier.weight(1f))

                Button(
                    onClick = { viewModel.loginWithEmailPassword() },
                    enabled = isButtonEnabled && !state.showSignInProgressBar,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(8.dp)
                ) {
                    if (state.showSignInProgressBar) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.login),
                            style = CairoTypography.bodyMedium
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                LoginWithGoogleComp(
                    modifier = Modifier.fillMaxWidth(),
                    isLogin = true
                ) {
                    viewModel.loginWithGoogle()
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, widthDp = 320, heightDp = 640)
@Composable
fun LoginScreenPreview(modifier: Modifier = Modifier) {

    LoginScreen(
        onRegisterClick = {

        }
    ) { }
}
