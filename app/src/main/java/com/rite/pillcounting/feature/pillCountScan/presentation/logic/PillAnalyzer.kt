package com.rite.pillcounting.feature.pillCountScan.presentation.logic

import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.ImageProxy
import org.tensorflow.lite.Interpreter

/**
 * Main class that ties together preprocessing, inference, and postprocessing.
 */
class PillAnalyzer(
    private val interpreter: Interpreter,
    private val viewWidth: Int,
    private val viewHeight: Int,
    private val onPillCountUpdated: (
        Int,
        List<Postprocessor.Detection>,
        Bitmap,
        Matrix
    ) -> Unit
) {
    companion object {
        private const val TAG = "PillAnalyzer"
    }

    private var lastTransformationMatrix: Matrix? = null

    fun getLastTransformationMatrix(): Matrix? = lastTransformationMatrix?.let { Matrix(it) }

    fun analyze(imageProxy: ImageProxy) {
        var bitmap: Bitmap? = null
        try {
            Log.d(TAG, "--- Start Analysis ---")

            // 🧩 Step 1: Preprocessing
            val preprocessStart = System.currentTimeMillis()
            val (inputBuffer, bmp) = Preprocessor.preprocess(imageProxy)
            bitmap = bmp
            val preprocessEnd = System.currentTimeMillis()
            Log.d(TAG, "🧠 Preprocessing time: ${preprocessEnd - preprocessStart} ms")

            // 🧩 Step 2: Model inference
            val detShape = interpreter.getOutputTensor(0).shape()
            val out0 = Array(1) { Array(detShape[1]) { FloatArray(detShape[2]) } }

            val maskShape = interpreter.getOutputTensor(1).shape()
            val out1 = Array(1) { Array(maskShape[1]) { Array(maskShape[2]) { FloatArray(maskShape[3]) } } }

            val outputs = mutableMapOf<Int, Any>(
                0 to out0,
                1 to out1
            )

            val inferenceStart = System.currentTimeMillis()
            interpreter.runForMultipleInputsOutputs(arrayOf(inputBuffer), outputs)
            val inferenceEnd = System.currentTimeMillis()

            val inferenceTime = inferenceEnd - inferenceStart
            Log.d(TAG, "⚡ Model inference time: $inferenceTime ms")

            // 🧩 Step 3: Postprocessing
            val postStart = System.currentTimeMillis()
            val (detections, matrix) = Postprocessor.parseDetections(
                det = out0[0],
                imageWidth = imageProxy.width,
                imageHeight = imageProxy.height,
                rotationDegrees = imageProxy.imageInfo.rotationDegrees,
                viewWidth = viewWidth,
                viewHeight = viewHeight
            )
            val postEnd = System.currentTimeMillis()
            Log.d(TAG, "📊 Postprocessing time: ${postEnd - postStart} ms")

            // 🧩 Total frame time
            val totalTime = postEnd - preprocessStart
            Log.d(TAG, "⏱️ Total frame analysis time: $totalTime ms")

            lastTransformationMatrix = matrix
            onPillCountUpdated(detections.size, detections, bitmap, matrix)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception in analyze: ${e.message}", e)
            bitmap?.recycle()
        } finally {
            imageProxy.close()
        }
    }
}
