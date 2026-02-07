package com.example.admin

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.admin.ui.control_screen.ControlScreen
import com.example.admin.ui.institute_main.InstituteMainScreen
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HassanAlHawaryTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val context = LocalContext.current

                val topBarTitle = when (currentRoute) {
                    "control_screen" -> "Admin Control Panel"
                    "articles_upload" -> "Upload Article"
                    "audios_upload" -> "Upload Audio"
                    "videos_upload" -> "Upload Video"
                    "images_upload" -> "Upload Images"
                    "institute_upload" -> "Institute Management"
                    "upload_quiz" -> "Upload Quiz"
                    "upload_announcement" -> "Upload Announcement"
                    "playlists/{levelName}" -> navBackStackEntry?.arguments?.getString("levelName") ?: "Playlists"
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
                            ControlScreen(
                                onNavigate = { route ->
                                    navController.navigate(route)
                                }
                            )
                        }
                        composable("articles_upload") {
                            ArticleUploadScreen(
                                onArticleUploaded = {
                                    navController.popBackStack()
                                }
                            )
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
                        composable("institute_upload") {
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
                            UploadAnnouncementScreen()
                        }
                        composable(
                            route = "playlists/{levelName}",
                            arguments = listOf(navArgument("levelName") { type = NavType.StringType })
                        ) {
                            val levelName = it.arguments?.getString("levelName") ?: ""
                            PlaylistScreen(levelName = levelName, onEditPlaylist = {
                                // Placeholder for a real navigation
                                Toast.makeText(context, "Edit playlist $it", Toast.LENGTH_SHORT).show()
                            })
                        }
                    }
                }
            }
        }
    }
}