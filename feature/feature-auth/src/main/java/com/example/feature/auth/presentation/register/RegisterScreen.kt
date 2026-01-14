package com.example.feature.auth.presentation.register

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
import androidx.compose.material.icons.filled.Person
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
fun RegisterScreen(
    modifier: Modifier,
    onLoginClick: () -> Unit,
    onSuccessfulRegister: () -> Unit
) {
    val registerVm: AuthViewModel = hiltViewModel()
    val state by registerVm.state.collectAsState()

    val context = LocalContext.current

    LaunchedEffect(key1 = state.isSignInSuccessful, key2 = state.errorMessage.isNullOrBlank()) {
        if (state.isSignInSuccessful) {
            onSuccessfulRegister()
        }
        if (state.errorMessage != null) {
            Toast.makeText(
                context,
                state.errorMessage,
                Toast.LENGTH_LONG
            ).show()

        }
    }



    Column(
        modifier = modifier
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
            loginRegister = R.string.register,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(.3f)
        )

        OutlinedField(
            modifier = Modifier.fillMaxWidth(),
            label = R.string.enter_name,
            icon = Icons.Default.Person,
            value = state.userName,
            onValueChange = {
                registerVm.userNameChanged(it)
            },
            errorMessage = state.enterValidEmailMsg
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )
        OutlinedField(
            modifier = Modifier.fillMaxWidth(),
            label = R.string.enter_email,
            icon = Icons.Default.Email,
            value = state.enteredEmail,
            onValueChange = {
                registerVm.emailChanged(it)
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
                registerVm.passwordChanged(it)
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
            isLogin = false,

            ) {
            onLoginClick()
        }

        Spacer(Modifier.weight(1f))

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),

            shape = RoundedCornerShape(8.dp),
            onClick = {
                registerVm.registerNewUserWithEmailPassword(
                    userName = state.userName,
                    email = state.enteredEmail,
                    password = state.enteredPassword
                )
            }
        ) {
            Text(
                text = stringResource(R.string.register),
                style = CairoTypography.bodyMedium
            )
        }

        LoginWithGoogleComp(
            modifier = Modifier.fillMaxWidth(),
            isLogin = false,
        ) {
            val loginResult = registerVm.loginWithGoogle()
        }

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