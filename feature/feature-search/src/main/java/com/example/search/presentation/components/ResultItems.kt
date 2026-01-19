package com.example.search.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.algolia.search.model.response.ResponseSearch
import kotlinx.serialization.json.jsonPrimitive

data class SearchHit(
    val title: String?,
    val type: String?
)

@Composable
fun ArticleResultItem(hit: SearchHit, modifier: Modifier = Modifier) {
    Card(modifier = modifier, elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(Icons.Default.Person, contentDescription = "Article")
            Text(text = hit.title ?: "No title", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun VideoResultItem(hit: SearchHit, modifier: Modifier = Modifier) {
    Card(modifier = modifier, elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = "Video")
            Text(text = hit.title ?: "No title", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun AudioResultItem(hit: SearchHit, modifier: Modifier = Modifier) {
    Card(modifier = modifier, elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(Icons.Default.Notifications, contentDescription = "Audio")
            Text(text = hit.title ?: "No title", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

// Helper function to parse the raw JSON from Algolia
fun parseHit(hit: ResponseSearch.Hit): SearchHit {
    val title = hit.json["title"]?.jsonPrimitive?.content
    val type = hit.json["type"]?.jsonPrimitive?.content
    return SearchHit(title, type)
}