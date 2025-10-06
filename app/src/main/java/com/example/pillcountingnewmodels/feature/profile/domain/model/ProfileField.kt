package com.example.pillcountingnewmodels.feature.profile.domain.model

import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType

data class ProfileField(
    val value: String,
    val onChange: (String) -> Unit,
    val labelRes: Int,
    val error: Int?,
    val readOnly: Boolean = false
)
