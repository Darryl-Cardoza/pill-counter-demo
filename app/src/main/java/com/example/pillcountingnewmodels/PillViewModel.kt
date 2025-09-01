package com.example.pillcountingnewmodels.viewmodel

import android.app.Application
import android.content.res.AssetFileDescriptor
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

/**
 * A sealed interface to represent the different states of the UI, particularly
 * concerning the TFLite model initialization.
 */
sealed interface UiState {
    /** The model is currently being loaded. */
    object Loading : UiState
    /** The model has been successfully loaded and the app is ready. */
    object Ready : UiState
    /** An error occurred during model initialization. */
    data class Error(val message: String) : UiState
}

/**
 * Manages the UI state and business logic for the pill counting feature.
 *
 * This ViewModel is responsible for:
 * - Safely initializing the TFLite [Interpreter] in the background.
 * - Exposing the overall UI state ([UiState]) to the composable UI.
 * - Holding and updating the pill count and analysis mode (live vs. static).
 * - Ensuring resources like the interpreter are properly closed when the ViewModel is cleared.
 */
class PillViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "PillViewModel"
        private const val MODEL_FILENAME = "best_float32.tflite"
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _pillCount = MutableStateFlow(0)
    val pillCount: StateFlow<Int> = _pillCount.asStateFlow()

    private val _isLiveAnalyzing = MutableStateFlow(true)
    val isLiveAnalyzing: StateFlow<Boolean> = _isLiveAnalyzing.asStateFlow()

    // The interpreter is now private and nullable, managed by the UiState.
    private var interpreter: Interpreter? = null

    init {
        // Initialize the interpreter asynchronously as soon as the ViewModel is created.
        initializeInterpreter()
    }

    /**
     * Loads the TFLite model from assets and initializes the interpreter on a background thread.
     * Updates the [uiState] to reflect the loading progress, success, or failure.
     */
    private fun initializeInterpreter() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = UiState.Loading
            try {
                val modelBuffer = loadModelFile(MODEL_FILENAME)
                val options = Interpreter.Options().apply {
                    setUseXNNPACK(true) // Enable hardware acceleration delegate
                    numThreads = 4     // Optimize for multi-core processors
                }
                interpreter = Interpreter(modelBuffer, options)
                _uiState.value = UiState.Ready
                Log.i(TAG, "✅ Interpreter initialized successfully.")
            } catch (e: Exception) {
                val errorMessage = "Failed to initialize TFLite interpreter."
                Log.e(TAG, "❌ $errorMessage", e)
                _uiState.value = UiState.Error(errorMessage)
            }
        }
    }

    /**
     * Provides the interpreter instance safely. Throws an exception if the interpreter
     * is not ready, indicating a programming error.
     *
     * @return The initialized [Interpreter] instance.
     * @throws IllegalStateException if called before the interpreter is ready.
     */
    fun getInterpreter(): Interpreter {
        check(_uiState.value is UiState.Ready && interpreter != null) {
            "Interpreter is not ready or has been cleared."
        }
        return interpreter!!
    }

    fun updatePillCount(count: Int) {
        _pillCount.value = count
    }

    fun toggleLiveAnalyzing() {
        _isLiveAnalyzing.update { !it }
    }

    /**
     * Maps the TFLite model file from the assets folder into a MappedByteBuffer.
     */
    private fun loadModelFile(modelFilename: String): MappedByteBuffer {
        val fileDescriptor: AssetFileDescriptor = getApplication<Application>().assets.openFd(modelFilename)
        FileInputStream(fileDescriptor.fileDescriptor).use { inputStream ->
            val fileChannel = inputStream.channel
            return fileChannel.map(
                FileChannel.MapMode.READ_ONLY,
                fileDescriptor.startOffset,
                fileDescriptor.declaredLength
            )
        }
    }

    /**
     * Called when the ViewModel is no longer used and will be destroyed.
     * Ensures the TFLite interpreter is closed to release native resources.
     */
    override fun onCleared() {
        super.onCleared()
        interpreter?.close()
        interpreter = null
        Log.d(TAG, "Interpreter resources have been released.")
    }
}