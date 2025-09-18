package com.example.pillcountingnewmodels.core.room.dao

import androidx.room.*
import com.example.pillcountingnewmodels.core.room.models.DrugMasterEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for the [DrugMasterEntity] table.
 *
 * Provides methods for inserting, updating, querying, and deleting drug master records.
 */
@Dao
interface DrugMasterDao {

    /* ────────────────────────── Insert / Update ────────────────────────── */

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(drug: DrugMasterEntity): Long

    /**
     * Insert or update a single [DrugMasterEntity].
     *
     * @param drug The drug entity to insert or replace.
     * @return The newly inserted row ID.
     */
    @Upsert
    suspend fun upsert(drug: DrugMasterEntity): Long

    /**
     * Insert or update a list of [DrugMasterEntity] records.
     *
     * @param drugs The list of drug entities to insert or replace.
     * @return List of newly inserted row IDs.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(drugs: List<DrugMasterEntity>): List<Long>

    /**
     * Update an existing [DrugMasterEntity].
     *
     * @param drug The entity with updated fields.
     */
    @Update
    suspend fun update(drug: DrugMasterEntity)

    @Query("UPDATE drug_master SET drugName = :drugName WHERE ndc = :ndc")
    suspend fun updateDrugNameByNdc(ndc: String, drugName: String): Int

    suspend fun upsertAndReturnId(ndc: String, drugName: String): Long {
        // Check if exists
        val existingId = getDrugIdByNdc(ndc)
        return if (existingId != null) {
            // Update existing row
            updateDrugNameByNdc(ndc, drugName)
            existingId
        } else {
            // Insert new row
            insert(
                DrugMasterEntity(
                    ndc = ndc,
                    drugName = drugName
                )
            )
        }
    }


    /* ───────────────────────────── Queries ────────────────────────────── */

    /**
     * Retrieve a drug by its [drugId].
     *
     * @param id The primary key of the drug.
     * @return The matching [DrugMasterEntity], or null if not found.
     */
    @Query("SELECT * FROM drug_master WHERE drugId = :id LIMIT 1")
    suspend fun getById(id: Long): DrugMasterEntity?

    /**
     * Retrieve a drug by its National Drug Code (NDC).
     *
     * @param ndc The unique NDC string.
     * @return The matching [DrugMasterEntity], or null if not found.
     */
    @Query("SELECT * FROM drug_master WHERE ndc = :ndc LIMIT 1")
    suspend fun getDrugByNdc(ndc: String): DrugMasterEntity?

    @Query("SELECT drugId FROM drug_master WHERE ndc = :ndc LIMIT 1")
    suspend fun getDrugIdByNdc(ndc: String): Long?

    /**
     * Search for drugs by name or NDC.
     *
     * @param q Optional search query. Matches drug name or NDC containing [q].
     * @return List of [DrugMasterEntity] records matching the query.
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
     * Observe all drugs in the table, ordered by creation time.
     *
     * @return A [Flow] that emits the current list of [DrugMasterEntity].
     */
    @Query("SELECT * FROM drug_master ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<DrugMasterEntity>>

    /* ───────────────────────────── Deletes ────────────────────────────── */

    /**
     * Delete a drug by its [drugId].
     *
     * @param id The primary key of the drug to delete.
     */
    @Query("DELETE FROM drug_master WHERE drugId = :id")
    suspend fun deleteById(id: Long)

    /**
     * Clear all drug records from the table.
     */
    @Query("DELETE FROM drug_master")
    suspend fun clear()
}
