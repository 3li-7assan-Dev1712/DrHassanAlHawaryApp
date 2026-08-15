package com.example.feature.video.presentation.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.domain.module.Video
import com.example.feature.video.domain.use_case.GetPaginatedVideoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class VideosViewModel @Inject constructor(
    getPaginatedVideoUseCase: GetPaginatedVideoUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val categoryId: String? = savedStateHandle["categoryId"]
    val categoryTitle: String? = savedStateHandle["categoryTitle"]

    val videos: Flow<PagingData<Video>> = getPaginatedVideoUseCase(categoryId).cachedIn(viewModelScope)

}
