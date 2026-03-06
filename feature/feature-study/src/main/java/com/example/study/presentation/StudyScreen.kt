package com.example.study.presentation

import android.util.Log
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.study.presentation.dashboard.GuestContent
import com.example.study.presentation.dashboard.NotChannelMemberContent
import com.example.study.presentation.dashboard.StudentDashboardContent
import com.example.study.presentation.dashboard.StudyTopAppBar
import com.example.study.presentation.model.StudyScreenUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyScreen(
    viewModel: StudyViewModel = hiltViewModel(),
    onNavigateToLogin: () -> Unit,
    onLevelClick: (String) -> Unit,
    onQuizClick: (String) -> Unit,
    userEmail: String? = null,
    idToken: String? = null
) {


    Log.d("StudyScreen", "StudyScreen: userEmail: $userEmail")
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
        topBar = {

            if (uiState is StudyScreenUiState.StudentDashboard) {
                StudyTopAppBar()
//                CustomTopAppBar()
            } else
                CenterAlignedTopAppBar(title = { Text("My Study Dashboard") })
        }

    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = paddingValues.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding()
                ),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is StudyScreenUiState.Loading -> CircularProgressIndicator()
                is StudyScreenUiState.Error -> Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error
                )

                is StudyScreenUiState.StudentDashboard -> {
                    // Show the rich dashboard for students
                    StudentDashboardContent(
                        studentData = state.studentData,
                        onDisconnect = { /* viewModel.onDisconnectTelegram() */ },
                        onLevelClick = onLevelClick,
                        onQuizClick = onQuizClick
                    )
                }

                is StudyScreenUiState.Guest -> {
                    // Show the simple connect button for guests
                    GuestContent(
                        onConnect = onNavigateToLogin,
                        userEmail = userEmail,
                        idToken = idToken
                    )
                }

                is StudyScreenUiState.NotChannelMember -> {
                    NotChannelMemberContent(
                        studentData = state.studentData,
                        onDisconnect = { /* viewModel.onDisconnectTelegram() */ },
                        onRefreshClick = {
                            viewModel.onRefreshStudentData()
                        }
                    )
                }

            }
        }
    }
}

@Composable
fun TelegramLoginButton() {

    val context = LocalContext.current
    val telegramLoginUrl =
        "https://oauth.telegram.org/auth?bot_id=8255460260&origin=https://dr-hassan-al-hawary.web.app&return_to=https://dr-hassan-al-hawary.web.app/telegram-callback.html&request_access=write"

    Button(onClick = {
        val builder = CustomTabsIntent.Builder()
        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(context, telegramLoginUrl.toUri())
    }) {
        Text("Connect to Telegram")
    }
}


