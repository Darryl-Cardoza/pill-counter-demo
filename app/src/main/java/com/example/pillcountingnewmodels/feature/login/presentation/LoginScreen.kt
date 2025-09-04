package com.example.pillcountingnewmodels.feature.login.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.pillcountingnewmodels.R
import com.example.pillcountingnewmodels.core.utils.compose.AppInfo
import com.example.pillcountingnewmodels.core.utils.compose.DrawableIconTextField
import com.example.pillcountingnewmodels.core.utils.compose.ActionButtonPrimary
import com.example.pillcountingnewmodels.core.utils.compose.SplitResponsive
import com.example.pillcountingnewmodels.feature.login.viewmodel.LoginViewModel
import com.example.pillcountingnewmodels.navigation.Routes
import com.example.pillcountingnewmodels.ui.theme.AppTheme


@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }

    val loginState = viewModel.loginState

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .background(AppTheme.extendedColors.secondaryBackground)
    ) {
        SplitResponsive(
            topOrLeft = {
                AppInfo(context)
            },
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
                        verticalArrangement = Arrangement.Center
                    ) {

                        //Email
                        DrawableIconTextField(
                            value = email,
                            onValueChange = { email = it },
                            placeholder = context.getString(R.string.email),
                            iconRes = R.drawable.profile,
                            iconColor = MaterialTheme.colorScheme.secondary,
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next,
                            onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
                        )

                        Spacer(Modifier.height(25.dp))

                        //Password
                        DrawableIconTextField(
                            value = password,
                            onValueChange = { password = it },
                            placeholder = context.getString(R.string.password),
                            iconRes = R.drawable.password,
                            iconColor = MaterialTheme.colorScheme.secondary,
                            keyboardType = KeyboardType.Password,
                            isPassword = true,
                            imeAction = ImeAction.Done
                        )

                        Spacer(Modifier.height(25.dp))

                        //Checkbox
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp) // same size as Checkbox
                                    .border(
                                        width = 0.5.dp,
                                        color = AppTheme.extendedColors.textColor, // border color
                                        shape = RoundedCornerShape(3.dp)
                                    )
                                    .background(
                                        Color.White,
                                        shape = RoundedCornerShape(3.dp)
                                    )// white background
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
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = context.getString(R.string.remember_me),
                                color = AppTheme.extendedColors.textColor,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }

                        Spacer(Modifier.height(20.dp))

                        //Login Button
                        ActionButtonPrimary(
                            text = "LOGIN",
                            onClick = { viewModel.login(email, password) },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )

                        if (loginState == "SUCCESS") {
                            LaunchedEffect(Unit) {
                                navController.navigate(Routes.DASHBOARD) {
                                    popUpTo(Routes.DASHBOARD) { inclusive = true }
                                }
                            }
                        }
                        if (loginState == "FAILED") {
                            Text("Invalid credentials", color = Color.Red)
                        }
                    }

                    //Bottom texts Register and Forgot Password
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .fillMaxWidth(),
                    ) {
                        Text(
                            text = context.getString(R.string.register),
                            color = AppTheme.extendedColors.textColor,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.clickable {
                                navController.navigate(Routes.REGISTER)
                            }
                        )
                        Text(
                            text = context.getString(R.string.forgot_password),
                            color = AppTheme.extendedColors.textColor,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.clickable {
                                navController.navigate(Routes.FORGOT_PASSWORD)
                            }
                        )
                    }

                    Spacer(Modifier.height(20.dp))
                }
            }
        )
    }
}


