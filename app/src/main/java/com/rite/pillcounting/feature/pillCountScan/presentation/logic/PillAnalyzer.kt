package com.rite.pillcounting.feature.pillCountScan.presentation.logic

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.ImageProxy
import com.rite.pillcounting.core.utils.logger.AppLogger
import org.tensorflow.lite.Interpreter

class PillAnalyzer(
    private val interpreter: Interpreter,
    private val onPillCountUpdated: (
        pillCount: Int,
        detections: List<Detection>,
        debugBitmap: Bitmap,
        transformMatrix: Matrix,
        imageWidth: Int,
        imageHeight: Int
    ) -> Unit
) {

    private val logger = AppLogger("PillAnalyzer")

    fun analyze(imageProxy: ImageProxy) {
        val overallStart = System.currentTimeMillis()
        var letterboxedBitmap: Bitmap? = null

        logger.i(
            """
            🟢 [PillAnalyzer] Frame received
            Camera image size = ${imageProxy.width} x ${imageProxy.height}
            Rotation          = ${imageProxy.imageInfo.rotationDegrees}
            """.trimIndent()
        )

        try {
            // --------------------------------------------------
            // STEP 1: PREPROCESS
            // --------------------------------------------------
            val preprocessStart = System.currentTimeMillis()

            val (inputBuffer, bitmap640) =
                ImagePreprocessor.preprocess(imageProxy)

            letterboxedBitmap = bitmap640

            Letterbox.currentScaleInfo?.let {
                logger.i(
                    """
                    📐 [Letterbox]
                    scale = ${it.scale}
                    padX  = ${it.padX}
                    padY  = ${it.padY}
                    input = ${it.inputSize}x${it.inputSize}
                    """.trimIndent()
                )
            }

            logger.i(
                """
                ✅ [Preprocess]
                Time   = ${System.currentTimeMillis() - preprocessStart} ms
                Bitmap = ${bitmap640.width} x ${bitmap640.height}
                """.trimIndent()
            )

            // --------------------------------------------------
            // STEP 2: MODEL INFERENCE
            // --------------------------------------------------
            val inferStart = System.currentTimeMillis()

            val outputShape = interpreter.getOutputTensor(0).shape()
            logger.i(
                """
                📦 [Model]
                Output tensor shape = ${outputShape.contentToString()}
                """.trimIndent()
            )

            val output =
                Array(1) { Array(outputShape[1]) { FloatArray(outputShape[2]) } }

            interpreter.run(inputBuffer, output)

            logger.i(
                "⚡ [Inference] Time = ${System.currentTimeMillis() - inferStart} ms"
            )

            // --------------------------------------------------
            // STEP 3: SPLIT OUTPUT
            // --------------------------------------------------
            val raw = output[0]
            val numAnchors = raw[0].size
            logger.i("🔍 [Decode] Raw predictions count = ${raw.size}")

            val coords = Array(numAnchors) { FloatArray(4) }
            val conf = Array(numAnchors) { FloatArray(1) }

            raw.take(5).forEachIndexed { i, row ->
                logger.d(
                    "Raw[$i] cx=${row[0]}, cy=${row[1]}, w=${row[2]}, h=${row[3]}, conf=${row[4]}"
                )
            }

            for (i in 0 until numAnchors) {
                coords[i][0] = raw[0][i] // cx
                coords[i][1] = raw[1][i] // cy
                coords[i][2] = raw[2][i] // w
                coords[i][3] = raw[3][i] // h
                conf[i][0] = raw[4][i] // confidence
            }

            // --------------------------------------------------
            // STEP 4: POSTPROCESS
            // --------------------------------------------------
            val postStart = System.currentTimeMillis()

            val scaleInfo = Letterbox.currentScaleInfo
                ?: throw IllegalStateException("Letterbox scale info missing")

            val detections = Postprocessor.decode(
                coords = coords,
                conf = conf,
                confThreshold = 0.70f,
                scale = scaleInfo.scale,
                padX = scaleInfo.padX,
                padY = scaleInfo.padY
            )

            logger.i(
                """
                🎯 [Postprocess]
                Time            = ${System.currentTimeMillis() - postStart} ms
                Final detections = ${detections.size}
                """.trimIndent()
            )

            // -----------------------------------------------------
            // STEP 5: NMS (same as iOS)
            // -----------------------------------------------------
            val finalDetections = NMS.run(
                detections = detections,
                iouThreshold = 0.80f
            )

            logger.i(
                "✂️ [NMS] final = ${finalDetections.size}"
            )

            // --------------------------------------------------
            // STEP 6: CALLBACK
            // --------------------------------------------------
            logger.i(
                """
                🧮 [Result]
                FINAL COUNT = ${detections.size}
                Total frame time = ${System.currentTimeMillis() - overallStart} ms
                """.trimIndent()
            )

            onPillCountUpdated(
                finalDetections.size,
                finalDetections,
                letterboxedBitmap,
                Matrix(),
                imageProxy.width,
                imageProxy.height
            )

        } catch (e: Exception) {
            logger.e("❌ [PillAnalyzer] Frame analysis failed", e)
            letterboxedBitmap?.recycle()
        } finally {
            imageProxy.close()
        }
    }
}
