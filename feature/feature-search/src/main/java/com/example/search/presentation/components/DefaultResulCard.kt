package com.example.search.presentation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.search.presentation.model.SearchHit


@Composable
fun DefaultResultCard(hit: SearchHit, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Text(
            text = hit.highlightedTitle?.text ?: "Unsupported Item",
            modifier = Modifier.padding(16.dp)
        )
    }
}



