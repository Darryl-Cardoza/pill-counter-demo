package com.rite.pillcounting.feature.pillCountScan.domain

import android.content.Context
import android.util.Log
import com.rite.pillcounting.core.security.ModelDecryptor
import com.rite.pillcounting.core.utils.logger.AppLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.gpu.GpuDelegate
import java.io.File
import java.nio.ByteBuffer
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Holds both loaded interpreters after [PillDetectionModelLoader.getOrLoadInterpreters] completes.
 */
data class LoadedModels(
    val pillInterpreter: Interpreter,
    val trayInterpreter: Interpreter
)

@Singleton
class PillDetectionModelLoader @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val logger = AppLogger("PillModelLoader")
    private val mutex = Mutex()

    // Singleton interpreter instances
    private var pillInterpreter: Interpreter? = null
    private var trayInterpreter: Interpreter? = null

    // GPU delegates (one per interpreter; GPU delegate is NOT thread-safe across instances)
    private var pillGpuDelegate: GpuDelegate? = null
    private var trayGpuDelegate: GpuDelegate? = null

    companion object {
        private const val PILL_MODEL_FILENAME = "pillcountingmodel"
        private const val TRAY_MODEL_FILENAME = "traymodel"
        private const val TAG = "LoadModel"
    }

    // ------------------------------------------------------------------
    // PUBLIC API
    // ------------------------------------------------------------------

    /**
     * Returns both interpreters, loading them in parallel if not yet initialised.
     * Thread-safe via [Mutex].
     */
    suspend fun getOrLoadInterpreters(): LoadedModels {
        Log.i(TAG, "getOrLoadInterpreters() called")

        return mutex.withLock {
            // Fast-path: both already loaded
            val existingPill = pillInterpreter
            val existingTray = trayInterpreter
            if (existingPill != null && existingTray != null) {
                Log.i(TAG, "Both models already loaded — returning singletons")
                return@withLock LoadedModels(existingPill, existingTray)
            }

            Log.i(TAG, "One or both models missing — loading now (parallel)…")
            logger.i("Loading pill + tray models in parallel…")

            // Load both models concurrently on IO, then create delegates on Main
            withContext(Dispatchers.IO) {
                coroutineScope {
                    val pillBufferDeferred = async { loadModelFile(PILL_MODEL_FILENAME) }
                    val trayBufferDeferred = async { loadModelFile(TRAY_MODEL_FILENAME) }

                    val pillBuffer = pillBufferDeferred.await()
                    val trayBuffer = trayBufferDeferred.await()

                    Log.i(TAG, "Both model buffers decrypted — setting up delegates")

                    // GPU delegate setup must happen on Main thread
                    var pillDelegate: GpuDelegate? = null
                    var trayDelegate: GpuDelegate? = null

                    withContext(Dispatchers.Main) {
                        val compatList = CompatibilityList()
                        if (compatList.isDelegateSupportedOnThisDevice) {
                            try {
                                pillDelegate = GpuDelegate(compatList.bestOptionsForThisDevice)
                                Log.i(TAG, "Pill GPU delegate created")
                            } catch (e: Exception) {
                                logger.w("Pill GPU delegate failed", e)
                                pillDelegate?.close()
                                pillDelegate = null
                            }

                            try {
                                trayDelegate = GpuDelegate(compatList.bestOptionsForThisDevice)
                                Log.i(TAG, "Tray GPU delegate created")
                            } catch (e: Exception) {
                                logger.w("Tray GPU delegate failed", e)
                                trayDelegate?.close()
                                trayDelegate = null
                            }
                        }
                    }

                    // Build pill interpreter options
                    val pillOptions = Interpreter.Options().apply {
                        if (pillDelegate != null) {
                            addDelegate(pillDelegate)
                        } else {
                            setUseXNNPACK(true)
                            numThreads = Runtime.getRuntime().availableProcessors().coerceAtMost(4)
                            Log.i(TAG, "Pill model — CPU fallback")
                        }
                    }

                    // Build tray interpreter options
                    val trayOptions = Interpreter.Options().apply {
                        if (trayDelegate != null) {
                            addDelegate(trayDelegate)
                        } else {
                            setUseXNNPACK(true)
                            numThreads = Runtime.getRuntime().availableProcessors().coerceAtMost(4)
                            Log.i(TAG, "Tray model — CPU fallback")
                        }
                    }

                    val newPill = Interpreter(pillBuffer, pillOptions)
                    val newTray = Interpreter(trayBuffer, trayOptions)

                    // Persist singletons
                    pillGpuDelegate = pillDelegate
                    trayGpuDelegate = trayDelegate
                    pillInterpreter = newPill
                    trayInterpreter = newTray

                    Log.i(TAG, "Both interpreters initialised successfully")
                    logger.i("Pill + Tray interpreters ready")

                    LoadedModels(newPill, newTray)
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // PRIVATE HELPERS
    // ------------------------------------------------------------------

    private fun loadModelFile(modelName: String): ByteBuffer {
        val encFile = File(context.filesDir, "$modelName.enc")

        if (!encFile.exists()) {
            context.assets.open("$modelName.enc").use { input ->
                encFile.outputStream().use { output -> input.copyTo(output) }
            }
        }

        val decryptedBytes = ModelDecryptor.decryptToBytes(encFile)
        return ByteBuffer.allocateDirect(decryptedBytes.size).apply {
            put(decryptedBytes)
            rewind()
        }
    }

    // ------------------------------------------------------------------
    // CLEANUP
    // ------------------------------------------------------------------

    fun close() {
        pillInterpreter?.close()
        trayInterpreter?.close()
        pillGpuDelegate?.close()
        trayGpuDelegate?.close()

        pillInterpreter = null
        trayInterpreter = null
        pillGpuDelegate = null
        trayGpuDelegate = null

        logger.i("All model resources released")
        Log.i(TAG, "All model resources released and cleared")
    }
}