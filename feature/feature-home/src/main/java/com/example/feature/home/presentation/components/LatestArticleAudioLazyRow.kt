package com.example.feature.home.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.ui.theme.BrandTokens
import com.example.core.ui.theme.Cairo
import com.example.core.ui.components.shimmer
import com.example.feature.home.R

/**
 * A home section: [SectionHeader] (title + "عرض الكل") over a horizontal list of
 * cards. Item content gets the LazyItemScope so cards can size themselves
 * relative to the row (e.g. 85% wide, so the next card peeks).
 */
@Composable
fun <T> LatestArticleAudioLazyRow(
    title: String,
    showLoading: Boolean,
    items: List<T>,
    itemContent: @Composable LazyItemScope.(item: T) -> Unit,
    modifier: Modifier = Modifier,
    onSeeAll: (() -> Unit)? = null,
    itemSpacing: Dp = 8.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
    itemKey: ((item: T) -> Any)? = null,
    emptyMessage: String? = null,
) {
    if (items.isEmpty() && !showLoading && emptyMessage == null) return

    Column(modifier = modifier.fillMaxWidth()) {
        SectionHeader(title = title, onSeeAll = onSeeAll, isLoading = showLoading)
        if (items.isNotEmpty() || showLoading) {
            LazyRow(
                contentPadding = contentPadding,
                horizontalArrangement = Arrangement.spacedBy(itemSpacing),
            ) {
                items(items = items, key = itemKey) { item -> itemContent(item) }
            }
        } else if (emptyMessage != null) {
            Text(
                text = emptyMessage,
                color = BrandTokens.textMuted,
                fontFamily = Cairo,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
    }
}

/** Section title at the start, a gold "عرض الكل" link at the end. */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    onSeeAll: (() -> Unit)? = null,
    isLoading: Boolean = false,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = title,
            color = BrandTokens.textPrimary,
            fontFamily = Cairo,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            modifier = Modifier
                .padding(vertical = 8.dp)
                .shimmer(isLoading = isLoading),
        )
        if (onSeeAll != null) {
            // 48dp-tall touch target around the small link text.
            Box(
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .clickable(role = Role.Button, onClick = onSeeAll)
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.see_all),
                    color = BrandTokens.gold,
                    fontFamily = Cairo,
                    fontSize = 12.sp,
                )
            }
        }
    }
}
