package com.example.hassanalhawary.ui.screens.home_screen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.hassanalhawary.ui.theme.CairoTypography


/*
reusable lazy row for latest articles and audio
 */
@Composable
fun <T> LatestArticleAudioLazyRow(
    title: String,
    showLoading: Boolean,
    items: List<T>,
    itemContent: @Composable (item: T) -> Unit,
    modifier: Modifier = Modifier,
    itemSpacing: Dp = 12.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    itemKey: ((item: T) -> Any)? = null
) {
    if (showLoading) {

        Box(
            modifier = modifier
                .padding(4.dp)
                .fillMaxWidth()
                .height(120.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }

    } else if (items.isEmpty()) {
        Box(
            modifier = modifier
                .padding(4.dp)
                .width(180.dp)
                .height(120.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "No data available", style = MaterialTheme.typography.bodyMedium)
        }
    } else {

        Column(modifier = modifier.fillMaxWidth()) {
            Text(
                text = title,
                style = CairoTypography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp, bottom = 8.dp) // Give title some top padding
            )
            LazyRow(
                contentPadding = contentPadding,
                horizontalArrangement = Arrangement.spacedBy(itemSpacing)
            ) {
                items(
                    items = items,
                    key = itemKey
                ) { item ->
                    itemContent(item)
                }
            }
        }
    }
}