package com.example.pillcountingnewmodels.core.room.dao

import androidx.room.*
import com.example.pillcountingnewmodels.core.room.models.CountStatus
import com.example.pillcountingnewmodels.core.room.models.PillCountTxnEntity
import com.example.pillcountingnewmodels.core.room.models.StatusTypeCount
import com.example.pillcountingnewmodels.core.room.relation.PillCountTxnWithDetails
import com.example.pillcountingnewmodels.feature.history.domain.model.PillCountWithDrug
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for managing [PillCountTxnEntity] records (transaction headers).
 *
 * ### Design Goals
 * - Ensure **txnId stability**: transaction PKs must not reset or break foreign keys in details.
 * - Avoid `OnConflictStrategy.REPLACE` which deletes and reinserts rows.
 * - Provide safe upsert methods for single and bulk inserts.
 * - Offer reactive [Flow] queries for live dashboards.
 */
@Dao
interface PillCountTxnDao {

    /* ────────────────────────── Create / Update ────────────────────────── */

    /**
     * Insert a new transaction.
     *
     * - Uses [OnConflictStrategy.IGNORE] to avoid accidental PK resets.
     * - Returns the new rowId (txnId) if inserted, or `-1` if a conflict occurred.
     *
     * @param txn The [PillCountTxnEntity] to insert.
     * @return RowId (txnId) if inserted, or -1 if ignored.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(txn: PillCountTxnEntity): Long

    /**
     * Update an existing transaction, matched by primary key ([txnId]).
     *
     * @param txn The updated transaction entity.
     */
    @Update
    suspend fun update(txn: PillCountTxnEntity)

    /**
     * Safely upsert a transaction while preserving [txnId].
     *
     * - If the transaction already exists (same txnId), update in place.
     * - If not, insert a new record.
     * - Never resets [txnId], ensuring foreign key stability in child tables.
     *
     * @param txn The transaction to upsert.
     * @return The stable [txnId].
     */
    @Transaction
    suspend fun upsertPreservingId(txn: PillCountTxnEntity): Long {
        return if (txn.txnId != 0L) {
            val existing = getById(txn.txnId)
            if (existing != null) {
                update(txn.copy(txnId = existing.txnId))
                existing.txnId
            } else {
                insertIgnore(txn).let { newId ->
                    if (newId == -1L) {
                        getById(txn.txnId)?.txnId
                            ?: throw IllegalStateException("Txn insert failed unexpectedly")
                    } else newId
                }
            }
        } else {
            insertIgnore(txn).takeIf { it != -1L }
                ?: throw IllegalStateException("Insert failed: transaction already exists")
        }
    }

    /**
     * Bulk upsert for multiple transactions, preserving PKs.
     *
     * - Runs [upsertPreservingId] for each entity.
     * - Ensures all txnIds remain stable.
     *
     * @param txns List of [PillCountTxnEntity].
     * @return List of stable [txnId]s.
     */
    @Transaction
    suspend fun upsertAllPreservingId(txns: List<PillCountTxnEntity>): List<Long> {
        return txns.map { upsertPreservingId(it) }
    }

    /* ─────────────────────────────── Reads ─────────────────────────────── */

    /**
     * Retrieve a transaction by its primary key.
     *
     * @param id The txnId.
     * @return Matching [PillCountTxnEntity] or null if not found.
     */
    @Query("SELECT * FROM pill_count_txn WHERE txnId = :id LIMIT 1")
    suspend fun getById(id: Long): PillCountTxnEntity?

    /**
     * Observe a transaction by its primary key.
     *
     * Emits updates whenever the entity changes.
     *
     * @param id The txnId.
     * @return A [Flow] emitting [PillCountTxnEntity] or null.
     */
    @Query("SELECT * FROM pill_count_txn WHERE txnId = :id LIMIT 1")
    fun observeById(id: Long): Flow<PillCountTxnEntity?>

    /**
     * Retrieve all active (non-deleted) transactions ordered by newest first.
     */
    @Query(
        """
        SELECT * FROM pill_count_txn
        WHERE isDeleted = 0
        ORDER BY createdAt DESC
        """
    )
    suspend fun getAllActive(): List<PillCountTxnEntity>

    /**
     * Observe all active (non-deleted) transactions ordered by newest first.
     */
    @Query(
        """
        SELECT * FROM pill_count_txn
        WHERE isDeleted = 0
        ORDER BY createdAt DESC
        """
    )
    fun observeAllActive(): Flow<List<PillCountTxnEntity>>

    /**
     * Paged query for transactions with optional user and drug filters.
     *
     * @param localId Optional FK to user.
     * @param drugId Optional FK to drug.
     * @param limit Maximum rows to return.
     * @param offset Rows to skip (pagination).
     */
    @Query(
        """
        SELECT * FROM pill_count_txn
        WHERE isDeleted = 0
          AND (:localId IS NULL OR localId = :localId)
          AND (:drugId IS NULL OR drugId = :drugId)
        ORDER BY createdAt DESC
        LIMIT :limit OFFSET :offset
        """
    )
    suspend fun queryPaged(
        localId: Long? = null,
        drugId: Long? = null,
        limit: Int = 50,
        offset: Int = 0
    ): List<PillCountTxnEntity>

    /* ───────────────────────── Field Updates ───────────────────────────── */

