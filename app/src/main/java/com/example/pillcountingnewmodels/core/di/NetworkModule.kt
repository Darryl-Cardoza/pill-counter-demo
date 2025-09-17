package com.example.pillcountingnewmodels.di

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.example.pillcountingnewmodels.core.api.HeaderInterceptor
import com.example.pillcountingnewmodels.core.network.IApplicationSettingInterface
import com.example.pillcountingnewmodels.core.room.dao.UserDao
import com.example.pillcountingnewmodels.core.utils.ColorAdapter
import com.example.pillcountingnewmodels.core.utils.PreferenceHelper
import com.example.pillcountingnewmodels.feature.barcodeScan.data.remote.IDrugAPI
import com.example.pillcountingnewmodels.feature.barcodeScan.data.repository.DrugRepository
import com.example.pillcountingnewmodels.feature.barcodeScan.domain.repository.IDrugRepository
import com.example.pillcountingnewmodels.feature.dashboard.data.UserDetailRepository
import com.example.pillcountingnewmodels.feature.dashboard.data.remote.IUserDetailAPI
import com.example.pillcountingnewmodels.feature.dashboard.domain.data.IUserDetailRepository
import com.example.pillcountingnewmodels.feature.forgotPassword.data.ForgotPasswordRepository
import com.example.pillcountingnewmodels.feature.forgotPassword.data.remote.IForgotPasswordAPI
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
    private const val MAIN_API_BASE_URL = "http://192.168.0.79:8000/"
//    private const val MAIN_API_BASE_URL = "http://192.168.0.23:8000/"

    /** The base URL for the openFDA API. */
    const val DRUG_API_BASE_URL = "https://api.fda.gov/"

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
            .addInterceptor(HeaderInterceptor()) // Custom header interceptor
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
            .add(ColorAdapter())
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    /**
     * Provides a singleton instance of [Retrofit] for the main application API.
     */
    @Provides
    @Singleton
    @MainApi
    fun provideMainRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit {
        return Retrofit.Builder()
            .baseUrl(MAIN_API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    /**
     * Provides a singleton instance of [Retrofit] specifically for the Drug (FDA) API.
     */
    @Provides
    @Singleton
    @DrugApiQualifier
    fun provideDrugRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit {
        return Retrofit.Builder()
            .baseUrl(DRUG_API_BASE_URL)
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
    fun provideApplicationSettingsApi(@MainApi retrofit: Retrofit): IApplicationSettingInterface {
        return retrofit.create(IApplicationSettingInterface::class.java)
    }

    /**
     * Creates and provides an implementation of the [ILoginApi] service for authentication.
     */
    @Provides
    @Singleton
    fun provideLoginApi(@MainApi retrofit: Retrofit): ILoginApi {
        return retrofit.create(ILoginApi::class.java)
    }

    /**
     * Creates and provides an implementation of the [IRegisterAPI] service for user registration.
     */
    @Provides
    @Singleton
    fun provideRegisterApi(@MainApi retrofit: Retrofit): IRegisterAPI {
        return retrofit.create(IRegisterAPI::class.java)
    }

    /**
     * Creates and provides an implementation of the [IVerifyPinAPI] service for verifying the user registration.
     */
    @Provides
    @Singleton
    fun provideVerifyPinApi(@MainApi retrofit: Retrofit): IVerifyPinAPI {
        return retrofit.create(IVerifyPinAPI::class.java)
    }

    /**
     * Creates and provides an implementation of the [IForgotPasswordAPI] service for verifying the user registration.
     */
    @Provides
    @Singleton
    fun provideForgotPasswordApi(@MainApi retrofit: Retrofit): IForgotPasswordAPI {
        return retrofit.create(IForgotPasswordAPI::class.java)
    }

    /**
     * Creates and provides an implementation of the [IUserDetailAPI] service for getting the user details.
     */
    @Provides
    @Singleton
    fun provideUserDetailsApi(@MainApi retrofit: Retrofit): IUserDetailAPI {
        return retrofit.create(IUserDetailAPI::class.java)
    }

    /**
     * Creates and provides an implementation of the [IDrugAPI] service for verifying the user registration.
     */
    @Provides
    @Singleton
    fun provideDrugApi(@DrugApiQualifier retrofit: Retrofit): IDrugAPI {
        return retrofit.create(IDrugAPI::class.java)
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
     * Provides the concrete implementation of the [IForgotPasswordRepository].
     */
    @Provides
    @Singleton
    fun provideUserDetailRepository(
        api: IUserDetailAPI,
        applicationSettingApi: IApplicationSettingInterface,
        preferenceHelper: PreferenceHelper,
        ioDispatcher: CoroutineDispatcher
    ): IUserDetailRepository {
        return UserDetailRepository(
            api = api,
            applicationSettingApi = applicationSettingApi,
            preferenceHelper = preferenceHelper,
            ioDispatcher = ioDispatcher
        )
    }

    /**
     * Provides the concrete implementation of the [IDrugRepository].
     */
    @Provides
    @Singleton
    fun provideDrugRepository(
        drugApi: IDrugAPI
    ): IDrugRepository {
        return DrugRepository(
            api = drugApi
        )
    }

    /**
     * Provides the concrete implementation of the [IApplicationSettingsRepository].
     */
    @Provides
    @Singleton
    fun provideApplicationSettingsRepository(
        apiService: IApplicationSettingInterface,
        preferenceHelper: PreferenceHelper
    ): IApplicationSettingsRepository {
        return ApplicationSettingsRepository(
            apiService = apiService,
            preferenceHelper = preferenceHelper
        )
    }
}

