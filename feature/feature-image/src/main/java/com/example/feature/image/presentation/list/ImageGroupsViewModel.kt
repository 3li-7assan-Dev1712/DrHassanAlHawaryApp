package com.example.feature.image.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.feature.image.domain.model.ImageGroup
import com.example.feature.image.domain.use_case.GetImageGroupsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow


import javax.inject.Inject


@HiltViewModel
class ImagesGroupsViewModel @Inject constructor(
    private val getImageGroupsUseCase: GetImageGroupsUseCase,
) : ViewModel() {



    val imageGroups: Flow<PagingData<ImageGroup>> = getImageGroupsUseCase()
        .cachedIn(viewModelScope)

}