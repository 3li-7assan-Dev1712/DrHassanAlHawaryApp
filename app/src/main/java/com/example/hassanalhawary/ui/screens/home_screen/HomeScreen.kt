package com.example.hassanalhawary.ui.screens.home_screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.hassanalhawary.R
import com.example.hassanalhawary.domain.model.Question
import com.example.hassanalhawary.ui.components.SearchBar
import com.example.hassanalhawary.ui.components.TopAppBar
import com.example.hassanalhawary.ui.screens.home_screen.components.QuestionOfTheDay


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
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(R.string.app_name)
        )

        Spacer(modifier = Modifier.height(8.dp))

        SearchBar(
            query = "",
            onQueryChanged = {},
            hint = "Search"
        )

        Spacer(modifier = Modifier.height(8.dp))

        QuestionOfTheDay(
            question = sampleQuestion,
            onNavigateToDetail = { questionId ->
                println("Navigate to detail for question ID: $questionId")
            }
        )
    }


}

