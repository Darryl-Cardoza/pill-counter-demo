package com.example.pillcountingnewmodels.core.utils

import com.example.pillcountingnewmodels.core.utils.DateUtils.convertToDate

/**
 * Universal GS1 Barcode Decoder.
 * Automatically extracts all known data fields and returns them as a key-value map.
 *
 * @author
 *   Stratgik | Buildnetic R&D
 */
object GS1Decoder {

    private val regexPatterns = mapOf(
        "GTIN" to Regex("(?:\\(01\\)|01)(\\d{13})"),
        "LotNumber" to Regex("(?:\\(10\\)|10)([\\w\\d]{1,20})"),
        "SerialNumber" to Regex("(?:\\(21\\)|21)([\\w\\d]{1,20})"),
        "ProductionDate" to Regex("(?:\\(11\\)|11)(\\d{6})"),
        "PackingDate" to Regex("(?:\\(13\\)|13)(\\d{6})"),
        "SellByDate" to Regex("(?:\\(15\\)|15)(\\d{6})"),
        "ExpirationDate" to Regex("(?:\\(17\\)|17)(\\d{6})"),
        "NetWeightKgs" to Regex("(?:\\(310[0-4]\\)|310[0-4])(\\d{6})"),
        "GrossWeightKgs" to Regex("(?:\\(330[0-4]\\)|330[0-4])(\\d{6})"),
        "NetWeightPounds" to Regex("(?:\\(320[0-4]\\)|320[0-4])(\\d{6})"),
        "GrossWeightPounds" to Regex("(?:\\(340[0-4]\\)|340[0-4])(\\d{6})")
    )

    /**
     * Decodes a given GS1 barcode and returns all extracted fields.
     * @param rawBarcode the input GS1 barcode string
     * @return Map<String, String> with decoded key-value pairs
     */
    fun decode(rawBarcode: String): Map<String, String> {
        // Clean GS1 escape sequences
        val barcode = rawBarcode.replace(Regex("\\](?i)(c1|j1|q3|e0|d2)"), "")

        val result = mutableMapOf<String, String>()

        for ((key, pattern) in regexPatterns) {
            val match = pattern.find(barcode)
            var value = match?.groups?.get(1)?.value ?: ""

            when (key) {
                "ExpirationDate", "ProductionDate", "PackingDate", "SellByDate" -> {
                    if (value.isNotEmpty()) value = convertToDate(value)
                }
                "NetWeightKgs", "GrossWeightKgs", "NetWeightPounds", "GrossWeightPounds" -> {
                    if (value.isNotEmpty()) value = calculateWeight(match)
                }
            }

            if (value.isNotEmpty()) result[key] = value
        }

        return result
    }

    /**
     * Utility function to calculate weights.
     */
    private fun calculateWeight(match: MatchResult?): String {
        if (match == null) return ""
        val identifier = match.value.take(4)
        val value = match.groups[1]?.value?.toIntOrNull() ?: return ""
        val decimals = identifier.last().digitToIntOrNull() ?: 0
        val divisor = Math.pow(10.0, decimals.toDouble())
        return (value / divisor).toString()
    }
}
