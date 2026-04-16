package com.example.profile.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.use_cases.DeleteAccountUseCase
import com.example.domain.use_cases.study.DeleteStudentDataUseCase
import com.example.profile.domain.use_case.GetUserDataUseCase
import com.example.profile.domain.use_case.SignOutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileScreenViewModel @Inject constructor(
    getUserDataUseCase: GetUserDataUseCase,
    private val singOutUseCase: SignOutUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    private val deleteStudentDataUseCase: DeleteStudentDataUseCase
) : ViewModel() {

    private val _state: MutableStateFlow<ProfileUiState> = MutableStateFlow(
        ProfileUiState(
            userData = null,
            currentAppVersion = "1.0.0",

            )
    )

    init {
        viewModelScope.launch {

            val data = getUserDataUseCase()
            _state.update { it.copy(userData = data) }
        }
    }
    val state = _state.asStateFlow()


    fun signOut() {

        viewModelScope.launch {
            val result = singOutUseCase()
            _state.value = _state.value.copy(
                signOutResult = result
            )


        }


    }

    fun deleteAccount() {
        viewModelScope.launch {
            _state.update { it.copy(isDeleting = true) }
            val result = deleteAccountUseCase()
            if (result.isSuccess) {
                deleteStudentDataUseCase() // clear local db
                _state.update {
                    it.copy(
                        isDeleting = false,
                        signOutResult = com.example.domain.module.SignOutResult(success = true)
                    )
                }
            } else {
                _state.update {
                    it.copy(
                        isDeleting = false,
                        signOutResult = com.example.domain.module.SignOutResult(
                            success = false,
                            error = result.exceptionOrNull()?.message ?: "فشل حذف الحساب"
                        )
                    )
                }
            }
        }
    }

    fun onSignOutResultConsumed() {
        _state.update { it.copy(signOutResult = null) }
    }
}