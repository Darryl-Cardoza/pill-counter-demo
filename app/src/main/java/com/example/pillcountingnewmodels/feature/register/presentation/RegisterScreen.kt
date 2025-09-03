package com.example.pillcountingnewmodels.feature.register.presentation

import android.util.Patterns
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.pillcountingnewmodels.R
import com.example.pillcountingnewmodels.core.utils.ToastUtils
import com.example.pillcountingnewmodels.core.utils.compose.AppInfo
import com.example.pillcountingnewmodels.core.utils.compose.DrawableIconTextField
import com.example.pillcountingnewmodels.core.utils.compose.ActionButtonPrimary
import com.example.pillcountingnewmodels.core.utils.compose.SplitResponsive
import com.example.pillcountingnewmodels.navigation.Routes
import com.example.pillcountingnewmodels.ui.theme.AppTheme

@Composable
fun RegisterScreen(
    navController: NavController,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    // Focus requesters for each field
    val emailFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }
    val confirmPasswordFocusRequester = remember { FocusRequester() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .background(AppTheme.extendedColors.secondaryBackground)
    ) {
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.align(Alignment.TopStart)
                .padding(15.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.back),
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        SplitResponsive(
            topOrLeft = {
                AppInfo(context)
            },
            bottomOrRight = {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.Center
                ) {

                    //Email
                    DrawableIconTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = "Email",
                        iconRes = R.drawable.profile,
                        iconColor = MaterialTheme.colorScheme.secondary,
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next,
                        onImeAction = { passwordFocusRequester.requestFocus() },
                        modifier = Modifier.focusRequester(emailFocusRequester)
                    )

                    Spacer(Modifier.height(25.dp))

                    //Password
                    DrawableIconTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = "Password",
                        iconRes = R.drawable.password,
                        iconColor = MaterialTheme.colorScheme.secondary,
                        keyboardType = KeyboardType.Password,
                        isPassword = true,
                        imeAction = ImeAction.Next,
                        onImeAction = { confirmPasswordFocusRequester.requestFocus() },
                        modifier = Modifier.focusRequester(passwordFocusRequester)
                    )

                    Spacer(Modifier.height(25.dp))

                    //Confirm Password
                    DrawableIconTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        placeholder = "Confirm Password",
                        iconRes = R.drawable.password,
                        iconColor = MaterialTheme.colorScheme.secondary,
                        keyboardType = KeyboardType.Password,
                        isPassword = true,
                        imeAction = ImeAction.Done,
                        onImeAction = { focusManager.clearFocus() },
                        modifier = Modifier.focusRequester(confirmPasswordFocusRequester)
                    )

                    Spacer(Modifier.height(25.dp))

                    //Login Button
                    ActionButtonPrimary(
                        text = "REGISTER",
                        onClick = {
                            when {
                                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                                    ToastUtils.show(context, "Invalid email")
                                    emailFocusRequester.requestFocus()
                                }
                                password.isEmpty() -> {
                                    ToastUtils.show(context, "Password cannot be empty")
                                    passwordFocusRequester.requestFocus()
                                }
                                confirmPassword.isEmpty() -> {
                                    ToastUtils.show(context, "Confirm Password cannot be empty")
                                    confirmPasswordFocusRequester.requestFocus()
                                }
                                password != confirmPassword -> {
                                    ToastUtils.show(context, "Passwords do not match")
                                    confirmPasswordFocusRequester.requestFocus()
                                }
                                else -> {
                                    navController.navigate("${Routes.OTP_VERIFY}/$email")
                                }
                            }
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        )
    }
}