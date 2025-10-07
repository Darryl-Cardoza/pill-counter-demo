package com.example.pillcountingnewmodels.feature.forgotPassword.di

import com.example.pillcountingnewmodels.feature.forgotPassword.data.ForgotPasswordRepository
import com.example.pillcountingnewmodels.feature.forgotPassword.data.remote.IForgotPasswordAPI
import com.example.pillcountingnewmodels.feature.forgotPassword.domain.data.IForgotPasswordRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * Dagger Hilt module that provides all dependencies required for the
 * **Forgot Password** feature of the application.
 *
 * This module defines how to construct and provide the [IForgotPasswordAPI]
 * (Retrofit service interface) and the [IForgotPasswordRepository]
 * (data repository) which manage the password recovery process.
 *
 * These dependencies handle network communication, token validation,
 * and user password reset workflows.
 *
 * All dependencies here are scoped as singletons to ensure that only
 * one instance exists across the application's lifecycle.
 *
 * @see IForgotPasswordAPI
 * @see IForgotPasswordRepository
 * @see ForgotPasswordRepository
 */
@Module
@InstallIn(SingletonComponent::class)
object ForgotPasswordModule {

    /**
     * Provides a singleton instance of the [IForgotPasswordAPI].
     *
     * This API interface defines the Retrofit endpoints for initiating
     * password recovery requests such as verifying user credentials,
     * sending reset links or codes, and confirming password resets.
     *
     * @param retrofit The [Retrofit] instance used to generate the API implementation.
     * @return A concrete implementation of [IForgotPasswordAPI].
     */
    @Provides
    @Singleton
    fun provideForgotPasswordApi(
        retrofit: Retrofit
    ): IForgotPasswordAPI =
        retrofit.create(IForgotPasswordAPI::class.java)

    /**
     * Provides a singleton instance of the [IForgotPasswordRepository].
     *
     * The repository serves as the abstraction layer between the
     * [IForgotPasswordAPI] and the domain layer. It manages all
     * network operations related to password recovery and ensures
     * that the process is executed on the proper coroutine dispatcher.
     *
     * @param api The [IForgotPasswordAPI] instance for performing network requests.
     * @param ioDispatcher The [CoroutineDispatcher] used for executing I/O-bound operations.
     * @return A concrete implementation of [IForgotPasswordRepository].
     */
    @Provides
    @Singleton
    fun provideForgotPasswordRepository(
        api: IForgotPasswordAPI,
        ioDispatcher: CoroutineDispatcher
    ): IForgotPasswordRepository =
        ForgotPasswordRepository(
            forgotPasswordAPI = api,
            ioDispatcher = ioDispatcher
        )
}
