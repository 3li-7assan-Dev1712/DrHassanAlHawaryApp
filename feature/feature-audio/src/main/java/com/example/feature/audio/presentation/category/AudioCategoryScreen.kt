package com.example.feature.audio.presentation.category

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.ui.R
import com.example.core.ui.components.AdaptiveShellPreview
import com.example.core.ui.components.CategoryDef
import com.example.core.ui.components.CategoryScreenContent
import com.example.core.ui.components.ContentCategories
import com.example.core.ui.components.EmptyDetail
import com.example.core.ui.components.TwoPaneLayout
import com.example.core.ui.icons.TablerIcons
import com.example.core.ui.theme.layoutTokens

/**
 * Audio categories: the shared compact list (see [ContentCategories]), with "الكل" first.
 * "الكل" is reported as [ContentCategories.ALL_ID]; the caller opens the unfiltered list.
 */
@Composable
fun AudioCategoryScreen(
    onCategoryClick: (id: String, title: String) -> Unit,
    onNavigateUp: () -> Unit,
    viewModel: AudioCategoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val categories = remember(uiState.categories) {
        ContentCategories.resolve(listOf(ContentCategories.ALL_ID to "الكل") + uiState.categories.map { it.id to it.title })
    }
    AudioCategoriesAdaptiveContent(
        isLoading = uiState.isLoading,
        error = uiState.error,
        categories = categories,
        onBack = onNavigateUp,
        onClick = { onCategoryClick(it.id, it.name) },
    )
}

/**
 * Expanded: the categories in the list pane and, until one is opened, the empty detail pane
 * (Figma `57:498`); otherwise the phone screen.
 */
@Composable
fun AudioCategoriesAdaptiveContent(
    isLoading: Boolean,
    error: String?,
    categories: List<CategoryDef>,
    onBack: () -> Unit,
    onClick: (CategoryDef) -> Unit,
) {
    val list = @Composable {
        CategoryScreenContent(
            title = "الصوتيات",
            isLoading = isLoading,
            error = error,
            categories = categories,
            onBack = onBack,
            onClick = onClick,
        )
    }
    if (layoutTokens.isExpanded) {
        TwoPaneLayout(
            listPane = { list() },
            detailPane = {
                EmptyDetail(
                    icon = TablerIcons.Headphones,
                    title = stringResource(R.string.empty_detail_audio_title),
                    subtitle = stringResource(R.string.empty_detail_audio_subtitle),
                )
            },
        )
    } else {
        list()
    }
}

@Composable
private fun AudioCategoriesPreview(darkTheme: Boolean) {
    AdaptiveShellPreview(darkTheme = darkTheme) {
        AudioCategoriesAdaptiveContent(
            isLoading = false,
            error = null,
            categories = ContentCategories.all,
            onBack = {},
            onClick = {},
        )
    }
}

@Preview(name = "Audio categories - compact, light", locale = "ar", widthDp = 360, heightDp = 800)
@Composable
private fun AudioCategoriesCompactLightPreview() = AudioCategoriesPreview(darkTheme = false)

@Preview(name = "Audio categories - compact, dark", locale = "ar", widthDp = 360, heightDp = 800)
@Composable
private fun AudioCategoriesCompactDarkPreview() = AudioCategoriesPreview(darkTheme = true)

@Preview(name = "Audio categories - medium, light", locale = "ar", device = "spec:width=800dp,height=1280dp,dpi=320")
@Composable
private fun AudioCategoriesMediumLightPreview() = AudioCategoriesPreview(darkTheme = false)

@Preview(name = "Audio categories - medium, dark", locale = "ar", device = "spec:width=800dp,height=1280dp,dpi=320")
@Composable
private fun AudioCategoriesMediumDarkPreview() = AudioCategoriesPreview(darkTheme = true)

@Preview(name = "Audio categories - expanded, light", locale = "ar", device = "spec:width=1280dp,height=800dp,dpi=320")
@Composable
private fun AudioCategoriesExpandedLightPreview() = AudioCategoriesPreview(darkTheme = false)

@Preview(name = "Audio categories - expanded, dark", locale = "ar", device = "spec:width=1280dp,height=800dp,dpi=320")
@Composable
private fun AudioCategoriesExpandedDarkPreview() = AudioCategoriesPreview(darkTheme = true)
