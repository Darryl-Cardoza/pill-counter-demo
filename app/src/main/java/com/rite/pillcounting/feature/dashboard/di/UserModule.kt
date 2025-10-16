package com.rite.pillcounting.feature.dashboard.di

import com.rite.pillcounting.core.settings.data.remote.IApplicationSettingInterface
import com.rite.pillcounting.core.utils.preference.PreferenceHelper
import com.rite.pillcounting.feature.dashboard.data.UserDetailRepository
import com.rite.pillcounting.feature.dashboard.data.remote.IUserDetailAPI
import com.rite.pillcounting.feature.dashboard.domain.data.IUserDetailRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * Dagger Hilt module responsible for providing all dependencies required for the
 * **User Dashboard / User Details** feature of the application.
 *
 * This module defines how instances of the [IUserDetailAPI] (Retrofit API service)
 * and [IUserDetailRepository] (data repository) are constructed and injected.
 * These dependencies handle retrieval and synchronization of user-specific data,
 * including account details, app configurations, and settings.
 *
 * All dependencies here are scoped as singletons to ensure a shared instance
 * throughout the application's lifecycle.
 *
 * @see IUserDetailAPI
 * @see IUserDetailRepository
 * @see UserDetailRepository
 */
@Module
@InstallIn(SingletonComponent::class)
object UserModule {

    /**
     * Provides a singleton instance of the [IUserDetailAPI].
     *
     * This API interfaceDetail defines the Retrofit endpoints responsible for fetching
     * user details, preferences, and other account-related data from the backend.
     *
     * @param retrofit The [Retrofit] instance used to create the API implementation.
     * @return A concrete implementation of [IUserDetailAPI].
     */
    @Provides
    @Singleton
    fun provideUserDetailApi(
        retrofit: Retrofit
    ): IUserDetailAPI =
        retrofit.create(IUserDetailAPI::class.java)

    /**
     * Provides a singleton instance of the [IUserDetailRepository].
     *
     * The repository acts as the bridge between remote APIs ([IUserDetailAPI]),
     * application configuration ([IApplicationSettingInterface]), and local
     * preferences ([PreferenceHelper]). It manages user-related data flow,
     * caching, and synchronization, ensuring separation of concerns and
     * maintainability.
     *
     * @param api The [IUserDetailAPI] used for retrieving user details from the backend.
     * @param applicationSettingApi The [IApplicationSettingInterface] used for app-level configuration data.
     * @param preferenceHelper The [PreferenceHelper] used for caching and persistent storage of user data.
     * @param ioDispatcher The [CoroutineDispatcher] used for executing I/O-bound operations off the main thread.
     * @return A concrete implementation of [IUserDetailRepository].
     */
    @Provides
    @Singleton
    fun provideUserDetailRepository(
        api: IUserDetailAPI,
        applicationSettingApi: IApplicationSettingInterface,
        preferenceHelper: PreferenceHelper,
        ioDispatcher: CoroutineDispatcher
    ): IUserDetailRepository =
        UserDetailRepository(
            api = api,
            applicationSettingApi = applicationSettingApi,
            preferenceHelper = preferenceHelper,
            ioDispatcher = ioDispatcher
        )
}
