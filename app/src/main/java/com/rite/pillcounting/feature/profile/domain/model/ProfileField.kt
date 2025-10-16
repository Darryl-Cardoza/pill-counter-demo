package com.rite.pillcounting.feature.profile.domain.model

data class ProfileField(
    val value: String,
    val onChange: (String) -> Unit,
    val labelRes: Int,
    val error: Int?,
    val readOnly: Boolean = false
)
