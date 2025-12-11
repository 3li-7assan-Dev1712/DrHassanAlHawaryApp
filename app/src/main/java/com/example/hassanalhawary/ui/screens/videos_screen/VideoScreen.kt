package com.example.hassanalhawary.ui.screens.videos_screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hassanalhawary.R
import com.example.hassanalhawary.ui.screens.videos_screen.components.Video
import com.example.hassanalhawary.ui.screens.videos_screen.components.VideoCard
import com.example.hassanalhawary.ui.theme.HassanAlHawaryTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideosScreen(
    videos: List<Video>,
    onNavigateBack: () -> Unit,
    onNavigateToVideo: (String) -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.videos)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            items(videos) { video ->
                VideoCard(
                    video = video,
                    onVideoClick = { videoUrl ->
                        // The simplest way to play a video is to launch a YouTube/web intent
//                        val intent = Intent(Intent.ACTION_VIEW, videoUrl.toUri())
//                        context.startActivity(intent)
                        onNavigateToVideo(videoUrl)
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun VideosScreenPreview() {
    val mockVideos = remember {
        listOf(
            Video(
                "1",
                "The Principles of Islamic Jurisprudence",
                "A deep dive into Usool Al-Fiqh.",
                "45:12",
                "https://www.youtube.com/watch?v=_-6I2j5nP1M&t=1s",
                R.drawable.design_2
            ),
            Video(
                "1",
                "The Principles of Islamic Jurisprudence",
                "A deep dive into Usool Al-Fiqh.",
                "45:12",
                "https://www.youtube.com/watch?v=_-6I2j5nP1M&t=999s",
                R.drawable.design_2
            ),
            Video(
                "1",
                "The Principles of Islamic Jurisprudence",
                "A deep dive into Usool Al-Fiqh.",
                "45:12",
                "https://www.youtube.com/watch?v=_-6I2j5nP1M&t=999s",
                R.drawable.design_2
            ),
            Video(
                "1",
                "The Principles of Islamic Jurisprudence",
                "A deep dive into Usool Al-Fiqh.",
                "45:12",
                "https://www.youtube.com/watch?v=_-6I2j5nP1M&t=999s",
                R.drawable.design_2
            ),
            Video(
                "1",
                "The Principles of Islamic Jurisprudence",
                "A deep dive into Usool Al-Fiqh.",
                "45:12",
                "https://www.youtube.com/watch?v=_-6I2j5nP1M&t=999s",
                R.drawable.design_2
            ),
            Video(
                "1",
                "The Principles of Islamic Jurisprudence",
                "A deep dive into Usool Al-Fiqh.",
                "45:12",
                "https://www.youtube.com/watch?v=_-6I2j5nP1M&t=999s",
                R.drawable.design_2
            ),
            Video(
                "1",
                "The Principles of Islamic Jurisprudence",
                "A deep dive into Usool Al-Fiqh.",
                "45:12",
                "https://www.youtube.com/watch?v=_-6I2j5nP1M&t=999s",
                R.drawable.design_2
            ),
            Video(
                "1",
                "The Principles of Islamic Jurisprudence",
                "A deep dive into Usool Al-Fiqh.",
                "45:12",
                "https://www.youtube.com/watch?v=_-6I2j5nP1M&t=999s",
                R.drawable.design_2
            ),

            )
    }
    HassanAlHawaryTheme {
        VideosScreen(onNavigateBack = {}, videos = mockVideos) {

        }
    }
}