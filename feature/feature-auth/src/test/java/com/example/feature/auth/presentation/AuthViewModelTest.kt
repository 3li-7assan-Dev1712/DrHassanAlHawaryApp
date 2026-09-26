package com.example.feature.auth.presentation

import com.example.domain.module.LoginError
import com.example.domain.module.LoginResult
import com.example.domain.module.UserData
import com.example.domain.repository.AuthRepository
import com.example.domain.use_cases.LoginWithGoogleUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeAuthRepository
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repository = FakeAuthRepository()
        viewModel = AuthViewModel(LoginWithGoogleUseCase(repository))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `idle then loading then success`() = runTest(dispatcher) {
        assertEquals(AuthUiState.Idle, viewModel.state.value)

        viewModel.loginWithGoogle(ACTIVITY)
        assertEquals(AuthUiState.Loading, viewModel.state.value)

        repository.complete(LoginResult(data = USER))
        advanceUntilIdle()
        assertEquals(AuthUiState.Success, viewModel.state.value)
    }

    @Test
    fun `cancelling the picker returns silently to idle`() = runTest(dispatcher) {
        viewModel.loginWithGoogle(ACTIVITY)
        assertEquals(AuthUiState.Loading, viewModel.state.value)

        repository.complete(LoginResult(error = LoginError.Cancelled))
        advanceUntilIdle()
        assertEquals(AuthUiState.Idle, viewModel.state.value)
    }

    @Test
    fun `error then retry signs in`() = runTest(dispatcher) {
        viewModel.loginWithGoogle(ACTIVITY)
        repository.complete(LoginResult(error = LoginError.Network))
        advanceUntilIdle()
        assertEquals(AuthUiState.Error, viewModel.state.value)

        viewModel.loginWithGoogle(ACTIVITY) // the snackbar's retry action
        assertEquals(AuthUiState.Loading, viewModel.state.value)

        repository.complete(LoginResult(data = USER))
        advanceUntilIdle()
        assertEquals(AuthUiState.Success, viewModel.state.value)
        assertEquals(2, repository.calls)
    }

    @Test
    fun `a second tap while loading is ignored`() = runTest(dispatcher) {
        viewModel.loginWithGoogle(ACTIVITY)
        viewModel.loginWithGoogle(ACTIVITY)
        advanceUntilIdle()

        assertEquals(1, repository.calls)
        assertEquals(AuthUiState.Loading, viewModel.state.value)
    }

    @Test
    fun `no google account shows the add-account state until dismissed`() = runTest(dispatcher) {
        viewModel.loginWithGoogle(ACTIVITY)
        repository.complete(LoginResult(error = LoginError.NoAccount))
        advanceUntilIdle()
        assertEquals(AuthUiState.NoAccount, viewModel.state.value)

        viewModel.dismissMessage()
        assertEquals(AuthUiState.Idle, viewModel.state.value)
    }

    @Test
    fun `an unexpected exception becomes the error state`() = runTest(dispatcher) {
        viewModel.loginWithGoogle(ACTIVITY)
        repository.fail(IllegalStateException("boom"))
        advanceUntilIdle()
        assertEquals(AuthUiState.Error, viewModel.state.value)
    }

    /**
     * Each login suspends until the test queues an outcome; queueing before the login
     * coroutine has started works too.
     */
    private class FakeAuthRepository : AuthRepository {
        var calls = 0
            private set
        private val outcomes = Channel<Result<LoginResult>>(Channel.UNLIMITED)

        fun complete(result: LoginResult) {
            outcomes.trySend(Result.success(result))
        }

        fun fail(error: Throwable) {
            outcomes.trySend(Result.failure(error))
        }

        override suspend fun loginWithGoogle(activityContext: Any): LoginResult {
            calls++
            return outcomes.receive().getOrThrow()
        }

        override suspend fun getLoggedInUser(): LoginResult? = null
        override suspend fun getUserSecurityRole(): String = "none"
        override suspend fun signOut() = Unit
        override suspend fun deleteAccount(): Result<Unit> = Result.success(Unit)
        override fun observeAuthState(): Flow<Boolean> = emptyFlow()
    }

    private companion object {
        val ACTIVITY = Any()
        val USER = UserData(userId = "uid", username = "user", email = "a@b.c", idToken = "t", userProfilePictureUrl = null)
    }
}
