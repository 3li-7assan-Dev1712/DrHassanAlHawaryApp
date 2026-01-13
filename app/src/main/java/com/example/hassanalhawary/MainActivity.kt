package com.example.hassanalhawary

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.core_ui.splash_screen.SplashScreen
import com.example.feature.about_dr_hassan.presentation.AboutDrHassanScreen
import com.example.feature.article.presentation.detail.ArticleDetailScreen
import com.example.feature.article.presentation.list.ArticleListScreen
import com.example.feature.audio.presentation.detail.AudioDetailScreen
import com.example.feature.audio.presentation.list.AudioListScreen
import com.example.feature.home.presentation.HomeScreen
import com.example.feature.image.presentation.detail.ImageScreen
import com.example.feature.image.presentation.list.ImagesGroupsScreen
import com.example.feature.video.presentation.detail.VideoPlayerScreen
import com.example.feature.video.presentation.list.VideosScreen
import com.example.hassanalhawary.ui.navigation.BottomNavigationBar
import com.example.hassanalhawary.ui.navigation.Routes
import com.example.hassanalhawary.ui.screens.login_screen.LoginScreen
import com.example.hassanalhawary.ui.screens.profile_screen.ProfileScreen
import com.example.hassanalhawary.ui.screens.register_screen.RegisterScreen
import com.example.hassanalhawary.ui.screens.search_screen.SearchScreen
import com.example.hassanalhawary.ui.screens.study_zone_screen.StudyZoneScreen
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
                        onLogout = {

                        })
                } else {
                    AuthNavHost(
                        onLoginSuccess = {
//                            rootNavController.navigate("home_screen")
                        })
                }



                LaunchedEffect(key1 = mainActivityState.navigateTo) {
                    if (mainActivityState.navigateTo != null) {
                        Toast.makeText(
                            applicationContext, "Sign in successful", Toast.LENGTH_LONG
                        ).show()
                        mainActivityViewModel.hideProgressBar()
                    }
                }


            }

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
            "audio_detail_screen/{title}/{audioId}",
            "ask_question_screen",
            "splash_screen",
            Routes.ARTICLES_SCREEN,
            Routes.AUDIO_LIST_SCREEN,
            Routes.IMAGES_SCREEN,
            Routes.IMAGE_DETAIL_SCREEN,
            Routes.KHOTAB_SCREEN,
            "${Routes.VIDEO_PLAYER_SCREEN}/{videoUrl}",
            Routes.VIDEOS_SCREEN,
            Routes.ABOUT_DR_HASSAN_SCREEN
        )
    }

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
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        bottomBar = {
            if (shouldShowBottomNav) {
                BottomNavigationBar(
                    modifier = Modifier.fillMaxWidth(), navController = navController
                )
            }
        }) { innerPadding ->

        NavHost(
            navController,
            startDestination = "splash_screen",
            modifier = Modifier.padding(innerPadding)
        ) {

            composable("splash_screen") {
                SplashScreen(
                    onShowSplashScreenTimeEnd = {
                        navController.navigate("home_screen") {
                            popUpTo("splash_screen") {
                                inclusive = true
                            }
                        }
                    })
            }

            composable("home_screen") {
                HomeScreen(onNavigateToDetailArticle = { articleId ->
                    navController.navigate("detail_article_screen/$articleId")

                }, onNavigateToDetailAudio = { title, audioUrl ->
                    val encodedUrl = Uri.encode(audioUrl)
                    navController.navigate("audio_detail_screen/$title/$encodedUrl")
                }, onCategoryClick = { route ->
                    navController.navigate(route)
                }


                )
            }
            composable("search_screen") {

                SearchScreen()
            }
            composable("profile_screen") {

            }

            composable("articles_screen") {

                ArticleListScreen(onNavigateToArticleDetail = { articleId ->
                    navController.navigate("detail_article_screen/$articleId")
                }, onNavigateBack = {
                    navController.popBackStack()
                })
            }
            composable("detail_article_screen/{articleId}") {
                ArticleDetailScreen {
                    navController.popBackStack()
                }
            }
            composable("audio_list_screen") {
                AudioListScreen(onNavigateToAudioDetail = { title, audioUrl ->
                    val encodedUrl = Uri.encode(audioUrl)
                    navController.navigate("audio_detail_screen/$title/$encodedUrl")
                }, onNavigateBack = {
                    navController.popBackStack()
                })
            }

            composable(
                route = "audio_detail_screen/{title}/{audioUrl}",
                arguments = listOf(navArgument("title") {
                    type = NavType.StringType
                }, navArgument("audioUrl") {
                    type = NavType.StringType
                })
            ) {
                AudioDetailScreen(
                    onNavigateUp = {
                        navController.popBackStack()
                    })
            }

            composable("study_zone_screen") {
                StudyZoneScreen()
            }

            composable(Routes.IMAGES_SCREEN) {

                ImagesGroupsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onGroupClick = { groupId ->
                        navController.navigate("${Routes.IMAGE_DETAIL_SCREEN}/$groupId")
                    }
                )
            }
            composable(
                route = "${Routes.IMAGE_DETAIL_SCREEN}/{groupId}",
                arguments = listOf(
                    navArgument("groupId") {
                        type = NavType.StringType
                    }
                )
            ) {
                ImageScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )

            }
            composable(Routes.ABOUT_DR_HASSAN_SCREEN) {
                AboutDrHassanScreen {
                    navController.popBackStack()
                }
            }
            composable(Routes.VIDEOS_SCREEN) {

                VideosScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }, onNavigateToVideo = { videoUrl ->
                        val encodedUrl = Uri.encode(videoUrl)
                        navController.navigate("${Routes.VIDEO_PLAYER_SCREEN}/$encodedUrl")
                    }
                )
            }
            composable(

                route = "${Routes.VIDEO_PLAYER_SCREEN}/{videoUrl}", arguments = listOf(
                    navArgument("videoUrl") {
                        type = NavType.StringType
                    })

            ) {
                val videoUrl = it.arguments?.getString("videoUrl")
                if (videoUrl != null) {
                    VideoPlayerScreen(
                        videoUrl = videoUrl, onNavigateBack = {
                            navController.popBackStack()
                        })
                }

            }
            composable(Routes.KHOTAB_SCREEN) {

            }
            composable(Routes.PROFILE_SCREEN) {
                ProfileScreen(
                    onLogout = onLogout,
                    onNavigateToAboutApp = {
//                        navController.navigate("cv_screen")
                    }
                )
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
        navController = navController, startDestination = "splash_screen"
    ) {


        composable("splash_screen") {
            SplashScreen(
                onShowSplashScreenTimeEnd = {
                    navController.navigate("login_screen") {
                        popUpTo("splash_screen") {
                            inclusive = true
                        }
                    }
                })
        }

        composable(route = "login_screen") {
            LoginScreen(

                onRegisterClick = {
                    navController.navigate("register_screen")
                    navController.clearBackStack("login_screen")
                }, onSuccessfulLogin = {
//                    navController.navigate("home_screen")
                    // go to home screen
                    /*Toast.makeText(
                        ,
                        "Login successful",
                        Toast.LENGTH_LONG
                    ).show()*/
                })
        }
        composable("register_screen") {
            RegisterScreen(modifier = Modifier.fillMaxSize(), onLoginClick = {
                navController.popBackStack()
            }, onSuccessfulRegister = {
                onLoginSuccess()
                // go to home screen
                /*  Toast.makeText(
                      applicationContext,
                      "Register successful",
                      Toast.LENGTH_LONG
                  ).show()*/
            })
        }
    }


}


