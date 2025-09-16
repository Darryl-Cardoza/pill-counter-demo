package com.example.pillcountingnewmodels.feature.dashboard.presentation.data

import com.example.pillcountingnewmodels.core.models.RefreshTokenRequest
import com.example.pillcountingnewmodels.core.models.RefreshTokenResponse
import com.example.pillcountingnewmodels.core.network.IApplicationSettingInterface
import com.example.pillcountingnewmodels.core.utils.MainDispatcherRule
import com.example.pillcountingnewmodels.core.utils.PreferenceHelper
import com.example.pillcountingnewmodels.feature.dashboard.data.UserDetailRepository
import com.example.pillcountingnewmodels.feature.dashboard.data.remote.IUserDetailAPI
import com.example.pillcountingnewmodels.feature.dashboard.domain.model.UserDetail
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class UserDetailRepositoryTest {

    private val scheduler = TestCoroutineScheduler()
    private val dispatcher = StandardTestDispatcher(scheduler)

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(dispatcher)

    private val api: IUserDetailAPI = mockk()
    private val applicationSettingApi: IApplicationSettingInterface = mockk()
    private val preferenceHelper: PreferenceHelper = mockk()
    private lateinit var repository: UserDetailRepository

    private val mockUserDetail = UserDetail(
        id = "123",
        name = "John Doe",
        email = "john.doe@example.com"
    )

    @Before
    fun setup() {
        repository = UserDetailRepository(api, applicationSettingApi, preferenceHelper, dispatcher)
    }

    @Test
    fun `getUserDetail returns success when API call succeeds`() = runTest(scheduler) {
        coEvery { api.getUserDetail("Bearer token") } returns Response.success(mockUserDetail)

        val result = repository.getUserDetail("token")

        assertTrue(result.isSuccess)
        assertEquals(mockUserDetail, result.getOrNull())
    }

    @Test
    fun `getUserDetail returns failure when response body is null`() = runTest(scheduler) {
        coEvery { api.getUserDetail("Bearer token") } returns Response.success(null)

        val result = repository.getUserDetail("token")

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getUserDetail refreshes token and retries when 401 and refresh succeeds`() = runTest(scheduler) {
        val refreshToken = "refresh123"
        val newAccessToken = "newToken"

        coEvery { api.getUserDetail("Bearer token") } returns Response.error(
            401, ResponseBody.create("application/json".toMediaTypeOrNull(), "{}")
        )
        coEvery { preferenceHelper.getRefreshToken() } returns refreshToken
        coEvery { applicationSettingApi.refreshToken(RefreshTokenRequest(refreshToken)) } returns
                RefreshTokenResponse(accessToken = newAccessToken, refreshToken = "newRefresh")
        coEvery { preferenceHelper.saveTokens(newAccessToken, "newRefresh") } just Runs
        coEvery { api.getUserDetail("Bearer $newAccessToken") } returns Response.success(mockUserDetail)

        val result = repository.getUserDetail("token")

        assertTrue(result.isSuccess)
        assertEquals(mockUserDetail, result.getOrNull())
    }

    @Test
    fun `getUserDetail fails when 401 and no refresh token`() = runTest(scheduler) {
        coEvery { api.getUserDetail("Bearer token") } returns Response.error(
            401, ResponseBody.create("application/json".toMediaTypeOrNull(), "{}")
        )
        coEvery { preferenceHelper.getRefreshToken() } returns null

        val result = repository.getUserDetail("token")

        assertTrue(result.isFailure)
        assertEquals("No refresh token available", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getUserDetail fails when 401 and refresh token request fails`() = runTest(scheduler) {
        val refreshToken = "refresh123"
        coEvery { api.getUserDetail("Bearer token") } returns Response.error(
            401, ResponseBody.create("application/json".toMediaTypeOrNull(), "{}")
        )
        coEvery { preferenceHelper.getRefreshToken() } returns refreshToken
        coEvery { applicationSettingApi.refreshToken(RefreshTokenRequest(refreshToken)) } throws Exception("Refresh failed")

        val result = repository.getUserDetail("token")

        assertTrue(result.isFailure)
        assertEquals("Refresh failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getUserDetail fails when 401, refresh succeeds, but retry fails`() = runTest(scheduler) {
        val refreshToken = "refresh123"
        val newAccessToken = "newToken"

        coEvery { api.getUserDetail("Bearer token") } returns Response.error(
            401, ResponseBody.create("application/json".toMediaTypeOrNull(), "{}")
        )
        coEvery { preferenceHelper.getRefreshToken() } returns refreshToken
        coEvery { applicationSettingApi.refreshToken(RefreshTokenRequest(refreshToken)) } returns
                RefreshTokenResponse(accessToken = newAccessToken, refreshToken = refreshToken)
        coEvery { preferenceHelper.saveTokens(newAccessToken, refreshToken) } just Runs
        coEvery { api.getUserDetail("Bearer $newAccessToken") } returns Response.error(
            500, ResponseBody.create("application/json".toMediaTypeOrNull(), "{}")
        )

        val result = repository.getUserDetail("token")

        assertTrue(result.isFailure)
        assertEquals(
            "Failed to fetch user detail after token refresh: HTTP 500",
            result.exceptionOrNull()?.message
        )
    }

    @Test
    fun `getUserDetail fails for other HTTP errors`() = runTest(scheduler) {
        coEvery { api.getUserDetail("Bearer token") } returns Response.error(
            404, ResponseBody.create("application/json".toMediaTypeOrNull(), "{}")
        )

        val result = repository.getUserDetail("token")

        assertTrue(result.isFailure)
        assertEquals("Error fetching user detail: 404", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getUserDetail fails for HttpException`() = runTest(scheduler) {
        coEvery { api.getUserDetail("Bearer token") } throws HttpException(
            Response.error<Any>(500, ResponseBody.create("application/json".toMediaTypeOrNull(), "{}"))
        )

        val result = repository.getUserDetail("token")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is HttpException)
    }

    @Test
    fun `getUserDetail fails for generic Exception`() = runTest(scheduler) {
        coEvery { api.getUserDetail("Bearer token") } throws Exception("Generic failure")

        val result = repository.getUserDetail("token")

        assertTrue(result.isFailure)
        assertEquals("Generic failure", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getUserDetail fails when token refresh response has null accessToken`() = runTest(scheduler) {
        val refreshToken = "refresh123"

        // Original request returns 401
        coEvery { api.getUserDetail("Bearer token") } returns Response.error(
            401, ResponseBody.create("application/json".toMediaTypeOrNull(), "{}")
        )

        // Refresh token exists
        coEvery { preferenceHelper.getRefreshToken() } returns refreshToken

        // Refresh response returns null accessToken
        coEvery { applicationSettingApi.refreshToken(RefreshTokenRequest(refreshToken)) } returns
                RefreshTokenResponse(accessToken = null, refreshToken = "newRefresh", message = "Invalid refresh token")

        val result = repository.getUserDetail("token")

        assertTrue(result.isFailure)
        assertEquals(
            "Failed to refresh token: Invalid refresh token",
            result.exceptionOrNull()?.message
        )
    }

    @Test
    fun `getUserDetail fails when retry after token refresh returns null body`() = runTest(scheduler) {
        val refreshToken = "refresh123"
        val newAccessToken = "newToken"

        // Original request returns 401
        coEvery { api.getUserDetail("Bearer token") } returns Response.error(
            401, ResponseBody.create("application/json".toMediaTypeOrNull(), "{}")
        )

        // Refresh token exists
        coEvery { preferenceHelper.getRefreshToken() } returns refreshToken

        // Refresh succeeds with valid accessToken
        coEvery { applicationSettingApi.refreshToken(RefreshTokenRequest(refreshToken)) } returns
                RefreshTokenResponse(accessToken = newAccessToken, refreshToken = "newRefresh")

        coEvery { preferenceHelper.saveTokens(newAccessToken, "newRefresh") } just Runs

        // Retry returns 200 but null body
        coEvery { api.getUserDetail("Bearer $newAccessToken") } returns Response.success(null)

        val result = repository.getUserDetail("token")

        assertTrue(result.isFailure)
        assertEquals(
            "Empty response body after retry",
            result.exceptionOrNull()?.message
        )
    }

}
