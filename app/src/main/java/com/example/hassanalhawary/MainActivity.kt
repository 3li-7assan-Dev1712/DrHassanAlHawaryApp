package com.example.hassanalhawary

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.core_ui.splash_screen.SplashScreen
import com.example.feature.about_dr_hassan.presentation.AboutDrHassanScreen
import com.example.feature.article.presentation.detail.ArticleDetailScreen
import com.example.feature.article.presentation.list.ArticleListScreen
import com.example.feature.audio.presentation.detail.AudioDetailScreen
import com.example.feature.audio.presentation.list.AudioListScreen
import com.example.feature.auth.presentation.login.LoginScreen
import com.example.feature.auth.presentation.register.RegisterScreen
import com.example.feature.home.presentation.HomeScreen
import com.example.feature.image.presentation.detail.ImageScreen
import com.example.feature.image.presentation.list.ImagesGroupsScreen
import com.example.feature.video.presentation.detail.VideoPlayerScreen
import com.example.feature.video.presentation.list.VideosScreen
import com.example.hassanalhawary.ui.navigation.BottomNavigationBar
import com.example.hassanalhawary.ui.navigation.Routes
import com.example.hassanalhawary.ui.theme.HassanAlHawaryTheme
import com.example.profile.domain.model.privacyBody
import com.example.profile.domain.model.termsBody
import com.example.profile.presentation.about_app.AboutAppScreen
import com.example.profile.presentation.components.ProfileRoute
import com.example.profile.presentation.legal.LegalTextScreen
import com.example.profile.presentation.navigation.ProfileDestinations
import com.example.profile.presentation.profile.ProfileScreen
import com.example.profile.presentation.rate_app.RateAppScreen
import com.example.profile.presentation.share_app.ShareAppScreen
import com.example.profile.presentation.support.SupportScreen
import com.example.search.presentation.SearchScreen
import com.example.study.presentation.StudyScreen
import com.example.study.presentation.detail.LessonDetailScreen
import com.example.study.presentation.lessons.LessonsListScreen
import com.example.study.presentation.playlist.PlaylistScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {


    val TAG = "MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HassanAlHawaryTheme {

                val mainActivityViewModel = viewModel<MainActivityViewModel>()
                val mainActivityState by mainActivityViewModel.state.collectAsState()

                val isLoggedIn = mainActivityState.isUserLoggedIn

                Log.d(TAG, "onCreate: isLoggedIn: $isLoggedIn")
                val rootNavController =
                    rememberNavController() // Single NavController for switching graphs

                if (false) {
                    SplashScreen(onShowSplashScreenTimeEnd = {
                        mainActivityViewModel.updateShowSplashVal(false)
                    })
                } else {
                    // After the splash screen, we decide which graph to show based on the login state.
                    val isLoggedIn = mainActivityState.isUserLoggedIn
                    when {
                        mainActivityState.isLoading -> {

                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }

                        }

                        isLoggedIn -> {
                            MainAppContent(
                                onLogout = {
                                    Log.d(TAG, "Logout event received")
                                    mainActivityViewModel.logoutSuccess()
                                }
                            )

                        }

                        else -> {
                            AuthNavHost(
                                onLoginSuccess = {
                                    mainActivityViewModel.loginSuccess()
                                }
                            )
                        }
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
                "detail_article_screen/{articleId}/{paragraphIndex}",
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
                startDestination = "home_screen",
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
                        navController.navigate("detail_article_screen/$articleId/-1")

                    }, onNavigateToDetailAudio = { title, audioUrl ->
                        val encodedUrl = Uri.encode(audioUrl)
                        navController.navigate("audio_detail_screen/$title/$encodedUrl")
                    }, onCategoryClick = { route ->
                        navController.navigate(route)
                    }


                    )
                }
                composable("search_screen") {

                    SearchScreen { searchResultMetaData ->
                        val encodedUrl = Uri.encode(searchResultMetaData.url)
                        when (searchResultMetaData.type) {
                            "article" -> {

                                val objectID = searchResultMetaData.objectID
                                val articleId = if (objectID.contains("_")) {
                                    objectID.substringBeforeLast("_")
                                } else {
                                    objectID
                                }
                                val paragraphIndex = if (objectID.contains("_")) {
                                    objectID.substringAfterLast("_")
                                } else {
                                    objectID
                                }
                                val route = "detail_article_screen/$articleId/$paragraphIndex"
                                Log.d(TAG, "MainAppContent: route")
                                navController.navigate(route)
                            }

                            "audio" -> navController.navigate("audio_detail_screen/${searchResultMetaData.title}/${encodedUrl}")
                            "image_group" -> navController.navigate("${Routes.IMAGE_DETAIL_SCREEN}/${searchResultMetaData.objectID}")
                            "video" -> navController.navigate("${Routes.VIDEO_PLAYER_SCREEN}/${encodedUrl}")
                            else -> {

                            }
                        }
                    }
                }
                composable("articles_screen") {

                    ArticleListScreen(onNavigateToArticleDetail = { articleId ->
                        navController.navigate("detail_article_screen/$articleId/-1")
                    }, onNavigateBack = {
                        navController.popBackStack()
                    })
                }
                composable(
                    // Update the route to include an optional parameter
                    route = "detail_article_screen/{articleId}/{paragraphIndex}",
                    arguments = listOf(
                        navArgument("articleId") { type = NavType.StringType },
                        navArgument("paragraphIndex") {
                            type = NavType.IntType
                            defaultValue = -1
                        }
                    )
                ) { backStackEntry ->
                    ArticleDetailScreen(
//                        paragraphIndex = backStackEntry.arguments?.getInt("paragraphIndex")?.takeIf { it != -1 },
                        onNavigateBack = { navController.popBackStack() }
                    )
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

                composable(
                    route = "telegram_login?data={data}",
                    arguments = listOf(
                        navArgument("data") {
                            type = NavType.StringType
                            nullable = true
                        }
                    ),
                    deepLinks = listOf(
                        navDeepLink {
                            uriPattern =
                                "com.example.hassanalhawary://telegram-login?data={data}"
                        }
                    )
                ) {

                    StudyScreen(
                        onLevelClick = { levelId ->
                            navController.navigate("${Routes.PLAYLIST_SCREEN}/$levelId")
                        },
                        onNavigateToLogin = {
                        }
                    )
                }

                composable(

                    route = "${Routes.PLAYLIST_SCREEN}/{levelId}",
                    arguments = listOf(navArgument("levelId") {
                        type = NavType.StringType
                    })

                ) {
                    PlaylistScreen(
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onPlaylistClick = { playlistId ->
                            navController.navigate("${Routes.LESSONS_SCREEN}/$playlistId")
                        }
                    )
                }
                composable(
                    route = "${Routes.LESSONS_SCREEN}/{playlistId}",
                    arguments = listOf(navArgument("playlistId") {
                        type = NavType.StringType
                    })

                ) {

                    LessonsListScreen(
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onLessonClick = { lessonId ->
                            navController.navigate("${Routes.LESSON_DETAIL_SCREEN}/$lessonId")
                        }
                    )

                }

                composable(
                    route = "${Routes.LESSON_DETAIL_SCREEN}/{lessonId}",
                    arguments = listOf(navArgument("lessonId") {
                        type = NavType.StringType
                    })

                ) {

                    LessonDetailScreen(
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        /*onPlayAudioClick = {

                        }, onOpenPdfClick = {

                        }, lesson = Lesson(
                            id = "1",
                            title = "Introduction to Islamic Beliefs",
                            audioUrl = "",
                            pdfUrl = "",
                            duration = "1.32"
                        )*/
                    )

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

                // profile screens
                composable(Routes.PROFILE_SCREEN) {
                    ProfileScreen(
                        onNavigate = { route ->
                            when (route) {
                                ProfileRoute.About -> navController.navigate(ProfileDestinations.ABOUT)
                                ProfileRoute.Share -> navController.navigate(ProfileDestinations.SHARE)
                                ProfileRoute.Rate -> navController.navigate(ProfileDestinations.RATE)
                                ProfileRoute.Privacy -> navController.navigate(ProfileDestinations.PRIVACY)
                                ProfileRoute.Terms -> navController.navigate(ProfileDestinations.TERMS)
                                ProfileRoute.Licenses -> navController.navigate(ProfileDestinations.LICENSES)
                                ProfileRoute.Support -> navController.navigate(ProfileDestinations.SUPPORT)
                            }
                        },
                        onLogout = { /* your auth flow */ }
                    )
                }

                composable(ProfileDestinations.ABOUT) {
                    AboutAppScreen(onBack = { navController.popBackStack() })
                }

                composable(ProfileDestinations.SHARE) {
                    ShareAppScreen(
                        onBack = { navController.popBackStack() },
                        onShareClick = { /* launch share intent */ }
                    )
                }

                composable(ProfileDestinations.RATE) {
                    RateAppScreen(
                        onBack = { navController.popBackStack() },
                        onRateClick = { /* open Play Store */ }
                    )
                }

                composable(ProfileDestinations.PRIVACY) {
                    LegalTextScreen(
                        title = "سياسة الخصوصية",
                        body = privacyBody,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(ProfileDestinations.TERMS) {
                    LegalTextScreen(
                        title = "الشروط والأحكام",
                        body = termsBody,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(ProfileDestinations.LICENSES) {
                    LegalTextScreen(
                        title = "التراخيص والمصادر المفتوحة",
                        body = "ضع نص التراخيص هنا أو افتح شاشة licenses الرسمية.",
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(ProfileDestinations.SUPPORT) {
                    SupportScreen(
                        onBack = { navController.popBackStack() },
                        onEmailClick = { /* open email intent */ },
                        onTelegramClick = { /* open telegram channel */ }
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
            navController = navController, startDestination = "login_screen"
        ) {


            composable(route = "login_screen") {
                LoginScreen(

                    onRegisterClick = {
                        navController.navigate("register_screen")
                        navController.clearBackStack("login_screen")
                    }, onSuccessfulLogin = {
                        onLoginSuccess()
                    })
            }
            composable("register_screen") {
                RegisterScreen(modifier = Modifier.fillMaxSize(), onLoginClick = {
                    navController.popBackStack()
                }, onSuccessfulRegister = {
                    onLoginSuccess()
                })
            }
        }
    }

}


