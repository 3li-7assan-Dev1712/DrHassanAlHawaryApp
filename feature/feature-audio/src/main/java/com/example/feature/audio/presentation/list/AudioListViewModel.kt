package com.example.feature.audio.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.feature.audio.domain.model.Audio
import com.example.feature.audio.domain.use_case.GetPaginatedAudioUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


@OptIn(FlowPreview::class)
@HiltViewModel
class AudioListViewModel @Inject constructor(


    getPaginatedAudioUseCase: GetPaginatedAudioUseCase


) : ViewModel() {


    private val _rawSearchInput = MutableStateFlow("")
    val rawSearchInput = _rawSearchInput.asStateFlow()


    companion object {
        const val SEARCH_DEBOUNCE_MS = 500L // 500 milliseconds debounce time
    }


    private val _uiState = MutableStateFlow<AudioListUiState>(AudioListUiState.Loading())
    val uiState: StateFlow<AudioListUiState> = _uiState.asStateFlow()


    @OptIn(ExperimentalCoroutinesApi::class)
    val audios: Flow<PagingData<Audio>> = rawSearchInput
        .debounce(SEARCH_DEBOUNCE_MS) // Debounce to avoid querying on every keystroke
        .distinctUntilChanged() // Only query if the text actually changed
        .flatMapLatest { query -> //  flatMapLatest to switch to the new Flow from the use case
            getPaginatedAudioUseCase(query)
        }
        .cachedIn(viewModelScope)


    fun onSearchQueryChanged(query: String) {
        viewModelScope.launch {

            _rawSearchInput.value = query
        }

    }

}