package com.rite.pillcounting

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.rite.pillcounting.feature.pillCountScan.domain.PillDetectionModelLoader
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class PillCountingApplication : Application() {

    @Inject
    lateinit var modelLoader: PillDetectionModelLoader

    // Create a scope that lives as long as the application process
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

        // Pre-load the TensorFlow model in the background immediately
        applicationScope.launch {
            Log.i("LoadModel", "Application onCreate: Triggering background model load...")
            try {
                // This call is thread-safe thanks to the Mutex in the loader.
                // It will decrypt the model and init the GPU delegate now.
                modelLoader.getOrLoadInterpreter()
                Log.i("LoadModel", "Application onCreate: Model pre-loading complete!")
            } catch (e: Exception) {
                Log.e("LoadModel", "Application onCreate: Model pre-loading failed", e)
            }
        }
    }
}