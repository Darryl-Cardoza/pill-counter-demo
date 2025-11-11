package com.example.pillcountingnewmodels.feature.register.presentation.data

import com.example.pillcountingnewmodels.feature.otp.data.VerifyPinRepository
import com.example.pillcountingnewmodels.feature.register.data.remote.IVerifyPinAPI
import com.example.pillcountingnewmodels.feature.register.domain.model.VerifyPinRequest
import com.example.pillcountingnewmodels.feature.register.domain.model.VerifyPinResponse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class)
class VerifyPinRepositoryTest {

    private lateinit var repository: VerifyPinRepository
    private val api: IVerifyPinAPI = mock()

    private val email = "test@example.com"
    private val otp = "1234"
    private val request = VerifyPinRequest(email, otp)
    private val successResponse = VerifyPinResponse(isSuccess = true)

    @Before
    fun setUp() {
        // repository initialized in each test with proper dispatcher
    }

    @Test
    fun `verifyPin returns success when API succeeds`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        repository = VerifyPinRepository(api, dispatcher)

        whenever(api.verifyPin(request)).thenReturn(successResponse)

        val result = repository.verifyPin(email, otp)

        assertTrue(result.isSuccess)
        assertEquals(successResponse, result.getOrNull())
        verify(api, times(1)).verifyPin(request)
    }

    @Test
    fun `verifyPin returns failure when API throws runtime exception`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        repository = VerifyPinRepository(api, dispatcher)

        val exception = RuntimeException("Network error")
        whenever(api.verifyPin(request)).thenThrow(exception)

        val result = repository.verifyPin(email, otp)

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        verify(api, times(1)).verifyPin(request)
    }

    @Test
    fun `verifyPin wraps HttpException as failure`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        repository = VerifyPinRepository(api, dispatcher)

        val httpException = mock<retrofit2.HttpException>()
        whenever(api.verifyPin(request)).thenThrow(httpException)

        val result = repository.verifyPin(email, otp)

        assertTrue(result.isFailure)
        assertEquals(httpException, result.exceptionOrNull())
        verify(api, times(1)).verifyPin(request)
    }

    @Test
    fun `verifyPin calls API correct number of times`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        repository = VerifyPinRepository(api, dispatcher)

        whenever(api.verifyPin(request)).thenReturn(successResponse)

        repository.verifyPin(email, otp)
        repository.verifyPin(email, otp)

        verify(api, times(2)).verifyPin(request)
    }

    @Test
    fun `verifyPin runs on provided dispatcher`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        repository = VerifyPinRepository(api, dispatcher)

        var executed = false
        whenever(api.verifyPin(request)).thenAnswer {
            executed = true
            successResponse
        }

        val job = launch(dispatcher) { repository.verifyPin(email, otp) }

        // coroutine not yet executed
        assertTrue(!executed)

        // advance scheduler to run the coroutine
        testScheduler.advanceUntilIdle()

        assertTrue(executed)
    }

    @Test
    fun `verifyPin handles generic network exceptions as failure`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        repository = VerifyPinRepository(api, dispatcher)

        val exception = RuntimeException("IO/network error")
        whenever(api.verifyPin(request)).thenThrow(exception)

        val result = repository.verifyPin(email, otp)

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `verifyPin handles delayed API response correctly`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        repository = VerifyPinRepository(api, dispatcher)

        var executed = false
        val delayTime = 1000L

        whenever(api.verifyPin(request)).thenAnswer {
            runBlocking {
                executed = true
                delay(delayTime)  // simulate network delay
                successResponse
            }
        }

        val resultDeferred = launch(dispatcher) { repository.verifyPin(email, otp) }

        // coroutine not yet executed
        assertTrue(!executed)

        // advance virtual time partially
        testScheduler.advanceTimeBy(delayTime / 2)
        assertTrue(executed) // already entered suspend function
        // but result not yet completed

        // advance time to complete the suspend
        testScheduler.advanceUntilIdle()

        resultDeferred.join() // ensure coroutine completes

        val result = repository.verifyPin(email, otp)
        assertTrue(result.isSuccess)
        assertEquals(successResponse, result.getOrNull())
    }

}
