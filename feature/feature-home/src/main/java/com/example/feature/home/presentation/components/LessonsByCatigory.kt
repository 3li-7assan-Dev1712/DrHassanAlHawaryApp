package com.example.feature.home.presentation.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.ui.icons.TablerIcons
import com.example.core.ui.navigation.Routes
import com.example.core.ui.theme.Brand
import com.example.core.ui.theme.Cairo

data class Category(
    val route: String,
    val name: String,
    @DrawableRes val iconRes: Int,
)

/**
 * The home screen's six content shortcuts: a compact 3 x 2 grid of tiles
 * (gold Tabler icon + label), about half the height of the old square cards
 * so the latest articles show above the fold.
 */
@Composable
fun LessonsByCategory(
    categories: List<Category>,
    modifier: Modifier = Modifier,
    onCategoryClick: (String) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        categories.chunked(COLUMNS).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { category ->
                    CategoryTile(
                        category = category,
                        onClick = { onCategoryClick(category.route) },
                        modifier = Modifier.weight(1f),
                    )
                }
                // Keep tile widths equal if the last row is short.
                repeat(COLUMNS - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
fun CategoryTile(
    category: Category,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = modifier
            .clip(shape) // ripple stays inside the tile
            .background(Brand.colors.surface)
            .clickable(role = Role.Button, onClickLabel = category.name, onClick = onClick)
            .heightIn(min = 48.dp)
            .padding(horizontal = 4.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // The label below is what TalkBack reads (clickable merges it), so no duplicate description here.
        Icon(
            painter = painterResource(category.iconRes),
            contentDescription = null,
            tint = Brand.colors.gold,
            modifier = Modifier.size(24.dp),
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = category.name,
            color = Brand.colors.textSecondary,
            fontFamily = Cairo,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private const val COLUMNS = 3

@Preview(name = "Content grid", widthDp = 360)
@Composable
private fun LessonsByCategoryPreview() {
    Column(Modifier.background(Brand.colors.background).padding(vertical = 12.dp)) {
        LessonsByCategory(
            categories = listOf(
                Category(Routes.ARTICLES_SCREEN, "المقالات", TablerIcons.Notebook),
                Category(Routes.AUDIO_LIST_SCREEN, "الصوتيات", TablerIcons.Headphones),
                Category(Routes.VIDEOS_SCREEN, "الفيديوهات", TablerIcons.Video),
                Category(Routes.Q_A_SCREEN, "فاسألوا", TablerIcons.MessageQuestion),
                Category(Routes.IMAGES_SCREEN, "التصاميم", TablerIcons.Photo),
                Category(Routes.ABOUT_DR_HASSAN_SCREEN, "عن الشيخ", TablerIcons.UserCircle),
            ),
            onCategoryClick = {},
        )
    }
}
