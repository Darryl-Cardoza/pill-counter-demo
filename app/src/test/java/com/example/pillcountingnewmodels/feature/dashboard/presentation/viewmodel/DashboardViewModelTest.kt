package com.example.pillcountingnewmodels.feature.dashboard.presentation.viewmodel

import app.cash.turbine.test
import com.example.pillcountingnewmodels.core.utils.MainDispatcherRule
import com.example.pillcountingnewmodels.core.utils.PreferenceHelper
import com.example.pillcountingnewmodels.feature.dashboard.domain.data.IUserDetailRepository
import com.example.pillcountingnewmodels.feature.dashboard.domain.model.UserDetail
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.*

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: IUserDetailRepository
    private lateinit var preferenceHelper: PreferenceHelper
    private lateinit var viewModel: DashboardViewModel

    private val mockUserDetail = UserDetail(
        id = "123",
        name = "John Doe",
        email = "john.doe@example.com"
    )

    @Before
    fun setUp() {
        repository = mock(IUserDetailRepository::class.java)
        preferenceHelper = mock(PreferenceHelper::class.java)
    }

    /** Dashboard counts initialization **/
    @Test
    fun `init loads dashboard data with zero counts`() = runTest {
        `when`(preferenceHelper.getAccessToken()).thenReturn("valid_token")
        `when`(repository.getUserDetail("valid_token")).thenReturn(Result.success(mockUserDetail))

        viewModel = DashboardViewModel(repository, preferenceHelper)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("0", state.completedFixedCount)
            assertEquals("0", state.partialFixedCount)
            assertEquals("0", state.completedRegularCount)
            assertEquals("0", state.partialRegularCount)
            cancelAndIgnoreRemainingEvents()
        }
    }

    /** Token missing scenario **/
    @Test
    fun `fetchUserDetail sets error when token is null`() = runTest {
        `when`(preferenceHelper.getAccessToken()).thenReturn(null)
        viewModel = DashboardViewModel(repository, preferenceHelper)

        viewModel.uiState.test {
            awaitItem() // initial dashboard data
            val errorState = awaitItem()
            assertEquals("Access token not found", errorState.userDetailError)
            assertEquals(false, errorState.isLoadingUserDetail)
            cancelAndIgnoreRemainingEvents()
        }
    }

    /** Token blank scenario **/
    @Test
    fun `fetchUserDetail sets error when token is blank`() = runTest {
        `when`(preferenceHelper.getAccessToken()).thenReturn("")
        viewModel = DashboardViewModel(repository, preferenceHelper)

        viewModel.uiState.test {
            awaitItem()
            val errorState = awaitItem()
            assertEquals("Access token not found", errorState.userDetailError)
            assertEquals(false, errorState.isLoadingUserDetail)
            cancelAndIgnoreRemainingEvents()
        }
    }

    /** Successful fetch **/
    @Test
    fun `fetchUserDetail sets userDetail on success`() = runTest {
        `when`(preferenceHelper.getAccessToken()).thenReturn("valid_token")
        `when`(repository.getUserDetail("valid_token")).thenReturn(Result.success(mockUserDetail))

        viewModel = DashboardViewModel(repository, preferenceHelper)

        viewModel.uiState.test {
            awaitItem() // initial dashboard data
            val loadingState = awaitItem()
            assertEquals(true, loadingState.isLoadingUserDetail)

            val successState = awaitItem()
            assertEquals(mockUserDetail, successState.userDetail)
            assertEquals(false, successState.isLoadingUserDetail)
            assertNull(successState.userDetailError)
            cancelAndIgnoreRemainingEvents()
        }
    }

    /** Repository failure scenario **/
    @Test
    fun `fetchUserDetail sets error on failure`() = runTest {
        val exception = Exception("Network error")
        `when`(preferenceHelper.getAccessToken()).thenReturn("valid_token")
        `when`(repository.getUserDetail("valid_token")).thenReturn(Result.failure(exception))

        viewModel = DashboardViewModel(repository, preferenceHelper)

        viewModel.uiState.test {
            awaitItem() // initial dashboard data
            val loadingState = awaitItem()
            assertEquals(true, loadingState.isLoadingUserDetail)

            val errorState = awaitItem()
            assertNull(errorState.userDetail)
            assertEquals(false, errorState.isLoadingUserDetail)
            assertEquals("Network error", errorState.userDetailError)
            cancelAndIgnoreRemainingEvents()
        }
    }

    /** Multiple consecutive fetches **/
    @Test
    fun `fetchUserDetail handles consecutive calls correctly`() = runTest {
        `when`(preferenceHelper.getAccessToken()).thenReturn("valid_token")
        `when`(repository.getUserDetail("valid_token")).thenReturn(Result.success(mockUserDetail))

        viewModel = DashboardViewModel(repository, preferenceHelper)

        // Trigger fetchUserDetail manually multiple times
        viewModel.uiState.test {
            awaitItem() // initial state

            viewModel.fetchUserDetail()
            val loadingState1 = awaitItem()
            assertEquals(true, loadingState1.isLoadingUserDetail)

            val successState1 = awaitItem()
            assertEquals(mockUserDetail, successState1.userDetail)

            viewModel.fetchUserDetail()
            val loadingState2 = awaitItem()
            assertEquals(true, loadingState2.isLoadingUserDetail)

            val successState2 = awaitItem()
            assertEquals(mockUserDetail, successState2.userDetail)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
