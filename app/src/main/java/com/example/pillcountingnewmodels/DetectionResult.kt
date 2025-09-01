package com.example.pillcountingnewmodels

/**
 * A data class to hold the corner coordinates of a detected bounding box.
 * All coordinates are normalized to the range [0.0, 1.0].
 *
 * @property x1 The left edge of the bounding box.
 * @property y1 The top edge of the bounding box.
 * @property x2 The right edge of the bounding box.
 * @property y2 The bottom edge of the bounding box.
 */
data class DetectionResult(
    val x1: Float,
    val y1: Float,
    val x2: Float,
    val y2: Float
)
