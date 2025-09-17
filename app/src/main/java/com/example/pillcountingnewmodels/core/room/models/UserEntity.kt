package com.example.pillcountingnewmodels.core.room.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a user in the system, persisted locally in Room.
 *
 * Mirrors the API user payload (excluding auth token) and adds a
 * local incremental ID for device-side needs. `userId` is the PK.
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val userId: String,
    @ColumnInfo(defaultValue = "0")
    val localId: Long = 0L,

    val email: String? = null,
    val name: String? = null,
    val phoneNumber: String? = null,
    val avatarUrl: String? = null,
    val role: String? = null,

    @ColumnInfo(defaultValue = "0")
    val isVerified: Boolean = false,

    val language: String? = null,
    val timezone: String? = null,
    val theme: String? = null,
    val fontSize: String? = null,
    val notifications: Boolean? = null,
    val experimental: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
