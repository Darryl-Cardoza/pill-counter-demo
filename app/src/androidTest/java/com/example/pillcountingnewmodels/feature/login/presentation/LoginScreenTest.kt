package com.example.pillcountingnewmodels.feature.login.presentation

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.pillcountingnewmodels.feature.login.viewmodel.LoginViewModel
import com.example.pillcountingnewmodels.feature.login.domain.model.LoginUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.any

// -----------------------------
// Helper: find TextField by placeholder text
// -----------------------------
fun SemanticsNodeInteractionsProvider.onTextFieldWithPlaceholder(
    placeholder: String
): SemanticsNodeInteraction {
    // This helper is slightly modified to be more robust.
    // It looks for a node that has a placeholder text property.
    return onNode(
        hasSetTextAction() and hasContentDescription(placeholder)
    )
}

@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val fakeUiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    private val mockViewModel = mock<LoginViewModel> {
        on { uiState }.thenReturn(fakeUiState)
    }
    private val mockNavController = mock<NavController>()

    @Test
    fun loginScreen_initialUIElementsAreVisible() {
        composeTestRule.setContent {
            LoginScreen(navController = mockNavController, viewModel = mockViewModel)
        }

        composeTestRule.onTextFieldWithPlaceholder("Email").assertExists()
        composeTestRule.onNodeWithText("LOGIN").assertExists()
        composeTestRule.onNodeWithText("Remember Me").assertExists()
    }

    @Test
    fun loginScreen_canTypeEmailAndToggleRememberMe() {
        composeTestRule.setContent {
            LoginScreen(navController = mockNavController, viewModel = mockViewModel)
        }

        // Type email
        val emailNode = composeTestRule.onTextFieldWithPlaceholder("Email")
        emailNode.performTextInput("test@example.com")
        composeTestRule.waitForIdle()
        // Assuming the TextField updates its text value directly
        emailNode.assert(hasText("test@example.com"))

        // Toggle checkbox
        composeTestRule.onNode(hasClickAction() and hasText("Remember Me"))
            .performClick()
    }

    @Test
    fun loginScreen_clickLogin_callsViewModelLogin() = runBlocking {
        composeTestRule.setContent {
            LoginScreen(navController = mockNavController, viewModel = mockViewModel)
        }

        // Type email
        composeTestRule.onTextFieldWithPlaceholder("Email")
            .performTextInput("test@example.com")
        composeTestRule.waitForIdle()

        // Click login
        composeTestRule.onNodeWithText("LOGIN").performClick()

        // Verify ViewModel login called
        verify(mockViewModel).login(email = "test@example.com")
    }

    @Test
    fun loginScreen_showsLoadingState() {
        composeTestRule.setContent {
            LoginScreen(navController = mockNavController, viewModel = mockViewModel)
        }

        // Trigger Loading state
        composeTestRule.runOnIdle { fakeUiState.value = LoginUiState.Loading }
        composeTestRule.waitForIdle()

        // Assert CircularProgressIndicator exists
        composeTestRule.onNodeWithTag("circularProgress").assertExists()
    }

    @Test
    fun loginScreen_showsErrorState() {
        composeTestRule.setContent {
            LoginScreen(navController = mockNavController, viewModel = mockViewModel)
        }

        val errorMessage = "Invalid credentials"

        // Trigger Error state
        composeTestRule.runOnIdle { fakeUiState.value = LoginUiState.Error(errorMessage) }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(errorMessage).assertExists()
    }

//    @Test
//    fun loginScreen_successNavigatesToOtpVerify() = runBlocking {
//        composeTestRule.setContent {
//            LoginScreen(navController = mockNavController, viewModel = mockViewModel)
//        }
//
//        // Trigger Success state
//        composeTestRule.runOnIdle { fakeUiState.value = LoginUiState.Success }
//        composeTestRule.waitForIdle()
//
//        // Verify navigation & ViewModel reset
//        verify(mockNavController).navigate(any<String>())
//        verify(mockViewModel).clearAll()
//    }

    // --- ADDED TESTS ---

    @Test
    fun loginScreen_loginButtonIsDisabledWhenEmailIsEmpty() {
        composeTestRule.setContent {
            LoginScreen(navController = mockNavController, viewModel = mockViewModel)
        }

        // Button should be disabled when the email field is empty
        composeTestRule.onNodeWithText("LOGIN").assertIsNotEnabled()
    }

    @Test
    fun loginScreen_loginButtonEnablesAfterTypingEmail() {
        composeTestRule.setContent {
            LoginScreen(navController = mockNavController, viewModel = mockViewModel)
        }

        // Initially disabled
        composeTestRule.onNodeWithText("LOGIN").assertIsNotEnabled()

        // Type email
        composeTestRule.onTextFieldWithPlaceholder("Email")
            .performTextInput("test@example.com")

        // Now it should be enabled
        composeTestRule.onNodeWithText("LOGIN").assertIsEnabled()
    }

    @Test
    fun loginScreen_disablesInputsWhenLoading() {
        composeTestRule.setContent {
            LoginScreen(navController = mockNavController, viewModel = mockViewModel)
        }

        // Set UI to loading state
        composeTestRule.runOnIdle { fakeUiState.value = LoginUiState.Loading }

        // Assert that the text field and button are disabled
        composeTestRule.onTextFieldWithPlaceholder("Email").assertIsNotEnabled()
        composeTestRule.onNodeWithText("LOGIN").assertIsNotEnabled()
    }
}