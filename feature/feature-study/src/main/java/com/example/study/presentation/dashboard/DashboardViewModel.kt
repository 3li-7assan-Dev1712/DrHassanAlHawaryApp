package com.example.study.presentation.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.use_cases.study.GetMotivationalMessagesUseCase
import com.example.study.domain.use_case.GetLevelsUseCase
import com.example.study.domain.use_case.SyncLevelsUseCase
import com.example.study.presentation.model.DashboardUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    val getLevelsUseCase: GetLevelsUseCase,
    val syncLevelsUseCase: SyncLevelsUseCase,
    val getMotivationalMessagesUseCase: GetMotivationalMessagesUseCase,
) : ViewModel() {

    val TAG = "DashboardViewModel"

    private val _uiState = MutableStateFlow(DashboardUiState())

    val uiState = _uiState.asStateFlow()


    init {
        viewModelScope.launch {
            try {
                syncLevelsUseCase()
                getLevelsUseCase().collect { levels ->
                    Log.d(TAG, "levels: ${levels?.size}")
                    if (levels.isNullOrEmpty()) {
                        _uiState.update {
                            it.copy(levelsErrorMessage = "No levels found", loadingLevels = false)
                        }

                    } else {
                        _uiState.update {
                            it.copy(
                                levels = levels, loadingLevels = false
                            )
                        }
                    }
                }


            } catch (e: Exception) {
                Log.d(TAG, "onRefresh: ${e.message}")
            }
        }
        viewModelScope.launch {
            try {
                val messages = getMotivationalMessagesUseCase()
                Log.d(TAG, "messages: ${messages.size}")
                _uiState.update {
                    it.copy(
                        motivationalMessages = messages,
                        loadingMotivationalMessages = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        motivationalMessagesErrorMessage = e.message,
                        loadingMotivationalMessages = false
                    )
                }
            }
        }
    }

}
