package com.rite.pillcounting.core.hl7.hl7MessageHandler.domain.utils

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.rite.hl7.hl7.domain.utils.HL7Constants
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter


object HL7Utils {

    private val mutex = Mutex()
    private var counter: Int = 0
    private const val MAX_COUNTER = 999

    /**
     * Generate unique HL7 Message Control ID (MSH-10)
     */
    suspend fun generateMessageControlId(): String =
        mutex.withLock {
            val timestamp = formatTimestamp()
            counter = if (counter >= MAX_COUNTER) 0 else counter + 1
            timestamp + counter.toString().padStart(3, '0')
        }

    /**
     * Current HL7 timestamp (UTC)
     */
    fun formatTimestamp(): String =
        formatInstant(Instant.now())

    /**
     * HL7 timestamp from epoch millis
     */
    fun formatTimestamp(timeMillis: Long): String =
        formatInstant(Instant.ofEpochMilli(timeMillis))

    /**
     * CMP-safe HL7 timestamp formatter
     * Uses java.time internally
     */


    private val HL7_FORMATTER: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyyMMddHHmmss")

    /**
     * CMP-safe HL7 timestamp formatter
     */
    private fun formatInstant(instant: Instant): String {
        val dt = LocalDateTime.ofInstant(instant, ZoneOffset.UTC)
        return dt.format(HL7_FORMATTER)
    }

    /**
     * Escape HL7 special characters
     */
    fun escapeHL7Text(text: String?): String {
        if (text.isNullOrEmpty()) return ""
        return text
            .replace("\\", "\\E\\")
            .replace("|", "\\F\\")
            .replace("^", "\\S\\")
            .replace("&", "\\T\\")
            .replace("~", "\\R\\")
    }

    fun buildField(value: String?): String = value ?: ""

    fun buildComponent(vararg parts: String?): String =
        parts.joinToString(HL7Constants.COMPONENT_SEPARATOR) { it ?: "" }

    fun buildSegment(segmentType: String, vararg fields: String): String =
        buildString {
            append(segmentType)
            append(HL7Constants.FIELD_SEPARATOR)
            append(fields.joinToString(HL7Constants.FIELD_SEPARATOR))
        }
}
