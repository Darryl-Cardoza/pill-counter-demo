package com.example.pillcountingnewmodels.feature.register.presentation

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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.pillcountingnewmodels.R
import com.example.pillcountingnewmodels.core.utils.compose.AppInfo
import com.example.pillcountingnewmodels.core.utils.compose.DrawableIconTextField
import com.example.pillcountingnewmodels.core.utils.compose.HollowWhiteButton
import com.example.pillcountingnewmodels.core.utils.compose.SplitResponsive

@Composable
fun RegisterScreen(
    navController: NavController,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
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
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next,
                        onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
                    )

                    Spacer(Modifier.height(25.dp))

                    //Password
                    DrawableIconTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = "Password",
                        iconRes = R.drawable.password,
                        keyboardType = KeyboardType.Password,
                        isPassword = true,
                        imeAction = ImeAction.Next
                    )

                    Spacer(Modifier.height(25.dp))

                    //Password
                    DrawableIconTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = "Confirm Password",
                        iconRes = R.drawable.password,
                        keyboardType = KeyboardType.Password,
                        isPassword = true,
                        imeAction = ImeAction.Done
                    )

                    Spacer(Modifier.height(25.dp))

                    //Login Button
                    HollowWhiteButton(
                        text = "REGISTER",
                        onClick = { },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        )
    }
}