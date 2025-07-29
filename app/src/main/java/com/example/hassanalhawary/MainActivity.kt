package com.example.hassanalhawary

import android.os.Bundle
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.hassanalhawary.ui.screens.home_screen.HomeScreen
import com.example.hassanalhawary.ui.screens.login_screen.LoginScreen
import com.example.hassanalhawary.ui.screens.register_screen.RegisterScreen
import com.example.hassanalhawary.ui.theme.HassanAlHawaryTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HassanAlHawaryTheme {

                val mainActivityViewModel = viewModel<MainActivityViewModel>()
                val mainActivityState by mainActivityViewModel.state.collectAsState()

                LaunchedEffect(key1 = mainActivityState.navigateTo) {
                    if (mainActivityState.navigateTo != null) {
                        Toast.makeText(
                            applicationContext,
                            "Sign in successful",
                            Toast.LENGTH_LONG
                        ).show()
                        mainActivityViewModel.hideProgressBar()
                    }
                }

                Surface (
                    modifier = Modifier.fillMaxSize()
                ) {


                    val navController = rememberNavController()
                    val navHost =
                        NavHost(navController, startDestination = "home_screen") {

                            composable(route = "login_screen") {
                                LoginScreen(

                                    onRegisterClick = {
                                        Toast.makeText(
                                            applicationContext,
                                            "Navigate to register screen",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        navController.navigate("register_screen")
                                        navController.clearBackStack("login_screen")
                                    },
                                    onSuccessfulLogin = {
                                        // go to home screen
                                        Toast.makeText(
                                            applicationContext,
                                            "Login successful",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                )
                            }

                            composable("home_screen") {
                                HomeScreen(

                                )
                            }

                            composable("register_screen") {
                                RegisterScreen(
                                    modifier = Modifier.fillMaxSize(),
                                    onLoginClick = {
                                        navController.popBackStack()
                                    },
                                    onSuccessfulRegister = {
                                        // go to home screen
                                        Toast.makeText(
                                            applicationContext,
                                            "Register successful",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                )
                            }
                        }


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