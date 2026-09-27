package com.example.feature.video.presentation.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.example.core.ui.R
import com.example.core.ui.components.AppTopBar
import com.example.core.ui.theme.Brand
import com.example.core.ui.theme.HassanAlHawaryTheme
import com.example.domain.module.Video
import com.example.feature.video.presentation.components.VideoCard


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideosScreen(
    viewModel: VideosViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToVideo: (String, String) -> Unit
) {
    val videos = viewModel.videos.collectAsLazyPagingItems()

    VideosScreenContent(
        videos = videos,
        categoryTitle = viewModel.categoryTitle,
        onNavigateBack = onNavigateBack,
        onNavigateToVideo = onNavigateToVideo
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideosScreenContent(
    videos: LazyPagingItems<Video>,
    categoryTitle: String? = null,
    onNavigateBack: () -> Unit,
    onNavigateToVideo: (String, String) -> Unit
) {
    // "الكل" is a category on the previous screen, not a title: show "الفيديوهات" for it.
    val title = categoryTitle?.takeIf { it.isNotBlank() && it != ALL_CATEGORIES_TITLE }
        ?: stringResource(id = R.string.videos)
    Scaffold(
        containerColor = Brand.colors.background,
        topBar = { AppTopBar(title = title, onBack = onNavigateBack) },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val isMediatorRefreshing = videos.loadState.mediator?.refresh is LoadState.Loading

            if (isMediatorRefreshing) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        strokeWidth = 3.dp,
                        color = Brand.colors.accentStrong
                    )
                }
            } else if (videos.itemCount == 0 && videos.loadState.refresh is LoadState.NotLoading) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.VideoLibrary,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.no_videos_available),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        count = videos.itemCount,
                        key = videos.itemKey { it.id }
                    ) { index ->
                        val video = videos[index]
                        if (video != null) {
                            VideoCard(
                                video = video,
                                onVideoClick = {
                                    onNavigateToVideo(video.videoUrl, video.title)
                                }
                            )
                        }
                    }

                    item {
                        if (videos.loadState.append is LoadState.Loading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(32.dp),
                                    strokeWidth = 2.dp,
                                    color = Brand.colors.accentStrong
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private const val ALL_CATEGORIES_TITLE = "الكل"

@Preview(
    showBackground = true,
    showSystemUi = true,
    device = Devices.PIXEL_7,
    name = "قائمة الفيديوهات"
)
@Composable
fun VideosScreenPreview() {
    HassanAlHawaryTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Surface(color = MaterialTheme.colorScheme.surface) {
                // In a real preview we'd need to mock LazyPagingItems, 
                // but this shows the overall screen structure.
            }
        }
    }
}