package app.netlify.devalihassan.admin.ui.maintenance

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.netlify.devalihassan.admin.R
import com.example.domain.use_cases.GetAppConfigUseCase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

data class MaintenanceUiState(
    val isLoading: Boolean = true,
    /** What `config/app_config` says now (followed live, so a console change shows too). */
    val isOn: Boolean = false,
    val savedMessage: String = "",
    /** The message as typed, saved with «حفظ الرسالة» or when switching on. */
    val draftMessage: String = "",
    val isSaving: Boolean = false,
    @StringRes val messageRes: Int? = null,
    /** A failure the string resources don't cover (the server's own text). */
    val errorText: String? = null,
    val isError: Boolean = false,
) {
    val canSaveMessage: Boolean get() = !isLoading && !isSaving && draftMessage.trim() != savedMessage
}

/**
 * The maintenance switch for the user app: `config/app_config` fields `maintenanceMode` and
 * `maintenanceMessage`, written with merge so the update fields stay as they are. The user
 * app follows that document live, so a change reaches open apps within seconds.
 *
 * The Firestore rules decide who may write it; a denied write shows [R.string.maintenance_denied].
 */
@HiltViewModel
class MaintenanceViewModel @Inject constructor(
    private val getAppConfigUseCase: GetAppConfigUseCase,
    private val firestore: FirebaseFirestore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MaintenanceUiState())
    val uiState = _uiState.asStateFlow()

    init {
        observeConfig()
    }

    private fun observeConfig() {
        viewModelScope.launch {
            var received = false
            getAppConfigUseCase()
                .catch { showError(R.string.maintenance_read_failed) }
                .onCompletion { cause ->
                    // A denied read closes the listener without an error.
                    if (cause == null && !received) showError(R.string.maintenance_read_failed)
                }
                .collect { config ->
                    received = true
                    _uiState.update { state ->
                        // Keep what the admin is typing; otherwise follow the saved message.
                        val typing = !state.isLoading && state.draftMessage.trim() != state.savedMessage
                        state.copy(
                            isLoading = false,
                            isOn = config.maintenanceMode,
                            savedMessage = config.maintenanceMessage,
                            draftMessage = if (typing) state.draftMessage else config.maintenanceMessage,
                        )
                    }
                }
        }
    }

    fun onDraftMessageChange(text: String) {
        _uiState.update { it.copy(draftMessage = text) }
    }

    /** Switching on saves the typed message with it, so users see it from the first moment. */
    fun setMaintenance(on: Boolean) {
        val fields = if (on) {
            mapOf(MODE to true, MESSAGE to _uiState.value.draftMessage.trim())
        } else {
            mapOf(MODE to false)
        }
        save(fields, if (on) R.string.maintenance_turned_on else R.string.maintenance_turned_off)
    }

    fun saveMessage() {
        save(mapOf(MESSAGE to _uiState.value.draftMessage.trim()), R.string.maintenance_message_saved)
    }

    private fun save(fields: Map<String, Any>, @StringRes doneRes: Int) {
        if (_uiState.value.isSaving) return
        _uiState.update { it.copy(isSaving = true, messageRes = null, errorText = null, isError = false) }
        viewModelScope.launch {
            try {
                // Offline, Firestore keeps the write and sends it later; await() waits for the server.
                val sent = withTimeoutOrNull(SERVER_WAIT_MS) {
                    firestore.collection("config").document("app_config")
                        .set(fields, SetOptions.merge())
                        .await()
                    true // await() itself returns null (Void)
                }
                if (sent == null) showError(R.string.maintenance_pending) else showMessage(doneRes)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val denied = (e as? FirebaseFirestoreException)?.code ==
                    FirebaseFirestoreException.Code.PERMISSION_DENIED
                if (denied) {
                    showError(R.string.maintenance_denied)
                } else {
                    _uiState.update { it.copy(isSaving = false, errorText = e.message ?: e.toString(), isError = true) }
                }
            }
        }
    }

    private fun showMessage(@StringRes res: Int) {
        _uiState.update { it.copy(isSaving = false, messageRes = res, errorText = null, isError = false) }
    }

    private fun showError(@StringRes res: Int) {
        _uiState.update { it.copy(isLoading = false, isSaving = false, messageRes = res, errorText = null, isError = true) }
    }

    private companion object {
        const val MODE = "maintenanceMode"
        const val MESSAGE = "maintenanceMessage"
        const val SERVER_WAIT_MS = 15_000L
    }
}
