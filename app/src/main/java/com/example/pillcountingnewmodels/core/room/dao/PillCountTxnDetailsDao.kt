package com.example.pillcountingnewmodels.core.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.pillcountingnewmodels.core.room.models.PillCountTxnDetailsEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for accessing and managing [PillCountTxnDetailsEntity] records
 * (transaction detail lines).
 *
 * Provides CRUD operations, query helpers, soft delete handling,
 * and utility methods for pill counts and images.
 */
@Dao
interface PillCountTxnDetailsDao {

    /* ────────────────────────── Create / Update ────────────────────────── */

    /**
     * Insert or replace a single transaction detail.
     *
     * @param detail The detail to insert or replace.
     * @return The row ID of the inserted entity.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(detail: PillCountTxnDetailsEntity): Long

    /**
     * Insert or replace multiple transaction details.
     *
     * @param details List of details to insert or replace.
     * @return Row IDs of the inserted entities.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(details: List<PillCountTxnDetailsEntity>): List<Long>

    /**
     * Update an existing transaction detail.
     *
     * @param detail The entity with updated fields.
     */
    @Update
    suspend fun update(detail: PillCountTxnDetailsEntity)

    /* ─────────────────────────────── Reads ─────────────────────────────── */

    /**
     * Get a detail by its primary key.
     *
     * @param id The detail ID.
     * @return The matching [PillCountTxnDetailsEntity], or null if not found.
     */
    @Query("SELECT * FROM pill_count_txn_details WHERE txnDetailsId = :id LIMIT 1")
    suspend fun getById(id: Long): PillCountTxnDetailsEntity?


    /**
     * Observe all non-deleted details for a given transaction.
     * Results are ordered by `createdAt` (newest first).
     *
     * @param txnId The parent transaction ID.
     * @return A [Flow] emitting updates to the detail list.
     */
    @Query(
        """
        SELECT * FROM pill_count_txn_details
        WHERE txnId = :txnId AND isDeleted = 0
        ORDER BY createdAt DESC
        """
    )
    fun observeAllForTxn(txnId: Long): Flow<List<PillCountTxnDetailsEntity>>

    /* ──────────────────────────── Soft Delete ───────────────────────────── */

    /**
     * Soft delete a detail by its ID.
     *
     * @param id  The detail ID.
     * @param now Timestamp for update (epoch millis).
     */
    @Query("UPDATE pill_count_txn_details SET isDeleted = 1, updatedAt = :now WHERE txnDetailsId = :id")
    suspend fun softDelete(id: Long, now: Long = System.currentTimeMillis())

    /**
     * Soft delete all details for a given transaction.
     *
     * @param txnId The parent transaction ID.
     * @param now   Timestamp for update (epoch millis).
     */
    @Query("UPDATE pill_count_txn_details SET isDeleted = 1, updatedAt = :now WHERE txnId = :txnId")
    suspend fun softDeleteByTxnId(txnId: Long, now: Long = System.currentTimeMillis())

    /* ───────────────────────────── Helpers ─────────────────────────────── */

    /**
     * Get the total pill count across all active details for a transaction.
     *
     * @param txnId The parent transaction ID.
     * @return Sum of pill counts, or null if no details exist.
     */
    @Query("SELECT SUM(COALESCE(pillCount, 0)) FROM pill_count_txn_details WHERE txnId = :txnId AND isDeleted = 0")
    suspend fun sumPillCountForTxn(txnId: Long): Int?

    /**
     * Get the most recent detail with an image for a transaction.
     *
     * @param txnId The parent transaction ID.
     * @return The latest [PillCountTxnDetailsEntity] with an image, or null if none found.
     */
    @Query(
        """
        SELECT * FROM pill_count_txn_details 
        WHERE txnId = :txnId AND imagePath IS NOT NULL AND isDeleted = 0
        ORDER BY createdAt DESC
        LIMIT 1
        """
    )
    suspend fun getLatestImageDetail(txnId: Long): PillCountTxnDetailsEntity?

    @Query("""
    SELECT COALESCE(SUM(pillCount), 0) 
    FROM pill_count_txn_details 
    WHERE txnId = :txnId
""")
    suspend fun getTotalPillCountForTxn(txnId: Long): Int

}
