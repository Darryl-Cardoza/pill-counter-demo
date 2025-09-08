package com.example.pillcountingnewmodels.feature.otp.presentation.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.pillcountingnewmodels.R
import com.example.pillcountingnewmodels.core.utils.compose.ActionButtonPrimary
import com.example.pillcountingnewmodels.core.utils.compose.AppInfo
import com.example.pillcountingnewmodels.core.utils.compose.BackButton
import com.example.pillcountingnewmodels.core.utils.compose.OTPTextField
import com.example.pillcountingnewmodels.core.utils.compose.SplitResponsive
import com.example.pillcountingnewmodels.feature.otp.viewmodel.VerifyPinViewModel
import com.example.pillcountingnewmodels.feature.register.domain.model.VerifyPinUiState
import com.example.pillcountingnewmodels.ui.theme.AppTheme
import kotlinx.coroutines.delay

@Composable
fun OTPScreen(
    navController: NavController,
    userEmail: String,
    viewModel: VerifyPinViewModel = hiltViewModel()
) {
    var otp by remember { mutableStateOf("") }
    val verifyPinUiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val timer = 60
    var secRemaining by remember { mutableIntStateOf(timer) }
    var isTimerRunning by remember { mutableStateOf(true) }

    LaunchedEffect(isTimerRunning) {
        while (isTimerRunning && secRemaining > 0) {
            delay(1000L)
            secRemaining -= 1
        }
        if (secRemaining == 0) {
            isTimerRunning = false
        }
    }

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
                        text = "${stringResource(R.string.code_sent_to)} \n$userEmail",
                        color = AppTheme.extendedColors.textColor,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(50.dp))

                    // OTP Input Field
                    OTPTextField(
                        otp = otp,
                        onOtpChange = {
                            otp = it
                            viewModel.resetState()
                        },
                        boxCount = 4,
                        boxSize = 56.dp
                    )

                    Spacer(Modifier.height(40.dp))

                    Text(
                        text = if (isTimerRunning) stringResource(
                            R.string.pre_resend_code,
                            secRemaining
                        ) else stringResource(R.string.resend_code),
                        color = if (isTimerRunning) AppTheme.extendedColors.textColor else MaterialTheme.colorScheme.secondary,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = if (!isTimerRunning) {
                            Modifier.clickable {
                                // TODO: Add logic to call a 'resend OTP' API endpoint
                                secRemaining = timer
                                isTimerRunning = true
                            }
                        } else {
                            Modifier
                        }
                    )

                    Spacer(Modifier.height(20.dp))

                    // Handle UI state changes
                    when (val state = verifyPinUiState) {
                        is VerifyPinUiState.Idle -> {
                            Spacer(Modifier.height(20.dp))
                        }
                        is VerifyPinUiState.Error -> {
                            Text(
                                text = state.message,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }
                        is VerifyPinUiState.Loading -> {
                            CircularProgressIndicator(modifier = Modifier.padding(bottom = 16.dp))
                        }
                        is VerifyPinUiState.Success -> {
                            LaunchedEffect(Unit) {
                                // Navigate to Dashboard on successful verification
                                navController.navigate(Screen.Dashboard.route) {
                                    // Clear the entire back stack up to the dashboard
                                    popUpTo(Screen.Dashboard.route) { inclusive = true }
                                }
                            }
                        }
                    }

                    // Verify Button
                    ActionButtonPrimary(
                        text = stringResource(R.string.verify).uppercase(),
                        onClick = {
                            /*if (verifyPinUiState !is VerifyPinUiState.Loading) {
                                viewModel.verifyPin(userEmail, otp)
                            }*/
                            navController.navigate(Screen.Dashboard.route) {
                                // Clear the entire back stack up to the dashboard
                                popUpTo(Screen.Dashboard.route) { inclusive = true }
                            }
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        )
    }
}
