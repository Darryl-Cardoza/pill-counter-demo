package com.example.pillcountingnewmodels.core.room.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.intellij.lang.annotations.Language

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val userId: String,
    val name: String,
    val email: String?,
    val role: String?,
    val language: String?,
    val createdAt: Long = System.currentTimeMillis()
)