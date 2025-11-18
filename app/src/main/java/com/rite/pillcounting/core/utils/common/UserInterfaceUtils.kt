package com.rite.pillcounting.core.utils.common

import Screen
import android.content.Context
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.navigation.NavController
import com.rite.pillcounting.R
import com.rite.pillcounting.core.utils.constants.Dimens.buttonCornerRadius
import com.rite.pillcounting.core.utils.constants.Dimens.buttonHeight
import com.rite.pillcounting.core.utils.constants.Dimens.extraSmall
import com.rite.pillcounting.core.utils.constants.Dimens.small
import com.rite.pillcounting.ui.theme.AppTheme
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

/**
 * **UserInterfaceUtils**
 *
 * Provides reusable UI helper utilities and lightweight Composables for:
 * - Common dialogs
 * - Toasts
 * - Text fields
 * - App branding info blocks
 * - Date/time formatting utilities
 *
 * Centralizes shared UI logic to maintain consistent look and behavior across screens.
 */
object UserInterfaceUtils {

    // ───────────────────────────── Toast Helpers ─────────────────────────────

    /** Displays a short Toast with plain text. */
    fun showToast(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(context, message, duration).show()
    }

    /** Displays a short Toast using a string resource ID. */
    fun showToast(context: Context, @StringRes resId: Int, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(context, context.getString(resId), duration).show()
    }

    // ───────────────────────────── Color Extensions ─────────────────────────────

    /** Converts a hex color string (e.g. `#FF5733`) to a Compose [Color]. */
    fun String.toColor(): Color = Color(this.toColorInt())

    // ───────────────────────────── Date/Time Formatting ─────────────────────────────

    /** Formats a nullable epoch millis timestamp into `dd-MM-yyyy hh:mm a` format. */
    fun Long?.toFormattedDate(): String {
        return if (this != null && this > 0) {
            try {
                val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a")
                    .withLocale(Locale.getDefault())
                    .withZone(ZoneId.systemDefault())
                formatter.format(Instant.ofEpochMilli(this))
            } catch (e: Exception) {
                "-"
            }
        } else "-"
    }

    /** Converts a timestamp to a formatted date string. Default: `"dd MMM yyyy"`. */
    fun Long.toDateString(pattern: String = "dd MMM yyyy"): String {
        val formatter = SimpleDateFormat(pattern, Locale.getDefault())
        return formatter.format(Date(this))
    }

    /** Converts a timestamp to a formatted time string. Default: `"hh:mm a"`. */
    fun Long.toTimeString(pattern: String = "hh:mm a"): String {
        val formatter = SimpleDateFormat(pattern, Locale.getDefault())
        return formatter.format(Date(this))
    }

    // ───────────────────────────── Security Dialog ─────────────────────────────

    /**
     * Displays a blocking dialog listing detected runtime security violations.
     * Calls [HelperFunctions.exitApp] on confirm or cancel.
     */
    @Composable
    fun SecurityErrorDialog(violations: List<String>) {
        val title = stringResource(R.string.security_alert_title)
        val confirmText = stringResource(R.string.exit_app)
        val cancelText = stringResource(R.string.close_app)

        val message = buildString {
            append(stringResource(R.string.security_violation_intro))
            append("\n\n")
            violations.forEach { append("• $it\n") }
        }

        CommonDialog(
            title = title,
            message = message,
            confirmText = confirmText,
            cancelText = cancelText,
            onConfirm = { HelperFunctions.exitApp() },
            onCancel = { HelperFunctions.exitApp() }
        )
    }

    // ───────────────────────────── Static Info Blocks ─────────────────────────────

    /** Displays app logo, title, and version in a vertically centered column. */
    @Composable
    fun AppInfo() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                Icon(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = stringResource(R.string.app_name),
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(responsiveDp(100.dp))
                )

                Spacer(modifier = Modifier.height(responsiveDp(20.dp)))

