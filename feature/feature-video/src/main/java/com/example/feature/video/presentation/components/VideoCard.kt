package com.example.feature.video.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.core.ui.R
import com.example.feature.video.domain.model.Video


@Composable
fun VideoCard(
    video: Video,
    onVideoClick: (String) -> Unit, // Pass the videoUrl or videoId
    modifier: Modifier = Modifier
) {


    // Construct the thumbnail URL
    val thumbnailUrl = "https://img.youtube.com/vi/${video.youtubeVideoId}/hqdefault.jpg"

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onVideoClick(video.videoUrl) }
    ) {
        // Thumbnail with play icon and duration
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            AsyncImage(
                model = thumbnailUrl,
                contentDescription = "Video Thumbnail",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                // Optional: Show a placeholder while loading
                placeholder = painterResource(R.drawable.design_3)
            )

            // Play Icon in the center
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play Video",
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier
                    .size(64.dp)
                    .align(Alignment.Center)
            )


        }

        Spacer(modifier = Modifier.height(8.dp))

        // Title and Description
        Text(
            text = video.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 2
        )
    }
}