package com.rite.pillcounting.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Provides the I/O dispatcher for background operations.
     * Hilt will use this to inject a [CoroutineDispatcher] where requested.
     */
    @Provides
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}
