package com.example.pillcountingnewmodels.core.room.dao


data class PillCountWithDrugAndTotal(
    val txnId: Long,
    val drugName: String?,
    val createdAt: Long,
    val targetCount: Int?,
    val barcodeImage: String?,
    val totalPillCount: Int
)


