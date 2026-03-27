package com.example.feature.auth.presentation.register

import android.widget.Toast
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
import androidx.compose.material3.Text
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
fun RegisterScreen(
    modifier: Modifier = Modifier,
    onLoginClick: () -> Unit,
    onSuccessfulRegister: () -> Unit
) {

    val viewModel: AuthViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()
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

    val isButtonEnabled =
        state.userName.isNotBlank() &&
                state.enteredEmail.isNotBlank() &&
                state.enteredPassword.isNotBlank()

    Scaffold(containerColor = Color.Transparent) { padding ->


        val scrollState = rememberScrollState()
        Column(
            modifier = modifier
                .padding(padding)
                .padding(horizontal = 20.dp)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(4.dp))

            if (state.showSignInProgressBar) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(16.dp))

            WelcomeScreen(
                loginRegister = R.string.register,
                modifier = Modifier
                    .fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            OutlinedField(
                modifier = Modifier.fillMaxWidth(),
                label = R.string.enter_name,
                icon = Icons.Default.Person,
                value = state.userName,
                onValueChange = { viewModel.userNameChanged(it) }
            )

            Spacer(Modifier.height(12.dp))

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

            val passwordValidation =
                viewModel.getPasswordValidation(state.enteredPassword)

            if (state.enteredPassword.isNotEmpty()) {

                Spacer(Modifier.height(8.dp))

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

            Spacer(Modifier.height(16.dp))

            LoginRegisterSection(
                modifier = Modifier.fillMaxWidth(),
                isLogin = false
            ) {
                onLoginClick()
            }


            Spacer(Modifier.weight(1f))
            Button(
                onClick = { viewModel.registerNewUser() },
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
                        text = stringResource(R.string.register),
                        style = CairoTypography.bodyMedium
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            LoginWithGoogleComp(
                modifier = Modifier.fillMaxWidth(),
                isLogin = false
            ) {
                viewModel.loginWithGoogle()
            }

            Spacer(Modifier.height(16.dp))
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
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(Modifier.width(6.dp))

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

@Preview(showBackground = true, showSystemUi = true, widthDp = 320, heightDp = 640)
@Composable
fun RegisterPreviewComposeable() {

    RegisterScreen(
        modifier = Modifier.fillMaxSize(),
        onLoginClick = {},
    ) {

    }

}
