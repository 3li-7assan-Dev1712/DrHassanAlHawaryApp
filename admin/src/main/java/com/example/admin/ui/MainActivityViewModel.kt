package com.example.admin.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.use_cases.CheckIfUserIsAdminUseCase
import com.example.domain.use_cases.GetUserIdTokenUseCase
import com.example.domain.use_cases.IsUserLoggedInUseCase
import com.example.domain.use_cases.study.DeleteStudentDataUseCase
import com.example.domain.use_cases.study.GetStudentDataUseCase
import com.example.domain.use_cases.study.StoreStudentDataUseCase
import com.example.profile.domain.use_case.GetUserDataUseCase
import com.example.profile.domain.use_case.SignOutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val isUserLoggedInUseCase: IsUserLoggedInUseCase,
    private val getStudentDataUseCase: GetStudentDataUseCase,
    private val getCurrentUserDataUseCase: GetUserDataUseCase,
    private val storeStudentDataUseCase: StoreStudentDataUseCase,
    private val getUserIdTokenUseCase: GetUserIdTokenUseCase,
    private val deleteStudentDataUseCase: DeleteStudentDataUseCase,
    private val checkIfUserIsAdminUseCase: CheckIfUserIsAdminUseCase,
    private val signOutUseCase: SignOutUseCase
) : ViewModel() {

    val TAG = "MainActivityViewModel"


    private val _state = MutableStateFlow(MainActivityState())

    val state = _state.asStateFlow()

    init {
        checkUserAuthState()
        checkTelegramConnectionState()
    }

    fun checkTelegramConnectionState() {
        viewModelScope.launch {

            getStudentDataUseCase().collectLatest { adminData ->
                _state.update {
                    it.copy(
                        isAdminConnectedToTelegram = adminData != null
                    )
                }
            }


        }
    }

    fun checkUserAuthState() {
        viewModelScope.launch {
            val isLoggedIn = isUserLoggedInUseCase()
            Log.d(TAG, "checkUserAuthState: isLoggedIn : $isLoggedIn")

            if (isLoggedIn) {
                val userData = getCurrentUserDataUseCase()
                val idToken = getUserIdTokenUseCase()
                val userSecurityRole = checkIfUserIsAdminUseCase()
                val isAdmin = userSecurityRole == "admin" || userSecurityRole == "super_admin"
                val isSuperAdmin = userSecurityRole == "super_admin"

                _state.update {
                    it.copy(
                        isAdminLoggedIn = true,
                        isUserAdmin = isAdmin,
                        isUserSuperAdmin = isSuperAdmin,
                        currentUserDate = userData,
                        idToken = idToken,
                        isLoading = false
                    )
                }
            } else {
                _state.update {
                    it.copy(
                        isAdminLoggedIn = false,
                        isUserAdmin = false,
                        isUserSuperAdmin = false,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun loginSuccess() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val idToken = getUserIdTokenUseCase()
            val userData = getCurrentUserDataUseCase()
            val userSecurityRule = checkIfUserIsAdminUseCase()
            val isAdmin = userSecurityRule == "admin" || userSecurityRule == "super_admin"
            val isSuperAdmin = userSecurityRule == "super_admin"


            _state.update {
                it.copy(
                    isAdminLoggedIn = true,
                    isUserAdmin = isAdmin,
                    isUserSuperAdmin = isSuperAdmin,
                    currentUserDate = userData,
                    idToken = idToken,
                    isLoading = false
                )
            }

            val uid = userData?.userId
            if (uid != null && isAdmin)
                storeStudentDataUseCase(uid)
        }
    }

    fun logoutSuccess() {

        viewModelScope.launch {
            signOutUseCase()
            _state.update {
                it.copy(
                    isAdminLoggedIn = false,
                    isUserAdmin = false,
                    isUserSuperAdmin = false,
                    currentUserDate = null,
                    idToken = null
                )
            }
            deleteStudentDataUseCase()
        }
    }

}
