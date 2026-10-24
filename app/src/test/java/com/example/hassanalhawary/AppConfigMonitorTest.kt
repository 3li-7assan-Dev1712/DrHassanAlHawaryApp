package app.netlify.devalihassan

import com.example.domain.module.AppConfig
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppConfigMonitorTest {

    private val on = AppConfig(maintenanceMode = true, maintenanceMessage = "نعود قريبًا")
    private val off = AppConfig()

    private fun TestScope.monitor(
        observeConfig: () -> Flow<AppConfig>,
        fetchConfig: suspend () -> AppConfig? = { null },
        authState: Flow<Boolean> = MutableStateFlow(true),
    ) = AppConfigMonitor(observeConfig, fetchConfig, authState, backgroundScope, firstReadTimeoutMs = 1_500)

    @Test
    fun waitsForTheFirstRead_thenApplies() = runTest {
        val listener = MutableSharedFlow<AppConfig>()
        val monitor = monitor({ listener })
        runCurrent()
        assertFalse(monitor.checked.value)
        assertNull(monitor.config.value)

        listener.emit(on)
        runCurrent()
        assertTrue(monitor.checked.value)
        assertEquals(on, monitor.config.value)
    }

    @Test
    fun nothingArrives_checkedAfterTheTimeout_andNotUnderMaintenance() = runTest {
        val monitor = monitor({ MutableSharedFlow() })
        advanceTimeBy(1_499)
        assertFalse(monitor.checked.value)
        advanceTimeBy(2)
        assertTrue(monitor.checked.value)
        assertNull(monitor.config.value)
        assertEquals(listOf(false), maintenanceValues(monitor))
    }

    @Test
    fun listenerFails_failsOpen() = runTest {
        val monitor = monitor({ flow { throw IllegalStateException("listener failed") } })
        runCurrent()
        assertTrue(monitor.checked.value)
        assertNull(monitor.config.value)
        assertEquals(listOf(false), maintenanceValues(monitor))
    }

    @Test
    fun listenerStops_aDeniedRead_failsOpen() = runTest {
        val monitor = monitor({ emptyFlow() })
        runCurrent()
        assertTrue(monitor.checked.value)
        assertEquals(listOf(false), maintenanceValues(monitor))
    }

    @Test
    fun isUnderMaintenance_saysNothingBeforeTheCheck() = runTest {
        val monitor = monitor({ MutableSharedFlow() })
        assertEquals(emptyList<Boolean>(), maintenanceValues(monitor))
    }

    @Test
    fun maintenance_followsTheDocumentLive() = runTest {
        val listener = MutableSharedFlow<AppConfig>()
        val monitor = monitor({ listener })
        val values = mutableListOf<Boolean>()
        backgroundScope.launch { monitor.isUnderMaintenance.collect { values += it } }
        runCurrent()
        listener.emit(off)
        runCurrent()
        listener.emit(on)
        runCurrent()
        listener.emit(on.copy(maintenanceMessage = "رسالة أخرى"))
        runCurrent()
        listener.emit(off)
        runCurrent()
        assertEquals(listOf(false, true, false), values)
    }

    @Test
    fun listenerRestarts_onEverySignInChange() = runTest {
        val auth = MutableStateFlow(false)
        var listens = 0
        monitor({ listens++; MutableSharedFlow() }, authState = auth)
        runCurrent()
        auth.value = true
        runCurrent()
        auth.value = true
        runCurrent()
        assertEquals(2, listens)
    }

    @Test
    fun retry_appliesTheServerValue() = runTest {
        val listener = MutableSharedFlow<AppConfig>()
        val monitor = monitor({ listener }, fetchConfig = { off })
        runCurrent()
        listener.emit(on)
        runCurrent()

        monitor.retry()
        assertTrue(monitor.isRetrying.value)
        advanceTimeBy(2_000) // the retry runs in the background scope, which advanceUntilIdle skips
        runCurrent()
        assertEquals(off, monitor.config.value)
        assertFalse(monitor.isRetrying.value)
    }

    @Test
    fun retry_failing_keepsTheConfig() = runTest {
        val listener = MutableSharedFlow<AppConfig>()
        val monitor = monitor({ listener }, fetchConfig = { throw IllegalStateException("offline") })
        runCurrent()
        listener.emit(on)
        runCurrent()

        monitor.retry()
        advanceTimeBy(2_000) // the retry runs in the background scope, which advanceUntilIdle skips
        runCurrent()
        assertEquals(on, monitor.config.value)
        assertFalse(monitor.isRetrying.value)
    }

    @Test
    fun retry_whileRetrying_isIgnored() = runTest {
        var fetches = 0
        val monitor = monitor({ MutableSharedFlow() }, fetchConfig = { fetches++; null })
        monitor.retry()
        monitor.retry()
        advanceTimeBy(2_000) // the retry runs in the background scope, which advanceUntilIdle skips
        runCurrent()
        assertEquals(1, fetches)
    }

    /** What [AppConfigMonitor.isUnderMaintenance] says now (nothing before the check). */
    private fun TestScope.maintenanceValues(monitor: AppConfigMonitor): List<Boolean> {
        val values = mutableListOf<Boolean>()
        val job = backgroundScope.launch { monitor.isUnderMaintenance.collect { values += it } }
        runCurrent()
        job.cancel()
        return values
    }
}
