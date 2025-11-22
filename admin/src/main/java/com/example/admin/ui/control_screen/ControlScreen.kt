package com.example.admin.ui.control_screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.admin.ui.control_screen.components.AudioUploadScreen
import com.example.admin.ui.upload_article_screen.ArticleUploadScreen

@ExperimentalMaterial3Api
@Composable
fun ControlScreen(
    controlScreenViewModel: ControlScreenViewModel = hiltViewModel()
) {
    // 0 = Article, 1 = Audio
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("New Article", "New Audio")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Control Panel") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)

            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            // Content changes based on the selected tab
            when (selectedTabIndex) {
                0 -> ArticleUploadScreen {

                }
                1 -> AudioUploadScreen()
            }
        }
    }
}


