package com.example.pillcountingnewmodels.core.room.dao

import androidx.room.*
import com.example.pillcountingnewmodels.core.room.models.UserEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for managing [UserEntity] persistence.
 *
 * This DAO enforces **localId stability**:
 * - `localId` is the Room primary key and the **only FK** exposed to other tables.
 * - `userId` is a unique business identifier (from server/JWT) but is **not** a FK.
 * - Once assigned, a `localId` is stable across app restarts, syncs, or logins.
 *
 * ### Professional Practices
 * - Never use `OnConflictStrategy.REPLACE` → it destroys PKs and breaks FKs.
 * - Always update in place (`update`) to preserve `localId`.
 * - Use `@Transaction` for compound upsert logic.
 * - Use Flow return types for reactive UI updates.
 */
@Dao
interface UserDao {

    /* ────────────────────────── Insert / Update ────────────────────────── */

    /**
     * Inserts a new [UserEntity].
     *
     * - Ignores insert if a duplicate `userId` already exists.
     * - Returns the new rowId (localId) if inserted, or `-1` if ignored.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(user: UserEntity): Long

    /**
     * Updates an existing [UserEntity] (matched on PK = localId).
     *
     * - Only updates non-null fields that are set in [user].
     * - Use [upsertPreservingLocalId] for safe upsert.
     */
    @Update
    suspend fun update(user: UserEntity)

    /**
     * Upserts a [UserEntity] while preserving `localId`.
     *
     * - If the user already exists (matched by `userId`), it reuses its `localId` and updates the row.
     * - If the user does not exist, it inserts a new record (auto-generating a `localId`).
     * - Returns the stable `localId` for the user.
     */
    @Transaction
    suspend fun upsertPreservingLocalId(user: UserEntity): Long {
        val existing = getByUserId(user.userId)
        return if (existing != null) {
            val toSave = user.copy(localId = existing.localId)
            update(toSave)
            existing.localId
        } else {
            insertIgnore(user).let { newId ->
                if (newId == -1L) {
                    // Race condition: inserted by another coroutine → fetch again
                    getByUserId(user.userId)?.localId
                        ?: throw IllegalStateException("User insert failed unexpectedly")
                } else {
                    newId
                }
            }
        }
    }

    /**
     * Bulk upsert with preservation of [localId] stability.
     * Existing users are updated, new users are inserted.
     */
    @Transaction
    suspend fun upsertAllPreservingLocalId(users: List<UserEntity>): List<Long> {
        return users.map { upsertPreservingLocalId(it) }
    }

    /* ─────────────────────────────── Reads ─────────────────────────────── */

    /**
     * Retrieves a user by their localId (Room PK).
     */
    @Query("SELECT * FROM users WHERE localId = :localId LIMIT 1")
    suspend fun getByLocalId(localId: Long): UserEntity?

    /**
     * Observes a user by localId (Room PK).
     */
    @Query("SELECT * FROM users WHERE localId = :localId LIMIT 1")
    fun observeByLocalId(localId: Long): Flow<UserEntity?>

    /**
     * Retrieves a user by their **business id** (`userId`).
     */
    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    suspend fun getByUserId(userId: String): UserEntity?

    /**
     * Observes a user by `userId`.
     * Emits updates whenever the row changes.
     */
    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    fun observeByUserId(userId: String): Flow<UserEntity?>

    /**
     * Retrieves all users ordered by creation timestamp (newest first).
     */
    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    suspend fun getAll(): List<UserEntity>

    /**
     * Observes all users ordered by creation timestamp (newest first).
     */
    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<UserEntity>>

    /**
     * Finds a user by email.
     */
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): UserEntity?

    /**
     * Finds a user by phone number.
     */
    @Query("SELECT * FROM users WHERE phoneNumber = :phone LIMIT 1")
    suspend fun findByPhone(phone: String): UserEntity?

    /**
     * Performs a case-insensitive search by name or email.
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

    /* ─────────────────────────────── Counts ─────────────────────────────── */

    /**
     * Counts total number of users in the table.
     */
    @Query("SELECT COUNT(*) FROM users")
    suspend fun countAll(): Int

    /**
     * Checks if a user exists for the given `userId`.
     */
    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE userId = :userId)")
    suspend fun exists(userId: String): Boolean

    /* ────────────────────────────── Deletes ────────────────────────────── */

    /**
     * Deletes a user by `userId`.
     */
    @Query("DELETE FROM users WHERE userId = :userId")
    suspend fun deleteByUserId(userId: String)

    /**
     * Bulk delete users by `userId`s.
     */
    @Query("DELETE FROM users WHERE userId IN (:userIds)")
    suspend fun deleteByUserIds(userIds: List<String>)

    /**
     * Clears the entire table.
     *
     * ⚠️ Use with caution: this will break FKs in dependent tables.
     * Prefer marking users as inactive instead.
     */
    @Query("DELETE FROM users")
    suspend fun clear()
}
