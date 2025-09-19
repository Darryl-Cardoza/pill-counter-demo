package com.example.pillcountingnewmodels.feature.login.presentation

import Screen
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.pillcountingnewmodels.R
import com.example.pillcountingnewmodels.core.utils.compose.ActionButtonPrimary
import com.example.pillcountingnewmodels.core.utils.compose.AppInfo
import com.example.pillcountingnewmodels.core.utils.compose.DrawableIconTextField
import com.example.pillcountingnewmodels.core.utils.compose.SplitResponsive
import com.example.pillcountingnewmodels.feature.login.domain.model.LoginUiState
import com.example.pillcountingnewmodels.feature.login.viewmodel.LoginViewModel
import com.example.pillcountingnewmodels.feature.pillCountScan.presentation.compose.TargetPillsCountDialog
import com.example.pillcountingnewmodels.ui.theme.AppTheme

/**
 * Composable that renders the Login Screen UI.
 *
 * It follows a unidirectional data flow by observing states from [LoginViewModel]
 * and delegating user actions to it.
 *
 * @param navController NavController for screen navigation.
 * @param viewModel LoginViewModel instance scoped to this screen.
 */
@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }

    val loginUiState by viewModel.uiState.collectAsState()

    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .background(AppTheme.extendedColors.secondaryBackground)
    ) {
        SplitResponsive(
            topOrLeft = { AppInfo(context) },
            bottomOrRight = {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Email Input Field
                        DrawableIconTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                viewModel.resetLoginState()
                            },
                            placeholder = stringResource(R.string.email),
                            iconRes = R.drawable.profile,
                            iconColor = MaterialTheme.colorScheme.secondary,
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Done
                        )

                        Spacer(Modifier.height(25.dp))

                        // Remember Me Checkbox
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .border(
                                        width = 0.5.dp,
                                        color = AppTheme.extendedColors.textColor,
                                        shape = RoundedCornerShape(3.dp)
                                    )
                                    .background(
                                        Color.White, shape = RoundedCornerShape(3.dp)
                                    )
                            ) {
                                Checkbox(
                                    checked = rememberMe,
                                    onCheckedChange = { rememberMe = it },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = Color.White,
                                        uncheckedColor = Color.White,
                                        checkmarkColor = Color.Black,
                                    ),
                                    modifier = Modifier.scale(0.8f)
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.remember_me),
                                color = AppTheme.extendedColors.textColor,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }

                        Spacer(Modifier.height(20.dp))

                        // Display login UI state feedback
                        when (val state = loginUiState) {
                            is LoginUiState.Idle -> Spacer(Modifier.height(30.dp))

                            is LoginUiState.Error -> Text(
                                text = state.message,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            is LoginUiState.Loading -> CircularProgressIndicator(
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            is LoginUiState.Success -> {
                                LaunchedEffect(Unit) {

                                    viewModel.clearAllStates()
                                    navController.navigate(
                                        Screen.OtpVerify.createRoute(
                                            email = email,
                                            rememberMe = rememberMe
                                        )
                                    )
                                    email = ""
                                    rememberMe = false
                                }
                            }

                        }

                        // Login Button
                        ActionButtonPrimary(
                            text = "LOGIN",
                            onClick = { viewModel.login(email = email) },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                }
            }
        )
    }
}
