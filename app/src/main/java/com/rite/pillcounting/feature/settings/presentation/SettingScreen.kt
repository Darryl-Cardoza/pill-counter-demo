package com.rite.pillcounting.feature.settings.presentation

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.rite.pillcounting.R
import com.rite.pillcounting.core.settings.domain.model.enums.ScheduleCode
import com.rite.pillcounting.core.settings.presentation.viewmodel.MainActivityViewModel
import com.rite.pillcounting.core.utils.common.HistoryRetention
import com.rite.pillcounting.core.utils.common.UserInterfaceUtils.BackButton
import com.rite.pillcounting.core.utils.common.UserInterfaceUtils.CommonDialog
import com.rite.pillcounting.ui.theme.AppTheme
import com.rite.pillcounting.ui.theme.LocalExtendedColors

/**
 * Settings screen for managing app preferences such as:
 * - Default scan behavior
 * - Notes confirmation
 * - History save duration
 */
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: MainActivityViewModel = hiltViewModel()
) {
    // State holders for preferences
    //var isBarcodeScanFirst by remember { mutableStateOf(true) }
    val isAskToAddNotes by viewModel.isAskToAddNotes.collectAsState()

    // Load history options from resources
    val historyOptions = stringArrayResource(R.array.history_options).toList()
    val historyOptionDays = HistoryRetention.optionsDays

    val selectedDays by viewModel.selectedHistoryOption.collectAsState()
    val selectedOption = historyOptions[historyOptionDays.indexOf(selectedDays)]
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var tempSelectedOption by remember { mutableStateOf("") }

    // Detect orientation for layout adjustments
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    val extendedColors = LocalExtendedColors.current
    val isSoundEnabled by viewModel.isSoundOn.collectAsState()
    val isRequireDoubleCount by viewModel.isRequireDoubleCountEnable.collectAsState()
    val isHapticEnable by viewModel.isHapticOn.collectAsState()
    val isRequireBackCountEnable by viewModel.isRequireBackCountEnable.collectAsState()
    var showCLearAllDataConfirmDialog by remember { mutableStateOf(false) }
    val selectedSchedules by viewModel.selectedSchedules.collectAsState()
    val schedules = ScheduleCode.entries
    val isSoundOverrideEnable by viewModel.isSoundOverride.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(extendedColors.secondaryBackground)
    ) {
        // Header with back navigation and title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            BackButton(navController = navController)

            Text(
                text = stringResource(R.string.settings_title),
                fontSize = 16.sp,
                color = extendedColors.textColor
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                .verticalScroll(rememberScrollState())

        ) {

            // Toggle: Ask to add notes
            SettingSwitch(
                labelRes = R.string.setting_ask_to_add_notes,
                checked = isAskToAddNotes,
                onCheckedChange = { newValue ->
                    viewModel.toggleAskToAddNotes(newValue)
                },
                checkedTrackColor = MaterialTheme.colorScheme.secondary
            )

            HorizontalDivider(color = colorResource(R.color.border_gray).copy(alpha = 0.3f))

            SettingSwitch(
                labelRes = R.string.require_double_count,
                checked = isRequireDoubleCount,
                onCheckedChange = { newValue ->
                    viewModel.toggleRequireDoubleCountOnOff(newValue)
                },
                checkedTrackColor = MaterialTheme.colorScheme.secondary
            )

            if (isRequireDoubleCount) {
                Text(
                    text = stringResource(R.string.select_schedule_codes),
                    fontSize = 16.sp,
                    color = extendedColors.textColor,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .padding(horizontal = 16.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    schedules.take(3).forEach { code ->
                        Box(modifier = Modifier.weight(1f)) {
                            ScheduleCheckBox(
                                label = code.name,
                                checked = selectedSchedules.contains(code),
                                onCheckedChange = { viewModel.toggleSchedule(code) }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    schedules.drop(3).forEach { code ->
                        Box(modifier = Modifier.weight(1f)) {
                            ScheduleCheckBox(
                                label = code.name,
                                checked = selectedSchedules.contains(code),
                                onCheckedChange = { viewModel.toggleSchedule(code) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))
                }

                HorizontalDivider(
                    color = colorResource(R.color.border_gray).copy(alpha = 0.3f)
                )
            }
            HorizontalDivider(color = colorResource(R.color.border_gray).copy(alpha = 0.3f))

            SettingSwitch(
                labelRes = R.string.require_back_count,
                checked = isRequireBackCountEnable,
                onCheckedChange = { newValue ->
                    viewModel.toggleRequireBackCountOnOff(newValue)
                },
                checkedTrackColor = MaterialTheme.colorScheme.secondary
            )

            HorizontalDivider(color = colorResource(R.color.border_gray).copy(alpha = 0.3f))

            Spacer(modifier = Modifier.height(16.dp))
            // History save options
            Text(
                text = stringResource(R.string.setting_save_history_for),
                fontSize = 16.sp,
                color = extendedColors.textColor,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .padding(horizontal = 16.dp)
            )

            SettingRadioGroup(
                options = historyOptions,
                selectedOption = selectedOption,
                onOptionSelected = { option ->
                    if (option != tempSelectedOption) {
                        tempSelectedOption = option
                        showConfirmationDialog = true
                    }
                },
                isLandscape = isLandscape
            )
            HorizontalDivider(color = colorResource(R.color.border_gray).copy(alpha = 0.3f))

            SettingSwitch(
                labelRes = R.string.pill_counting_sound,
                checked = isSoundEnabled,
                onCheckedChange = { newValue ->
                    viewModel.toggleSoundOnOff(newValue)
                },
                checkedTrackColor = MaterialTheme.colorScheme.secondary
            )
            HorizontalDivider(color = colorResource(R.color.border_gray).copy(alpha = 0.3f))

            SettingSwitch(
                labelRes = R.string.haptic_feedback,
                checked = isHapticEnable,
                onCheckedChange = { newValue ->
                    viewModel.toggleHapticOnOff(newValue)
                },
                checkedTrackColor = MaterialTheme.colorScheme.secondary
            )

            HorizontalDivider(color = colorResource(R.color.border_gray).copy(alpha = 0.3f))

            SettingSwitch(
                labelRes = R.string.voice_feedback,
                checked = isSoundOverrideEnable,
                onCheckedChange = { newValue ->
                    viewModel.toggleSoundOverride(newValue)
                },
                checkedTrackColor = MaterialTheme.colorScheme.secondary
            )


            HorizontalDivider(color = colorResource(R.color.border_gray).copy(alpha = 0.3f))
            Text(
                text = stringResource(R.string.clear_all_local_data),
                fontSize = 16.sp,
                color = extendedColors.textColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        showCLearAllDataConfirmDialog = true
                    }
                    .padding(vertical = 12.dp, horizontal = 16.dp)
            )
        }
    }

    if (showCLearAllDataConfirmDialog) {
        CommonDialog(
            message = stringResource(R.string.are_you_sure_you_want_to_clear_all_data),
            confirmText = stringResource(R.string.ok),
            cancelText = stringResource(R.string.cancel),
            onConfirm = {
                showCLearAllDataConfirmDialog = true
                viewModel.deleteAllTransaction()
                showCLearAllDataConfirmDialog = false
            },
            onCancel = { showCLearAllDataConfirmDialog = false }
        )
    }

    if (showConfirmationDialog) {
        val days = historyOptionDays[historyOptions.indexOf(tempSelectedOption)]
        CommonDialog(
            message = "${stringResource(R.string.save_history_confirmation)} $tempSelectedOption ${
                stringResource(
                    R.string.save_history_note
                )
            }",
            confirmText = stringResource(R.string.yes),
            cancelText = stringResource(R.string.no),
            onConfirm = {
                viewModel.updateHistoryOption(days)
                showConfirmationDialog = false
            },
            onCancel = {
                showConfirmationDialog = false
            }
        )
    }
}

@Composable
fun ScheduleCheckBox(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .wrapContentWidth()
            .padding(horizontal = 4.dp)
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.secondary,
                uncheckedColor = AppTheme.extendedColors.textColor,
                checkmarkColor = Color.White
            )
        )

        Text(
            text = label,
            fontSize = 14.sp,
            color = AppTheme.extendedColors.textColor,
        )
    }
}

