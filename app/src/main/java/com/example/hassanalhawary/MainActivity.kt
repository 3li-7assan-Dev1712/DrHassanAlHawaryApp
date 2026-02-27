package com.example.hassanalhawary

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
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
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
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
import com.example.feature.onboarding.presentation.OnboardingScreen
import com.example.feature.video.presentation.detail.VideoPlayerScreen
import com.example.feature.video.presentation.list.VideosScreen
import com.example.hassanalhawary.core.util.LocaleForce
import com.example.hassanalhawary.ui.navigation.BottomNavigationBar
import com.example.hassanalhawary.ui.navigation.Routes
import com.example.hassanalhawary.ui.theme.HassanAlHawaryTheme
import com.example.profile.presentation.about_app.AboutAppScreen
import com.example.profile.presentation.components.LegalTextScreen
import com.example.profile.presentation.components.ProfileRoute
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
import com.example.study.presentation.quiz.AnswerQuizScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {


    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleForce.wrap(newBase))
    }

    private val mainActivityViewModel: MainActivityViewModel by viewModels()
    val TAG = "MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()


        splashScreen.setKeepOnScreenCondition {
            !mainActivityViewModel.appReady.value
        }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeState by mainActivityViewModel.themeState.collectAsState()
            val onboardingCompleted by mainActivityViewModel.onboardingCompleted.collectAsState()
            val mainActivityState by mainActivityViewModel.state.collectAsState()

            // Optional extra safety: don't draw anything until splash should go away
            if (!mainActivityViewModel.appReady.collectAsState().value) return@setContent

            HassanAlHawaryTheme(darkTheme = themeState.isDarkTheme) {

                when (onboardingCompleted) {
                    null -> {
                        // still loading onboarding flag -> splash is still visible anyway
                        return@HassanAlHawaryTheme
                    }

                    false -> {
                        OnboardingScreen(
                            onFinished = { mainActivityViewModel.updateOnboardingCompleted() }
                        )
                    }

                    true -> {
                        when {
                            mainActivityState.isLoading -> {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator()
                                }
                            }

                            mainActivityState.isUserLoggedIn -> {
                                MainAppContent(
                                    onLogout = { mainActivityViewModel.logoutSuccess() },
                                    isDarkThemeEnabled = themeState.isDarkTheme,
                                    userEmail = mainActivityState.currentUserDate?.email ?: ""
                                )
                            }

                            else -> {
                                AuthNavHost(onLoginSuccess = {
                                    mainActivityViewModel.loginSuccess()

                                })
                            }
                        }
                    }
                }
                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = {

                    }
                )

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= 33) {
                        launcher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }
        }
    }


    @Composable

    fun MainAppContent(
        onLogout: () -> Unit,
        isDarkThemeEnabled: Boolean = false,
        userEmail: String
        ) {
        val navController = rememberNavController()

        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        // routes where the bottom nav should be shown
        val routesWithBottomNav = remember {
            setOf(
                Routes.HOME_SCREEN,
                Routes.SEARCH_SCREEN,
                Routes.PROFILE_SCREEN,
                "telegram_login?data={data}"
            )
        }

        val shouldShowBottomNav = remember(currentRoute) {
            derivedStateOf {
                currentRoute != null && routesWithBottomNav.any { routePattern ->
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
                    route = "telegram_login?data={data}&t={t}",
                    arguments = listOf(
                        navArgument("data") {
                            type = NavType.StringType
                            nullable = true
                        },
                        navArgument("t") {
                            type = NavType.LongType
                            defaultValue = -1L
                        }
                    ),
                    deepLinks = listOf(
                        navDeepLink {
                            uriPattern =
                                "com.example.hassanalhawary://telegram-login?data={data}&t={t}"
                        }
                    )
                ) {
                    StudyScreen(
                        userEmail = userEmail,
                        onLevelClick = { levelId ->
                            navController.navigate("${Routes.PLAYLIST_SCREEN}/$levelId")
                        },
                        onNavigateToLogin = { },
                        onQuizClick = { quizId ->
                            navController.navigate("${Routes.QUIZ_SCREEN}/$quizId")
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

                    route = "${Routes.QUIZ_SCREEN}/{quizId}",
                    arguments = listOf(navArgument("quizId") {
                        type = NavType.StringType
                    })

                ) {
                    AnswerQuizScreen(
                        onNavigateBack = {
                            navController.popBackStack()
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
                        onThemeChanged = { isDarkTheme ->
                            Log.d(TAG, "MainAppContent: isDarkTheme: $isDarkTheme")
                            mainActivityViewModel.updateDarkThemePreference(isDarkTheme)
                        },
                        isDarkTheme = isDarkThemeEnabled,
                        onLogout = {
                            onLogout()
                        }
                    )
                }

                composable(ProfileDestinations.ABOUT) {
                    AboutAppScreen(onBack = { navController.popBackStack() })
                }

                composable(ProfileDestinations.SHARE) {
                    ShareAppScreen(
                        "com.example.hassanalhawary",
                        onBack = { navController.popBackStack() },

                        )
                }

                composable(ProfileDestinations.RATE) {
                    RateAppScreen(
                        packageName = "com.example.hassanalhawary",
                        onBack = { navController.popBackStack() },

                        )
                }

                composable(ProfileDestinations.PRIVACY) {
                    LegalTextScreen(
                        title = "سياسة الخصوصية",
                        assetFileName = "privacy.md",
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(ProfileDestinations.TERMS) {
                    LegalTextScreen(
                        title = "الشروط والأحكام",
                        assetFileName = "terms.md",
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(ProfileDestinations.LICENSES) {
                    LegalTextScreen(
                        title = "التراخيص والمصادر المفتوحة",
                        assetFileName = "licenses.md",
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(ProfileDestinations.SUPPORT) {
                    SupportScreen(
                        onBack = { navController.popBackStack() }
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
