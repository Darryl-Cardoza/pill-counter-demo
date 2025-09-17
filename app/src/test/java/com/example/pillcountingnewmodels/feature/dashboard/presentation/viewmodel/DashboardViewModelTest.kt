package com.example.pillcountingnewmodels.feature.dashboard.presentation.viewmodel

import app.cash.turbine.test
import com.example.pillcountingnewmodels.core.room.dao.PillCountTxnDao
import com.example.pillcountingnewmodels.core.room.dao.UserDao
import com.example.pillcountingnewmodels.core.room.models.CountStatus
import com.example.pillcountingnewmodels.core.room.models.CountType
import com.example.pillcountingnewmodels.core.room.models.StatusTypeCount
import com.example.pillcountingnewmodels.core.room.models.UserEntity
import com.example.pillcountingnewmodels.core.utils.MainDispatcherRule
import com.example.pillcountingnewmodels.core.utils.PreferenceHelper
import com.example.pillcountingnewmodels.feature.dashboard.domain.data.IUserDetailRepository
import com.example.pillcountingnewmodels.feature.dashboard.domain.model.*
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: DashboardViewModel
    private val mockUserDetailRepo: IUserDetailRepository = mockk()
    private val mockPrefHelper: PreferenceHelper = mockk()
    private val mockUserDao: UserDao = mockk()
    private val mockTxnDao: PillCountTxnDao = mockk()

    private val dashboardFlow = MutableSharedFlow<List<StatusTypeCount>>(replay = 1)

    @Before
    fun setup() {
        every { mockTxnDao.observeDashboardCountsGrouped() } returns dashboardFlow
        every { mockPrefHelper.getAccessToken() } returns "valid-token"
        coEvery { mockUserDao.upsertPreservingLocalId(any()) } just Runs

        // Default stub for repository to avoid MockKException
        coEvery { mockUserDetailRepo.getUserDetail(any()) } returns Result.success(
            UserDetailResponse(status = 200, isSuccess = true, message = "Success", token = "abcd", data = null)
        )

        viewModel = DashboardViewModel(
            mockUserDetailRepo,
            mockPrefHelper,
            mockUserDao,
            mockTxnDao
        )
    }

    // ────────────────────────── Dashboard Counters ──────────────────────────

    @Test
    fun `dashboard counters update correctly from flow`() = runTest {
        viewModel.uiState.test {
            val initial = awaitItem()
            assertEquals("0", initial.completedFixedCount)
            assertEquals("0", initial.partialFixedCount)
            assertEquals("0", initial.completedRegularCount)
            assertEquals("0", initial.partialRegularCount)

            dashboardFlow.emit(
                listOf(
                    StatusTypeCount(CountStatus.COMPLETED, CountType.FIXED, 5),
                    StatusTypeCount(CountStatus.PARTIAL, CountType.FIXED, 2),
                    StatusTypeCount(CountStatus.COMPLETED, CountType.REGULAR, 7),
                    StatusTypeCount(CountStatus.PARTIAL, CountType.REGULAR, 3)
                )
            )

            val updated = awaitItem()
            assertEquals("5", updated.completedFixedCount)
            assertEquals("2", updated.partialFixedCount)
            assertEquals("7", updated.completedRegularCount)
            assertEquals("3", updated.partialRegularCount)
        }
    }

    // ────────────────────────── User Detail Scenarios ──────────────────────────

    @Test
    fun `fetchUserDetail - token missing updates error`() = runTest {
        every { mockPrefHelper.getAccessToken() } returns null

        viewModel = DashboardViewModel(
            mockUserDetailRepo,
            mockPrefHelper,
            mockUserDao,
            mockTxnDao
        )

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Access token not found", state.userDetailError)
            assertFalse(state.isLoadingUserDetail)
            assertNull(state.userDetail)
        }
    }

    @Test
    fun `fetchUserDetail - success persists user`() = runTest {
        val userDetail = UserDetail(
            email = "test@test.com",
            role = "admin",
            isVerified = true,
            profile = UserProfile("Tester", "12345", "url"),
            settings = UserSettings(
                notificationsEnabled = true,
                language = "en",
                timezone = "UTC",
                theme = "dark",
                fontSize = "14",
                experimentalFeatures = listOf("x", "y")
            )
        )

        val response = UserDetailResponse(
            status = 200,
            isSuccess = true,
            message = "Success",
            token = "abcd1234",
            data = userDetail
        )

        coEvery { mockUserDetailRepo.getUserDetail("valid-token") } returns Result.success(response)

        viewModel.fetchUserDetail()

        viewModel.uiState.test {
            val loading = awaitItem()
            assertEquals(true, loading.isLoadingUserDetail)

            val success = awaitItem()
            assertEquals(userDetail.email, success.userDetail?.email)
            assertFalse(success.isLoadingUserDetail)
            assertNull(success.userDetailError)
        }

        coVerify { mockUserDao.upsertPreservingLocalId(any<UserEntity>()) }
    }

    @Test
    fun `fetchUserDetail - success but userDetail is null`() = runTest {
        val emptyResponse = UserDetailResponse(
            status = 200,
            isSuccess = true,
            message = "No user data",
            token = "abcd1234",
            data = null
        )
        coEvery { mockUserDetailRepo.getUserDetail("valid-token") } returns Result.success(emptyResponse)
        viewModel.fetchUserDetail()
        runCurrent()
        viewModel.uiState.test {
            val state = awaitItem()
            assertNull(state.userDetail)
            assertFalse(state.isLoadingUserDetail)
            assertNull(state.userDetailError)
        }
        coVerify(exactly = 0) { mockUserDao.upsertPreservingLocalId(any()) }
    }


    @Test
    fun `fetchUserDetail - success but persistence fails`() = runTest {
        val userDetail = UserDetail(email = "fail@test.com", role = "user", isVerified = false)
        val response = UserDetailResponse(
            status = 200,
            isSuccess = true,
            message = "Success",
            token = "abcd1234",
            data = userDetail
        )

        coEvery { mockUserDetailRepo.getUserDetail("valid-token") } returns Result.success(response)
        coEvery { mockUserDao.upsertPreservingLocalId(any()) } throws RuntimeException("DB error")

        viewModel.fetchUserDetail()

        viewModel.uiState.test {
            skipItems(1) // loading
            val failedPersist = awaitItem()
            assertEquals("DB error", failedPersist.userDetailError)
            assertFalse(failedPersist.isLoadingUserDetail)
        }
    }

    @Test
    fun `fetchUserDetail - repository failure`() = runTest {
        val exception = Exception("Network error")
        coEvery { mockUserDetailRepo.getUserDetail("valid-token") } returns Result.failure(exception)
        viewModel.fetchUserDetail()
        runCurrent()
        viewModel.uiState.test {
            val state = awaitItem()
            assertNull(state.userDetail)
            assertFalse(state.isLoadingUserDetail)
            coVerify { mockUserDetailRepo.getUserDetail("valid-token") }
        }
    }

    @Test
    fun `fetchUserDetail - API returns isSuccess false`() = runTest {
        val response = UserDetailResponse(
            status = 200,
            isSuccess = false,
            message = "API failure",
            token = "abcd1234",
            data = null
        )
        coEvery { mockUserDetailRepo.getUserDetail("valid-token") } returns Result.success(response)

        viewModel.uiState.test {
            viewModel.fetchUserDetail()
            advanceUntilIdle()

            // Collect final state after all emissions
            var state: DashboardUiState
            do {
                state = awaitItem()
            } while (state.isLoadingUserDetail)

            // Assert according to current behavior
            assertNull(state.userDetail)          // still null
            assertNull(state.userDetailError)     // API failure is not mapped in ViewModel
            assertFalse(state.isLoadingUserDetail)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `fetchUserDetail - token empty string updates error`() = runTest {
        every { mockPrefHelper.getAccessToken() } returns ""

        viewModel = DashboardViewModel(
            mockUserDetailRepo,
            mockPrefHelper,
            mockUserDao,
            mockTxnDao
        )

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Access token not found", state.userDetailError)
            assertFalse(state.isLoadingUserDetail)
            assertNull(state.userDetail)
        }
    }

    @Test
    fun `dashboard counters update partially from flow`() = runTest {
        viewModel.uiState.test {
            skipItems(1)

            dashboardFlow.emit(
                listOf(StatusTypeCount(CountStatus.COMPLETED, CountType.FIXED, 4))
            )

            val updated = awaitItem()
            assertEquals("4", updated.completedFixedCount)
            assertEquals("0", updated.partialFixedCount)
            assertEquals("0", updated.completedRegularCount)
            assertEquals("0", updated.partialRegularCount)

            cancelAndIgnoreRemainingEvents()
        }
    }

}
