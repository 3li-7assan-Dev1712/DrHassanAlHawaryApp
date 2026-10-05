package com.example.feature.video.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import coil.compose.SubcomposeAsyncImage
import com.example.core.ui.components.shimmer
import com.example.core.ui.icons.TablerIcons
import com.example.core.ui.theme.Brand
import com.example.core.ui.theme.HassanAlHawaryTheme
import com.example.domain.module.FixedCategories
import com.example.domain.module.Video
import com.example.domain.text.ArabicDates
import java.util.Date

/**
 * A video card: 16:9 thumbnail (cropped, which also removes the black bars baked into
 * YouTube's 4:3 hqdefault.jpg), 40dp play overlay, category pill, title and date phrase.
 * The Video model has no duration, so there is no duration badge.
 */
@Composable
fun VideoCard(
    video: Video,
    onVideoClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = Brand.colors
    val videoId = video.youtubeVideoId

    Surface(
        onClick = { onVideoClick(video.videoUrl) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = colors.surface,
        border = BorderStroke(0.5.dp, colors.divider),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16 / 9f)
                    .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                    .background(colors.surfaceMuted)
            ) {
                // The sharper maxresdefault.jpg first; not every video has one, so fall back
                // to the hqdefault.jpg the app always used.
                YoutubeThumbnail(
                    url = "https://img.youtube.com/vi/$videoId/maxresdefault.jpg",
                    fallback = { YoutubeThumbnail(url = "https://img.youtube.com/vi/$videoId/hqdefault.jpg", fallback = null) },
                )

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.Center)
                        .background(Color.Black.copy(alpha = 0.55f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(TablerIcons.PlayerPlay),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                }

                val categoryTitle = FixedCategories.VIDEO_CATEGORIES.find { it.id == video.categoryId }?.title
                if (categoryTitle != null) {
                    Text(
                        text = categoryTitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onAccentContainer,
                        modifier = Modifier
                            .padding(10.dp)
                            .align(Alignment.TopStart)
                            .background(colors.accentContainer, RoundedCornerShape(50))
                            .padding(horizontal = 10.dp, vertical = 3.dp),
                    )
                }
            }

            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, lineHeight = 1.5.em),
                    color = colors.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(TablerIcons.Clock),
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = colors.textMuted,
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = ArabicDates.published(System.currentTimeMillis(), video.publishDate.time),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textMuted,
                    )
                }
            }
        }
    }
}

@Composable
private fun YoutubeThumbnail(url: String, fallback: (@Composable () -> Unit)?) {
    SubcomposeAsyncImage(
        model = url,
        contentDescription = null,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop,
        loading = { Box(modifier = Modifier.fillMaxSize().shimmer(cornerRadius = 0.dp)) },
        error = { fallback?.invoke() },
    )
}

@Composable
private fun VideoCardPreviewContent() {
    VideoCard(
        video = Video(
            id = "1",
            title = "شرح كتاب التوحيد - الدرس الأول من السلسلة المباركة",
            videoUrl = "",
            publishDate = Date(System.currentTimeMillis() - 3 * 86_400_000L),
            youtubeVideoId = "ogfYd705cRs",
            categoryId = "scientific_lessons"
        ),
        onVideoClick = {},
        modifier = Modifier.padding(16.dp),
    )
}

@Preview(name = "Video card - light", locale = "ar", widthDp = 360, showBackground = true, backgroundColor = 0xFFF4EEE5)
@Composable
private fun VideoCardLightPreview() {
    HassanAlHawaryTheme(darkTheme = false) { VideoCardPreviewContent() }
}

@Preview(name = "Video card - dark", locale = "ar", widthDp = 360, showBackground = true, backgroundColor = 0xFF1A1512)
@Composable
private fun VideoCardDarkPreview() {
    HassanAlHawaryTheme(darkTheme = true) { VideoCardPreviewContent() }
}
