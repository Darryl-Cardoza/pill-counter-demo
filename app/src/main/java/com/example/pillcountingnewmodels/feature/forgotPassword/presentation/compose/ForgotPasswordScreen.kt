package com.example.pillcountingnewmodels.feature.forgotpassword.presentation.compose

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
import androidx.compose.material3.CircularProgressIndicator
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
import com.example.pillcountingnewmodels.R
import com.example.pillcountingnewmodels.core.utils.compose.ActionButtonPrimary
import com.example.pillcountingnewmodels.core.utils.compose.AppInfo
import com.example.pillcountingnewmodels.core.utils.compose.BackButton
import com.example.pillcountingnewmodels.core.utils.compose.DrawableIconTextField
import com.example.pillcountingnewmodels.core.utils.compose.SplitResponsive
import com.example.pillcountingnewmodels.feature.forgotPassword.domain.model.ForgotPasswordUiState
import com.example.pillcountingnewmodels.feature.forgotPassword.viewmodel.ForgotPasswordViewModel
import com.example.pillcountingnewmodels.ui.theme.AppTheme

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
        BackButton(navController)

        SplitResponsive(
            topOrLeft = {
                AppInfo(context)
            },
            bottomOrRight = {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = stringResource(R.string.password_recovery),
                        color = AppTheme.extendedColors.textColor,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(Modifier.height(15.dp))

                    Text(
                        text = stringResource(R.string.password_recovery_desc),
                        color = AppTheme.extendedColors.textColor,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(35.dp))

                    //Email
                    DrawableIconTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            viewModel.resetState()
                        },
                        placeholder = stringResource(R.string.your_registered_email),
                        iconRes = R.drawable.profile,
                        iconColor = MaterialTheme.colorScheme.secondary,
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done,
                        onImeAction = { viewModel.sendOtp(email) }
                    )

                    Spacer(Modifier.height(55.dp))

                    // Handle UI state changes
                    when (val state = uiState) {
                        is ForgotPasswordUiState.Idle -> {
                            Spacer(Modifier.height(20.dp))
                        }
                        is ForgotPasswordUiState.Error -> {
                            Text(
                                text = state.message,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }
                        is ForgotPasswordUiState.Loading -> {
                            CircularProgressIndicator(modifier = Modifier.padding(bottom = 16.dp))
                        }
                        is ForgotPasswordUiState.Success -> {
                            LaunchedEffect(Unit) {
                                navController.navigate(Screen.OtpVerify.createRoute(state.email))
                            }
                        }
                    }

                    //Send otp
                    ActionButtonPrimary(
                        text = stringResource(R.string.send_otp).uppercase(),
                        onClick = {
                            if (uiState !is ForgotPasswordUiState.Loading) {
                                viewModel.sendOtp(email)
                            }
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        )
    }
}
