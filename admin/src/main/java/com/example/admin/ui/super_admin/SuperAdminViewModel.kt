package app.netlify.devalihassan.admin.ui.super_admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.repository.AuthRepository
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
    /** The signed-in super admin; they can't demote or remove themselves (no lock-out). */
    val currentUid: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class SuperAdminViewModel @Inject constructor(
    private val getAdminsUseCase: GetAdminsUseCase,
    private val addAdminUseCase: AddAdminUseCase,
    private val removeAdminUseCase: RemoveAdminUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SuperAdminUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadAdmins()
        viewModelScope.launch {
            val uid = authRepository.getLoggedInUser()?.data?.userId
            _uiState.update { it.copy(currentUid = uid) }
        }
    }

    /** Promotes to [ROLE_SUPER_ADMIN] or demotes to [ROLE_ADMIN]; setUserRole also syncs the admins doc. */
    fun changeRole(email: String, role: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
            addAdminUseCase(email, role)
                .onSuccess {
                    val message = if (role == ROLE_SUPER_ADMIN) "Promoted to super admin" else "Demoted to admin"
                    _uiState.update { it.copy(successMessage = message) }
                    loadAdmins()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
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
            addAdminUseCase(email, ROLE_ADMIN)
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

    companion object {
        /** Role claims, as checked by the cloud functions and MainActivityViewModel. */
        const val ROLE_ADMIN = "admin"
        const val ROLE_SUPER_ADMIN = "super_admin"
    }
}
