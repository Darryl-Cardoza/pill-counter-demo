package com.example.pillcountingnewmodels.core.utils.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import com.example.pillcountingnewmodels.ui.theme.AppTheme

@Composable
fun OTPTextField(
    otp: String,
    onOtpChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    boxCount: Int = 4,
    boxSize: Dp = 56.dp,
    cornerRadius: Dp = 8.dp,
    boxBackground: Color = AppTheme.extendedColors.inputBackground,
    textColor: Color = AppTheme.extendedColors.textColor,
    isPassword: Boolean = false
) {
    val focusManager = LocalFocusManager.current
    val focusRequesters = remember { List(boxCount) { FocusRequester() } }

    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until boxCount) {
            val char = otp.getOrNull(i)?.toString() ?: ""
            BasicTextField(
                value = char,
                onValueChange = { value ->
                    if (value.length <= 1 && value.all { it.isDigit() }) {
                        val newOtp = otp.toCharArray().toMutableList()
                        if (i < newOtp.size) {
                            if (value.isEmpty()) {
                                // Clear current box
                                newOtp[i] = ' '
                                onOtpChange(newOtp.joinToString("").trim())

                                // Move focus backward if possible
                                if (i > 0) {
                                    focusRequesters[i - 1].requestFocus()
                                }
                            } else {
                                // Fill current box
                                newOtp[i] = value.first()
                                onOtpChange(newOtp.joinToString("").trim())

                                // Move focus forward
                                if (i < boxCount - 1) {
                                    focusRequesters[i + 1].requestFocus()
                                }
                            }
                        } else if (value.isNotEmpty()) {
                            newOtp.add(value.first())
                            onOtpChange(newOtp.joinToString("").trim())
                            if (i < boxCount - 1) {
                                focusRequesters[i + 1].requestFocus()
                            }
                        }
                    }
                },
                modifier = Modifier
                    .size(boxSize)
                    .background(boxBackground, shape = androidx.compose.foundation.shape.RoundedCornerShape(cornerRadius))
                    .focusRequester(focusRequesters[i]),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = if (i == boxCount - 1) ImeAction.Done else ImeAction.Next),
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = textColor,
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center
                ),
                cursorBrush = SolidColor(AppTheme.extendedColors.textColor),
                visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.Center) {
                        innerTextField()
                    }
                }
            )
        }
    }
}
