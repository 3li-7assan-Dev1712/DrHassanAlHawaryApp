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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.core.ui.R
import com.example.core.ui.components.AppTopBar
import com.example.core.ui.theme.Brand
import com.example.domain.module.Student
import com.example.study.presentation.dashboard.InstituteComingSoonContent
import com.example.study.presentation.dashboard.InstituteComingSoonTwoPane
import com.example.core.ui.theme.layoutTokens
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

    // Expanded: the "coming soon" state in two panes (Figma 61:2211). Loading keeps the
    // phone layout.
    val current = uiState
    if (layoutTokens.isExpanded && current !is StudyScreenUiState.Loading) {
        InstituteComingSoonTwoPane(studentData = current.student, title = stringResource(R.string.dr_hassan_institute))
        return
    }

    Scaffold(
        containerColor = Brand.colors.background,
        contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
        // Tab root: start-aligned title, no back arrow.
        topBar = { AppTopBar(title = stringResource(R.string.dr_hassan_institute)) }
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
                is StudyScreenUiState.Loading -> CircularProgressIndicator(color = Brand.colors.accentStrong)

                // Student interaction is temporarily paused: every state shows the "coming
                // soon" placeholder instead of the dashboard, the Telegram connect button
                // (GuestContent) or NotChannelMemberContent.
                else -> InstituteComingSoonContent(studentData = state.student)
            }
        }
    }
}

/** The student on the "coming soon" screen; a guest (Telegram not connected yet) has none. */
private val StudyScreenUiState.student: Student?
    get() = when (this) {
        is StudyScreenUiState.StudentDashboard -> studentData
        is StudyScreenUiState.NotChannelMember -> studentData
        else -> null
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