    /**
     * Update the status of a transaction.
     *
     * @param txnId The transaction PK.
     * @param status New [CountStatus].
     * @param now Update timestamp (epoch millis).
     */
    @Query("UPDATE pill_count_txn SET status = :status, updatedAt = :now WHERE txnId = :txnId")
    suspend fun updateStatus(
        txnId: Long,
        status: CountStatus,
        now: Long = System.currentTimeMillis()
    )

    /**
     * Update the target count of a transaction.
     *
     * @param txnId Transaction PK.
     * @param target New target count (nullable).
     * @param now Update timestamp.
     */
    @Query("UPDATE pill_count_txn SET targetCount = :target, updatedAt = :now WHERE txnId = :txnId")
    suspend fun updateTargetCount(
        txnId: Long,
        target: Int?,
        now: Long = System.currentTimeMillis()
    )

    /**
     * Update the note of a transaction.
     *
     * @param txnId Transaction PK.
     * @param note Optional note string.
     * @param now Update timestamp.
     */
    @Query("UPDATE pill_count_txn SET note = :note, updatedAt = :now WHERE txnId = :txnId")
    suspend fun updateNote(
        txnId: Long,
        note: String?,
        now: Long = System.currentTimeMillis()
    )

    /**
     * Soft-delete a transaction (sets `isDeleted = 1`).
     *
     * @param txnId Transaction PK.
     * @param now Update timestamp.
     */
    @Query("UPDATE pill_count_txn SET isDeleted = 1, updatedAt = :now WHERE txnId = :txnId")
    suspend fun softDelete(
        txnId: Long,
        now: Long = System.currentTimeMillis()
    )

    /* ──────────────────────────── Relations ────────────────────────────── */

    /**
     * Fetch a transaction and its details in a single call.
     *
     * @param id The transaction PK.
     * @return [PillCountTxnWithDetails] or null if not found.
     */
    @Transaction
    @Query("SELECT * FROM pill_count_txn WHERE txnId = :id LIMIT 1")
    suspend fun getWithDetails(id: Long): PillCountTxnWithDetails?

    /**
     * Retrieve all transactions for a given user, with details included.
     *
     * @param localId Optional user FK. Null → returns all users.
     * @return List of [PillCountTxnWithDetails].
     */
    @Transaction
    @Query(
        """
        SELECT * FROM pill_count_txn
        WHERE isDeleted = 0
          AND (:localId IS NULL OR localId = :localId)
        ORDER BY createdAt DESC
        """
    )
    suspend fun getAllByUserWithDetails(localId: Long?): List<PillCountTxnWithDetails>

    /**
     * Aggregate dashboard counts, grouped by [CountStatus] and count type.
     *
     * @return One row per (status, countType).
     */
    @Query(
        """
        SELECT status AS status,
               countType AS countType,
               COUNT(*) AS cnt
        FROM pill_count_txn
        WHERE isDeleted = 0
        GROUP BY status, countType
        """
    )
    suspend fun getDashboardCountsGrouped(): List<StatusTypeCount>

    /**
     * Live dashboard counts, grouped by [CountStatus] and count type.
     *
     * @return Flow emitting aggregate counts on updates.
     */
    @Query(
        """
        SELECT status AS status,
               countType AS countType,
               COUNT(*) AS cnt
        FROM pill_count_txn
        WHERE isDeleted = 0
        GROUP BY status, countType
        """
    )
    fun observeDashboardCountsGrouped(): Flow<List<StatusTypeCount>>


    @Query(
        """
    SELECT dm.drugName
    FROM pill_count_txn AS pct
    LEFT JOIN drug_master AS dm ON pct.drugId = dm.drugId
    WHERE pct.txnId = :transactionId
    LIMIT 1
    """
    )
    suspend fun getDrugNameForTransaction(transactionId: Long): String?

    @Query("UPDATE pill_count_txn SET status = :newStatus, updatedAt = :updatedAt WHERE txnId = :txnId")
    suspend fun updateTxnStatus(txnId: Long, newStatus: CountStatus, updatedAt: Long = System.currentTimeMillis())



//    @Query("""
//        SELECT t.txnId, d.drugName, t.targetCount, t.createdAt
//        FROM pill_count_txn AS t
//        LEFT JOIN drug_master AS d ON t.drugId = d.drugId
//        WHERE t.isDeleted = 0
//        ORDER BY t.createdAt DESC
//    """)
//    fun getAllTransactions(): Flow<List<PillCountWithDrug>>

    @Query("""
        SELECT t.txnId, d.drugName, t.targetCount, t.createdAt
        FROM pill_count_txn AS t
        LEFT JOIN drug_master AS d ON t.drugId = d.drugId
        WHERE t.isDeleted = 0 
          AND t.createdAt BETWEEN :start AND :end
        ORDER BY t.createdAt DESC
    """)
    fun getTransactionsWithDrugByDate(
        start: Long,
        end: Long
    ): Flow<List<PillCountWithDrug>>

    // Query to get raw pill_count_txn entities
    @Query("SELECT * FROM pill_count_txn WHERE createdAt BETWEEN :start AND :end")
    fun getTransactionsByDateRaw(
        start: Long,
        end: Long
    ): Flow<List<PillCountTxnEntity>>

    // Delete transactions in a date range
    @Query("DELETE FROM pill_count_txn WHERE createdAt BETWEEN :start AND :end")
    suspend fun deleteTransactionsByDate(
        start: Long,
        end: Long
    )


}
