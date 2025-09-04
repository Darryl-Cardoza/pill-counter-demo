package com.example.pillcountingnewmodels.feature.register.presentation

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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.pillcountingnewmodels.R
import com.example.pillcountingnewmodels.core.utils.compose.AppInfo
import com.example.pillcountingnewmodels.core.utils.compose.ActionButtonPrimary
import com.example.pillcountingnewmodels.core.utils.compose.BackButton
import com.example.pillcountingnewmodels.core.utils.compose.OTPTextField
import com.example.pillcountingnewmodels.core.utils.compose.SplitResponsive
import com.example.pillcountingnewmodels.ui.theme.AppTheme

@Composable
fun OTPScreen(
    navController: NavController,
    userEmail: String,
) {
    var otp by remember { mutableStateOf("") }
    val context = LocalContext.current

    val timer = 60
    var secRemaining by remember { mutableIntStateOf(timer) }
    var isTimerRunning by remember { mutableStateOf(true) }

    LaunchedEffect(isTimerRunning) {
        while (isTimerRunning && secRemaining > 0) {
            kotlinx.coroutines.delay(1000L)
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
                        text = "${context.getString(R.string.code_sent_to)} \n$userEmail",
                        color = AppTheme.extendedColors.textColor,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(50.dp))

                    //otp
                    OTPTextField(
                        otp = otp,
                        onOtpChange = { otp = it },
                        boxCount = 4,
                        boxSize = 56.dp
                    )

                    Spacer(Modifier.height(50.dp))

                    Text(
                        text = if (isTimerRunning) stringResource(
                            R.string.pre_resend_code,
                            secRemaining
                        ) else context.getString(R.string.resend_code),
                        color = if (isTimerRunning) AppTheme.extendedColors.textColor else MaterialTheme.colorScheme.secondary,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = if (!isTimerRunning) {
                            Modifier.clickable {
                                secRemaining = timer
                                isTimerRunning = true
                            }
                        } else {
                            Modifier
                        }
                    )


                    Spacer(Modifier.height(40.dp))

                    //Login Button
                    ActionButtonPrimary(
                        text = context.getString(R.string.verify).uppercase(),
                        onClick = {

                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        )

    }
}