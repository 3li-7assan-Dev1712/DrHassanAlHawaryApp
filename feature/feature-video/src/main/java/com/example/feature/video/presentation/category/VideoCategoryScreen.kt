package com.example.feature.video.presentation.category

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.ui.components.CategoryScreenContent
import com.example.core.ui.components.ContentCategories

/** Video categories: the same shared compact list as audio (see [ContentCategories]). */
@Composable
fun VideoCategoryScreen(
    onCategoryClick: (id: String, title: String) -> Unit,
    onNavigateUp: () -> Unit,
    viewModel: VideoCategoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // The VM already puts its own "الكل" (id "all" = ContentCategories.ALL_ID) first.
    val categories = remember(uiState.categories) {
        ContentCategories.resolve(uiState.categories.map { it.id to it.title })
    }
    CategoryScreenContent(
        title = "الفيديوهات",
        isLoading = uiState.isLoading,
        error = uiState.error,
        categories = categories,
        onBack = onNavigateUp,
        onClick = { onCategoryClick(it.id, it.name) },
    )
}
