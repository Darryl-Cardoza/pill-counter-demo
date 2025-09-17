package com.example.pillcountingnewmodels.core.room.relation


import androidx.room.Embedded
import androidx.room.Relation
import com.example.pillcountingnewmodels.core.room.models.PillCountTxnDetailsEntity
import com.example.pillcountingnewmodels.core.room.models.PillCountTxnEntity

data class PillCountTxnWithDetails(
    @Embedded val txn: PillCountTxnEntity,
    @Relation(
        parentColumn = "txnId",
        entityColumn = "txnId",
    )
    val details: List<PillCountTxnDetailsEntity>
)