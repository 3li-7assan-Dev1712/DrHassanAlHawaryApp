package com.example.search.presentation.components

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.core.ui.R
import com.example.search.presentation.model.SearchHit


@Composable
fun MediaResultCard(hit: SearchHit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Log.d("ResultItems", "MediaResultCard: ${hit.youtubeVideoId}")
        Column {
            AsyncImage(
                model = if (hit.type == "video")
                    "https://img.youtube.com/vi/${hit.youtubeVideoId}/hqdefault.jpg"
                else hit.url,
                contentDescription = hit.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(CardDefaults.shape),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.design_3)
                // You can add placeholder/error drawables here
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = hit.highlightedTitle ?: buildAnnotatedString {
                        append(hit.title ?: "No Title")
                    },
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
