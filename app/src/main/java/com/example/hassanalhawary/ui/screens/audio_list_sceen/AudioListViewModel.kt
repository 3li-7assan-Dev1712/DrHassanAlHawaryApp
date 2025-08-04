package com.example.hassanalhawary.ui.screens.audio_list_sceen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AudioListViewModel @Inject constructor() : ViewModel() {


    private val _uiState = MutableStateFlow<AudioListUiState>(AudioListUiState.Loading)
    val uiState: StateFlow<AudioListUiState> = _uiState.asStateFlow()

    init {
        loadAudios()
    }


    fun loadAudios() {

        viewModelScope.launch {
            _uiState.value = AudioListUiState.Success(sampleAudiosForListScreen)

        }


    }


}