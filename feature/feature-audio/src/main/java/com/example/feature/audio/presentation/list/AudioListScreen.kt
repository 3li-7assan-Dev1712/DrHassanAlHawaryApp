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
import com.example.core.ui.theme.HassanAlHawaryTheme
import com.example.feature.audio.domain.model.Audio
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
    onNavigateToAudioDetail: (title: String, audioId: String) -> Unit = { _, _ -> },
    onNavigateBack: () -> Unit = {}
) {

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                title = {
                    Text(
                        text = stringResource(R.string.audios),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )

                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = stringResource(R.string.back),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)
            )
        },
        modifier = modifier.fillMaxSize()
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(contentPadding)
        ) {

            val isMediatorRefreshing = audios.loadState.mediator?.refresh is LoadState.Loading

            if (isMediatorRefreshing) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
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
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.no_audios_available),
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
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }


            }

        }
    }
}

@Preview(showBackground = true, showSystemUi = true, device = Devices.PIXEL_7, name = "قائمة الصوتيات")
@Composable
fun AudioListScreenPreview() {
    HassanAlHawaryTheme {
        // Mock data logic for pure preview if needed, or wrap existing Composable
        // Note: For a true stateless preview of the list, we'd need to mock LazyPagingItems
        // but here we just provide the shell or ensure the main Composable can render.
    }
}
