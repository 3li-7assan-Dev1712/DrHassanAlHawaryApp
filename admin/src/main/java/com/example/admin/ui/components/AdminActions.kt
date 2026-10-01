package app.netlify.devalihassan.admin.ui.components

import android.widget.Toast
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * For list screens whose one-off actions (delete) run while the list stays on screen:
 * [isBusy] drives a blocking overlay for the whole call, and a failure lands in
 * [actionError] instead of being dropped. Shown by [AdminActionFeedback].
 */
abstract class AdminActionViewModel : ViewModel() {

    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    private val _actionError = MutableStateFlow<String?>(null)
    val actionError: StateFlow<String?> = _actionError.asStateFlow()

    fun onActionErrorShown() {
        _actionError.value = null
    }

    /** Runs [action] behind the overlay; ignores taps while another action is running. */
    protected fun runAction(action: suspend () -> Result<*>, onSuccess: () -> Unit) {
        if (_isBusy.value) return
        _isBusy.value = true
        viewModelScope.launch {
            val result = try {
                action()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure<Unit>(e)
            } finally {
                _isBusy.value = false
            }
            result
                .onSuccess { onSuccess() }
                .onFailure { _actionError.value = it.message ?: "Operation failed" }
        }
    }
}

/** The overlay + error toast for an [AdminActionViewModel]. */
@Composable
fun AdminActionFeedback(viewModel: AdminActionViewModel) {
    val isBusy by viewModel.isBusy.collectAsState()
    val error by viewModel.actionError.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.onActionErrorShown()
        }
    }

    if (isBusy) BusyOverlay()
}

/** A spinner in a non-dismissable dialog: dims the screen and blocks taps and Back until done. */
@Composable
fun BusyOverlay() {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
    ) {
        CircularProgressIndicator()
    }
}
