package com.example.hassanalhawary.ui.screens.audio_list_sceen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hassanalhawary.R
import com.example.hassanalhawary.ui.components.SearchBar


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioListScreen(
    onNavigateToAudioDetail: (title: String, audioId: String) -> Unit,
    modifier: Modifier = Modifier,
    audiosViewModel: AudioListViewModel = hiltViewModel()
) {


    val uiState by audiosViewModel.uiState.collectAsStateWithLifecycle()

    AudioListComposeble(
        modifier = modifier,
        uiState = uiState,
        onSearchQueryChanged = audiosViewModel::onSearchQueryChanged,
        onNavigateToAudioDetail = { title, audioUrl ->

            onNavigateToAudioDetail(title, audioUrl)
        })


}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioListComposeble(
    modifier: Modifier = Modifier,
    uiState: AudioListUiState,
    onSearchQueryChanged: (String) -> Unit = {},
    onNavigateToAudioDetail: (title: String, audioId: String) -> Unit = { _, _ -> }
) {
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            Column {

                TopAppBar(
                    title = { Text(stringResource(R.string.lectures)) },

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
            when (uiState) {
                is AudioListUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is AudioListUiState.Success -> {
                    if (uiState.displayedAudios.isEmpty()) {
                        Text(
                            text = stringResource(R.string.no_audios_available),
                            modifier = Modifier.align(Alignment.Center),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    } else {
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
                                uiState.displayedAudios,
                                key = { it.audioUrl }) { audio ->
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
                    }
                }

                is AudioListUiState.Error -> {
                    Text(
                        text = stringResource(R.string.error_msg),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}