/**
 * Reusable setting row with a label and a switch.
 */
@Composable
fun SettingSwitch(
    @StringRes labelRes: Int,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    checkedTrackColor: Color = MaterialTheme.colorScheme.secondary
) {
    val extendedColors = LocalExtendedColors.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
    ) {
        Text(
            text = stringResource(labelRes),
            fontSize = 16.sp,
            color = extendedColors.textColor,
            modifier = Modifier.weight(1f)
        )

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = checkedTrackColor,
                uncheckedThumbColor = Color.White,
                uncheckedBorderColor = Color.Transparent,
                checkedBorderColor = Color.Transparent
            ),
            thumbContent = null
        )
    }
}

/**
 * Radio button group for selecting one option out of a list.
 * Layout adapts based on device orientation.
 */
@Composable
private fun SettingRadioGroup(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    isLandscape: Boolean
) {
    val extendedColors = LocalExtendedColors.current

    val container: @Composable (@Composable () -> Unit) -> Unit =
        if (isLandscape) {
            { content ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) { content() }
            }
        } else {
            { content -> Column { content() } }
        }

    container {
        options.forEach { option ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { onOptionSelected(option) }
                    .padding(
                        vertical = if (isLandscape) 0.dp else 6.dp,
                        horizontal = 8.dp
                    )
            ) {
                RadioButton(
                    selected = selectedOption == option,
                    onClick = { onOptionSelected(option) },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = MaterialTheme.colorScheme.secondary
                    )
                )
                Text(
                    text = option,
                    fontSize = 14.sp,
                    color = extendedColors.textColor,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}
