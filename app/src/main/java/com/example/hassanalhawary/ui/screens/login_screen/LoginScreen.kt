package com.example.hassanal_hawary.ui.screens.login_screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hassanalhawary.R
import com.example.hassanalhawary.core.components.LoginRegisterSection
import com.example.hassanalhawary.core.components.LoginWithGoogleComp
import com.example.hassanalhawary.core.components.OutlinedField
import com.example.hassanalhawary.core.components.WelcomeScreen
import com.example.hassanalhawary.core.util.LoginRegisterProviderElement
import com.example.hassanalhawary.ui.theme.CairoTypography


@Composable
fun LoginScreen(
    state: LoginState,
    onRegisterClick: () -> Unit,
    onLoginRegisterElementClick: (LoginRegisterProviderElement) -> Unit,
    onNavigateTo: (String) -> Unit
) {

    val context = LocalContext.current
    LaunchedEffect(key1 = state.errorMessage) {
        state.errorMessage?.let { error ->
            Toast.makeText(
                context, error, Toast.LENGTH_LONG
            ).show()
        }
    }
    LaunchedEffect(key1 = state.navigateTo) {
        state.navigateTo?.let { destination ->
           onNavigateTo(destination)
        }
    }



    val loginViewModel: LoginViewModel = viewModel()
    val loginState by loginViewModel.state.collectAsState()


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 15.dp, vertical = 15.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        if (state.showSignInProgressBar) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
            )

        }

        Spacer(
            modifier = Modifier.height(15.dp)
        )

        WelcomeScreen(
            loginRegister = R.string.login,
            modifier = Modifier.fillMaxWidth().fillMaxHeight(.3f)
        )

        OutlinedField(
            modifier = Modifier.fillMaxWidth(),
            label = R.string.enter_email,
            icon = Icons.Default.Email,
            value = state.enteredEmail,
            onValueChange = {
                loginViewModel.emailChanged(it)
            },
            showError = state.errorMessage != null,
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
            showError = state.errorMessage != null,
            errorMessage = state.enterValidPassowrdMsg
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
//                onLoginBtnClick()
                /*signInViewModel.signInWithEmailAndPassword(
                    state.enteredEmail,
                    state.enteredPassword
                )*/
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

        }

    }
}

@Preview(showBackground = true, showSystemUi = true, widthDp = 320, heightDp = 640)
@Composable
fun LoginScreenPreview(modifier: Modifier = Modifier) {

    LoginScreen(
        state = LoginState(),
        onRegisterClick = {

        },
        onLoginRegisterElementClick = {

        }
    ) { }
}

