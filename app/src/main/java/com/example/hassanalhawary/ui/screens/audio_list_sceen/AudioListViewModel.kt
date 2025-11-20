package com.example.hassanalhawary.ui.screens.audio_list_sceen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.data.AudiosRepositoryImpl
import com.example.domain.module.Audio
import com.example.domain.use_cases.FilterAudiosUseCase
import com.example.domain.use_cases.GetAllAudiosUseCase
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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject


@OptIn(FlowPreview::class)
@HiltViewModel
class AudioListViewModel @Inject constructor(


    private val filterAudiosUseCase: FilterAudiosUseCase,
    private val getAllAudiosUseCase: GetAllAudiosUseCase,
    private val audiosRepository: AudiosRepositoryImpl


) : ViewModel() {



    private val _rawSearchInput = MutableStateFlow("")
    val rawSearchInput = _rawSearchInput.asStateFlow()


    private val _debouncedSearchQuery = MutableStateFlow("")

    companion object {
        const val SEARCH_DEBOUNCE_MS = 500L // 500 milliseconds debounce time
    }


    private val _uiState = MutableStateFlow<AudioListUiState>(AudioListUiState.Loading())
    val uiState: StateFlow<AudioListUiState> = _uiState.asStateFlow()




    @OptIn(ExperimentalCoroutinesApi::class)
    val audios: Flow<PagingData<Audio>> = _rawSearchInput
        .debounce(300L) // Debounce to avoid querying on every keystroke
        .distinctUntilChanged() // Only query if the text actually changed
        .flatMapLatest { query -> //  flatMapLatest to switch to the new Flow from the use case
            audiosRepository.getAudiosPagingData(query)
        }
        .cachedIn(viewModelScope)


    init {
        /*viewModelScope.launch {
            _rawSearchInput
                .debounce(SEARCH_DEBOUNCE_MS) // Apply debounce
                .distinctUntilChanged() // Only proceed if the query actually changed after debounce
                .collectLatest { debouncedQuery -> // Use collectLatest to cancel previous filtering if a new query comes in
                    _debouncedSearchQuery.value =
                        debouncedQuery // Update the debounced query holder
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
        loadAudios()*/
    }


    fun loadAudios() {


        viewModelScope.launch {
            val audiosResult = getAllAudiosUseCase()
            audiosResult.onEach { audiosFromDb ->
                _uiState.value = AudioListUiState.Success(
                    audios = audiosFromDb,
                    searchQuery = _rawSearchInput.value
                )
            }.launchIn(viewModelScope)

        }

    }


    fun onSearchQueryChanged(query: String) {
        viewModelScope.launch {

            _rawSearchInput.value = query

            /*_uiState.update {
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
            }*/
        }

    }

}