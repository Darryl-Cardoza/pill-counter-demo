package com.example.pillcountingnewmodels.feature.settings.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.pillcountingnewmodels.core.utils.PreferenceHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferenceHelper: PreferenceHelper
) : ViewModel() {

    // Holds the current state of "Ask to Add Notes"
    private val _isAskToAddNotes = MutableStateFlow(preferenceHelper.getShowNotesDialogSetting())
    val isAskToAddNotes: StateFlow<Boolean> = _isAskToAddNotes

    // Called when user toggles the switch
    fun toggleAskToAddNotes(newValue: Boolean) {
        _isAskToAddNotes.value = newValue
        preferenceHelper.saveShowNotesDialogSetting(newValue)
    }
}
