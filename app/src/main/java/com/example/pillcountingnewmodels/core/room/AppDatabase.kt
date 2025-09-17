package com.example.pillcountingnewmodels.core.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.pillcountingnewmodels.core.room.dao.DrugMasterDao
import com.example.pillcountingnewmodels.core.room.dao.PillCountTxnDao
import com.example.pillcountingnewmodels.core.room.dao.PillCountTxnDetailsDao
import com.example.pillcountingnewmodels.core.room.dao.UserDao
import com.example.pillcountingnewmodels.core.room.models.DrugMasterEntity
import com.example.pillcountingnewmodels.core.room.models.PillCountTxnDetailsEntity
import com.example.pillcountingnewmodels.core.room.models.PillCountTxnEntity
import com.example.pillcountingnewmodels.core.room.models.UserEntity

/**
 * Main Room database for the application.
 *
 * This database defines all the core entities and their DAOs used across
 * the pill counting application.
 *
 * ### Entities:
 * - [UserEntity] → Represents application users.
 * - [DrugMasterEntity] → Master list of drugs with NDC and metadata.
 * - [PillCountTxnEntity] → Pill count transaction headers (parent records).
 * - [PillCountTxnDetailsEntity] → Pill count transaction details (child records).
 *
 * ### Usage:
 * Obtain an instance of [AppDatabase] via `Room.databaseBuilder()` and use
 * the exposed DAO getters to interact with persistent data.
 *
 * ### Notes:
 * - Increase the [version] number and provide a migration strategy when making
 *   schema changes.
 * - `exportSchema = true` ensures schema history is exported for versioning.
 */
@Database(
    entities = [
        UserEntity::class,
        DrugMasterEntity::class,
        PillCountTxnEntity::class,
        PillCountTxnDetailsEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    /** DAO for accessing [UserEntity] records. */
    abstract fun userDao(): UserDao

    /** DAO for managing [DrugMasterEntity] records. */
    abstract fun drugMasterDao(): DrugMasterDao

    /** DAO for managing [PillCountTxnEntity] transaction headers. */
    abstract fun pillCountTxnDao(): PillCountTxnDao

    /** DAO for managing [PillCountTxnDetailsEntity] transaction details. */
    abstract fun pillCountTxnDetailsDao(): PillCountTxnDetailsDao
}
