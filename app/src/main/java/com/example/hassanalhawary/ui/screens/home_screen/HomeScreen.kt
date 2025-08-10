package com.example.hassanalhawary.ui.screens.home_screen

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hassanalhawary.R
import com.example.hassanalhawary.domain.model.Audio
import com.example.hassanalhawary.domain.model.Question
import com.example.hassanalhawary.domain.model.getFakeArticles
import com.example.hassanalhawary.ui.components.SearchBar
import com.example.hassanalhawary.ui.screens.home_screen.components.ArticleCard
import com.example.hassanalhawary.ui.screens.home_screen.components.AudioCard
import com.example.hassanalhawary.ui.screens.home_screen.components.LatestArticleAudioLazyRow
import com.example.hassanalhawary.ui.screens.home_screen.components.QuestionOfTheDay


val sampleArticles = getFakeArticles()

val sampleAudios = listOf(
    Audio("aud1", "Tafsir of Surah Al-Fatiha"),
    Audio("aud2", "The Life of the Prophet Muhammad (PBUH) - Part 1"),
    Audio("aud3", "Lessons from Surah Yusuf")
)

val sampleQOTD =
    Question("q1", "What are the five pillars of Islam?", "Shahada, Salat, Zakat, Sawm, Hajj")


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    homeScreenViewModel: HomeScreenViewModel = hiltViewModel(),
    onNavigateToDetailArticle: (articleId: String) -> Unit = {}
) {


    val homeScreenUiState by homeScreenViewModel.homeScreenUiState.collectAsStateWithLifecycle()

    val sampleQuestion = Question(
        id = "qotd123",
        question = "What is the primary virtue emphasized during the month of Ramadan?",
        answer = "Patience (Sabr) and Taqwa (God-consciousness) are among the primary virtues emphasized."
    )

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
                SearchBar(
                    modifier = Modifier.padding(horizontal = 15.dp),
                    searchQuery = "",
                    onQueryChanged = {},
                    hint = "Search"
                )

            }
        }
    ) { contentPadding ->

        Column(modifier = Modifier.padding(contentPadding)) {

            QuestionOfTheDay(
                question = sampleQuestion, onNavigateToDetail = { questionId ->
                    println("Navigate to detail for question ID: $questionId")
                })

            LatestArticleAudioLazyRow(
                title = "Latest Articles",
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
                title = "Latest Audios",
                showLoading = false,
                items = sampleAudios,
                itemKey = { audio -> audio.id }, // Provide a key
                itemContent = { audio ->
                    AudioCard(
                        modifier = Modifier
                            .padding(4.dp)
                            .width(180.dp)
                            .height(120.dp),
                        audio = audio,
                        onClick = { /* navigate to audio detail for audio.id */ }
                    )
                }
            )
        }

    }


}


@Preview(showBackground = true, showSystemUi = true, widthDp = 386, heightDp = 640)
@Composable
fun HomeScreenPreview() {

    HomeScreen(modifier = Modifier.fillMaxSize())
}

