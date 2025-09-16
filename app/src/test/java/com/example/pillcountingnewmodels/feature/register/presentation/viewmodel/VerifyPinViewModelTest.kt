package com.example.pillcountingnewmodels.feature.otp.viewmodel

import android.content.Context
import app.cash.turbine.test
import com.example.pillcountingnewmodels.R
import com.example.pillcountingnewmodels.core.utils.MainDispatcherRule
import com.example.pillcountingnewmodels.core.utils.PreferenceHelper
import com.example.pillcountingnewmodels.feature.otp.data.VerifyPinRepository
import com.example.pillcountingnewmodels.feature.register.domain.model.VerifyPinUiState
import com.example.pillcountingnewmodels.feature.register.domain.model.VerifyPinResponse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class)
class VerifyPinViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: VerifyPinRepository
    private lateinit var context: Context
    private lateinit var prefs: PreferenceHelper
    private lateinit var viewModel: VerifyPinViewModel

    @Before
    fun setup() {
        repository = mock()
        context = mock()
        prefs = mock()

        whenever(context.getString(R.string.error_invalid_otp)).thenReturn("Invalid OTP")
        whenever(context.getString(R.string.error_unknown)).thenReturn("Unknown error")

        viewModel = VerifyPinViewModel(repository, context, prefs)
    }

    @Test
    fun `verifyPin returns error when OTP length is invalid`() = runTest {
        val email = "test@example.com"
        val otp = "12"

        viewModel.uiState.test {
            viewModel.verifyPin(email, otp)

            assert(awaitItem() is VerifyPinUiState.Idle)
            val error = awaitItem()
            assert(error is VerifyPinUiState.Error && error.message == "Invalid OTP")
        }
    }

    @Test
    fun `verifyPin succeeds and saves tokens`() = runTest {
        val email = "test@example.com"
        val otp = "1234"

        val response = VerifyPinResponse(
            isSuccess = true,
            accessToken = "access_token",
            refreshToken = "refresh_token"
        )

        whenever(repository.verifyPin(email, otp)).thenReturn(Result.success(response))

        viewModel.uiState.test {
            viewModel.verifyPin(email, otp)

            assert(awaitItem() is VerifyPinUiState.Idle)
            assert(awaitItem() is VerifyPinUiState.Loading)
            assert(awaitItem() is VerifyPinUiState.Success)

            verify(prefs).saveTokens("access_token", "refresh_token")
        }
    }

    @Test
    fun `verifyPin succeeds without tokens`() = runTest {
        val email = "test@example.com"
        val otp = "1234"

        val response = VerifyPinResponse(isSuccess = true)

        whenever(repository.verifyPin(email, otp)).thenReturn(Result.success(response))

        viewModel.uiState.test {
            viewModel.verifyPin(email, otp)

            skipItems(2) // Idle, Loading
            val success = awaitItem()
            assert(success is VerifyPinUiState.Success)

            // No token saving should happen
            verify(prefs, never()).saveTokens(any(), any())
        }
    }

    @Test
    fun `verifyPin fails with exception`() = runTest {
        val email = "test@example.com"
        val otp = "1234"

        whenever(repository.verifyPin(email, otp)).thenReturn(
            Result.failure(RuntimeException("Server down"))
        )

        viewModel.uiState.test {
            viewModel.verifyPin(email, otp)

            assert(awaitItem() is VerifyPinUiState.Idle)
            assert(awaitItem() is VerifyPinUiState.Loading)
            val error = awaitItem()
            assert(error is VerifyPinUiState.Error && error.message == "Server down")
        }
    }

    @Test
    fun `verifyPin fails with unknown error when exception has no message`() = runTest {
        val email = "test@example.com"
        val otp = "1234"

        whenever(repository.verifyPin(email, otp)).thenReturn(
            Result.failure(RuntimeException())
        )

        viewModel.uiState.test {
            viewModel.verifyPin(email, otp)

            assert(awaitItem() is VerifyPinUiState.Idle)
            assert(awaitItem() is VerifyPinUiState.Loading)
            val error = awaitItem()
            assert(error is VerifyPinUiState.Error && error.message == "Unknown error")
        }
    }

    @Test
    fun `resetState changes Error back to Idle`() = runTest {
        val email = "test@example.com"
        val otp = "12"

        // Start collecting emissions from uiState
        viewModel.uiState.test {
            // Initial state should be Idle
            assert(awaitItem() is VerifyPinUiState.Idle)

            // Trigger verifyPin which emits Error after processing
            viewModel.verifyPin(email, otp)

            // Await the Error emission
            val errorState = awaitItem()
            assert(errorState is VerifyPinUiState.Error)

            // Call resetState()
            viewModel.resetState()

            // Now await the Idle emission caused by resetState()
            val idleAfterReset = awaitItem()
            assert(idleAfterReset is VerifyPinUiState.Idle)

            cancelAndIgnoreRemainingEvents()
        }
    }


    @Test
    fun `clearAfterSuccess resets Success to Idle`() = runTest {
        val email = "test@example.com"
        val otp = "1234"
        val response = VerifyPinResponse(isSuccess = true)

        whenever(repository.verifyPin(email, otp)).thenReturn(Result.success(response))

        viewModel.verifyPin(email, otp)

        viewModel.uiState.test {
            skipItems(3) // Idle, Loading, Success
            viewModel.clearAfterSuccess()
            assert(awaitItem() is VerifyPinUiState.Idle)
        }
    }

    @Test
    fun `verifyPin does nothing if already loading`() = runTest {
        val email = "test@example.com"
        val otp = "1234"

        // Mock repository with a delay
        whenever(repository.verifyPin(email, otp)).thenAnswer {
            runBlocking {
                delay(1000) // suspending delay
                VerifyPinResponse(isSuccess = true)
            }
        }

        viewModel.uiState.test {
            // Trigger first call
            viewModel.verifyPin(email, otp)

            // Wait for Loading state before calling again
            assert(awaitItem() is VerifyPinUiState.Idle)
            assert(awaitItem() is VerifyPinUiState.Loading)

            // Now trigger second call
            viewModel.verifyPin(email, otp)

            // Await final Success from first call
            val success = awaitItem()
            assert(success is VerifyPinUiState.Success)

            // Verify repository was called only once
            verify(repository, times(1)).verifyPin(email, otp)
        }
    }

    @Test
    fun `verifyPin returns error when OTP is empty`() = runTest {
        val email = "test@example.com"
        val otp = ""

        viewModel.uiState.test {
            viewModel.verifyPin(email, otp)

            // Idle -> Error
            assert(awaitItem() is VerifyPinUiState.Idle)
            val error = awaitItem()
            assert(error is VerifyPinUiState.Error && error.message == "Invalid OTP")
        }
    }
}
