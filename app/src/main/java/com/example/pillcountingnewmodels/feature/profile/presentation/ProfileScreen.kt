package com.example.pillcountingnewmodels.feature.profile.presentation

import Screen
import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.pillcountingnewmodels.R
import com.example.pillcountingnewmodels.core.utils.compose.ActionButtonPrimary
import com.example.pillcountingnewmodels.core.utils.compose.BackButton
import com.example.pillcountingnewmodels.core.utils.compose.CommonDialog
import com.example.pillcountingnewmodels.core.utils.compose.FloatingLabelTextField
import com.example.pillcountingnewmodels.core.utils.compose.HollowButton
import com.example.pillcountingnewmodels.feature.profile.domain.model.ProfileDeleteUiState
import com.example.pillcountingnewmodels.feature.profile.domain.model.ProfileField
import com.example.pillcountingnewmodels.feature.profile.domain.model.ProfileUpdateUiState
import com.example.pillcountingnewmodels.feature.profile.presentation.viewmodel.ProfileViewModel
import com.example.pillcountingnewmodels.navigation.AUTH_GRAPH_ROUTE
import com.example.pillcountingnewmodels.ui.theme.AppTheme

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    fromRoute: String? = navController.previousBackStackEntry?.destination?.route
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val focusManager = LocalFocusManager.current

    // Observe states
    val updateUiState by viewModel.updateUiState.collectAsState()
    val deleteUiState by viewModel.deleteUiState.collectAsState()

    // Local state for delete confirmation
    var showDeleteDialog by remember { mutableStateOf(false) }

    BackHandler { /* consume back press to prevent navigation */ }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.extendedColors.primaryBackground)
            .pointerInput(Unit) { detectTapGestures { focusManager.clearFocus() } }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Top bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                BackButton(navController)
                Text(
                    text = stringResource(R.string.profile_title),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppTheme.extendedColors.textColor,
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            // -------------------- INPUT FIELDS --------------------
            ResponsiveProfileFields(
                isLandscape = isLandscape,
                viewModel = viewModel
            )

            Spacer(Modifier.height(10.dp))

            // Checkbox
            if (fromRoute?.contains(Screen.Dashboard.route, ignoreCase = true) == true) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Checkbox(
                        checked = viewModel.doNotAskAgain,
                        onCheckedChange = { checked -> viewModel.toggleDoNotAskAgain(checked) }
                    )
                    Text(
                        stringResource(R.string.do_not_ask),
                        color = AppTheme.extendedColors.textColor
                    )
                }

                Spacer(Modifier.height(20.dp))
            }

            // -------------------- STATE FEEDBACK --------------------
            when (updateUiState) {
                is ProfileUpdateUiState.Loading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 16.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                }

                is ProfileUpdateUiState.Success -> {
                    Text(
                        text = "Profile updated successfully!",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                is ProfileUpdateUiState.Error -> {
                    Text(
                        text = (updateUiState as ProfileUpdateUiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                else -> {}
            }

            when (deleteUiState) {
                is ProfileDeleteUiState.Loading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 16.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                }

                is ProfileDeleteUiState.Success -> {
                    LaunchedEffect(Unit) {
                        navController.navigate(AUTH_GRAPH_ROUTE) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }

                is ProfileDeleteUiState.Error -> {
                    Text(
                        text = (deleteUiState as ProfileDeleteUiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                else -> {}
            }

            Spacer(Modifier.weight(1f))

            // -------------------- BUTTONS --------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
            ) {
                HollowButton(
                    text = stringResource(R.string.delete),
                    onClick = { showDeleteDialog = true },
                    color = MaterialTheme.colorScheme.primary,
                    modifier = if (isLandscape) Modifier else Modifier.weight(1f)
                )
                HollowButton(
                    text = stringResource(R.string.skip_alt),
                    onClick = { onBackClick() },
                    color = MaterialTheme.colorScheme.primary,
                    modifier = if (isLandscape) Modifier else Modifier.weight(1f)
                )
                ActionButtonPrimary(
                    text = stringResource(R.string.save),
                    onClick = { viewModel.updateProfile() },
                    modifier = if (isLandscape) Modifier else Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(20.dp))
        }
    }

    // -------------------- DELETE CONFIRMATION DIALOG --------------------
    if (showDeleteDialog) {
        CommonDialog(
            title = stringResource(R.string.confirm_delete_title),
            message = stringResource(R.string.confirm_delete_profile),
            confirmText = stringResource(R.string.delete),
            cancelText = stringResource(R.string.cancel),
            onConfirm = {
                showDeleteDialog = false
                viewModel.deleteProfile()
            },
            onCancel = { showDeleteDialog = false }
        )
    }
}

/**
 * Reusable profile field with validation error shown under it.
 */
@Composable
private fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    error: String?,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        FloatingLabelTextField(
            value = value,
            onValueChange = { if (!readOnly) onValueChange(it) }, // disable editing
            label = label,
            keyboardType = keyboardType,
            imeAction = imeAction,
            modifier = Modifier.fillMaxWidth(),
            enabled = !readOnly
        )
        if (!error.isNullOrEmpty()) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
        }
    }
}


@Composable
private fun ResponsiveProfileFields(
    isLandscape: Boolean,
    viewModel: ProfileViewModel
) {
    val fields = listOf(
        ProfileField(
            viewModel.firstName,
            { v -> viewModel.firstName = v },
            R.string.first_name,
            viewModel.firstNameError
        ),
        ProfileField(
            viewModel.lastName,
            { v -> viewModel.lastName = v },
            R.string.last_name,
            viewModel.lastNameError
        ),
        ProfileField(
            viewModel.pharmacyName,
            { v -> viewModel.pharmacyName = v },
            R.string.pharmacy_name,
            viewModel.pharmacyNameError
        ),
        ProfileField(
            viewModel.phoneNumber,
            { v -> viewModel.phoneNumber = v },
            R.string.phone_number,
            viewModel.phoneError
        ),
        ProfileField(
            viewModel.email,
            { _ -> },
            R.string.email,
            viewModel.emailError,
            readOnly = true
        ), // <— Email read-only
        ProfileField(
            viewModel.npi,
            { v -> viewModel.npi = v },
            R.string.npi_number,
            viewModel.npiError
        )
    )

    val errors = listOf(
        viewModel.firstNameError,
        viewModel.lastNameError,
        viewModel.pharmacyNameError,
        viewModel.phoneError,
        viewModel.emailError,
        viewModel.npiError
    )

    if (isLandscape) {
        for (i in fields.indices step 2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val field1 = fields[i]
                ProfileTextField(
                    value = field1.value,
                    onValueChange = field1.onChange,
                    label = stringResource(field1.labelRes),
                    error = field1.error?.let { stringResource(it) },
                    modifier = Modifier.weight(1f),
                    readOnly = field1.readOnly
                )

                if (i + 1 < fields.size) {
                    val field2 = fields[i + 1]
                    ProfileTextField(
                        value = field2.value,
                        onValueChange = field2.onChange,
                        label = stringResource(field2.labelRes),
                        error = field2.error?.let { stringResource(it) },
                        modifier = Modifier.weight(1f),
                        readOnly = field2.readOnly
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
        Spacer(Modifier.height(20.dp))
    } else {
        fields.forEach { field ->
            ProfileTextField(
                value = field.value,
                onValueChange = field.onChange,
                label = stringResource(field.labelRes),
                error = field.error?.let { stringResource(it) },
                readOnly = field.readOnly
            )
        }
    }

}

