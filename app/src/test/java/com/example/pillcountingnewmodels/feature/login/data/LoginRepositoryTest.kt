package com.example.pillcountingnewmodels.feature.login.data

import com.example.pillcountingnewmodels.feature.login.data.remote.ILoginApi
import com.example.pillcountingnewmodels.feature.login.domain.model.LoginRequest
import com.example.pillcountingnewmodels.feature.login.domain.model.LoginResponse
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

class LoginRepositoryTest {

    private lateinit var loginApi: ILoginApi

    @Before
    fun setUp() {
        loginApi = mock(ILoginApi::class.java)
    }

    @Test
    fun `login returns success when API succeeds`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val repository = LoginRepository(loginApi, testDispatcher)

        val username = "anyuser"
        val loginResponse = LoginResponse(
            status = 200,
            isSuccess = true,
            message = "Login successful",
            token = "token123",
            data = null
        )

        `when`(loginApi.login(LoginRequest(username))).thenReturn(loginResponse)

        val result = repository.login(username)

        assertTrue(result.isSuccess)
        assertEquals(loginResponse, result.getOrNull())
        verify(loginApi).login(LoginRequest(username))
    }

    @Test
    fun `login returns failure when API throws network exception`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val repository = LoginRepository(loginApi, testDispatcher)

        val username = "anyuser"
        val exception = RuntimeException("Network error")

        `when`(loginApi.login(LoginRequest(username))).thenThrow(exception)

        val result = repository.login(username)

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        verify(loginApi).login(LoginRequest(username))
    }

    @Test
    fun `login propagates unexpected exceptions`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val repository = LoginRepository(loginApi, testDispatcher)

        val username = "anyuser"
        val exception = RuntimeException("Unexpected error")

        `when`(loginApi.login(LoginRequest(username))).thenThrow(exception)

        val result = repository.login(username)

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        verify(loginApi).login(LoginRequest(username))
    }

    @Test
    fun `login multiple times with failures returns failure each time`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val repository = LoginRepository(loginApi, testDispatcher)

        val username = "anyuser"
        val exception = RuntimeException("API down")

        `when`(loginApi.login(LoginRequest(username))).thenThrow(exception)

        repeat(3) {
            val result = repository.login(username)
            assertTrue(result.isFailure)
            assertEquals(exception, result.exceptionOrNull())
        }

        verify(loginApi, times(3)).login(LoginRequest(username))
    }

    @Test
    fun `login returns success with business error when API indicates failure`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val repository = LoginRepository(loginApi, testDispatcher)

        val username = "wronguser"
        val errorResponse = LoginResponse(
            status = 200,
            isSuccess = false,
            message = "User not found",
            token = null,
            data = null
        )

        `when`(loginApi.login(LoginRequest(username))).thenReturn(errorResponse)

        val result = repository.login(username)

        // The API call succeeded, but the response indicates a business failure
        assertTrue(result.isSuccess)
        val responseData = result.getOrNull()
        assertEquals(errorResponse, responseData)
        assertEquals(false, responseData?.isSuccess)
        assertEquals("User not found", responseData?.message)
        verify(loginApi).login(LoginRequest(username))
    }
}
