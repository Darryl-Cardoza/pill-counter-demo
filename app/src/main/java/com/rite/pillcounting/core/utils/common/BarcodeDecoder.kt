package com.rite.pillcounting.core.utils.common

import android.util.Log
import com.rite.pillcounting.feature.barcodeScan.domain.model.BarcodeData
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.pow

/**
 * Injectable GS1 barcode decoder returning a structured BarcodeData model.
 * Uses DateUtils for robust date parsing and formatting.
 */
@Singleton
class BarcodeDecoder @Inject constructor() {

    private val regexPatterns = mapOf(
        "GTIN" to Regex("(?:\\(01\\)|01)(\\d{13,14})"),
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

    fun decode(rawBarcode: String): BarcodeData {
        val cleaned = rawBarcode.replace(Regex("\\](?i)(c1|j1|q3|e0|d2)"), "")

        var gtin: String? = null
        var lot: String? = null
        var serial: String? = null
        var prodDate: LocalDate? = null
        var packDate: LocalDate? = null
        var sellBy: LocalDate? = null
        var expiry: LocalDate? = null
        var netKg: Double? = null
        var grossKg: Double? = null
        var netLb: Double? = null
        var grossLb: Double? = null

        try {
            regexPatterns["GTIN"]?.find(cleaned)?.let { gtin = it.groupValues[1] }
            regexPatterns["LotNumber"]?.find(cleaned)?.let { lot = it.groupValues[1] }
            regexPatterns["SerialNumber"]?.find(cleaned)?.let { serial = it.groupValues[1] }

            prodDate = parseDate(cleaned, "ProductionDate")
            packDate = parseDate(cleaned, "PackingDate")
            sellBy = parseDate(cleaned, "SellByDate")
            expiry = parseDate(cleaned, "ExpirationDate")

            netKg = parseWeight(cleaned, "NetWeightKgs")
            grossKg = parseWeight(cleaned, "GrossWeightKgs")
            netLb = parseWeight(cleaned, "NetWeightPounds")
            grossLb = parseWeight(cleaned, "GrossWeightPounds")
        } catch (e: Exception) {
            Log.e("BarcodeDecoder", "Failed to decode GS1: ${e.message}")
        }

        return BarcodeData(
            gtin = gtin,
            lotNumber = lot,
            serialNumber = serial,
            productionDate = prodDate,
            packingDate = packDate,
            sellByDate = sellBy,
            expirationDate = expiry,
            netWeightKg = netKg,
            grossWeightKg = grossKg,
            netWeightLb = netLb,
            grossWeightLb = grossLb
        )
    }

    private fun parseDate(barcode: String, key: String): LocalDate? {
        val pattern = regexPatterns[key] ?: return null
        val raw = pattern.find(barcode)?.groupValues?.get(1) ?: return null
        return DateUtils.parseBarcodeDate(raw)
    }

    private fun parseWeight(barcode: String, key: String): Double? {
        val pattern = regexPatterns[key] ?: return null
        val match = pattern.find(barcode) ?: return null
        val identifier = match.value.take(4)
        val value = match.groups[1]?.value?.toIntOrNull() ?: return null
        val decimals = identifier.last().digitToIntOrNull() ?: 0
        val divisor = 10.0.pow(decimals.toDouble())
        return value / divisor
    }
}
