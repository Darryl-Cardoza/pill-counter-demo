package com.example.pillcountingnewmodels.feature.barcodeScan.di

import android.content.Context
import com.example.pillcountingnewmodels.feature.barcodeScan.presentation.analyzer.BarcodeAnalyzer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object ScannerModule {

    @Provides
    @Singleton
    fun provideBarcodeAnalyzer(
        @ApplicationContext context: Context
    ): BarcodeAnalyzer = BarcodeAnalyzer(context)
}
