package com.example.pillcountingnewmodels.feature.login.data

import com.example.pillcountingnewmodels.feature.login.data.remote.ILoginApi
import com.example.pillcountingnewmodels.feature.login.domain.model.LoginRequest
import com.example.pillcountingnewmodels.feature.login.domain.model.LoginResponse
import com.example.pillcountingnewmodels.feature.login.domain.model.LogoutRequest
import com.example.pillcountingnewmodels.feature.login.domain.model.LogoutResponse
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

    // -------------------- LOGIN TESTS --------------------

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

        // API call succeeded, but the response indicates a business failure
        assertTrue(result.isSuccess)
        val responseData = result.getOrNull()
        assertEquals(errorResponse, responseData)
        assertEquals(false, responseData?.isSuccess)
        assertEquals("User not found", responseData?.message)
        verify(loginApi).login(LoginRequest(username))
    }

    // -------------------- LOGOUT TESTS --------------------

    @Test
    fun `logout returns success when API succeeds`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val repository = LoginRepository(loginApi, testDispatcher)

        val refreshToken = "mock_refresh"
        val logoutResponse = LogoutResponse(
            status = 200,
            isSuccess = true,
            message = "Logout successful"
        )

        `when`(loginApi.logout(LogoutRequest(refreshToken))).thenReturn(logoutResponse)

        val result = repository.logout(refreshToken)

        assertTrue(result.isSuccess)
        assertEquals(logoutResponse, result.getOrNull())
        verify(loginApi).logout(LogoutRequest(refreshToken))
    }

    @Test
    fun `logout returns failure when API throws network exception`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val repository = LoginRepository(loginApi, testDispatcher)

        val refreshToken = "mock_refresh"
        val exception = RuntimeException("Network error")

        `when`(loginApi.logout(LogoutRequest(refreshToken))).thenThrow(exception)

        val result = repository.logout(refreshToken)

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        verify(loginApi).logout(LogoutRequest(refreshToken))
    }

    @Test
    fun `logout propagates unexpected exceptions`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val repository = LoginRepository(loginApi, testDispatcher)

        val refreshToken = "mock_refresh"
        val exception = IllegalStateException("Unexpected error")

        `when`(loginApi.logout(LogoutRequest(refreshToken))).thenThrow(exception)

        val result = repository.logout(refreshToken)

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        verify(loginApi).logout(LogoutRequest(refreshToken))
    }

    @Test
    fun `logout multiple times with failures returns failure each time`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val repository = LoginRepository(loginApi, testDispatcher)

        val refreshToken = "mock_refresh"
        val exception = RuntimeException("API down")

        `when`(loginApi.logout(LogoutRequest(refreshToken))).thenThrow(exception)

        repeat(3) {
            val result = repository.logout(refreshToken)
            assertTrue(result.isFailure)
            assertEquals(exception, result.exceptionOrNull())
        }

        verify(loginApi, times(3)).logout(LogoutRequest(refreshToken))
    }

    @Test
    fun `logout returns success with business error when API indicates failure`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val repository = LoginRepository(loginApi, testDispatcher)

        val refreshToken = "invalid_token"
        val errorResponse = LogoutResponse(
            status = 200,
            isSuccess = false,
            message = "Invalid token"
        )

        `when`(loginApi.logout(LogoutRequest(refreshToken))).thenReturn(errorResponse)

        val result = repository.logout(refreshToken)

        // API call succeeded, but response indicates a business failure
        assertTrue(result.isSuccess)
        val responseData = result.getOrNull()
        assertEquals(errorResponse, responseData)
        assertEquals(false, responseData?.isSuccess)
        assertEquals("Invalid token", responseData?.message)
        verify(loginApi).logout(LogoutRequest(refreshToken))
    }
}
