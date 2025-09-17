package com.example.pillcountingnewmodels.core.room.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.pillcountingnewmodels.core.room.models.PillCountTxnEntity
import com.example.pillcountingnewmodels.core.room.models.UserEntity

data class UserWithTxns(
    @Embedded val user: UserEntity,
    @Relation(
        parentColumn = "userId",
        entityColumn = "userId"
    )
    val txns: List<PillCountTxnEntity>
)