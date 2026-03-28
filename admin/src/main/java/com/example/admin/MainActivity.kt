package com.example.admin

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.admin.ui.MainActivityState
import com.example.admin.ui.MainActivityViewModel
import com.example.admin.ui.add_edit_lesson.AddEditLessonScreen
import com.example.admin.ui.add_edit_playlist.AddEditPlaylistScreen
import com.example.admin.ui.connect_telegram.ConnectTelegramScreen
import com.example.admin.ui.control_screen.ControlScreen
import com.example.admin.ui.institute_main.InstituteMainScreen
import com.example.admin.ui.lessons.LessonsScreen
import com.example.admin.ui.playlist.PlaylistScreen
import com.example.admin.ui.super_admin.SuperAdminScreen
import com.example.admin.ui.theme.HassanAlHawaryTheme
import com.example.admin.ui.upload_announcement.UploadAnnouncementScreen
import com.example.admin.ui.upload_article_screen.ArticleUploadScreen
import com.example.admin.ui.upload_audio_screen.AudioUploadScreen
import com.example.admin.ui.upload_images_screen.UploadImagesScreen
import com.example.admin.ui.upload_motivational_messages.UploadMotivationalMessagesScreen
import com.example.admin.ui.upload_quiz.UploadQuizScreen
import com.example.admin.ui.upload_video_screen.UploadVideoScreen
import com.example.admin.util.LocaleForce
import com.example.core.ui.animation.LoadingScreen
import com.example.feature.auth.presentation.login.LoginScreen
import com.example.feature.auth.presentation.register.RegisterScreen
import com.example.profile.presentation.profile.ProfileScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
@ExperimentalMaterial3Api
class MainActivity : ComponentActivity() {

    private val mainActivityViewModel: MainActivityViewModel by viewModels()

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleForce.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val splashScreen = installSplashScreen()


        splashScreen.setKeepOnScreenCondition {
            mainActivityViewModel.state.value.isLoading
        }

