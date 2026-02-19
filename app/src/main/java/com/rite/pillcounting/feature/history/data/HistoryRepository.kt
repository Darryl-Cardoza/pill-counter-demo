package com.rite.pillcounting.feature.history.data

import com.rite.pillcounting.core.room.dao.PillCountTxnDao
import com.rite.pillcounting.core.room.models.enums.CountStatus
import com.rite.pillcounting.core.room.models.enums.CountType
import com.rite.pillcounting.feature.history.domain.model.TxnWithDrugDto


import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class HistoryRepository @Inject constructor(
    private val dao: PillCountTxnDao
) {

    private fun LocalDate.toEpochRange(): Pair<Long, Long> {
        val zone = ZoneId.systemDefault()
        val start = atStartOfDay(zone).toInstant().toEpochMilli()
        val end = plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        return start to end
    }

    fun getTransactionsForDate(date: LocalDate,type: CountType?, status: CountStatus?): Flow<List<TxnWithDrugDto>> {
        val (start, end) = date.toEpochRange()
        return dao.getTransactionsWithDrugByDate(start, end, type, status)
    }

    suspend fun deleteTransactionsForDate(date: LocalDate, type: CountType?, status: CountStatus?) {
        val (start, end) = date.toEpochRange()

        // NORMAL history → delete everything in that date
        dao.deleteTransactionsByDate(start, end, type, status)
    }
}

