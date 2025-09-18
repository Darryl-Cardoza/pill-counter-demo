package com.example.pillcountingnewmodels.core.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.pillcountingnewmodels.core.room.models.UserEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for accessing and managing [UserEntity] records.
 *
 * Provides CRUD, existence checks, counts, flexible search, field-level updates,
 * and transactional helpers (e.g., preserving localId on upsert).
 */
@Dao
interface UserDao {

    /* ────────────────────────── Create / Update ────────────────────────── */

    /**
     * Insert or replace a single user (overwrites all fields).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(user: UserEntity)

    /**
     * Insert or replace multiple users (overwrites all fields).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(users: List<UserEntity>)

    /**
     * Update an existing user (partial update of provided fields).
     */
    @Update
    suspend fun update(user: UserEntity)

    /**
     * Get the next device-side incremental ID for `localId`.
     *
     * Note: Room only auto-increments PKs. Since `localId` is not a PK,
     * allocate it manually using this helper when needed.
     */
    @Query("SELECT COALESCE(MAX(localId), 0) + 1 FROM users")
    suspend fun nextLocalId(): Long

    /**
     * Transactional helper that upserts a user while preserving the existing `localId`
     * if the user already exists; otherwise assigns the next local ID.
     */
    @Transaction
    suspend fun upsertPreservingLocalId(user: UserEntity): Long {
        val existing = getById(user.userId)
        val toSave = when {
            existing != null -> user.copy(localId = existing.localId)
            else -> user.copy(localId = nextLocalId())
        }
        upsert(toSave)
        return toSave.localId
    }

    /**
     * Replace the table content with the provided list (clear + bulk upsert).
     * Useful after a full sync from server.
     */
    @Transaction
    suspend fun replaceAll(users: List<UserEntity>) {
        clear()
        upsertAll(users.mapIndexed { _, u ->
            // ensure localId allocation for new rows
            // note: if you want stable device IDs across runs, allocate in repo using nextLocalId()
            u
        })
    }

    /* ─────────────────────────────── Reads ─────────────────────────────── */

    /**
     * Get a user by their unique ID (primary key).
     */
    @Query("SELECT * FROM users WHERE userId = :id LIMIT 1")
    suspend fun getById(id: String): UserEntity?

    /**
     * Observe a user by their unique ID.
     */
    @Query("SELECT * FROM users WHERE userId = :id LIMIT 1")
    fun observeById(id: String): Flow<UserEntity?>

    /**
     * Get all users ordered by creation time (newest first).
     */
    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    suspend fun getAll(): List<UserEntity>

    /**
     * Observe all users ordered by creation time (newest first).
     */
    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<UserEntity>>

    /**
     * Find a user by email.
     */
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): UserEntity?

    /**
     * Find a user by phone number.
     */
    @Query("SELECT * FROM users WHERE phoneNumber = :phone LIMIT 1")
    suspend fun findByPhone(phone: String): UserEntity?

    /**
     * Case-insensitive search by name or email.
     */
    @Query(
        """
        SELECT * FROM users
        WHERE (:q IS NULL OR name LIKE '%' || :q || '%' ESCAPE '\' COLLATE NOCASE
               OR email LIKE '%' || :q || '%' ESCAPE '\' COLLATE NOCASE)
        ORDER BY createdAt DESC
        """
    )
    suspend fun search(q: String? = null): List<UserEntity>

    /**
     * Get/observe all users having a given role.
     */
    @Query("SELECT * FROM users WHERE role = :role ORDER BY createdAt DESC")
    suspend fun getAllByRole(role: String): List<UserEntity>

    @Query("SELECT * FROM users WHERE role = :role ORDER BY createdAt DESC")
    fun observeAllByRole(role: String): Flow<List<UserEntity>>

    /**
     * Count total users.
     */
    @Query("SELECT COUNT(*) FROM users")
    suspend fun countAll(): Int

    /**
     * Check existence by userId.
     */
    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE userId = :id)")
    suspend fun exists(id: String): Boolean

    /* ───────────────────────── Field-Level Updates ─────────────────────── */

    @Query("UPDATE users SET name = :name WHERE userId = :id")
    suspend fun updateName(id: String, name: String)

    @Query("UPDATE users SET email = :email WHERE userId = :id")
    suspend fun updateEmail(id: String, email: String?)

    @Query("UPDATE users SET phoneNumber = :phone WHERE userId = :id")
    suspend fun updatePhone(id: String, phone: String?)

    @Query("UPDATE users SET avatarUrl = :url WHERE userId = :id")
    suspend fun updateAvatar(id: String, url: String?)

    @Query("UPDATE users SET role = :role WHERE userId = :id")
    suspend fun updateRole(id: String, role: String?)

    @Query("UPDATE users SET isVerified = :verified WHERE userId = :id")
    suspend fun updateVerification(id: String, verified: Boolean)

    @Query("UPDATE users SET language = :language WHERE userId = :id")
    suspend fun updateLanguage(id: String, language: String?)

    @Query("UPDATE users SET timezone = :tz WHERE userId = :id")
    suspend fun updateTimezone(id: String, tz: String?)

    @Query("UPDATE users SET notifications = :enabled WHERE userId = :id")
    suspend fun updateNotifications(id: String, enabled: Boolean?)

    /* ────────────────────────────── Deletes ────────────────────────────── */

    /**
     * Delete a user by their unique ID.
     */
    @Query("DELETE FROM users WHERE userId = :id")
    suspend fun deleteById(id: String)

    /**
     * Bulk delete users by IDs.
     */
    @Query("DELETE FROM users WHERE userId IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)

    /**
     * Clear the entire table.
     */
    @Query("DELETE FROM users")
    suspend fun clear()
}
