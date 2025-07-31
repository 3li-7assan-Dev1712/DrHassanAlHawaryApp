package com.example.hassanalhawary

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
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
import com.example.hassanalhawary.ui.navigation.BottomNavigationBar
import com.example.hassanalhawary.ui.screens.articles_screen.ArticlesScreen
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

                val isLoggedIn = mainActivityState.isUserLoggedIn

                val rootNavController =
                    rememberNavController() // Single NavController for switching graphs

                if (isLoggedIn) {
                    MainAppContent(
                        onLogout = { }
                    )
                } else {
                    AuthNavHost(
                        onLoginSuccess = {

                        }
                    )
                }


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


//                val navHost =


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
}

@Composable
fun MainAppContent(
    onLogout: () -> Unit

) {
    val navController = rememberNavController()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BottomNavigationBar(
                modifier = Modifier.fillMaxWidth(),
                navController = navController
            )
        }
    ) { innerPadding ->

        NavHost(
            navController,
            startDestination = "home_screen",
            modifier = Modifier.padding(innerPadding)
        ) {


            composable("home_screen") {
                HomeScreen(

                )
            }
            composable("articles_screen")  {

                ArticlesScreen {

                }
            }
            composable("audios_screen") {

            }
            composable("questions_screen") {

            }


        }
    }
}

@Composable
fun AuthNavHost(
    onLoginSuccess: () -> Unit
) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "login_screen"
    ) {

        composable(route = "login_screen") {
            LoginScreen(

                onRegisterClick = {
//                    Toast.makeText(
//                        context =  LocalContext.current,
//                        text = "Navigate to register screen",
//                        duration  =Toast.LENGTH_LONG
//                    ).show()
                    navController.navigate("register_screen")
                    navController.clearBackStack("login_screen")
                },
                onSuccessfulLogin = {
                    // go to home screen
                    /*Toast.makeText(
                        ,
                        "Login successful",
                        Toast.LENGTH_LONG
                    ).show()*/
                }
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
                    /*  Toast.makeText(
                          applicationContext,
                          "Register successful",
                          Toast.LENGTH_LONG
                      ).show()*/
                }
            )
        }
    }


}


