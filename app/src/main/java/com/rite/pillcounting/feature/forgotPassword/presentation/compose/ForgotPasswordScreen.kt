package com.rite.pillcounting.feature.forgotPassword.presentation.compose

import Screen
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.rite.pillcounting.R
import com.rite.pillcounting.core.utils.common.UserInterfaceUtils
import com.rite.pillcounting.core.utils.common.UserInterfaceUtils.AppInfo
import com.rite.pillcounting.core.utils.common.UserInterfaceUtils.BackButton
import com.rite.pillcounting.core.utils.common.UserInterfaceUtils.DrawableIconTextField
import com.rite.pillcounting.core.utils.common.UserInterfaceUtils.ActionButtonPrimary
import com.rite.pillcounting.core.utils.common.UserInterfaceUtils.LoadingIndicator
import com.rite.pillcounting.core.utils.compose.SplitResponsive
import com.rite.pillcounting.feature.forgotPassword.domain.model.ForgotPasswordUiState
import com.rite.pillcounting.feature.forgotPassword.presentation.viewmodel.ForgotPasswordViewModel
import com.rite.pillcounting.ui.theme.AppTheme

/**
 * Forgot Password screen entry point.
 *
 * Layout:
 * - [AppInfo] section at the top/left.
 * - Form section at the bottom/right:
 *   - Title & description
 *   - Email input
 *   - State-driven feedback (loading, error, success)
 *   - [ActionButtonPrimary] for sending OTP
 *
 * @param navController Used for navigating to OTP verification screen.
 * @param viewModel ViewModel handling business logic and state for Forgot Password.
 */
@Composable
fun ForgotPasswordScreen(
    navController: NavController,
    viewModel: ForgotPasswordViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .background(AppTheme.extendedColors.secondaryBackground)
    ) {
        // Back navigation
        BackButton(navController)

        // Responsive split layout: app info vs form
        SplitResponsive(
            topOrLeft = { AppInfo() },
            bottomOrRight = {
                ForgotPasswordForm(
                    email = email,
                    onEmailChange = {
                        email = it
                        viewModel.resetState()
                    },
                    uiState = uiState,
                    onSendOtp = { trySendOtp(viewModel, email, context) },
                    navController = navController,
                    context = context
                )
            }
        )
    }
}

/**
 * Forgot Password form UI.
 */
@Composable
private fun ForgotPasswordForm(
    email: String,
    onEmailChange: (String) -> Unit,
    uiState: ForgotPasswordUiState,
    onSendOtp: () -> Unit,
    navController: NavController,
    context: Context
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title
        Text(
            text = stringResource(R.string.password_recovery),
            color = AppTheme.extendedColors.textColor,
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(Modifier.height(15.dp))

        // Description
        Text(
            text = stringResource(R.string.password_recovery_desc),
            color = AppTheme.extendedColors.textColor,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(35.dp))

        // Email input field
        DrawableIconTextField(
            value = email,
            onValueChange = onEmailChange,
            placeholder = stringResource(R.string.your_registered_email),
            iconRes = R.drawable.profile,
            iconColor = MaterialTheme.colorScheme.secondary,
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Done
        )

        Spacer(Modifier.height(55.dp))

        // State handling
        when (uiState) {
            is ForgotPasswordUiState.Idle -> Unit
            is ForgotPasswordUiState.Loading -> {
                LoadingIndicator()
            }

            is ForgotPasswordUiState.Error -> {
                LaunchedEffect(uiState.message) {
                    UserInterfaceUtils.showToast(context, uiState.message)
                }
            }

            is ForgotPasswordUiState.Success -> {
                LaunchedEffect(uiState.email) {
                    navController.navigate(
                        Screen.OtpVerify.createRoute(
                            email = uiState.email,
                            rememberMe = false
                        )
                    )
                }
            }
        }

        // Send OTP button
        ActionButtonPrimary(
            text = stringResource(R.string.send_otp).uppercase(),
            onClick = onSendOtp,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
    }
}

/**
 * Helper function to validate and send OTP.
 */
private fun trySendOtp(
    viewModel: ForgotPasswordViewModel,
    email: String,
    context: Context
) {
    if (email.isBlank()) {
        UserInterfaceUtils.showToast(context, context.getString(R.string.error_email_invalid))
    } else {
        viewModel.sendOtp(email)
    }
}
