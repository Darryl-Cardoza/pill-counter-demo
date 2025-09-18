package com.example.pillcountingnewmodels.core.utils.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pillcountingnewmodels.ui.theme.AppTheme

/**
 * A reusable common dialog component for confirmation or alert purposes.
 *
 * This dialog displays an optional [title], a [message], and two action buttons:
 * - Confirm button (primary action)
 * - Cancel button (secondary action)
 *
 * @param message The main text displayed inside the dialog body.
 * @param confirmText The label for the confirm (primary) action button.
 * @param cancelText The label for the cancel (secondary) action button. Defaults to "Cancel".
 * @param onConfirm Callback triggered when confirm button is pressed.
 * @param onCancel Callback triggered when cancel button is pressed or dialog is dismissed.
 * @param title Optional dialog title. If null, only the message will be shown.
 * @param shape The shape of the dialog background. Defaults to a rounded rectangle with 12.dp corner radius.
 */
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Optional title
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

                // Message
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
                verticalAlignment = Alignment.CenterVertically,
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
                    useContentPadding = false,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    )
}
