package com.example.feature.image.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.core.ui.components.shimmer
import com.example.domain.module.Image
import com.example.domain.module.ImageGroup

/**
 * One group's row in the vertically-scrolling images list: a title followed by
 * its own horizontally-scrolling, snapping strip of thumbnails. Replaces the old
 * single-preview-card-per-group grid, which only ever hinted at one image per
 * group behind a tap - this lets someone browse each group's actual contents
 * without leaving the list.
 *
 * [images] arrives empty while [ImagesGroupsViewModel.imagesForGroup]'s lazy,
 * per-row load is still in flight - shimmer placeholders fill the strip until
 * then rather than the row collapsing to just its title.
 */
@Composable
fun ImageGroupRow(
    group: ImageGroup,
    images: List<Image>,
    onTitleClick: () -> Unit,
    onImageClick: (index: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = group.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onTitleClick)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )

        val listState = rememberLazyListState()
        LazyRow(
            state = listState,
            flingBehavior = rememberSnapFlingBehavior(listState),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (images.isEmpty()) {
                items(PLACEHOLDER_COUNT) { ThumbnailPlaceholder() }
            } else {
                itemsIndexed(images, key = { _, image -> image.id }) { index, image ->
                    AsyncImage(
                        model = image.imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(THUMBNAIL_SIZE)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onImageClick(index) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ThumbnailPlaceholder() {
    Box(
        modifier = Modifier
            .size(THUMBNAIL_SIZE)
            .clip(RoundedCornerShape(14.dp))
            .shimmer(),
    )
}

private val THUMBNAIL_SIZE = 140.dp
private const val PLACEHOLDER_COUNT = 3
