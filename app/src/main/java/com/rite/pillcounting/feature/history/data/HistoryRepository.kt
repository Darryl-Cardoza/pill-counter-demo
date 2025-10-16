package com.rite.pillcounting.feature.history.data

import com.rite.pillcounting.core.room.dao.PillCountTxnDao
import com.rite.pillcounting.feature.history.domain.model.TxnWithDrugDto


import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

class HistoryRepository @Inject constructor(
    private val dao: PillCountTxnDao
) {
    private val formatter = DateTimeFormatter.ofPattern(
        "dd MMM yyyy • hh:mm a",
        Locale.getDefault()
    )

    fun getTransactionsForDate(date: LocalDate): Flow<List<TxnWithDrugDto>> {
        val startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        return dao.getTransactionsWithDrugByDate(startOfDay, endOfDay)
    }



    suspend fun deleteTransactionsForDate(date: LocalDate) {
        val startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        dao.deleteTransactionsByDate(startOfDay, endOfDay)
    }
}

