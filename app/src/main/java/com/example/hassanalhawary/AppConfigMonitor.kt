package app.netlify.devalihassan

import android.util.Log
import com.example.data.di.ApplicationScope
import com.example.domain.diagnostics.ErrorReporter
import com.example.domain.module.AppConfig
import com.example.domain.use_cases.FetchAppConfigUseCase
import com.example.domain.use_cases.GetAppConfigUseCase
import com.example.domain.use_cases.ObserveAuthStateUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The app's one listener on `config/app_config` (the update and maintenance switches), shared
 * by the activity (which screen to show) and HiltApplication (whether the syncs may run).
 *
 * It reads signed in or not, because maintenance has to reach signed-out users too. The
 * Firestore rules must allow reading that document without sign-in; until they do, the
 * signed-out read is denied, the listener stops quietly and the app runs as before. The
 * listener restarts on every sign-in change, so after signing in it reads again.
 *
 * It fails open: no network and nothing cached, a denied read or a malformed document all
 * leave the app running normally instead of locking people out.
 */
@Singleton
class AppConfigMonitor(
    private val observeConfig: () -> Flow<AppConfig>,
    private val fetchConfig: suspend () -> AppConfig?,
    authState: Flow<Boolean>,
    private val scope: CoroutineScope,
    firstReadTimeoutMs: Long = FIRST_READ_TIMEOUT_MS,
) {

    @Inject
    constructor(
        getAppConfigUseCase: GetAppConfigUseCase,
        fetchAppConfigUseCase: FetchAppConfigUseCase,
        observeAuthStateUseCase: ObserveAuthStateUseCase,
        @ApplicationScope scope: CoroutineScope,
    ) : this(
        observeConfig = { getAppConfigUseCase() },
        fetchConfig = { fetchAppConfigUseCase() },
        authState = observeAuthStateUseCase(),
        scope = scope,
    )

    private val _config = MutableStateFlow<AppConfig?>(null)

    /** The latest config; null until the first read, and when none could be read. */
    val config: StateFlow<AppConfig?> = _config.asStateFlow()

    private val _checked = MutableStateFlow(false)

    /**
     * True once the first read is in, the listener stopped, or [FIRST_READ_TIMEOUT_MS] passed:
     * the splash waits for it so the app doesn't flash before the maintenance screen.
     */
    val checked: StateFlow<Boolean> = _checked.asStateFlow()

    private val _isRetrying = MutableStateFlow(false)

    /** A [retry] is reading from the server. */
    val isRetrying: StateFlow<Boolean> = _isRetrying.asStateFlow()

    /** Once [checked]: whether maintenance is on. Nothing before, so syncs wait for the answer. */
    val isUnderMaintenance: Flow<Boolean> = combine(checked, config) { checked, config ->
        if (checked) config?.maintenanceMode == true else null
    }.filterNotNull().distinctUntilChanged()

    init {
        scope.launch {
            authState.distinctUntilChanged().collectLatest {
                observeConfig()
                    .catch { e ->
                        Log.e(TAG, "app config listener failed", e)
                        ErrorReporter.record("app config", e)
                    }
                    .collect { config ->
                        _config.value = config
                        _checked.value = true
                    }
                // The listener stopped (a denied read): nothing is coming, don't keep anyone waiting.
                _checked.value = true
            }
        }
        scope.launch {
            delay(firstReadTimeoutMs)
            _checked.value = true
        }
    }

    /**
     * The maintenance screen's «إعادة المحاولة»: one read from the server. The listener already
     * follows the document live; this answers right away and covers a listener that stopped.
     */
    fun retry() {
        if (_isRetrying.value) return
        _isRetrying.value = true
        scope.launch {
            try {
                val started = System.currentTimeMillis()
                val fresh = try {
                    fetchConfig()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e(TAG, "app config retry failed", e)
                    null
                }
                if (fresh != null) _config.value = fresh
                // Long enough to see that something happened, even when the answer is instant.
                val elapsed = System.currentTimeMillis() - started
                if (elapsed < MIN_RETRY_MS) delay(MIN_RETRY_MS - elapsed)
            } finally {
                _isRetrying.value = false
            }
        }
    }

    private companion object {
        const val TAG = "AppConfigMonitor"
        const val FIRST_READ_TIMEOUT_MS = 1_500L
        const val MIN_RETRY_MS = 800L
    }
}
