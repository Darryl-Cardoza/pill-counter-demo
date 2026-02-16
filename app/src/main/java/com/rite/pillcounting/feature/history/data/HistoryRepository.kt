package com.rite.pillcounting.feature.history.data

import com.rite.pillcounting.core.room.dao.PillCountTxnDao
import com.rite.pillcounting.core.room.models.enums.CountStatus
import com.rite.pillcounting.core.room.models.enums.CountType
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

    private fun LocalDate.toEpochRange(): Pair<Long, Long> {
        val zone = ZoneId.systemDefault()
        val start = atStartOfDay(zone).toInstant().toEpochMilli()
        val end = plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        return start to end
    }

    fun getTransactionsForDate(date: LocalDate): Flow<List<TxnWithDrugDto>> {
        val (start, end) = date.toEpochRange()
        return dao.getHistoryTransactions(start, end, null, null)
    }

    fun getDispenseTransactionsWithDrugByDate(date: LocalDate): Flow<List<TxnWithDrugDto>> {
        val (start, end) = date.toEpochRange()
        return dao.getHistoryTransactions(
            start,
            end,
            CountType.FIXED,
            CountStatus.COMPLETED
        )
    }

    fun getRegularTransactionsWithDrugByDate(date: LocalDate): Flow<List<TxnWithDrugDto>> {
        val (start, end) = date.toEpochRange()
        return dao.getHistoryTransactions(
            start,
            end,
            CountType.REGULAR,
            CountStatus.COMPLETED
        )
    }


    suspend fun deleteTransactionsForDate(date: LocalDate) {
        val (start, end) = date.toEpochRange()

        // NORMAL history → delete everything in that date
        dao.deleteTransactionsByDate(start, end, null, null)
    }

    suspend fun deleteRegularTransactionsForDate(date: LocalDate) {
        val (start, end) = date.toEpochRange()

        // Only REGULAR completed transactions
        dao.deleteTransactionsByDate(
            start,
            end,
            CountType.REGULAR,
            CountStatus.COMPLETED
        )
    }

    suspend fun deleteDispenseTransactionsForDate(date: LocalDate) {
        val (start, end) = date.toEpochRange()

        dao.deleteTransactionsByDate(
            start,
            end,
            CountType.FIXED,
            CountStatus.COMPLETED
        )
    }

}

