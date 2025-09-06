package com.example.pillcountingnewmodels.di

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.example.pillcountingnewmodels.core.network.IApplicationSettingInterface
import com.example.pillcountingnewmodels.core.room.dao.UserDao
import com.example.pillcountingnewmodels.feature.forgotPassword.data.remote.IForgotPasswordAPI
import com.example.pillcountingnewmodels.feature.forgotPassword.data.ForgotPasswordRepository
import com.example.pillcountingnewmodels.feature.forgotPassword.domain.data.IForgotPasswordRepository
import com.example.pillcountingnewmodels.feature.login.data.LoginRepository
import com.example.pillcountingnewmodels.feature.login.data.remote.ILoginApi
import com.example.pillcountingnewmodels.feature.login.domain.data.ILoginRepository
import com.example.pillcountingnewmodels.feature.otp.data.VerifyPinRepository
import com.example.pillcountingnewmodels.feature.register.data.RegisterRepository
import com.example.pillcountingnewmodels.feature.register.data.remote.IRegisterAPI
import com.example.pillcountingnewmodels.feature.register.data.remote.IVerifyPinAPI
import com.example.pillcountingnewmodels.feature.register.domain.data.IRegisterRepository
import com.example.pillcountingnewmodels.feature.register.domain.data.IVerifyPinRepository
import com.example.pillcountingnewmodels.feature.settings.data.repository.ApplicationSettingsRepository
import com.example.pillcountingnewmodels.feature.settings.domain.repository.IApplicationSettingsRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Hilt module that provides network-related dependencies for the application.
 *
 * This object is responsible for setting up and configuring Retrofit, OkHttpClient,
 * and the API service interfaces. All dependencies provided here are scoped as singletons
 * to ensure a single, shared instance is used throughout the application's lifecycle.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // TODO: Replace with the actual base URL of your production API.
    private const val BASE_URL = "https://your.api.com/"

    // --- Core Network Setup ---

    /**
     * Provides an [HttpLoggingInterceptor] for logging network request and response bodies.
     * This is an invaluable tool for debugging network issues during development.
     */
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
    }

    /**
     * Provides a singleton instance of [OkHttpClient], the underlying HTTP client for Retrofit.
     * It's configured with essential interceptors for logging and debugging, and standard timeouts.
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        @ApplicationContext context: Context
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor) // Logs network traffic to Logcat.
            .addInterceptor(ChuckerInterceptor(context)) // Provides an in-app UI for inspecting network traffic.
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Provides a Kotlin-aware Moshi instance.
     * The [KotlinJsonAdapterFactory] is essential for Moshi to correctly serialize/deserialize
     * Kotlin data classes, preventing reflection-based runtime crashes.
     */
    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    /**
     * Provides a singleton instance of [Retrofit].
     * It is configured with the base URL, the custom [OkHttpClient], and a Moshi converter
     * that is now built with our Kotlin-aware Moshi instance.
     */
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    // --- API Service Providers ---

    /**
     * Creates and provides an implementation of the [IApplicationSettingInterface] service.
     */
    @Provides
    @Singleton
    fun provideApplicationSettingsApi(retrofit: Retrofit): IApplicationSettingInterface {
        return retrofit.create(IApplicationSettingInterface::class.java)
    }

    /**
     * Creates and provides an implementation of the [ILoginApi] service for authentication.
     */
    @Provides
    @Singleton
    fun provideLoginApi(retrofit: Retrofit): ILoginApi {
        return retrofit.create(ILoginApi::class.java)
    }

    /**
     * Creates and provides an implementation of the [IRegisterAPI] service for user registration.
     */
    @Provides
    @Singleton
    fun provideRegisterApi(retrofit: Retrofit): IRegisterAPI {
        return retrofit.create(IRegisterAPI::class.java)
    }

    /**
     * Creates and provides an implementation of the [IVerifyPinAPI] service for verifying the user registration.
     */
    @Provides
    @Singleton
    fun provideVerifyPinApi(retrofit: Retrofit): IVerifyPinAPI {
        return retrofit.create(IVerifyPinAPI::class.java)
    }

    /**
     * Creates and provides an implementation of the [IVerifyPinAPI] service for verifying the user registration.
     */
    @Provides
    @Singleton
    fun provideForgotPasswordApi(retrofit: Retrofit): IForgotPasswordAPI {
        return retrofit.create(IForgotPasswordAPI::class.java)
    }

    /**
     * Provides the concrete implementation of the [ILoginRepository].
     */
    @Provides
    @Singleton
    fun provideLoginRepository(
        userDao: UserDao,
        loginApi: ILoginApi,
        ioDispatcher: CoroutineDispatcher
    ): ILoginRepository {
        return LoginRepository(
            userDao = userDao,
            loginApi = loginApi,
            ioDispatcher = ioDispatcher
        )
    }

    /**
     * Provides the concrete implementation of the [IRegisterRepository].
     */
    @Provides
    @Singleton
    fun provideRegisterRepository(
        userDao: UserDao,
        registerApi: IRegisterAPI,
        ioDispatcher: CoroutineDispatcher
    ): IRegisterRepository {
        return RegisterRepository(
            userDao = userDao,
            registerApi = registerApi,
            ioDispatcher = ioDispatcher
        )
    }

    /**
     * Provides the concrete implementation of the [IVerifyPinRepository].
     */
    @Provides
    @Singleton
    fun provideVerifyPinRepository(
        verifyPinApi: IVerifyPinAPI,
        ioDispatcher: CoroutineDispatcher
    ): IVerifyPinRepository {
        return VerifyPinRepository(
            verifyPinApi = verifyPinApi,
            ioDispatcher = ioDispatcher
        )
    }

    /**
     * Provides the concrete implementation of the [IForgotPasswordRepository].
     */
    @Provides
    @Singleton
    fun provideForgotPasswordRepository(
        forgotPasswordAPI: IForgotPasswordAPI,
        ioDispatcher: CoroutineDispatcher
    ): IForgotPasswordRepository {
        return ForgotPasswordRepository(
            forgotPasswordAPI = forgotPasswordAPI,
            ioDispatcher = ioDispatcher
        )
    }

    /**
     * Provides the concrete implementation of the [IApplicationSettingsRepository].
     */
    @Provides
    @Singleton
    fun provideApplicationSettingsRepository(
        apiService: IApplicationSettingInterface
    ): IApplicationSettingsRepository {
        return ApplicationSettingsRepository(apiService)
    }
}

