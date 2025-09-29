package com.example.pillcountingnewmodels.feature.history.domain.model


    data class PillCountWithDrug(
        val txnId: Long,
        val drugName: String?,
        val targetCount: Int?,
        val createdAt: Long
    )

