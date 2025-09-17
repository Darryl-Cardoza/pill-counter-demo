package com.example.pillcountingnewmodels.core.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.pillcountingnewmodels.core.room.models.CountStatus
import com.example.pillcountingnewmodels.core.room.models.PillCountTxnEntity
import com.example.pillcountingnewmodels.core.room.models.StatusTypeCount
import com.example.pillcountingnewmodels.core.room.relation.PillCountTxnWithDetails
import kotlinx.coroutines.flow.Flow

/**
 * DAO for accessing and managing [PillCountTxnEntity] records (transaction headers).
 *
 * Provides CRUD operations, paged/filterable queries, field updates, and
 * relation-based fetches (header with details).
 *
 * ### Notes
 * - Assumes `PillCountTxnEntity.userId` type matches `UserEntity.userId` (e.g., `String?`).
 * - Consider adding indices in `PillCountTxnEntity` for frequently-filtered columns
 *   like `userId`, `drugId`, `status`, and `createdAt` to optimize queries.
 */
@Dao
interface PillCountTxnDao {

    /* ────────────────────────── Create / Update ────────────────────────── */

    /**
     * Insert or replace a single transaction header.
     *
     * @param txn The transaction to insert or replace.
     * @return The row ID of the inserted entity.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(txn: PillCountTxnEntity): Long

    /**
     * Insert or replace multiple transaction headers.
     *
     * @param txns The list of transactions to insert or replace.
     * @return Row IDs for newly inserted entities.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(txns: List<PillCountTxnEntity>): List<Long>

    /**
     * Update an existing transaction header.
     *
     * @param txn The entity with updated fields.
     */
    @Update
    suspend fun update(txn: PillCountTxnEntity)

    /* ─────────────────────────────── Reads ─────────────────────────────── */

    /**
     * Get a transaction by its primary key.
     *
     * @param id The transaction ID.
     * @return The matching [PillCountTxnEntity], or null if not found.
     */
    @Query("SELECT * FROM pill_count_txn WHERE txnId = :id LIMIT 1")
    suspend fun getById(id: Long): PillCountTxnEntity?

    /**
     * Observe a transaction by its primary key.
     *
     * @param id The transaction ID.
     * @return A [Flow] emitting the entity when it changes (or null if missing).
     */
    @Query("SELECT * FROM pill_count_txn WHERE txnId = :id LIMIT 1")
    fun observeById(id: Long): Flow<PillCountTxnEntity?>

    /**
     * Get all non-deleted transactions ordered by newest first.
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
     * Observe all non-deleted transactions ordered by newest first.
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
     * Paged, filterable query for active transactions.
     *
     * @param userId Optional user filter (exact match).
     * @param drugId Optional drug filter (exact match).
     * @param limit  Max rows to return.
     * @param offset Rows to skip (for pagination).
     */
    @Query(
        """
        SELECT * FROM pill_count_txn
        WHERE isDeleted = 0
          AND (:userId IS NULL OR userId = :userId)
          AND (:drugId IS NULL OR drugId = :drugId)
        ORDER BY createdAt DESC
        LIMIT :limit OFFSET :offset
        """
    )
    suspend fun queryPaged(
        userId: String? = null,
        drugId: Long? = null,
        limit: Int = 50,
        offset: Int = 0
    ): List<PillCountTxnEntity>

    /* ───────────────────────── Field Updates ───────────────────────────── */

    /**
     * Update the status of a transaction.
     *
     * @param txnId  The transaction ID.
     * @param status The new [CountStatus].
     * @param now    Update timestamp (epoch millis).
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
     * @param txnId  The transaction ID.
     * @param target The new target count (nullable).
     * @param now    Update timestamp (epoch millis).
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
     * @param txnId The transaction ID.
     * @param note  The note text (nullable).
     * @param now   Update timestamp (epoch millis).
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
     * @param txnId The transaction ID.
     * @param now   Update timestamp (epoch millis).
     */
    @Query("UPDATE pill_count_txn SET isDeleted = 1, updatedAt = :now WHERE txnId = :txnId")
    suspend fun softDelete(
        txnId: Long,
        now: Long = System.currentTimeMillis()
    )

    /* ──────────────────────────── Relations ────────────────────────────── */

    /**
     * Get a transaction and its details in a single call.
     *
     * @param id The transaction ID.
     * @return [PillCountTxnWithDetails] or null if not found.
     */
    @Transaction
    @Query("SELECT * FROM pill_count_txn WHERE txnId = :id LIMIT 1")
    suspend fun getWithDetails(id: Long): PillCountTxnWithDetails?

    /**
     * Get all transactions for a given user with their details.
     *
     * @param userId The user ID to filter by (nullable → returns all users).
     */
    @Transaction
    @Query(
        """
        SELECT * FROM pill_count_txn
        WHERE isDeleted = 0
          AND (:userId IS NULL OR userId = :userId)
        ORDER BY createdAt DESC
        """
    )
    suspend fun getAllByUserWithDetails(userId: String?): List<PillCountTxnWithDetails>

    /**
     * Grouped aggregate: one row per (status, countType).
     * At most 4 rows (COMPLETED|PARTIAL × FIXED|REGULAR).
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
     * Live version (Flow) if you want the dashboard to auto-update.
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

}
