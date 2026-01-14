package com.example.feature.auth.presentation.login

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    val loginViewModel: AuthViewModel = hiltViewModel()
    val state by loginViewModel.state.collectAsState()

    val context = LocalContext.current

    LaunchedEffect(state.isSignInSuccessful) {
        if (state.isSignInSuccessful)
            onSuccessfulLogin()
    }
    LaunchedEffect(key1 = state.errorMessage) {
        state.errorMessage?.let { error ->
            Toast.makeText(
                context, error, Toast.LENGTH_LONG
            ).show()
        }
    }



    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 15.dp, vertical = 15.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {


        if (state.showSignInProgressBar) {
            Spacer(
                modifier = Modifier.height(16.dp)
            )
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
            )

        }

        Spacer(
            modifier = Modifier.height(15.dp)
        )

        WelcomeScreen(
            loginRegister = R.string.login,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(.3f)
        )

        OutlinedField(
            modifier = Modifier.fillMaxWidth(),
            label = R.string.enter_email,
            icon = Icons.Default.Email,
            value = state.enteredEmail,
            onValueChange = {
                loginViewModel.emailChanged(it)
            },
            showError = state.enterValidEmailMsg.isNotEmpty(),
            errorMessage = state.enterValidEmailMsg
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        OutlinedField(
            modifier = Modifier.fillMaxWidth(),
            label = R.string.enter_password,
            icon = Icons.Default.Lock,
            value = state.enteredPassword,
            onValueChange = {
                loginViewModel.passwordChanged(it)
            },
            showError = state.enterValidPasswordMsg.isNotEmpty(),
            errorMessage = state.enterValidPasswordMsg
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        LoginRegisterSection(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 15.dp,
                ),
            isLogin = true,

            ) {
            onRegisterClick()
        }

        Spacer(Modifier.weight(1f))

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),

            shape = RoundedCornerShape(8.dp),
            onClick = {
                loginViewModel.loginWithEmailPassword(
                    state.enteredEmail,
                    state.enteredPassword
                )
            }
        ) {
            Text(
                text = stringResource(R.string.login),
                style = CairoTypography.bodyMedium
            )
        }

        LoginWithGoogleComp(
            modifier = Modifier.fillMaxWidth(),
            isLogin = true,
        ) {
            val loginResult = loginViewModel.loginWithGoogle()
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

