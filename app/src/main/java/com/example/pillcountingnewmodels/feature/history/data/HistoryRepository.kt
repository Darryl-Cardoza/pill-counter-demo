package com.example.pillcountingnewmodels.feature.history.data

import com.example.pillcountingnewmodels.R
import com.example.pillcountingnewmodels.core.room.dao.PillCountTxnDao
import com.example.pillcountingnewmodels.feature.history.domain.model.CountRowData


import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
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

    fun getTransactionsForDate(date: LocalDate): Flow<List<CountRowData>> {
        val startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        return dao.getTransactionsWithDrugByDate(startOfDay, endOfDay).map { list ->
            list.map { txn ->
                CountRowData(
                    name = txn.drugName ?: "Unknown Drug",
                    count = txn.targetCount, // targetCount is non-null
                    iconRes = R.drawable.partial,
                    formattedTimestamp = Instant.ofEpochMilli(txn.createdAt)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime()
                        .format(formatter)
                )
            }
        }
    }

    suspend fun deleteTransactionsForDate(date: LocalDate) {
        val startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        dao.deleteTransactionsByDate(startOfDay, endOfDay)
    }
}

