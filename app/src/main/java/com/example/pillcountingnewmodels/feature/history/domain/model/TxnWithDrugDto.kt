package com.example.pillcountingnewmodels.feature.history.domain.model

import com.example.pillcountingnewmodels.core.room.models.enums.CountStatus
import com.example.pillcountingnewmodels.core.room.models.enums.CountType

data class TxnWithDrugDto(
    val txnId: Long,
    val countType: CountType,
    val status: CountStatus,
    val pillCount: Int?,
    val drugName: String?,
    val ndc: String?,
    val barcodeImage: String?,
    val createdAt: Long,
    val targetCount: Int?,
    val note: String?
)
