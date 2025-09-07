package com.example.pillcountingnewmodels.di

import javax.inject.Qualifier

/**
 * Qualifier to identify the Retrofit instance for the main application API.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MainApi

/**
 * Qualifier to identify the Retrofit instance specifically for the Drug/FDA API.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DrugApiQualifier
