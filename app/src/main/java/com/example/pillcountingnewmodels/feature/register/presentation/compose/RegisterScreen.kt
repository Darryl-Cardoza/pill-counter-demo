package com.example.pillcountingnewmodels.feature.register.presentation

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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.pillcountingnewmodels.R
import com.example.pillcountingnewmodels.core.utils.compose.ActionButtonPrimary
import com.example.pillcountingnewmodels.core.utils.compose.AppInfo
import com.example.pillcountingnewmodels.core.utils.compose.BackButton
import com.example.pillcountingnewmodels.core.utils.compose.DrawableIconTextField
import com.example.pillcountingnewmodels.core.utils.compose.SplitResponsive
import com.example.pillcountingnewmodels.feature.register.domain.model.RegisterUiState
import com.example.pillcountingnewmodels.feature.register.viewmodel.RegisterViewModel
import com.example.pillcountingnewmodels.ui.theme.AppTheme

@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val registerUiState by viewModel.uiState.collectAsState()

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

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
                    verticalArrangement = Arrangement.Center
                ) {

                    //Email
                    DrawableIconTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            viewModel.resetState()
                        },
                        placeholder = stringResource(R.string.email),
                        iconRes = R.drawable.profile,
                        iconColor = MaterialTheme.colorScheme.secondary,
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next,
                        onImeAction = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) }
                    )

                    Spacer(Modifier.height(25.dp))

                    //Password
                    DrawableIconTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            viewModel.resetState()
                        },
                        placeholder = stringResource(R.string.password),
                        iconRes = R.drawable.password,
                        iconColor = MaterialTheme.colorScheme.secondary,
                        keyboardType = KeyboardType.Password,
                        isPassword = true,
                        imeAction = ImeAction.Next,
                        onImeAction = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) }
                    )

                    Spacer(Modifier.height(25.dp))

                    //Confirm Password
                    DrawableIconTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            viewModel.resetState()
                        },
                        placeholder = stringResource(R.string.confirm_password),
                        iconRes = R.drawable.password,
                        iconColor = MaterialTheme.colorScheme.secondary,
                        keyboardType = KeyboardType.Password,
                        isPassword = true,
                        imeAction = ImeAction.Done,
                        onImeAction = { viewModel.register(email, password, confirmPassword) }
                    )

                    Spacer(Modifier.height(25.dp))

                    // Handle UI state changes
                    when (val state = registerUiState) {
                        is RegisterUiState.Idle -> {
                            Spacer(Modifier.height(30.dp))
                        }
                        is RegisterUiState.Error -> {
                            Text(
                                text = state.message,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }
                        is RegisterUiState.Loading -> {
                            CircularProgressIndicator(modifier = Modifier.padding(bottom = 16.dp))
                        }
                        is RegisterUiState.Success -> {
                            LaunchedEffect(Unit) {
                                // Navigate to OTP screen on successful registration
                                navController.navigate(Screen.OtpVerify.createRoute(email))
                            }
                        }
                    }


                    //Register Button
                    ActionButtonPrimary(
                        text = stringResource(R.string.register).uppercase(),
                        onClick = {
                            if (registerUiState !is RegisterUiState.Loading) {
                                viewModel.register(email, password, confirmPassword)
                            }
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        )
    }
}
