package com.example.search.presentation.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.example.core.ui.components.shimmer
import com.example.domain.module.SearchResultMetaData
import com.example.search.presentation.model.SearchHit


@Composable
fun MediaResultCard(hit: SearchHit, modifier: Modifier = Modifier, onItemClick: (SearchResultMetaData) -> Unit) {
    Card(
        modifier = modifier
            .clickable {
                val type = hit.type
                val url = when(type) {
                    "video" -> hit.videoUrl
                    "audio" -> hit.audioUrl
                    else -> hit.previewImageUrl
                }
                onItemClick(
                    SearchResultMetaData(
                        objectID = hit.objectID,
                        title = hit.title,
                        type = hit.type,
                        url = url
                    )
                )
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Log.d("MediaResultCard", "MediaResultCard: $hit")
        Log.d("ResultItems", "MediaResultCard: ${hit.youtubeVideoId}")
        Column {
            val imageUrl = if (hit.type == "video")
                "https://img.youtube.com/vi/${hit.youtubeVideoId}/hqdefault.jpg"
            else hit.previewImageUrl

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(CardDefaults.shape)
            ) {
                // Background Image (Blurred and Cropped)
                SubcomposeAsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(24.dp),
                    loading = {
                        Box(modifier = Modifier.fillMaxSize().shimmer(16.dp))
                    },
                    error = {
                        Box(modifier = Modifier.fillMaxSize().shimmer(16.dp))
                    }
                )
                
                // A semi-transparent overlay to make the foreground pop
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                )
                
                // Foreground Image (Fit, without clipping)
                SubcomposeAsyncImage(
                    model = imageUrl,
                    contentDescription = hit.title,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                    loading = {
                        // Handled by the background
                    },
                    error = {
                        // Handled by the background
                    }
                )
            }
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
