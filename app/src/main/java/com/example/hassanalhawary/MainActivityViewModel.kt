package com.example.hassanalhawary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.module.AppConfig
import com.example.domain.use_cases.GetAppConfigUseCase
import com.example.domain.use_cases.GetUserIdTokenUseCase
import com.example.domain.use_cases.IsUserLoggedInUseCase
import com.example.domain.use_cases.datastore.ObserveDarkThemePreference
import com.example.domain.use_cases.datastore.ObserveOnboardingCompletedUseCase
import com.example.domain.use_cases.datastore.UpdateDarkThemePreference
import com.example.domain.use_cases.datastore.UpdateOnboardingCompletedUseCase
import com.example.domain.use_cases.study.DeleteStudentDataUseCase
import com.example.domain.use_cases.study.StoreStudentDataUseCase
import com.example.profile.domain.use_case.GetUserDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


data class ThemeUiState(
    val isReady: Boolean = false,
    val isDarkTheme: Boolean = false
)

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val isUserLoggedInUseCase: IsUserLoggedInUseCase,
    private val observeOnboardingCompletedUseCase: ObserveOnboardingCompletedUseCase,
    private val updateOnboardingCompletedUseCase: UpdateOnboardingCompletedUseCase,
    private val observeDarkThemePreferenceUseCase: ObserveDarkThemePreference,
    private val updateDarkThemePreferenceUseCase: UpdateDarkThemePreference,
    private val getCurrentUserDataUseCase: GetUserDataUseCase,
    private val storeStudentDataUseCase: StoreStudentDataUseCase,
    private val getUserIdTokenUseCase: GetUserIdTokenUseCase,
    private val deleteStudentDataUseCase: DeleteStudentDataUseCase,
    private val getAppConfigUseCase: GetAppConfigUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(MainActivityState())
    val state = _state.asStateFlow()

    private val _appConfig = MutableStateFlow<AppConfig?>(null)
    val appConfig = _appConfig.asStateFlow()

    // ✅ 1) onboarding starts as null (unknown), then becomes true/false
    val onboardingCompleted = observeOnboardingCompletedUseCase()
        .map<Boolean, Boolean?> { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val themeState = observeDarkThemePreferenceUseCase()
        .map { isDark ->
            ThemeUiState(isReady = true, isDarkTheme = isDark)
        }
        .catch {
            emit(ThemeUiState(isReady = true, isDarkTheme = false))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ThemeUiState(isReady = false, isDarkTheme = false)
        )

    // ✅ 2) single "app ready" flag for splash
    val appReady = kotlinx.coroutines.flow.combine(
        themeState,
        onboardingCompleted,
        state
    ) { theme, onboarding, main ->
        theme.isReady && onboarding != null && !main.isLoading
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    init {
        checkUserAuthState()
        observeAppConfig()
    }

    private fun observeAppConfig() {
        viewModelScope.launch {
            getAppConfigUseCase().collect { config ->
                _appConfig.value = config
            }
        }
    }

    fun updateOnboardingCompleted() {
        viewModelScope.launch { updateOnboardingCompletedUseCase() }
    }

    fun updateDarkThemePreference(isDarkTheme: Boolean) {
        viewModelScope.launch { updateDarkThemePreferenceUseCase(isDarkTheme) }
    }

    fun checkUserAuthState() {
        viewModelScope.launch {
            val isLoggedIn = isUserLoggedInUseCase()
            _state.update {
                it.copy(
                    isUserLoggedIn = isLoggedIn,
                    isLoading = false
                )
            }
            if (isLoggedIn) {
                val idToken = getUserIdTokenUseCase()
                _state.update {
                    it.copy(
                        currentUserDate = getCurrentUserDataUseCase(),
                        idToken = idToken
                    )
                }

            }
        }
    }

    fun loginSuccess() {
        viewModelScope.launch {
            _state.update { it.copy(isUserLoggedIn = true) }
            val idToken = getUserIdTokenUseCase()
            _state.update { it.copy(idToken = idToken) }
            val uid = getCurrentUserDataUseCase()?.userId
            if (uid != null)
                storeStudentDataUseCase(uid)
        }
    }

    fun logoutSuccess() {
        viewModelScope.launch {
            _state.update { it.copy(isUserLoggedIn = false) }
            deleteStudentDataUseCase()
        }
    }
}
