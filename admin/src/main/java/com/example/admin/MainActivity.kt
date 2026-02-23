package com.example.admin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
import com.example.admin.ui.theme.HassanAlHawaryTheme
import com.example.admin.ui.upload_announcement.UploadAnnouncementScreen
import com.example.admin.ui.upload_article_screen.ArticleUploadScreen
import com.example.admin.ui.upload_audio_screen.AudioUploadScreen
import com.example.admin.ui.upload_images_screen.UploadImagesScreen
import com.example.admin.ui.upload_quiz.UploadQuizScreen
import com.example.admin.ui.upload_video_screen.UploadVideoScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
@ExperimentalMaterial3Api
class MainActivity : ComponentActivity() {

    private val mainActivityViewModel: MainActivityViewModel by viewModels()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HassanAlHawaryTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val state: MainActivityState by mainActivityViewModel.state.collectAsState()

                val topBarTitle = when (currentRoute) {
                    "control_screen" -> "Admin Control Panel"
                    "articles_upload" -> "Upload Article"
                    "audios_upload" -> "Upload Audio"
                    "videos_upload" -> "Upload Video"
                    "images_upload" -> "Upload Images"
                    "telegram_login", "telegram_login?data={data}" -> "Institute Management"
                    "upload_quiz" -> "Upload Quiz"
                    "upload_announcement" -> "Upload Announcement"
                    "playlists/{levelName}" -> navBackStackEntry?.arguments?.getString("levelName")
                        ?: "Playlists"

                    "lessons/{playlistId}" -> "Lessons"
                    "add_edit_playlist?playlistId={playlistId}" -> {
                        if (navBackStackEntry?.arguments?.getString("playlistId") == null) "Add Playlist" else "Edit Playlist"
                    }

                    "add_edit_lesson?lessonId={lessonId}" -> {
                        if (navBackStackEntry?.arguments?.getString("lessonId") == null) "Add Lesson" else "Edit Lesson"
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
                                if (currentRoute != "control_screen" && currentRoute != null) {
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
                        startDestination = "control_screen",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("control_screen") {
                            ControlScreen {
                                navController.navigate(it)
                            }
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
                                onLevelClick = { levelName ->
                                    navController.navigate("playlists/$levelName")
                                }
                            )
                        }
                        composable("upload_quiz") {
                            UploadQuizScreen()
                        }
                        composable("upload_announcement") {
                            UploadAnnouncementScreen {
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
}
