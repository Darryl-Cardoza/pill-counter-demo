package com.example.pillcountingnewmodels.core.utils

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

/**
 * A custom Moshi adapter to convert between a hex color String (e.g., "#FF0000")
 * and a Jetpack Compose Color object.
 */
class ColorAdapter {

    @ToJson
    fun toJson(color: Color): String {
        // Convert the Color object to an ARGB hex string
        return String.format("#%08X", color.value.toLong())
    }

    @FromJson
    fun fromJson(hexColor: String): Color {
        // Convert the hex string to an ARGB integer, then to a Color object
        return Color(hexColor.toColorInt())
    }
}