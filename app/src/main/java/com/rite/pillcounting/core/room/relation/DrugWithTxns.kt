package com.rite.pillcounting.core.room.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.rite.pillcounting.core.room.models.DrugMasterEntity
import com.rite.pillcounting.core.room.models.PillCountTxnEntity

data class DrugWithTxns(
    @Embedded val drug: DrugMasterEntity,
    @Relation(
        parentColumn = "drugId",
        entityColumn = "drugId"
    )
    val txns: List<PillCountTxnEntity>
)