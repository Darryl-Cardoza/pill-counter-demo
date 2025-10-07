package com.example.pillcountingnewmodels.feature.register.di

import com.example.pillcountingnewmodels.core.room.dao.UserDao
import com.example.pillcountingnewmodels.feature.register.data.RegisterRepository
import com.example.pillcountingnewmodels.feature.register.data.remote.IRegisterAPI
import com.example.pillcountingnewmodels.feature.register.domain.data.IRegisterRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * Dagger Hilt module responsible for providing all dependencies required
 * for the **User Registration** feature.
 *
 * This module defines how instances of the registration-related API and
 * repository are created and shared within the application's dependency graph.
 * It encapsulates the setup for making registration requests, managing user
 * data, and handling the associated business logic.
 *
 * All dependencies provided here are scoped as singletons to ensure that
 * one shared instance exists throughout the application lifecycle.
 *
 * @see IRegisterAPI
 * @see IRegisterRepository
 * @see RegisterRepository
 */
@Module
@InstallIn(SingletonComponent::class)
object RegisterModule {

    /**
     * Provides a singleton instance of the [IRegisterAPI].
     *
     * This interface defines the Retrofit API endpoints related to user
     * registration — including account creation, validation, and submission
     * of registration data to the backend service.
     *
     * @param retrofit The [Retrofit] instance used to create the API implementation.
     * @return A concrete implementation of [IRegisterAPI].
     */
    @Provides
    @Singleton
    fun provideRegisterApi(
        retrofit: Retrofit
    ): IRegisterAPI =
        retrofit.create(IRegisterAPI::class.java)

    /**
     * Provides a singleton instance of the [IRegisterRepository].
     *
     * The repository acts as an intermediary between the data layer
     * (local database and remote API) and the domain layer, managing
     * registration requests and persistence of user data.
     *
     * @param userDao The [UserDao] used for interacting with the local database.
     * @param registerApi The [IRegisterAPI] used for performing network registration calls.
     * @param ioDispatcher The [CoroutineDispatcher] used to run I/O operations off the main thread.
     * @return A concrete implementation of [IRegisterRepository].
     */
    @Provides
    @Singleton
    fun provideRegisterRepository(
        userDao: UserDao,
        registerApi: IRegisterAPI,
        ioDispatcher: CoroutineDispatcher
    ): IRegisterRepository =
        RegisterRepository(
            userDao = userDao,
            registerApi = registerApi,
            ioDispatcher = ioDispatcher
        )
}
