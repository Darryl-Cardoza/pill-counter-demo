package com.example.pillcountingnewmodels.core.room.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.pillcountingnewmodels.core.room.models.DrugMasterEntity
import com.example.pillcountingnewmodels.core.room.models.PillCountTxnEntity

data class DrugWithTxns(
    @Embedded val drug: DrugMasterEntity,
    @Relation(
        parentColumn = "drugId",
        entityColumn = "drugId"
    )
    val txns: List<PillCountTxnEntity>
)