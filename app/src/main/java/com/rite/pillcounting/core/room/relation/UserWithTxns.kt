package com.rite.pillcounting.core.room.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.rite.pillcounting.core.room.models.PillCountTxnEntity
import com.rite.pillcounting.core.room.models.UserEntity

data class UserWithTxns(
    @Embedded val user: UserEntity,
    @Relation(
        parentColumn = "userId",
        entityColumn = "userId"
    )
    val txns: List<PillCountTxnEntity>
)