package com.rite.pillcounting.feature.barcodeScan.domain.model

data class GetNdcRequestModel(
    val target_ndc: String,
    val scanned_ndc: String
)