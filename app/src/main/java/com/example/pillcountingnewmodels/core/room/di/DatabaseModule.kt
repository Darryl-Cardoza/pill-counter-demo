package com.example.pillcountingnewmodels.core.room.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.pillcountingnewmodels.core.room.AppDatabase
import com.example.pillcountingnewmodels.core.room.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        val db = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "pill_counting_db"
        ).addCallback(object : RoomDatabase.Callback() { //to be removed later
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)

                    // Insert 2 manual records into "users" table
                    db.execSQL(
                        "INSERT INTO users (userId, name, email, role, language, createdAt) " +
                                "VALUES ('u1', 'Pravin', 'pravin@example.com', 'admin', 'en', strftime('%s','now')*1000)"
                    )

                    db.execSQL(
                        "INSERT INTO users (userId, name, email, role, language, createdAt) " +
                                "VALUES ('u2', 'Alex', 'alex@example.com', 'member', 'fr', strftime('%s','now')*1000)"
                    )
                }
            })
            .build()

        return db
    }

    @Provides
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao()
}