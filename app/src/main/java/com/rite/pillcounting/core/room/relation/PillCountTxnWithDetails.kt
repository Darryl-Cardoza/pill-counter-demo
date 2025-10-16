package com.rite.pillcounting.core.room.relation


import androidx.room.Embedded
import androidx.room.Relation
import com.rite.pillcounting.core.room.models.PillCountTxnDetailsEntity
import com.rite.pillcounting.core.room.models.PillCountTxnEntity

data class PillCountTxnWithDetails(
    @Embedded val txn: PillCountTxnEntity,
    @Relation(
        parentColumn = "txnId",
        entityColumn = "txnId",
    )
    val details: List<PillCountTxnDetailsEntity>
)