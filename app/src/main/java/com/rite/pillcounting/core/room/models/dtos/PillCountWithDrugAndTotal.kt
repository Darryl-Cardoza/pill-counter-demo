package com.rite.pillcounting.core.room.models.dtos

data class PillCountWithDrugAndTotal(
    val txnId: Long,
    val drugName: String?,
    val createdAt: Long,
    val targetCount: Int?,
    val barcodeImage: String?,
    val totalPillCount: Int,
    val isComingFromHL7 : Boolean,
    val isNdcVerified: Boolean
)