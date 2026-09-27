package com.example.feature.audio.presentation.list

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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
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
import com.example.domain.module.Audio
import com.example.feature.audio.presentation.components.AudioListItem


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioListScreen(
    onNavigateToAudioDetail: (title: String, audioId: String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    audiosViewModel: AudioListViewModel = hiltViewModel()
) {


    val audios = audiosViewModel.audios.collectAsLazyPagingItems()

    AudioListComposable(
        modifier = modifier,
        audios = audios,
        categoryTitle = audiosViewModel.categoryTitle,
        onNavigateToAudioDetail = { title, audioUrl ->
            onNavigateToAudioDetail(title, audioUrl)
        },
        onNavigateBack = onNavigateBack
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioListComposable(
    modifier: Modifier = Modifier,
    audios: LazyPagingItems<Audio>,
    categoryTitle: String? = null,
    onNavigateToAudioDetail: (title: String, audioId: String) -> Unit = { _, _ -> },
    onNavigateBack: () -> Unit = {}
) {

    val listState = rememberLazyListState()

    // Same "list looks scrolled down" issue as the article list: the mediator's
    // remote fetch (or a genuinely new audio) can insert newer items above the
    // anchor after the local cache already rendered. Track the newest item's key
    // and snap/animate back to it when it changes.
    var topItemKey by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(audios.itemCount) {
        if (audios.itemCount == 0) return@LaunchedEffect
        val newestKey = audios.peek(0)?.id
        if (newestKey != null && newestKey != topItemKey) {
            if (topItemKey == null) {
                listState.scrollToItem(0)
            } else {
                listState.animateScrollToItem(0)
            }
            topItemKey = newestKey
        }
    }

    Scaffold(
        containerColor = Brand.colors.background,
        topBar = {
            AppTopBar(title = categoryTitle ?: stringResource(R.string.audios), onBack = onNavigateBack)
        },
        modifier = modifier.fillMaxSize()
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {

            val isMediatorRefreshing = audios.loadState.mediator?.refresh is LoadState.Loading

            if (isMediatorRefreshing) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Brand.colors.accentStrong,
                        strokeWidth = 3.dp
                    )
                }
            } else if (audios.itemCount == 0 && audios.loadState.refresh is LoadState.NotLoading) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Headset,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Brand.colors.textMuted.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.no_audios_available),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Brand.colors.textMuted
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        count = audios.itemCount,
                        key = audios.itemKey { it.id }

                    ) { audioIndex ->
                        val audio = audios[audioIndex]
                        if (audio != null) {
                            AudioListItem(
                                audio = audio,
                                onClick = {
                                    onNavigateToAudioDetail(
                                        audio.title,
                                        audio.audioUrl
                                    )
                                }
                            )
                        }
                    }

                    // Handle loading state for the next page (APPEND)
                    item {
                        if (audios.loadState.append is LoadState.Loading) {
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

@Preview(
    showBackground = true,
    showSystemUi = true,
    device = Devices.PIXEL_7,
    name = "قائمة الصوتيات"
)
@Composable
fun AudioListScreenPreview() {
    HassanAlHawaryTheme {
        // Mock data logic for pure preview if needed, or wrap existing Composable
        // Note: For a true stateless preview of the list, we'd need to mock LazyPagingItems
        // but here we just provide the shell or ensure the main Composable can render.
    }
}
