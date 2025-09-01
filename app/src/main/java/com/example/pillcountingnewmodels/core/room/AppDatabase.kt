package com.example.pillcountingnewmodels.core.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.pillcountingnewmodels.core.room.dao.UserDao
import com.example.pillcountingnewmodels.core.room.models.UserEntity


@Database(entities = [UserEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}