package com.rite.pillcounting.core.room.models.dtos

import androidx.room.Relation
import com.rite.pillcounting.core.room.models.PillCountTxnDetailsEntity

data class TxnWithDetails(
    val txnId: Long,
    val drugName: String?,
    val ndc: String?,
    val targetCount: Int?,
    val expiry: String?,
    val lotNo: String?,
    val note: String?,
    val createdAt: Long,
    val barcodeImage: String?,
    val totalPillCount: Int,
    @Relation(
        parentColumn = "txnId",
        entityColumn = "txnId",
        entity = PillCountTxnDetailsEntity::class,
        projection = ["txnId", "pillCount", "imagePath"]
    )
    val txnDetails: List<TxnDetailInfo>
)

data class TxnDetailInfo(
    val txnId: Long?,
    val pillCount: Int?,
    val imagePath: String?
)
