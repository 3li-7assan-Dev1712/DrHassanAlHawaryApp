package com.example.hassanalhawary.ui.screens.home_screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hassanalhawary.R
import com.example.hassanalhawary.domain.model.Audio
import com.example.hassanalhawary.domain.model.Question
import com.example.hassanalhawary.domain.model.getFakeArticles
import com.example.hassanalhawary.ui.components.SearchBar
import com.example.hassanalhawary.ui.components.TopAppBar
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


@Composable
fun HomeScreen(
    modifier: Modifier = Modifier
) {

    val sampleQuestion = Question(
        id = "qotd123",
        question = "What is the primary virtue emphasized during the month of Ramadan?",
        answer = "Patience (Sabr) and Taqwa (God-consciousness) are among the primary virtues emphasized."
    )

    Column(modifier = modifier) {

        Spacer(modifier = Modifier.height(8.dp))

        TopAppBar(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp),
            title = stringResource(R.string.app_name)
        )

        Spacer(modifier = Modifier.height(8.dp))

        SearchBar(
            modifier = Modifier.padding(horizontal = 15.dp),
            searchQuery = "",
            onQueryChanged = {},
            hint = "Search"
        )

        Spacer(modifier = Modifier.height(8.dp))

        QuestionOfTheDay(
            question = sampleQuestion, onNavigateToDetail = { questionId ->
                println("Navigate to detail for question ID: $questionId")
            })

        LatestArticleAudioLazyRow(
            title = "Latest Articles",
            items = sampleArticles,
            itemKey = { article -> article.id }, // Provide a key
            itemContent = { article ->
                ArticleCard(
                    article = article,
                    onClick = { /* navigate to article detail for article.id */ }
                )
            }
        )

        Spacer(modifier = Modifier.height(16.dp)) // Space between sections

        // Latest Audios Section
        LatestArticleAudioLazyRow(
            itemSpacing = 8.dp,
            contentPadding = PaddingValues(vertical = 4.dp, horizontal = 12.dp),
            title = "Latest Audios",
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


@Preview(showBackground = true, showSystemUi = true, widthDp = 386, heightDp = 640)
@Composable
fun HomeScreenPreview() {

    HomeScreen(modifier = Modifier.fillMaxSize())
}