        enableEdgeToEdge()
        setContent {
            HassanAlHawaryTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val state: MainActivityState by mainActivityViewModel.state.collectAsState()

                val topBarTitle = when (currentRoute) {
                    "control_screen" -> stringResource(R.string.admin_control_panel)
                    "articles_upload" -> stringResource(R.string.upload_article)
                    "audios_upload" -> stringResource(R.string.upload_audio)
                    "videos_upload" -> stringResource(R.string.upload_video)
                    "profile_screen" -> stringResource(R.string.profile)
                    "login_screen" -> stringResource(R.string.login)
                    "register_screen" -> stringResource(R.string.register)
                    "images_upload" -> stringResource(R.string.upload_images)
                    "not_allowed" -> stringResource(R.string.access_denied)
                    "super_admin_panel" -> stringResource(R.string.super_admin_panel)
                    "telegram_login", "telegram_login?data={data}" -> stringResource(R.string.institute_management)
                    "upload_quiz" -> stringResource(R.string.upload_quiz)
                    "upload_announcement" -> stringResource(R.string.upload_announcement)
                    "upload_motivational_messages" -> stringResource(R.string.upload_motivational_messages)
                    "playlists/{levelName}" -> navBackStackEntry?.arguments?.getString("levelName")
                        ?: stringResource(R.string.playlists)

                    "lessons/{playlistId}" -> stringResource(R.string.lessons)
                    "add_edit_playlist?levelId={levelId}&playlistId={playlistId}" -> {
                        if (navBackStackEntry?.arguments?.getString("playlistId") == null) 
                            stringResource(R.string.add_playlist) 
                        else 
                            stringResource(R.string.edit_playlist)
                    }

                    "add_edit_lesson?playlistId={playlistId}&lessonId={lessonId}" -> {
                        if (navBackStackEntry?.arguments?.getString("lessonId") == null) 
                            stringResource(R.string.add_lesson) 
                        else 
                            stringResource(R.string.edit_lesson)
                    }

                    else -> ""
                }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(topBarTitle) },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                                navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            navigationIcon = {
                                if (currentRoute != "control_screen" && currentRoute != "not_allowed" && currentRoute != "login_screen" && currentRoute != null) {
                                    IconButton(onClick = { navController.popBackStack() }) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Back"
                                        )
                                    }
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination =
                            if (state.isLoading) {
                                "loading_screen"
                            } else {
                                if (state.isAdminLoggedIn) {
                                    if (state.isUserAdmin) "control_screen" else "not_allowed"
                                } else "login_screen"
                            },
                        modifier = Modifier.padding(innerPadding)
                    ) {


                        composable("loading_screen") {
                            LoadingScreen()
                        }

                        composable("super_admin_panel") {
                            SuperAdminScreen(
                                isSuperAdmin = state.isUserSuperAdmin
                            ) {
                                mainActivityViewModel.logoutSuccess()
                            }
                        }
                        // auth screens
                        composable(route = "login_screen") {
                            LoginScreen(

                                onRegisterClick = {
                                    navController.navigate("register_screen")
                                }, onSuccessfulLogin = {
                                    mainActivityViewModel.loginSuccess()
                                })
                        }
                        composable("register_screen") {
                            RegisterScreen(modifier = Modifier.fillMaxSize(), onLoginClick = {
                                navController.popBackStack()
                            }, onSuccessfulRegister = {
                                mainActivityViewModel.loginSuccess()
                            })
                        }

                        composable("not_allowed") {
                            NotAllowedScreen(onLogout = {
                                mainActivityViewModel.logoutSuccess()
                            })
                        }


                        composable("control_screen") {
                            ControlScreen { route ->
                                if (route == "telegram_login") {
                                    if (state.isAdminConnectedToTelegram) {
                                        navController.navigate(route)
                                    } else {
                                        navController.navigate("connect_telegram")

                                    }
                                } else {
                                    navController.navigate(route)
                                }
                            }
                        }
                        composable("connect_telegram") {
                            ConnectTelegramScreen()
                        }
                        composable("articles_upload") {
                            ArticleUploadScreen {
                                navController.popBackStack()
                            }
                        }
                        composable("audios_upload") {
                            AudioUploadScreen()
                        }
                        composable("videos_upload") {
                            UploadVideoScreen {
                                navController.popBackStack()
                            }
                        }
                        composable("images_upload") {
                            UploadImagesScreen()
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

                            InstituteMainScreen(
                                onUploadQuiz = { navController.navigate("upload_quiz") },
                                onUploadAnnouncement = { navController.navigate("upload_announcement") },
                                onUploadMotivationalMessages = { navController.navigate("upload_motivational_messages") },
                                onLevelClick = { levelName ->
                                    navController.navigate("playlists/$levelName")
                                }
                            )
                        }
                        composable("profile_screen") {
                            ProfileScreen(
                                isAdmin = true,
                                onNavigate = { route ->

                                },
                                onThemeChanged = { isDarkTheme ->

                                },
                                isDarkTheme = isSystemInDarkTheme(),
                                onLogout = {
                                    mainActivityViewModel.logoutSuccess()
                                }
                            )
                        }
                        composable("upload_quiz") {
                            UploadQuizScreen(onUploadSuccess = {
                                navController.popBackStack()
                            })
                        }
                        composable("upload_announcement") {
                            UploadAnnouncementScreen {
                                navController.popBackStack()
                            }
                        }
                        composable("upload_motivational_messages") {
                            UploadMotivationalMessagesScreen {
                                navController.popBackStack()
                            }
                        }
                        composable(
                            route = "playlists/{levelId}",
                            arguments = listOf(navArgument("levelId") {
                                type = NavType.StringType
                            })
                        ) {
                            val levelId = it.arguments?.getString("levelId") ?: ""
                            PlaylistScreen(
                                levelName = levelId,
                                onAddPlaylist = { navController.navigate("add_edit_playlist?levelId=$levelId") },
                                onEditPlaylist = { playlistId ->
                                    navController.navigate("add_edit_playlist?levelId=$levelId&playlistId=$playlistId")
                                },
                                onPlaylistClick = { playlistId ->
                                    navController.navigate("lessons/$playlistId")
                                }
                            )
                        }
                        composable(
                            route = "lessons/{playlistId}",
                            arguments = listOf(navArgument("playlistId") {
                                type = NavType.StringType
                            })
                        ) {
                            val playlistId = it.arguments?.getString("playlistId") ?: ""
                            LessonsScreen(
                                playlistId = playlistId,
                                onAddLesson = { navController.navigate("add_edit_lesson?playlistId=$playlistId") },
                                onEditLesson = { lessonId ->
                                    navController.navigate("add_edit_lesson?playlistId=$playlistId&lessonId=$lessonId")
                                }
                            )
                        }
                        composable(
                            route = "add_edit_playlist?levelId={levelId}&playlistId={playlistId}",
                            arguments = listOf(
                                navArgument("playlistId") {
                                    type = NavType.StringType; nullable = true
                                },
                                navArgument("levelId") {
                                    type = NavType.StringType; nullable = true
                                }

                            )
                        ) {
                            AddEditPlaylistScreen {
                                navController.popBackStack()
                            }
                        }
                        composable(
                            route = "add_edit_lesson?playlistId={playlistId}&lessonId={lessonId}",
                            arguments = listOf(
                                navArgument("playlistId") {
                                    type = NavType.StringType; nullable = true
                                },
                                navArgument("lessonId") {
                                    type = NavType.StringType; nullable = true
                                }
                            )
                        ) {
                            AddEditLessonScreen {
                                navController.popBackStack()
                                navController.popBackStack()

                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun NotAllowedScreen(onLogout: () -> Unit) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.access_denied),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.not_allowed_msg),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = onLogout) {
                Text(stringResource(R.string.logout))
            }
        }
    }
}
