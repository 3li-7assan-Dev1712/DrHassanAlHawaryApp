package com.example.feature.audio.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.domain.module.Audio
import com.example.feature.audio.domain.use_case.GetPaginatedAudioUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject


@OptIn(FlowPreview::class)
@HiltViewModel
class AudioListViewModel @Inject constructor(


    getPaginatedAudioUseCase: GetPaginatedAudioUseCase


) : ViewModel() {


    private val _uiState = MutableStateFlow<AudioListUiState>(AudioListUiState.Loading())
    val uiState: StateFlow<AudioListUiState> = _uiState.asStateFlow()


    @OptIn(ExperimentalCoroutinesApi::class)
    val audios: Flow<PagingData<Audio>> = getPaginatedAudioUseCase("").cachedIn(viewModelScope)


}