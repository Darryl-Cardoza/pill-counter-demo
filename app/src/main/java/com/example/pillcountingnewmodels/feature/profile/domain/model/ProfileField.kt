package com.example.pillcountingnewmodels.feature.profile.domain.model

import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType

data class ProfileField(
    val placeholder: String,
    var value: String,
    val keyboardType: KeyboardType = KeyboardType.Text,
    val imeAction: ImeAction = ImeAction.Next,
    val error: String? = null
)
