package com.example.hassanalhawary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.use_cases.IsUserLoggedInUseCase
import com.example.domain.use_cases.datastore.ObserveOnboardingCompletedUseCase
import com.example.domain.use_cases.datastore.UpdateOnboardingCompletedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val isUserLoggedInUseCase: IsUserLoggedInUseCase,
    private val observeOnboardingCompletedUseCase: ObserveOnboardingCompletedUseCase,
    private val updateOnboardingCompletedUseCase: UpdateOnboardingCompletedUseCase
) : ViewModel() {

    val TAG = "MainActivityViewModel"

    val onboardingCompleted = observeOnboardingCompletedUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)
    private val _state = MutableStateFlow(MainActivityState())

    val state = _state.asStateFlow()

    init {
        checkUserAuthState()
    }

    fun hideProgressBar() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    showProgressBar = false
                )
            }
        }
    }

    fun updateOnboardingCompleted() {
        viewModelScope.launch {
            updateOnboardingCompletedUseCase()
        }
    }


    fun checkUserAuthState() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isUserLoggedIn = isUserLoggedInUseCase(),
                    isLoading = false
                )
            }
        }
    }

    fun loginSuccess() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isUserLoggedIn = true
                )
            }
        }
    }

    fun logoutSuccess() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isUserLoggedIn = false
                )
            }
        }
    }

    fun updateShowSplashVal(show: Boolean) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    showSplashScreen = show
                )
            }
        }
    }
}