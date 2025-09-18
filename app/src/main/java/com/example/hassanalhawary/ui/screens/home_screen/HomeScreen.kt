package com.example.hassanalhawary.ui.screens.home_screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hassanalhawary.R
import com.example.hassanalhawary.domain.model.WisdomResult
import com.example.hassanalhawary.ui.screens.home_screen.components.ArticleCard
import com.example.hassanalhawary.ui.screens.home_screen.components.AudioCard
import com.example.hassanalhawary.ui.screens.home_screen.components.LatestArticleAudioLazyRow
import com.example.hassanalhawary.ui.screens.home_screen.components.WisdomOfTheDay


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    homeScreenViewModel: HomeScreenViewModel = hiltViewModel(),
    onNavigateToDetailArticle: (articleId: String) -> Unit = {},
    onNavigateToDetailAudio: (title: String, audioUrl: String) -> Unit = { _, _ -> },
) {


    val homeScreenUiState by homeScreenViewModel.homeScreenUiState.collectAsStateWithLifecycle()


    Scaffold(
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.app_name),
                            maxLines = 2,
                            style = MaterialTheme.typography.headlineMedium,
                            overflow = Ellipsis
                        )
                    },
                    windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)
                )
            }
        }
    ) { contentPadding ->

        Column(modifier = Modifier.padding(contentPadding)) {

            // check the network connectivity
            when (homeScreenUiState.wotdResult) {
                is WisdomResult.Failure -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No internet connection",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }

                is WisdomResult.Success -> {
                    WisdomOfTheDay(
                        wisdom = (homeScreenUiState.wotdResult as WisdomResult.Success).value.wisdomText,
                        isLoadings = homeScreenUiState.loadingWotd
                    )
                    LatestArticleAudioLazyRow(
                        title = stringResource(R.string.latest_articles),
                        showLoading = homeScreenUiState.loadingLatestArticles,
                        items = homeScreenUiState.latestArticles,
                        itemKey = { article -> article.id }, // Provide a key
                        itemContent = { article ->
                            ArticleCard(
                                article = article,
                                onClick = { articleId ->
                                    onNavigateToDetailArticle(articleId)
                                }
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp)) // Space between sections

                    // Latest Audios Section
                    LatestArticleAudioLazyRow(
                        itemSpacing = 8.dp,
                        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 12.dp),
                        title = stringResource(R.string.latest_audios),
                        showLoading = homeScreenUiState.loadingLatestAudios,
                        items = homeScreenUiState.latestAudios,
                        itemKey = { audio -> audio.audioUrl }, // Provide a key
                        itemContent = { audio ->
                            AudioCard(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .width(180.dp)
                                    .height(120.dp),
                                audio = audio,
                                onClick = {
                                    onNavigateToDetailAudio(
                                        audio.title,
                                        audio.audioUrl
                                    )
                                }
                            )
                        }
                    )
                }
            }


        }

    }


}


@Preview(showBackground = true, showSystemUi = true, widthDp = 386, heightDp = 640)
@Composable
fun HomeScreenPreview() {

    HomeScreen(modifier = Modifier.fillMaxSize())
}

