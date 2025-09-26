package com.example.pillcountingnewmodels.core.utils

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

fun String.toColor(): Color = Color(this.toColorInt())

fun Long?.toFormattedDate(): String {
    return if (this != null && this > 0) {
        try {
            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a")
                .withLocale(Locale.getDefault())
                .withZone(ZoneId.systemDefault())
            formatter.format(Instant.ofEpochMilli(this))
        } catch (e: Exception) {
            "-"
        }
    } else "-"
}