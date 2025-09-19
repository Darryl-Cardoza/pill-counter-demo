package com.example.pillcountingnewmodels.core.room.dao

import androidx.room.*
import com.example.pillcountingnewmodels.core.room.models.DrugMasterEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for managing [DrugMasterEntity] persistence.
 *
 * ### Design Principles
 * - **Stable Primary Key**: `drugId` (PK) is the only FK exposed to other tables
 *   (e.g., [com.example.pillcountingnewmodels.core.room.models.PillCountTxnEntity]).
 * - **Preserve IDs**: All upserts ensure the same `drugId` is reused if an NDC already exists.
 * - **Safe Defaults**:
 *   - Avoid `OnConflictStrategy.REPLACE` (can reset PKs and break FKs).
 *   - Prefer `IGNORE + update in place`.
 * - **Reactive Support**: Provides both `suspend` and `Flow` queries for UI observation.
 */
@Dao
interface DrugMasterDao {

    /* ────────────────────────── Insert / Update ────────────────────────── */

    /**
     * Inserts a new drug into the [drug_master] table.
     *
     * - Fails silently if an entry with the same [DrugMasterEntity.ndc] already exists.
     * - Returns:
     *   - Newly inserted rowId (drugId) if successful.
     *   - `-1` if ignored due to conflict.
     *
     * @param drug The [DrugMasterEntity] to insert.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(drug: DrugMasterEntity): Long

    /**
     * Updates an existing drug by matching on its [DrugMasterEntity.drugId].
     *
     * @param drug The entity with updated values (must have a valid PK).
     */
    @Update
    suspend fun update(drug: DrugMasterEntity)

    /**
     * Safely upserts a single [DrugMasterEntity].
     *
     * - If a row with the same NDC exists:
     *   - Reuses its stable [drugId].
     *   - Updates the row in place with new values.
     * - If no match is found:
     *   - Inserts a new row and returns its new [drugId].
     * - Prevents accidental PK resets or duplicate entries.
     *
     * @param drug The drug entity to insert or update.
     * @return The stable primary key ([drugId]).
     */
    @Transaction
    suspend fun upsertPreservingId(drug: DrugMasterEntity): Long {
        val existingId = getDrugIdByNdc(drug.ndc)
        return if (existingId != null) {
            update(drug.copy(drugId = existingId))
            existingId
        } else {
            val newId = insertIgnore(drug)
            if (newId == -1L) {
                // Possible race condition: fetch again
                getDrugIdByNdc(drug.ndc)
                    ?: throw IllegalStateException("Drug insert failed unexpectedly")
            } else {
                newId
            }
        }
    }

    /**
     * Bulk version of [upsertPreservingId].
     *
     * - Iterates through each drug and applies safe upsert logic.
     * - Ensures all PKs remain stable across syncs.
     *
     * @param drugs List of drug entities to insert or update.
     * @return List of stable PKs ([drugId]) corresponding to each entity.
     */
    @Transaction
    suspend fun upsertAllPreservingId(drugs: List<DrugMasterEntity>): List<Long> {
        return drugs.map { upsertPreservingId(it) }
    }

    /**
     * Updates only the `drugName` field of a record identified by its NDC.
     *
     * @param ndc National Drug Code of the target drug.
     * @param drugName New name to assign.
     * @return Number of rows updated (0 if none).
     */
    @Query("UPDATE drug_master SET drugName = :drugName WHERE ndc = :ndc")
    suspend fun updateDrugNameByNdc(ndc: String, drugName: String): Int

    /* ───────────────────────────── Queries ────────────────────────────── */

    /**
     * Retrieves a drug by its primary key.
     *
     * @param id Primary key ([drugId]).
     * @return The matching [DrugMasterEntity], or `null` if not found.
     */
    @Query("SELECT * FROM drug_master WHERE drugId = :id LIMIT 1")
    suspend fun getById(id: Long): DrugMasterEntity?

    /**
     * Retrieves a drug by its National Drug Code (NDC).
     *
     * @param ndc National Drug Code string.
     * @return The matching [DrugMasterEntity], or `null` if not found.
     */
    @Query("SELECT * FROM drug_master WHERE ndc = :ndc LIMIT 1")
    suspend fun getDrugByNdc(ndc: String): DrugMasterEntity?

    /**
     * Retrieves only the primary key ([drugId]) for a given NDC.
     *
     * @param ndc National Drug Code string.
     * @return [drugId] if found, or `null` if not present.
     */
    @Query("SELECT drugId FROM drug_master WHERE ndc = :ndc LIMIT 1")
    suspend fun getDrugIdByNdc(ndc: String): Long?

    /**
     * Performs a case-insensitive search by drug name or NDC.
     *
     * @param q Search query (nullable).
     * @return List of matching [DrugMasterEntity]s ordered by creation date (newest first).
     */
    @Query(
        """
        SELECT * FROM drug_master
        WHERE (:q IS NULL OR drugName LIKE '%' || :q || '%'
               OR ndc LIKE '%' || :q || '%')
        ORDER BY createdAt DESC
        """
    )
    suspend fun search(q: String? = null): List<DrugMasterEntity>

    /**
     * Observes all drugs in the table as a reactive stream.
     *
     * @return A [Flow] emitting the full list of drugs whenever data changes.
     */
    @Query("SELECT * FROM drug_master ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<DrugMasterEntity>>

    /* ───────────────────────────── Deletes ────────────────────────────── */

    /**
     * Deletes a drug by its primary key ([drugId]).
     *
     * @param id The PK of the row to delete.
     */
    @Query("DELETE FROM drug_master WHERE drugId = :id")
    suspend fun deleteById(id: Long)

    /**
     * Clears the entire [drug_master] table.
     *
     * ⚠️ WARNING:
     * - This will break any foreign key references in dependent tables.
     * - Use with caution — prefer marking entries inactive instead.
     */
    @Query("DELETE FROM drug_master")
    suspend fun clear()
}
