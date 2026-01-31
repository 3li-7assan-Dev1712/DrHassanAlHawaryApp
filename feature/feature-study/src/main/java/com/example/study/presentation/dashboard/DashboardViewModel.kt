package com.example.study.presentation.dashboard

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.study.domain.use_case.GetLevelsUseCase
import com.example.study.domain.use_case.SyncLevelsUseCase
import com.example.study.presentation.model.DashboardUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    val getLevelsUseCase: GetLevelsUseCase,
    val syncLevelsUseCase: SyncLevelsUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val TAG = "PlaylistViewModel"

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)

    val uiState = _uiState.asStateFlow()


    init {
        viewModelScope.launch {
            try {
                syncLevelsUseCase()
                getLevelsUseCase().collect { levels ->
                    if (levels.isNullOrEmpty()) {
                        _uiState.value = DashboardUiState.Error("No levels found")
                    } else {
                        _uiState.value = DashboardUiState.Success(levels)
                    }
                }

            } catch (e: Exception) {
                Log.d(TAG, "onRefresh: ${e.message}")
            }
        }
    }

}
