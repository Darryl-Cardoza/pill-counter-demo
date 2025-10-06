package com.example.pillcountingnewmodels.feature.login.viewmodel

import android.content.Context
import com.example.pillcountingnewmodels.R
import com.example.pillcountingnewmodels.core.models.ErrorDetail
import com.example.pillcountingnewmodels.core.models.ValidationResult
import com.example.pillcountingnewmodels.feature.login.data.LoginRepository
import com.example.pillcountingnewmodels.feature.login.domain.CredentialsValidator
import com.example.pillcountingnewmodels.feature.login.domain.model.*
import com.example.pillcountingnewmodels.core.utils.PreferenceHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import app.cash.turbine.test
import io.mockk.MockKAnnotations
import io.mockk.coEvery

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val repository: LoginRepository = mock()
    private val validator: CredentialsValidator = mock()
    private val context: Context = mock()
    private val preferenceHelper: PreferenceHelper = mock()

    private lateinit var viewModel: LoginViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val mockSuccessResponse = LoginResponse(
        status = 200,
        isSuccess = true,
        message = "Login successful",
        token = "mock_token_123",
        data = null,
        detail = null
    )

    private val mockFailureResponseWithDetail = LoginResponse(
        status = 400,
        isSuccess = false,
        message = null,
        token = null,
        data = null,
        detail = listOf(
            ErrorDetail(loc = listOf("email"), msg = "Email not registered", type = "value_error")
        )
    )

    private val mockLogoutSuccessResponse = LogoutResponse(
        status = 200,
        isSuccess = true,
        message = "Logout successful"
    )

    private val mockLogoutFailureResponse = LogoutResponse(
        status = 400,
        isSuccess = false,
        message = "Logout failed"
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        MockKAnnotations.init(this)
        viewModel = LoginViewModel(repository, validator, context, preferenceHelper)

        whenever(context.getString(R.string.error_unknown)).thenReturn("Unknown Error")
        whenever(context.getString(R.string.error_email_empty)).thenReturn("Email required")
        whenever(context.getString(R.string.error_email_invalid)).thenReturn("Invalid Email")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ---------- EMAIL VALIDATION ----------
    @Test
    fun `blank email returns error state`() = runTest {
        whenever(validator.validateEmail("")).thenReturn(ValidationResult(false, R.string.error_email_empty))

        viewModel.uiState.test {
            assert(awaitItem() is LoginUiState.Idle)
            viewModel.login("")
            val error = awaitItem()
            assert(error is LoginUiState.Error && error.message == "Email required")
        }
    }

    @Test
    fun `invalid email returns error state`() = runTest {
        whenever(validator.validateEmail("invalid")).thenReturn(ValidationResult(false, R.string.error_email_invalid))

        viewModel.uiState.test {
            assert(awaitItem() is LoginUiState.Idle)
            viewModel.login("invalid")
            val error = awaitItem()
            assert(error is LoginUiState.Error && error.message == "Invalid Email")
        }
    }

    // ---------- LOGIN SUCCESS ----------
    @Test
    fun `valid email returns success state`() = runTest {
        val email = "test@example.com"
        whenever(validator.validateEmail(email)).thenReturn(ValidationResult(true, null))
        whenever(repository.login(email)).thenReturn(Result.success(mockSuccessResponse))

        viewModel.uiState.test {
            assert(awaitItem() is LoginUiState.Idle)
            viewModel.login(email)
            testDispatcher.scheduler.advanceUntilIdle()
            val loading = awaitItem()
            val success = awaitItem()
            assert(loading is LoginUiState.Loading)
            assert(success is LoginUiState.Success)
        }
    }

    // ---------- LOGIN FAILURE ----------
    @Test
    fun `login failure returns error state`() = runTest {
        val exceptionMessage = "Network error"
        coEvery { repository.login(any()) } returns Result.failure(Exception(exceptionMessage))

        viewModel.login("test@example.com")

        viewModel.uiState.test {
            skipItems(1)
            val error = awaitItem()
            assert(error is LoginUiState.Error)
            assert((error as LoginUiState.Error).message.contains(exceptionMessage))
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ---------- LOGIN LOCKOUT AFTER 3 FAILURES ----------
    @Test
    fun `login stops after 3 consecutive failures`() = runTest {
        val email = "test@example.com"
        val exceptionMessage = "Network error"

        // Mock validation and repository
        whenever(validator.validateEmail(email)).thenReturn(ValidationResult(true, null))
        whenever(repository.login(email)).thenReturn(Result.failure(Exception(exceptionMessage)))

        viewModel.uiState.test {
            // Initial state
            assert(awaitItem() is LoginUiState.Idle)

            // Trigger login 3 times
            repeat(3) {
                viewModel.login(email)
                runCurrent() // advance coroutine scheduler

                // Expect Loading then Error for each attempt
                assert(awaitItem() is LoginUiState.Loading)
                val error = awaitItem() as LoginUiState.Error
                assert(error.message.contains(exceptionMessage))
            }

            // 4th attempt should NOT emit anything
            viewModel.login(email)
            runCurrent()

            // This ensures Turbine does not fail if flow is silent
            expectNoEvents()

            // Verify repo called exactly 3 times
            verify(repository, times(3)).login(email)
        }
    }

    // ---------- RESET / CLEAR ----------
    @Test
    fun `resetLoginState returns Idle`() = runTest {
        whenever(validator.validateEmail("invalid")).thenReturn(ValidationResult(false, R.string.error_email_invalid))
        viewModel.login("invalid")
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.resetLoginState()
        assert(viewModel.uiState.value is LoginUiState.Idle)
    }

    @Test
    fun `clearAllStates resets both login and logout to Idle`() = runTest {
        val email = "test@example.com"
        whenever(validator.validateEmail(email)).thenReturn(ValidationResult(true, null))
        whenever(repository.login(email)).thenReturn(Result.success(mockSuccessResponse))

        viewModel.login(email)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.clearAllStates()
        assert(viewModel.uiState.value is LoginUiState.Idle)
        assert(viewModel.logoutUiState.value is LogoutUiState.Idle)
    }

    // ---------- LOGIN EDGE CASES ----------
    @Test
    fun `login returns success when API responds with isSuccess false`() = runTest {
        val email = "test@example.com"
        val apiResponse = mockSuccessResponse.copy(isSuccess = false, message = "Invalid credentials")
        whenever(validator.validateEmail(email)).thenReturn(ValidationResult(true, null))
        whenever(repository.login(email)).thenReturn(Result.success(apiResponse))

        viewModel.uiState.test {
            assert(awaitItem() is LoginUiState.Idle)
            viewModel.login(email)
            testDispatcher.scheduler.advanceUntilIdle()
            val loading = awaitItem()
            val success = awaitItem()
            assert(loading is LoginUiState.Loading)
            assert(success is LoginUiState.Success)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `login returns error when API fails`() = runTest {
        val email = "test@example.com"
        val exceptionMessage = "Email not registered"
        whenever(validator.validateEmail(email)).thenReturn(ValidationResult(true, null))
        whenever(repository.login(email)).thenReturn(Result.failure(Exception(exceptionMessage)))

        viewModel.uiState.test {
            assert(awaitItem() is LoginUiState.Idle)
            viewModel.login(email)
            testDispatcher.scheduler.advanceUntilIdle()
            val loading = awaitItem()
            val error = awaitItem()
            assert(loading is LoginUiState.Loading)
            assert(error is LoginUiState.Error)
            assert((error as LoginUiState.Error).message.contains(exceptionMessage))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `login does not execute when already loading`() = runTest {
        val email = "test@example.com"
        whenever(validator.validateEmail(email)).thenReturn(ValidationResult(true, null))
        whenever(repository.login(email)).thenReturn(Result.success(mockSuccessResponse))

        viewModel.uiState.test {
            assert(awaitItem() is LoginUiState.Idle)
            viewModel.login(email)
            viewModel.login(email)
            testDispatcher.scheduler.advanceUntilIdle()

            val events = mutableListOf<LoginUiState>()
            repeat(4) { events.add(awaitItem()) }
            assert(events[0] is LoginUiState.Loading)
            assert(events[1] is LoginUiState.Success)
            assert(events[2] is LoginUiState.Loading)
            assert(events[3] is LoginUiState.Success)

            verify(repository, times(2)).login(email)
        }
    }

    // ---------- LOGOUT TESTS ----------
    @Test
    fun `logout with valid token returns success state`() = runTest {
        val token = "mock_refresh_token"
        whenever(repository.logout(token)).thenReturn(Result.success(mockLogoutSuccessResponse))

        viewModel.logoutUiState.test {
            assert(awaitItem() is LogoutUiState.Idle)
            viewModel.logout(token)
            testDispatcher.scheduler.advanceUntilIdle()
            val loading = awaitItem()
            val success = awaitItem()
            assert(loading is LogoutUiState.Loading)
            assert(success is LogoutUiState.Success)
        }
    }

    @Test
    fun `logout with valid token returns error state`() = runTest {
        val token = "mock_refresh_token"
        val exceptionMessage = mockLogoutFailureResponse.message ?: "Logout failed"
        whenever(repository.logout(token)).thenReturn(Result.failure(Exception(exceptionMessage)))

        viewModel.logoutUiState.test {
            assert(awaitItem() is LogoutUiState.Idle)
            viewModel.logout(token)
            testDispatcher.scheduler.advanceUntilIdle()
            val loading = awaitItem()
            val error = awaitItem()
            assert(loading is LogoutUiState.Loading)
            assert(error is LogoutUiState.Error && error.message.contains(exceptionMessage))
        }
    }

    @Test
    fun `logout does not break when multiple calls made during loading`() = runTest {
        val token = "mock_refresh_token"
        whenever(repository.logout(token)).thenReturn(Result.success(mockLogoutSuccessResponse))

        viewModel.logoutUiState.test {
            assert(awaitItem() is LogoutUiState.Idle)
            viewModel.logout(token)
            viewModel.logout(token)
            testDispatcher.scheduler.advanceUntilIdle()

            val events = mutableListOf<LogoutUiState>()
            repeat(4) { events.add(awaitItem()) }
            assert(events[0] is LogoutUiState.Loading)
            assert(events[1] is LogoutUiState.Success)
            assert(events[2] is LogoutUiState.Loading)
            assert(events[3] is LogoutUiState.Success)

            verify(repository, times(2)).logout(token)
        }
    }

    @Test
    fun `logout returns success even when API responds isSuccess false`() = runTest {
        val token = "mock_refresh_token"
        val apiResponse = mockLogoutSuccessResponse.copy(isSuccess = false, message = "Token invalid")
        whenever(repository.logout(token)).thenReturn(Result.success(apiResponse))

        viewModel.logoutUiState.test {
            assert(awaitItem() is LogoutUiState.Idle)
            viewModel.logout(token)
            testDispatcher.scheduler.advanceUntilIdle()
            val loading = awaitItem()
            val success = awaitItem()
            assert(loading is LogoutUiState.Loading)
            assert(success is LogoutUiState.Success)
        }
    }

    @Test
    fun `logout with empty token proceeds to repository call`() = runTest {
        val token = ""
        whenever(repository.logout(token)).thenReturn(Result.failure(Exception("Token invalid")))

        viewModel.logoutUiState.test {
            assert(awaitItem() is LogoutUiState.Idle)
            viewModel.logout(token)
            testDispatcher.scheduler.advanceUntilIdle()
            val loading = awaitItem()
            val error = awaitItem()
            assert(loading is LogoutUiState.Loading)
            assert(error is LogoutUiState.Error && error.message.contains("Token invalid"))
            verify(repository).logout(token)
        }
    }

    @Test
    fun `resetLogoutState sets state back to Idle`() = runTest {
        val token = "mock_refresh_token"
        whenever(repository.logout(token)).thenReturn(Result.success(mockLogoutSuccessResponse))

        viewModel.logout(token)
        testDispatcher.scheduler.advanceUntilIdle()
        assert(viewModel.logoutUiState.value is LogoutUiState.Success)
        viewModel.resetLogoutState()
        assert(viewModel.logoutUiState.value is LogoutUiState.Idle)
    }
}
