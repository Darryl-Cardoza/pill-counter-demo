package com.example.pillcountingnewmodels.core.room.models.dtos

import com.example.pillcountingnewmodels.core.room.models.enums.CountStatus
import com.example.pillcountingnewmodels.core.room.models.enums.CountType

/** Row for (status, countType) aggregate. */
data class StatusTypeCount(
    val status: CountStatus,
    val countType: CountType,
    val cnt: Int
)