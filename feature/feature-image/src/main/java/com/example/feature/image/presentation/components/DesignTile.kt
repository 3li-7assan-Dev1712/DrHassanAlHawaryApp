package com.example.feature.image.presentation.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import coil.compose.SubcomposeAsyncImage
import com.example.core.ui.theme.SharedKeys
import com.example.core.ui.theme.sharedContainer
import com.example.core.ui.components.shimmer
import com.example.core.ui.icons.TablerIcons
import com.example.core.ui.theme.Brand
import com.example.domain.module.ImageGroup
import com.example.domain.text.ArabicDates
import com.example.domain.text.DesignTitle

/**
 * One design post in the 2-column grid: the preview image (natural height, 10dp corners),
 * a count badge for multi-image posts ("٩ صور"), the cleaned title in full (up to 2
 * lines, wrapping rather than cut with "..") and the date phrase.
 */
@Composable
fun DesignTile(
    group: ImageGroup,
    imageCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Brand.colors
    val title = remember(group.id, group.title) { DesignTitle.clean(group.title) }

    Surface(onClick = onClick, color = Color.Transparent, modifier = modifier.fillMaxWidth()) {
        Column {
            Box {
                SubcomposeAsyncImage(
                    model = group.previewImageUrl,
                    contentDescription = title,
                    contentScale = ContentScale.FillWidth,
                    loading = {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .aspectRatio(4 / 5f)
                                .shimmer(cornerRadius = 10.dp),
                        )
                    },
                    error = {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .aspectRatio(4 / 5f)
                                .background(colors.surfaceMuted),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(painterResource(TablerIcons.Photo), null, tint = colors.textMuted)
                        }
                    },
                    // Grows from the tile to the full-screen viewer.
                    modifier = Modifier
                        .sharedContainer(SharedKeys.design(group.id), RoundedCornerShape(10.dp))
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp)),
                )
                if (imageCount > 1) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(50))
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    ) {
                        Icon(
                            painter = painterResource(TablerIcons.Photo),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = DesignTitle.imageCount(imageCount),
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold, lineHeight = 1.4.em),
                color = colors.textPrimary,
                maxLines = 2,
            )
            Text(
                text = ArabicDates.published(System.currentTimeMillis(), group.publishDate.time),
                style = MaterialTheme.typography.labelSmall,
                color = colors.textMuted,
            )
        }
    }
}
