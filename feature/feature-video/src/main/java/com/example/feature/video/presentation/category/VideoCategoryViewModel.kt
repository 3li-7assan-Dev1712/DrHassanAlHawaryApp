package com.example.feature.video.presentation.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.module.ContentCategory
import com.example.domain.module.ContentType
import com.example.domain.use_cases.videos.GetVideoCategoriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

const val ALL_VIDEO_CATEGORIES_ID = "all"

@HiltViewModel
class VideoCategoryViewModel @Inject constructor(
    private val getVideoCategoriesUseCase: GetVideoCategoriesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(VideoCategoryUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            getVideoCategoriesUseCase()
                .onStart { _uiState.update { it.copy(isLoading = true) } }
                .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                .collect { categories ->
                    val allCategory = ContentCategory(
                        id = ALL_VIDEO_CATEGORIES_ID,
                        title = "الكل",
                        type = ContentType.VIDEO,
                        description = "كل الفيديوهات بمختلف تصنيفاتها"
                    )
                    _uiState.update {
                        it.copy(isLoading = false, categories = listOf(allCategory) + categories)
                    }
                }
        }
    }
}