                Text(
                    text = stringResource(R.string.pill_count_app_title),
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.weight(1f))


            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.rite_title),
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.extendedColors.textColor
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "${stringResource(R.string.version)} ${stringResource(R.string.app_version_name)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.extendedColors.textColor
                )
            }
        }
    }


    // ───────────────────────────── Text Fields ─────────────────────────────

    /** A simple text input with rounded corners and adaptive background. */
    @Composable
    fun AppTextField(
        value: String,
        onValueChange: (String) -> Unit,
        modifier: Modifier = Modifier,
        cornerRadius: Dp = 8.dp,
        height: Dp = 56.dp,
        cursorColor: Color = AppTheme.extendedColors.textColor,
        keyboardType: KeyboardType = KeyboardType.Text,
        imeAction: ImeAction = ImeAction.Done
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(height)
                .background(
                    AppTheme.extendedColors.inputBackground,
                    RoundedCornerShape(cornerRadius)
                )
                .padding(horizontal = 15.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                cursorBrush = SolidColor(cursorColor),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    keyboardType = keyboardType,
                    imeAction = imeAction
                ),
                textStyle = LocalTextStyle.current.copy(color = AppTheme.extendedColors.textColor)
            )
        }
    }

    /**
     * Text field with an embedded icon, optional password toggle, and trailing action.
     */
    @Composable
    fun DrawableIconTextField(
        value: String,
        onValueChange: (String) -> Unit,
        placeholder: String,
        @DrawableRes iconRes: Int,
        iconColor: Color = MaterialTheme.colorScheme.primary,
        modifier: Modifier = Modifier,
        cornerRadius: Dp = 8.dp,
        height: Dp = 56.dp,
        cursorColor: Color = AppTheme.extendedColors.textColor,
        isPassword: Boolean = false,
        keyboardType: KeyboardType = KeyboardType.Text,
        imeAction: ImeAction = ImeAction.Done,
        trailingIcon: (@Composable (() -> Unit))? = null
    ) {
        var passwordVisible by remember { mutableStateOf(!isPassword) }

        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(responsiveDp(height))
                .background(
                    AppTheme.extendedColors.inputBackground,
                    RoundedCornerShape(cornerRadius)
                )
                .padding(horizontal = responsiveDp(15.dp)),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(responsiveDp(30.dp))
                )

                Spacer(modifier = Modifier.width(15.dp))
                Box(
                    modifier = Modifier
                        .width(0.5.dp)
                        .fillMaxHeight()
                        .background(Color.Gray)
                )
                Spacer(modifier = Modifier.width(15.dp))

                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.weight(1f),
                    cursorBrush = SolidColor(cursorColor),
                    singleLine = true,
                    visualTransformation = if (isPassword && !passwordVisible)
                        PasswordVisualTransformation() else VisualTransformation.None,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = keyboardType,
                        imeAction = imeAction
                    ),
                    textStyle = LocalTextStyle.current.copy(color = AppTheme.extendedColors.textColor),
                    decorationBox = { inner ->
                        Box(
                            modifier = Modifier.fillMaxHeight(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (value.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    color = AppTheme.extendedColors.textColor.copy(alpha = 0.6f)
                                )
                            }
                            inner()
                        }
                    }
                )

                if (isPassword) {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible)
                                Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                trailingIcon?.invoke()
            }
        }
    }

    // ───────────────────────────── Buttons & Dialogs ─────────────────────────────

    /** Displays a simple back button that navigates up or runs a custom action. */
    @Composable
    fun BackButton(
        navController: NavController,
        modifier: Modifier = Modifier,
        backIcon: Int = R.drawable.back,
        showBox: Boolean = false,
        onClick: (() -> Unit)? = null
    ) {

        val clickAction = { onClick?.invoke() ?: navController.popBackStack() }

        if (showBox) {
            // ---- Circle background version ----
            Box(
                modifier = modifier
                    .padding(small)
                    .size(responsiveDp(40.dp))
                    .background(Color.White, CircleShape)
                    .clickable { clickAction() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = backIcon),
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(responsiveDp(25.dp))
                )
            }
        } else {
            // ---- Normal version without background ----
            IconButton(
                onClick = { onClick?.invoke() ?: navController.popBackStack() },
                modifier = modifier
                    .padding(small)
                    .size(responsiveDp(40.dp))
            ) {
                Icon(
                    painter = painterResource(id = backIcon),
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(extraSmall)
                )
            }
        }
    }


    /** Displays a confirm/cancel dialog with customizable buttons and title. */
    @Composable
    fun CommonDialog(
        message: String,
        confirmText: String,
        cancelText: String,
        onConfirm: () -> Unit,
        onCancel: () -> Unit,
        title: String? = null,
        shape: RoundedCornerShape = RoundedCornerShape(12.dp)
    ) {
        AlertDialog(
            onDismissRequest = {},
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    title?.let {
                        Text(
                            text = it,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.extendedColors.textColor,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        )
                    }
                    Text(
                        text = message,
                        fontSize = 16.sp,
                        color = AppTheme.extendedColors.textColor,
                        textAlign = TextAlign.Center
                    )
                }
            },
            shape = shape,
            containerColor = AppTheme.extendedColors.primaryBackground,
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HollowButton(
                        text = cancelText.uppercase(),
                        onClick = onCancel,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    ActionButtonPrimary(
                        text = confirmText.uppercase(),
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        )
    }

    /** Displays a single-selection dialog with radio buttons and confirm/cancel actions. */
    @Composable
    fun CommonSingleSelectDialog(
        title: String,
        options: List<String>,
        selectedIndex: Int? = null,
        onCancel: () -> Unit,
        onOk: (Int) -> Unit
    ) {
        var currentSelection by remember { mutableStateOf(selectedIndex) }

        AlertDialog(
            onDismissRequest = {},
            shape = RoundedCornerShape(12.dp),
            containerColor = AppTheme.extendedColors.primaryBackground,
            text = {
                Column {
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.extendedColors.textColor,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    options.forEachIndexed { index, option ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { currentSelection = index }
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = currentSelection == index,
                                onClick = { currentSelection = index },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = MaterialTheme.colorScheme.primary
                                )
                            )
                            Spacer(modifier = Modifier.width(small))
                            Text(
                                text = option,
                                fontSize = 16.sp,
                                color = AppTheme.extendedColors.textColor
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HollowButton(
                        text = stringResource(R.string.cancel).uppercase(),
                        onClick = onCancel,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    ActionButtonPrimary(
                        text = stringResource(R.string.ok).uppercase(),
                        onClick = { currentSelection?.let { onOk(it) } },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        )
    }

    /** A reusable filled button with customizable color, height, and rounded corners. */
    @Composable
    fun FilledButton(
        text: String,
        onClick: () -> Unit,
        color: Color,
        modifier: Modifier = Modifier,
        buttonHeightDefault: Dp = buttonHeight,
    ) {
        Button(
            onClick = onClick,
            modifier = modifier
                .height(buttonHeightDefault)
                .widthIn(min = 100.dp)
                .border(
                    width = 1.dp,
                    color = color,
                    shape = RoundedCornerShape(buttonCornerRadius)
                ),
            colors = ButtonDefaults.buttonColors(
                containerColor = color,
                contentColor = Color.White,
            ),
        ) {
            Text(
                text = text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 13.sp
            )
        }
    }

    /**
     * A custom text input composable with a floating label, optional password visibility toggle,
     * and support for IME actions (Next / Done).
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
        onImeAction: (() -> Unit)? = null,
        enabled: Boolean = true
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
                    enabled = enabled,
                    readOnly = !enabled,
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
                        onNext = { focusManager.moveFocus(FocusDirection.Next) },
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

    /** A reusable hollow (outlined) button with transparent background and customizable color. */
    @Composable
    fun HollowButton(
        text: String,
        onClick: () -> Unit,
        color: Color,
        modifier: Modifier = Modifier,
        buttonHeightDefault: Dp = buttonHeight,
    ) {
        Button(
            onClick = onClick,
            modifier = modifier
                .height(buttonHeightDefault)
                .widthIn(min = 100.dp)
                .border(
                    width = 1.dp,
                    color = color,
                    shape = RoundedCornerShape(buttonCornerRadius)
                ),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = color,
            ),
        ) {
            Text(
                text = text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 13.sp
            )
        }
    }

    /**
     * A simple composable that displays a centered circular progress indicator
     * with a semi-transparent background, suitable for overlaying content.
     */
    @Composable
    fun LoadingIndicator() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
        }
    }

    /** A primary action button with customizable color, width, and enabled state. */
    @Composable
    fun ActionButtonPrimary(
        text: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        color: Color = MaterialTheme.colorScheme.primary,
        enabled: Boolean = true,
        width: Int = 100
    ) {
        Button(
            onClick = onClick,
            modifier = modifier
                .height(buttonHeight)
                .widthIn(min = width.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = color,
                contentColor = Color.White,
                disabledContainerColor = AppTheme.extendedColors.secondaryBackground, // or use a theme color
                disabledContentColor = Color.LightGray // or use a theme color
            ),
            shape = RoundedCornerShape(buttonCornerRadius),
            enabled = enabled
        ) {
            Text(text)
        }
    }

    /** A navigation icon button that opens the app’s main menu screen. */
    @Composable
    fun MenuButton(
        navController: NavController,
        modifier: Modifier = Modifier,
        backIcon: Int = R.drawable.menu,
    ) {
        IconButton(
            onClick = {
                navController.navigate(Screen.Menu.route)
            },
            modifier = modifier
                .padding(small)
                .size(responsiveDp(40.dp))
        ) {
            Icon(
                painter = painterResource(id = backIcon),
                contentDescription = "Menu",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(extraSmall)
            )
        }
    }

    /** A customizable OTP input field with multiple boxes, auto-focus, and optional password masking. */
    @Composable
    fun OTPTextField(
        otp: String,
        onOtpChange: (String) -> Unit,
        boxCount: Int = 4,
        boxSize: Dp = 56.dp,
        cornerRadius: Dp = 8.dp,
        boxBackground: Color = AppTheme.extendedColors.inputBackground,
        textColor: Color = AppTheme.extendedColors.textColor,
        isPassword: Boolean = false
    ) {
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
                        .size(responsiveDp(boxSize))
                        .background(
                            boxBackground,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(
                                cornerRadius
                            )
                        )
                        .focusRequester(focusRequesters[i]),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = if (i == boxCount - 1) ImeAction.Done else ImeAction.Next
                    ),
                    textStyle = TextStyle(
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

    @Composable
    fun responsiveDp(baseDp: Dp): Dp {
        val configuration = LocalConfiguration.current
        val smallestWidthDp = minOf(configuration.screenWidthDp, configuration.screenHeightDp)
        val scale = when {
            smallestWidthDp < 600 -> 1f    //phone
            smallestWidthDp < 840 -> 1.5f  //small tablets
            else -> 2f           //large tablets
        }
        return baseDp * scale
    }

    @Composable
    fun responsiveSp(baseSp: TextUnit): TextUnit {
        val configuration = LocalConfiguration.current
        val smallestWidthDp = minOf(configuration.screenWidthDp, configuration.screenHeightDp)
        val scale = when {
            smallestWidthDp < 600 -> 1f   //phone
            smallestWidthDp < 840 -> 1.5f //small tablets
            else -> 2f          //large tablets
        }
        return (baseSp.value * scale).sp
    }

}
