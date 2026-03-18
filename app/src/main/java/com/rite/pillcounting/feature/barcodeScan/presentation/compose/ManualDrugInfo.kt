package com.rite.pillcounting.feature.barcodeScan.presentation.compose

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rite.pillcounting.R
import com.rite.pillcounting.core.utils.common.NdcVisualTransformation
import com.rite.pillcounting.core.utils.common.UserInterfaceUtils.ActionButtonPrimary
import com.rite.pillcounting.core.utils.common.UserInterfaceUtils.AppTextField
import com.rite.pillcounting.core.utils.common.UserInterfaceUtils.HollowButton
import com.rite.pillcounting.core.utils.common.UserInterfaceUtils.responsiveDp
import com.rite.pillcounting.feature.barcodeScan.presentation.viewmodel.ScanBarcodeViewModel
import com.rite.pillcounting.ui.theme.AppTheme

/**
 * Manual entry panel used in Split UI:
 * - Portrait: compact row layout (label left, field right)
 * - Landscape: "sheet" style (labels on top, bigger fields, rounded left corners)
 *
 * NOTE: Do NOT pass NavController inside UI composables.
 * Call navController.popBackStack() from parent and pass as onDismiss.
 */
@Composable
fun ManualDrugInfo(
    viewModel: ScanBarcodeViewModel,
    onConfirm: (drugName: String, ndc: String) -> Unit,
    onDismiss: () -> Unit
) {
    // Local form state (kept inside this composable)

    var errorMessage by remember { mutableStateOf<Int?>(0) }

    // Detect orientation
    val isLandscape =
        LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    /**
     * Single validation handler used by both portrait and landscape.
     * - Shows error if any field is empty
     * - Calls onConfirm with trimmed values
     */
    fun handleOk() {
        if (viewModel.drugName.isBlank() || viewModel.ndc.isBlank()) {
            errorMessage = R.string.all_fields_are_required
        } else {
            onConfirm( viewModel.drugName.trim(),  viewModel.ndc.trim())
        }
    }

    /**
     * Shared "error UI":
     * - Only consumes vertical space when error exists (as you requested)
     * - Keeps spacing consistent by adding a fallback spacer
     */
    @Composable
    fun ErrorOrSpacer() {
        if (errorMessage != 0) {
            Text(
                text = stringResource(errorMessage!!),
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 6.dp)
            )
            Spacer(Modifier.height(12.dp))
        } else {
            Spacer(Modifier.height(18.dp))
        }
    }

    // Outer container: portrait uses normal background, landscape uses transparent parent
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isLandscape) Color.Transparent else AppTheme.extendedColors.primaryBackground)
    ) {
        if (isLandscape) {
            // Landscape: right-side sheet look (rounded left corners)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(
                        RoundedCornerShape(
                            topStart = 28.dp,
                            bottomStart = 28.dp,
                            topEnd = 0.dp,
                            bottomEnd = 0.dp
                        )
                    )
                    .background(AppTheme.extendedColors.primaryBackground)
                    .padding(horizontal = 22.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.Start
            ) {
                // Push content to center vertically (equal spacing top and bottom)
                Spacer(modifier = Modifier.weight(1f))

                // --- Field 1: NDC (label on top) ---
                Text(
                    text = stringResource(R.string.ndc_number),
                    color = AppTheme.extendedColors.textColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal
                )
                Spacer(Modifier.height(10.dp))
                AppTextField(
                    value = viewModel.ndc,
                    onValueChange = {
                        viewModel.ndc = it.filter(Char::isDigit).take(11)
                        errorMessage = 0
                    },
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next,
                    visualTransformation = NdcVisualTransformation(),
                    modifier = Modifier
                        .weight(0.78f)
                        .height(responsiveDp(45.dp))
                )

                Spacer(Modifier.height(20.dp))

                // --- Field 2: Drug Name (label on top) ---
                Text(
                    text = stringResource(R.string.drug_name),
                    color = AppTheme.extendedColors.textColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal
                )
                Spacer(Modifier.height(10.dp))
                AppTextField(
                    value =  viewModel.drugName,
                    onValueChange = {  viewModel.drugName = it; errorMessage = 0 },
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                )

                Spacer(Modifier.height(14.dp))

                // Error area (only shows when needed)
                ErrorOrSpacer()

                // Buttons (large pill)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HollowButton(
                        text = "CANCEL",
                        onClick = onDismiss,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clip(RoundedCornerShape(50))
                    )

                    ActionButtonPrimary(
                        text = "OK",
                        onClick = ::handleOk,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clip(RoundedCornerShape(50))
                    )
                }

                // Push content to center vertically (equal spacing top and bottom)
                Spacer(modifier = Modifier.weight(1f))
            }
        } else {
            // Portrait: compact split section with side-by-side label + field
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 20.dp, end = 20.dp, bottom = 20.dp)
            ) {
                // Center vertically in portrait too (optional)
//                Spacer(modifier = Modifier.weight(0.5f))
                Spacer(modifier = Modifier.weight(0.4f))
                // --- NDC row (label left, field right) ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.ndc_number),
                        color = AppTheme.extendedColors.textColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier.weight(0.42f)
                    )
                    AppTextField(
                        value = viewModel.ndc,
                        onValueChange = {
                            viewModel.ndc = it.filter(Char::isDigit).take(11)
                            errorMessage = 0
                        },
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next,
                        visualTransformation = NdcVisualTransformation(),
                        modifier = Modifier
                            .weight(0.78f)
                            .height(responsiveDp(45.dp))
                    )
                }

                Spacer(modifier = Modifier.weight(0.5f))

                // --- Drug Name row ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.drug_name),
                        color = AppTheme.extendedColors.textColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier.weight(0.42f)
                    )

                    AppTextField(
                        value =  viewModel.drugName,
                        onValueChange = {  viewModel.drugName = it; errorMessage = 0 },
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done,
                        modifier = Modifier
                            .weight(0.78f)
                            .height(responsiveDp(45.dp))
                    )
                }

                Spacer(modifier = Modifier.weight(0.4f))

                // Error area (only shows when needed)
                ErrorOrSpacer()

                // Buttons row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HollowButton(
                        text = stringResource(R.string.manual_drug_info_cancel),
                        onClick = onDismiss, // parent can popBackStack()
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )

                    ActionButtonPrimary(
                        text = stringResource(R.string.manual_drug_info_ok),
                        onClick = ::handleOk,
                        modifier = Modifier.weight(1f)
                    )
                }

            }
        }
    }
}
