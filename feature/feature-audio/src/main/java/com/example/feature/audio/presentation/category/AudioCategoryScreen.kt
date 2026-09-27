package com.example.feature.audio.presentation.category

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.ui.components.CategoryScreenContent
import com.example.core.ui.components.ContentCategories

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
    CategoryScreenContent(
        title = "الصوتيات",
        isLoading = uiState.isLoading,
        error = uiState.error,
        categories = categories,
        onBack = onNavigateUp,
        onClick = { onCategoryClick(it.id, it.name) },
    )
}
