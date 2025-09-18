package com.example.pillcountingnewmodels.feature.profile.presentation

import android.content.res.Configuration
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.pillcountingnewmodels.core.utils.compose.FloatingLabelTextField
import com.example.pillcountingnewmodels.core.utils.compose.HollowButton
import com.example.pillcountingnewmodels.feature.profile.domain.model.ProfileDeleteUiState
import com.example.pillcountingnewmodels.feature.profile.domain.model.ProfileField
import com.example.pillcountingnewmodels.feature.profile.domain.model.ProfileUpdateUiState
import com.example.pillcountingnewmodels.feature.profile.presentation.viewmodel.ProfileViewModel
import com.example.pillcountingnewmodels.ui.theme.AppTheme

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val focusManager = LocalFocusManager.current

    // Observe states
    val updateUiState by viewModel.updateUiState.collectAsState()
    val deleteUiState by viewModel.deleteUiState.collectAsState()

    // Fields
    val fields = listOf(
        ProfileField(
            placeholder = stringResource(id = R.string.first_name),
            value = viewModel.firstName
        ),
        ProfileField(
            placeholder = stringResource(id = R.string.last_name),
            value = viewModel.lastName
        ),
        ProfileField(
            placeholder = stringResource(id = R.string.pharmacy_name),
            value = viewModel.pharmacyName
        ),
        ProfileField(
            placeholder = stringResource(id = R.string.phone_number),
            value = viewModel.phoneNumber,
            keyboardType = KeyboardType.Phone
        ),
        ProfileField(
            placeholder = stringResource(id = R.string.email),
            value = viewModel.email,
            keyboardType = KeyboardType.Email
        ),
        ProfileField(
            placeholder = stringResource(id = R.string.npi_number),
            value = viewModel.npi
        )
    )

    val fieldSetters = listOf<(String) -> Unit>(
        { viewModel.firstName = it },
        { viewModel.lastName = it },
        { viewModel.pharmacyName = it },
        { viewModel.phoneNumber = it },
        { viewModel.email = it },
        { viewModel.npi = it },
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.extendedColors.primaryBackground)
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
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

            // Fields
            if (isLandscape) {
                fields.chunked(2).forEachIndexed { rowIndex, pair ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        pair.forEachIndexed { colIndex, field ->
                            val index = rowIndex * 2 + colIndex
                            val isLast = index == fields.lastIndex
                            FloatingLabelTextField(
                                value = field.value,
                                onValueChange = fieldSetters[index],
                                label = field.placeholder,
                                keyboardType = field.keyboardType,
                                imeAction = if (isLast) ImeAction.Done else field.imeAction,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 4.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            } else {
                fields.forEachIndexed { index, field ->
                    val isLast = index == fields.lastIndex
                    FloatingLabelTextField(
                        value = field.value,
                        onValueChange = fieldSetters[index],
                        label = field.placeholder,
                        keyboardType = field.keyboardType,
                        imeAction = if (isLast) ImeAction.Done else field.imeAction,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }

            Spacer(Modifier.height(10.dp))

            // Checkbox
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Checkbox(
                    checked = viewModel.doNotAskAgain,
                    onCheckedChange = { viewModel.doNotAskAgain = it }
                )
                Text(
                    stringResource(R.string.do_not_ask),
                    color = AppTheme.extendedColors.textColor
                )
            }

            Spacer(Modifier.height(20.dp))

            // State feedback

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (updateUiState) {
                    is ProfileUpdateUiState.Idle -> {}
                    is ProfileUpdateUiState.Loading -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    is ProfileUpdateUiState.Success -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Profile updated successfully!",
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    is ProfileUpdateUiState.Error -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = (updateUiState as ProfileUpdateUiState.Error).message,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }


            // Delete state feedback
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (deleteUiState) {
                    is ProfileDeleteUiState.Idle -> {}
                    is ProfileDeleteUiState.Loading -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator()
                            Spacer(Modifier.height(20.dp))

                        }
                    }

                    is ProfileDeleteUiState.Success -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Profile deleted successfully!",
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(10.dp))

                        }
                    }

                    is ProfileDeleteUiState.Error -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = (deleteUiState as ProfileDeleteUiState.Error).message,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }


            Spacer(Modifier.height(20.dp))
            Spacer(modifier = Modifier.weight(1f))
            // Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
            ) {
                HollowButton(
                    text = stringResource(R.string.delete),
                    onClick = { viewModel.deleteProfile() },
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
                    useContentPadding = isLandscape,
                    modifier = if (isLandscape) Modifier else Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(20.dp))

        }
    }
}
