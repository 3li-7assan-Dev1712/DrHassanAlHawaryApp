package com.example.hassanalhawary.ui.screens.audio_list_sceen

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.example.domain.module.Audio
import com.example.hassanalhawary.R
import com.example.hassanalhawary.ui.components.SearchBar


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioListScreen(
    onNavigateToAudioDetail: (title: String, audioId: String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    audiosViewModel: AudioListViewModel = hiltViewModel()
) {


    val audios = audiosViewModel.audios.collectAsLazyPagingItems()


    val uiState by audiosViewModel.uiState.collectAsStateWithLifecycle()

    AudioListComposeble(
        modifier = modifier,
        audios = audios,
        uiState = uiState,
        onSearchQueryChanged = audiosViewModel::onSearchQueryChanged,
        onNavigateToAudioDetail = { title, audioUrl ->

            onNavigateToAudioDetail(title, audioUrl)
        },
        onNavigateBack)


}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioListComposeble(
    modifier: Modifier = Modifier,
    audios: LazyPagingItems<Audio>,
    uiState: AudioListUiState,
    onSearchQueryChanged: (String) -> Unit = {},
    onNavigateToAudioDetail: (title: String, audioId: String) -> Unit = { _, _ -> },
    onNavigateBack: () -> Unit = {}
) {
    val focusManager = LocalFocusManager.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        topBar = {
            Column {

                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    title = {
                        Text(
                            text = stringResource(R.string.audios),
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.fillMaxWidth(),
                        )

                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    },
                    windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)
                )
                SearchBar(
                    searchQuery = uiState.searchQuery,
                    hint = stringResource(R.string.search_hint),
                    onQueryChanged = onSearchQueryChanged,
                    onSearchClicked = {
                        focusManager.clearFocus() // Hide keyboard on search
                    })
            }
        },
        modifier = modifier.fillMaxSize()
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize()
        ) {

            val isMediatorRefreshing = audios.loadState.mediator?.refresh is LoadState.Loading

            Log.d("Ali 1712", "AudioListComposeble: val: $isMediatorRefreshing")
            Log.d("Ali 1712", "Load type: val: ${audios.loadState.refresh}")
            if (isMediatorRefreshing) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }else {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = 8.dp
                ),
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
                                .padding(16.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        }
                    }
                }
            }


        }

        }
    }
}