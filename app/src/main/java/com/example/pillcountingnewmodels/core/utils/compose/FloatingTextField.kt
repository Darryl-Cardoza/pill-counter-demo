package com.example.pillcountingnewmodels.core.utils.compose

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pillcountingnewmodels.ui.theme.AppTheme

/**
 * A custom text input composable with a floating label, optional password visibility toggle,
 * and support for IME actions (Next / Done).
 *
 * This field animates its label position and scale depending on focus state and
 * whether it contains text, mimicking Material floating label behavior while
 * allowing for theme customization.
 *
 * ### Features
 * - Floating label that animates above the field when focused or non-empty.
 * - Optional password toggle to show/hide input.
 * - Supports IME actions (`Next`, `Done`) with configurable behavior.
 * - Keyboard focus handling: moves focus forward on Next, clears focus (hides keyboard)
 *   on Done by default, or triggers [onImeAction] if provided.
 *
 * @param value Current text value of the input field.
 * @param onValueChange Callback invoked when the text value changes.
 * @param label Label text displayed as floating placeholder.
 * @param modifier Modifier applied to the composable.
 * @param cornerRadius Corner radius for the input background shape.
 * @param height Fixed height of the text field container.
 * @param cursorColor Color of the input cursor.
 * @param isPassword Whether this field is a password field (enables toggle icon).
 * @param keyboardType Keyboard type (e.g., text, number, email, phone).
 * @param imeAction IME action for the keyboard (e.g., Done, Next).
 * @param onImeAction Optional callback invoked when the Done action is pressed.
 * Defaults to clearing focus (hiding keyboard) if not supplied.
 */
@Composable
fun FloatingLabelTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 8.dp,
    height: Dp = 56.dp,
    cursorColor: Color = AppTheme.extendedColors.textColor,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Done,
    onImeAction: (() -> Unit)? = null
) {
    val focusManager = LocalFocusManager.current
    var passwordVisible by remember { mutableStateOf(!isPassword) }
    var isFocused by remember { mutableStateOf(false) }

    val horizontalPadding = 15.dp
    val topPadding = 15.dp

    // Floating label vertical offset
    val labelOffsetY by animateDpAsState(
        targetValue = if (isFocused || value.isNotEmpty()) {
            (-2).dp
        } else {
            // center vertically inside text field
            (height / 2) + 15.dp
        }, label = "labelOffsetY"
    )

    // Floating label scale
    val labelScale by animateFloatAsState(
        targetValue = if (isFocused || value.isNotEmpty()) 0.75f else 1f,
        label = "labelScale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = height + topPadding + 12.dp)
    ) {
        // Text field container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .align(Alignment.BottomCenter)
                .background(
                    AppTheme.extendedColors.inputBackground,
                    RoundedCornerShape(cornerRadius)
                )
                .padding(horizontal = horizontalPadding, vertical = 8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(
                    color = AppTheme.extendedColors.textColor,
                    fontSize = 16.sp
                ),
                visualTransformation = if (isPassword && !passwordVisible) {
                    PasswordVisualTransformation()
                } else {
                    VisualTransformation.None
                },
                cursorBrush = SolidColor(cursorColor),
                keyboardOptions = KeyboardOptions(
                    keyboardType = keyboardType,
                    imeAction = imeAction
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Next) },
                    onDone = {
                        if (onImeAction != null) {
                            onImeAction()
                        } else {
                            focusManager.clearFocus() // closes keyboard
                        }
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { isFocused = it.isFocused }
            )

            if (isPassword) {
                Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.Visibility
                            else Icons.Filled.VisibilityOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Floating label / placeholder
        Text(
            text = label,
            color = if (isFocused) MaterialTheme.colorScheme.primary
            else AppTheme.extendedColors.textColor.copy(alpha = 0.7f),
            fontSize = 16.sp * labelScale,
            modifier = Modifier
                .padding(start = horizontalPadding)
                .align(Alignment.TopStart)
                .offset(y = labelOffsetY)
        )
    }
}
