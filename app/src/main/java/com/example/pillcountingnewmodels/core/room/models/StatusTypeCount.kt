package com.example.pillcountingnewmodels.core.room.models

/** Row for (status, countType) aggregate. */
data class StatusTypeCount(
    val status: CountStatus,
    val countType: CountType,
    val cnt: Int
)