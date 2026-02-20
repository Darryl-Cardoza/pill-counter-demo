package com.rite.pillcounting.feature.pillCountScan.domain

import android.content.Context
import android.util.Log // Import Android Log
import com.rite.pillcounting.core.security.ModelDecryptor
import com.rite.pillcounting.core.utils.logger.AppLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
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

@Singleton
class PillDetectionModelLoader @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val logger = AppLogger("PillModelLoader")
    private val mutex = Mutex()

    // The single instance of the interpreter and delegate
    private var interpreter: Interpreter? = null
    private var gpuDelegate: GpuDelegate? = null

    companion object {
        private const val MODEL_FILENAME = "modelpilldetection"
        private const val TAG = "LoadModel" // Define your tag here
    }

    /**
     * Returns the existing interpreter or initializes a new one if it doesn't exist.
     * Thread-safe.
     */
    suspend fun getOrLoadInterpreter(): Interpreter {
        // 1. Log every time the function is called
        Log.i(TAG, "Request received: getOrLoadInterpreter()")

        return mutex.withLock {
            // 2. Check if it exists and log if we are skipping initialization
            if (interpreter != null) {
                Log.i(TAG, "Model ALREADY loaded. Returning singleton instance.")
                return@withLock interpreter!!
            }

            // 3. Log that we are actually starting the heavy work
            Log.i(TAG, " Model NOT found. Starting initialization (Decrypt + Load)...")
            logger.i("Initializing TensorFlow Interpreter (Singleton)...")

            return@withLock withContext(Dispatchers.IO) { // Added return@withLock
                try {
                    // 1. Decrypt Model
                    val buffer = loadModelFile()
                    val options = Interpreter.Options()

                    // 2. Setup GPU Delegate (Must be done on Main Thread usually)
                    var createdDelegate: GpuDelegate? = null

                    withContext(Dispatchers.Main) {
                        val compatList = CompatibilityList()
                        if (compatList.isDelegateSupportedOnThisDevice) {
                            try {
                                val delegateOptions = compatList.bestOptionsForThisDevice
                                createdDelegate = GpuDelegate(delegateOptions)
                                options.addDelegate(createdDelegate)
                                logger.i("GPU delegate initialized.")
                                Log.i(TAG, "GPU Delegate created.")
                            } catch (e: Exception) {
                                logger.w("GPU delegate failed, falling back to CPU.", e)
                                createdDelegate?.close()
                                createdDelegate = null
                            }
                        }
                    }

                    // 3. Setup CPU Fallback
                    if (createdDelegate == null) {
                        options.setUseXNNPACK(true)
                        options.numThreads =
                            Runtime.getRuntime().availableProcessors().coerceAtMost(4)
                        Log.i(TAG, "Using CPU fallback.")
                    }

                    // 4. Create Interpreter
                    val newInterpreter = Interpreter(buffer, options)

                    gpuDelegate = createdDelegate
                    interpreter = newInterpreter

                    logger.i("Interpreter initialized successfully.")
                    // 4. Log final success
                    Log.i(TAG, "Interpreter initialized and saved to singleton.")

                    return@withContext newInterpreter

                } catch (e: Exception) {
                    logger.e("Failed to initialize interpreter", e)
                    Log.e(TAG, "Model initialization FAILED: ${e.message}")
                    throw e
                }
            }
        }
    }

    private fun loadModelFile(): ByteBuffer {
        val encFile = File(context.filesDir, "$MODEL_FILENAME.enc")

        if (!encFile.exists()) {
            context.assets.open("$MODEL_FILENAME.enc").use { input ->
                encFile.outputStream().use { output -> input.copyTo(output) }
            }
        }

        val decryptedBytes = ModelDecryptor.decryptToBytes(encFile)
        return ByteBuffer.allocateDirect(decryptedBytes.size).apply {
            put(decryptedBytes)
            rewind()
        }
    }

    fun close() {
        interpreter?.close()
        interpreter = null
        gpuDelegate?.close()
        gpuDelegate = null
        logger.i("Model resources released.")
        Log.i(TAG, "🗑️ Model resources released and cleared.")
    }
}