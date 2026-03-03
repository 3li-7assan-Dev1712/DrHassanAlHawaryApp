package com.example.profile.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.profile.domain.use_case.GetGoogleUserDataUseCase
import com.example.profile.domain.use_case.SignOutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileScreenViewModel @Inject constructor(
    getGoogleUserDataUseCase: GetGoogleUserDataUseCase,
    val singOutUseCase: SignOutUseCase
) : ViewModel() {

    private val _state: MutableStateFlow<ProfileUiState> = MutableStateFlow(
        ProfileUiState(
            userData = getGoogleUserDataUseCase(),
            currentAppVersion = "1.0.0",

            )
    )

    val state = _state.asStateFlow()


    fun signOut() {

        viewModelScope.launch {
            val result = singOutUseCase()
            _state.value = _state.value.copy(
                signOutResult = result
            )


        }


    }

    fun onSignOutResultConsumed() {
        _state.update { it.copy(signOutResult = null) }
    }
}