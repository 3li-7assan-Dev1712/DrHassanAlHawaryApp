package com.example.hassanalhawary.ui.screens.audio_list_sceen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hassanalhawary.domain.use_cases.FilterAudiosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@OptIn(FlowPreview::class)
@HiltViewModel
class AudioListViewModel @Inject constructor(


    private val filterAudiosUseCase: FilterAudiosUseCase


) : ViewModel() {


    private val _rawSearchInput = MutableStateFlow("")

    private val _debouncedSearchQuery = MutableStateFlow("")

    companion object {
        const val SEARCH_DEBOUNCE_MS = 500L // 500 milliseconds debounce time
    }



    private val _uiState = MutableStateFlow<AudioListUiState>(AudioListUiState.Loading())
    val uiState: StateFlow<AudioListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _rawSearchInput
                .debounce(SEARCH_DEBOUNCE_MS) // Apply debounce
                .distinctUntilChanged() // Only proceed if the query actually changed after debounce
                .collectLatest { debouncedQuery -> // Use collectLatest to cancel previous filtering if a new query comes in
                    _debouncedSearchQuery.value = debouncedQuery // Update the debounced query holder
                    _uiState.update {
                        when (it) {
                            is AudioListUiState.Success -> {
                                it.copy(
                                    displayedAudios = filterAudiosUseCase(
                                        audios = it.audios,
                                        query = debouncedQuery
                                    )
                                )
                            }
                            else -> it
                        }
                    }
                }
        }
        loadAudios()
    }


    fun loadAudios() {

        viewModelScope.launch {
            _uiState.value = AudioListUiState.Success(
                audios = sampleAudiosForListScreen,
                searchQuery = _uiState.value.searchQuery
            )

        }


    }


    fun onSearchQueryChanged(query: String) {
        viewModelScope.launch {

            _rawSearchInput.value = query

            _uiState.update {
                when (it) {
                    is AudioListUiState.Success -> {
                        it.copy(
                            searchQuery = query,
                            displayedAudios = filterAudiosUseCase(
                                audios = it.audios,
                                query = _debouncedSearchQuery.value
                            )
                        )
                    }
                    else -> it
                }
            }
        }

    }

}