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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.hassanalhawary.ui.navigation.BottomNavigationBar
import com.example.hassanalhawary.ui.screens.articles_screen.ArticlesScreen
import com.example.hassanalhawary.ui.screens.audio_detail_screen.AudioDetailRoute
import com.example.hassanalhawary.ui.screens.audio_list_sceen.AudioListScreen
import com.example.hassanalhawary.ui.screens.detail_article_screen.DetailArticleScreen
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

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // routes where the bottom nav should be hidden
    val routesWithoutBottomNav = remember {
        setOf(
            "detail_article_screen/{articleId}",
            "audio_detail_screen/{audioId}" // Use the route pattern
        )
    }

    // More robust check for pattern routes like "audio_detail_screen/{audioId}"
    val shouldShowBottomNav = remember(currentRoute) {
        derivedStateOf {
            currentRoute != null && routesWithoutBottomNav.none { routePattern ->
                // Simple check for exact match or prefix match for routes with arguments
                if (routePattern.contains("{")) {
                    val baseRoutePattern = routePattern.substringBefore("/{")
                    currentRoute.startsWith(baseRoutePattern)
                } else {
                    currentRoute == routePattern
                }
            }
        }.value
    }



    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (shouldShowBottomNav) {
                BottomNavigationBar(
                    modifier = Modifier.fillMaxWidth(),
                    navController = navController
                )
            }
        }
    ) { innerPadding ->

        NavHost(
            navController,
            startDestination = "home_screen",
            modifier = Modifier.padding(innerPadding)
        ) {


            composable("home_screen") {
                HomeScreen(
                    onNavigateToDetailArticle = {articleId ->
                        navController.navigate("detail_article_screen/$articleId")
                        
                    }
                )
            }
            composable("articles_screen")  {

                ArticlesScreen { articleId ->
                    navController.navigate("detail_article_screen/$articleId")
                }
            }
            composable("detail_article_screen/{articleId}") {
                DetailArticleScreen {
                    navController.popBackStack()
                }
            }
            composable("audio_list_screen") {
                AudioListScreen(
                    onNavigateToAudioDetail = { audioId ->
                        navController.navigate("audio_detail_screen/$audioId")
                    }
                )
            }

            composable(
                route = "audio_detail_screen/{audioId}",
                arguments = listOf()
            ) {
                AudioDetailRoute(
                    onNavigateUp = {
                        navController.popBackStack()
                    }
                )
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


