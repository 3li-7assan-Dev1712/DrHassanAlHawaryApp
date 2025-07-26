package com.example.hassanalhawary

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hassanal_hawary.ui.screens.login_screens.LoginViewModel
import com.example.hassanal_hawary.ui.screens.login_screens.LoginScreen
import com.example.hassanalhawary.ui.theme.HassanAlHawaryTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HassanAlHawaryTheme {


                val loginViewModel = viewModel<LoginViewModel>()
                val state by loginViewModel.state.collectAsState()


                LaunchedEffect(key1 = state.isSignInSuccessful) {
                    if (state.isSignInSuccessful) {
                        Toast.makeText(
                            applicationContext,
                            "Sign in successful",
                            Toast.LENGTH_LONG
                        ).show()
                        loginViewModel.hideProgressBar()
                        // login success
                        loginViewModel.resetState()
                    }
                }


                Surface (
                    modifier = Modifier.fillMaxSize()
                ) {

                    LoginScreen(

                        state = state,
                        onRegisterClick = {
                            Toast.makeText(
                                applicationContext,
                                "Navigate to register screen",
                                Toast.LENGTH_LONG
                            ).show()
                        },
                        onLoginRegisterElementClick = {
                            loginViewModel.showProgressBar()
                            lifecycleScope.launch {
                                loginViewModel.loginWithGoogle()

                            }
                        },
                        onNavigateTo = { r ->
                            Log.d("MainAct", r)
                        }
                    )
                }
            }
        }

    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    HassanAlHawaryTheme {
        Greeting("Android")
    }
}