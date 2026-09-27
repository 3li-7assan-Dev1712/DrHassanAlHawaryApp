package com.example.core.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.ui.icons.TablerIcons
import com.example.core.ui.theme.Brand
import com.example.core.ui.theme.HassanAlHawaryTheme

/** One entry of the shared audio/video category list. */
data class CategoryDef(val id: String, val name: String, @DrawableRes val icon: Int, val order: Int)

/**
 * The ONE category definition used by both the audio and the video category screens:
 * id, Arabic name, Tabler icon and order. Ids match the stored category ids.
 */
object ContentCategories {
    const val ALL_ID = "all"

    val all: List<CategoryDef> = listOf(
        CategoryDef(ALL_ID, "الكل", TablerIcons.LayoutGrid, 0),
        CategoryDef("fatawah", "فتاوى", TablerIcons.Messages, 1),
        CategoryDef("scientific_lessons", "دروس علمية", TablerIcons.Books, 2),
        CategoryDef("khotab", "خطب الجمعة والعيدين", TablerIcons.BuildingMosque, 3),
        CategoryDef("lectures", "محاضرات", TablerIcons.Microphone, 4),
        CategoryDef("telawat", "تلاوات", TablerIcons.Book, 5),
    )

    /**
     * [items] (id to title, as loaded) mapped onto the shared definition and sorted by its
     * order. A category the definition doesn't know goes at the end with a generic icon
     * and its own title.
     */
    fun resolve(items: List<Pair<String, String>>): List<CategoryDef> = items.mapIndexed { index, (id, title) ->
        all.find { it.id == id } ?: CategoryDef(id, title, TablerIcons.Music, 100 + index)
    }.sortedBy { it.order }
}

/**
 * A compact list inside one surface card: a 40dp accentContainer circle with the icon,
 * the name, an optional [counts] line, a chevron, and hairlines between rows.
 */
@Composable
fun CategoryList(
    categories: List<CategoryDef>,
    onClick: (CategoryDef) -> Unit,
    modifier: Modifier = Modifier,
    counts: Map<String, String> = emptyMap(),
) {
    val colors = Brand.colors
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = colors.surface,
        border = BorderStroke(0.5.dp, colors.divider),
    ) {
        Column {
            categories.forEachIndexed { index, category ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onClick(category) }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(colors.accentContainer, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(category.icon),
                            contentDescription = null,
                            tint = colors.onAccentContainer,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp, fontWeight = FontWeight.Medium),
                            color = colors.textPrimary,
                        )
                        counts[category.id]?.let {
                            Text(text = it, style = MaterialTheme.typography.labelSmall, color = colors.textMuted)
                        }
                    }
                    Icon(
                        painter = painterResource(TablerIcons.ChevronLeft),
                        contentDescription = null,
                        tint = colors.textMuted,
                        modifier = Modifier.size(18.dp),
                    )
                }
                if (index < categories.lastIndex) {
                    HorizontalDivider(Modifier.padding(start = 66.dp, end = 14.dp), thickness = 0.5.dp, color = colors.divider)
                }
            }
        }
    }
}

/**
 * The whole category screen (audio and video share it): start-aligned [title] with a
 * back arrow on the app background, then the [CategoryList] - or a spinner / the error.
 */
@Composable
fun CategoryScreenContent(
    title: String,
    isLoading: Boolean,
    error: String?,
    categories: List<CategoryDef>,
    onBack: () -> Unit,
    onClick: (CategoryDef) -> Unit,
) {
    val colors = Brand.colors
    androidx.compose.material3.Scaffold(
        containerColor = colors.background,
        topBar = { AppTopBar(title = title, onBack = onBack) },
    ) { padding ->
        Box(
            Modifier
                .fillMaxWidth()
                .padding(padding),
        ) {
            when {
                isLoading -> androidx.compose.material3.CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                    color = colors.accentStrong,
                )
                error != null -> Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                )
                else -> CategoryList(
                    categories = categories,
                    onClick = onClick,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
    }
}

@Preview(name = "Categories - light", locale = "ar", widthDp = 360, showBackground = true, backgroundColor = 0xFFF4EEE5)
@Composable
private fun CategoryListLightPreview() {
    HassanAlHawaryTheme(darkTheme = false) {
        CategoryList(ContentCategories.all, onClick = {}, modifier = Modifier.padding(16.dp))
    }
}

@Preview(name = "Categories - dark", locale = "ar", widthDp = 360, showBackground = true, backgroundColor = 0xFF1A1512)
@Composable
private fun CategoryListDarkPreview() {
    HassanAlHawaryTheme(darkTheme = true) {
        CategoryList(ContentCategories.all, onClick = {}, modifier = Modifier.padding(16.dp))
    }
}
