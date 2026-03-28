package com.example.admin.ui.super_admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.use_cases.AddAdminUseCase
import com.example.domain.use_cases.GetAdminsUseCase
import com.example.domain.use_cases.RemoveAdminUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SuperAdminUiState(
    val admins: List<Map<String, Any>> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class SuperAdminViewModel @Inject constructor(
    private val getAdminsUseCase: GetAdminsUseCase,
    private val addAdminUseCase: AddAdminUseCase,
    private val removeAdminUseCase: RemoveAdminUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SuperAdminUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadAdmins()
    }

    fun loadAdmins() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            getAdminsUseCase()
                .onSuccess { list ->

                    _uiState.update { it.copy(admins = list, isLoading = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun addAdmin(email: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
            addAdminUseCase(email, "admin")
                .onSuccess {
                    _uiState.update { it.copy(successMessage = "Admin added successfully") }
                    loadAdmins()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun removeAdmin(uid: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
            removeAdminUseCase(uid)
                .onSuccess {
                    _uiState.update { it.copy(successMessage = "Admin removed successfully") }
                    loadAdmins()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }
    
    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}
