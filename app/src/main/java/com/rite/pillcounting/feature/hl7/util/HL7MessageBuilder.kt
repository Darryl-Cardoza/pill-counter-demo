package com.rite.pillcounting.feature.hl7.util


import android.os.Build
import com.rite.pillcounting.core.hl7.hl7MessageHandler.domain.model.ObservationData
import com.rite.pillcounting.core.hl7.imageWebService.NetworkUtils
import com.rite.pillcounting.core.room.models.PillCountTxnDetailsEntity
import com.rite.pillcounting.core.room.models.PillCountTxnEntity
import org.rite.hl7.hl7.domain.model.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * =========================================================
 * HL7MessageBuilder
 * =========================================================
 *
 * Responsibility:
 * - Build HL7 messages (RDS / INU)
 *
 * This class:
 * - Does NOT send messages
 * - Does NOT access database
 * - Does NOT use coroutines
 */
object HL7MessageBuilder {

    private const val IMAGE_PORT = 8443

    /* =========================================================
     * DISPENSE (RDS O13)
     * ========================================================= */

    @Suppress("SimpleDateFormat")
    fun buildDispenseMessage(
        txn: PillCountTxnEntity,
        txnDetails: List<PillCountTxnDetailsEntity>,
        drugCode: String,
        drugName: String,
        pharmacistId: String?,
        pharmacistName: String?,
        location: String?
    ): CompleteHL7Message {

        val now = now()
        val totalCount = txnDetails.sumOf { it.pillCount ?: 0 }

        val imageObx = buildImageObx(
            txnDetails,
            observationId = "DISP_IMG",
            label = "Dispense Image"
        )

        return CompleteHL7Message(
            messageId = System.currentTimeMillis().toString(),
            messageType = "RDS",
            triggerEvent = "O13",
            timestamp = now,
            sendingFacility = "PillCounter-${Build.MODEL}",

            header = buildHeader("RDS", "O13", now),

            patient = PatientData(
                patientId = txn.rxNo ?: txn.txnId.toString()
            ),

            order = OrderData(
                orderControl = "RE",
                orderStatus = "CM",
                placerOrderId = txn.rxNo ?: txn.txnId.toString()
            ),

            dispenses = listOf(
                DispenseData(
                    dispenseSubId = "1",
                    drugCode = drugCode,
                    drugName = drugName,
                    drugCodeSystem = "NDC",
                    dateTimeDispensed = now,
                    quantityDispensed = totalCount.toString(),
                    unitCode = "TAB",
                    unitText = "Tablets",
                    prescriptionNumber = txn.rxNo ?: txn.txnId.toString(),
                    pharmacistId = pharmacistId,
                    pharmacistGivenName = pharmacistName,
                    deliverToLocation = location,
                    dispensingNotes = txn.note,
                    lotNumber = txn.lotNo,
                    expirationDate = txn.expiry
                )
            ),

            obxSegments = imageObx,

            notes = buildCommonNotes(txn, totalCount)
        )
    }

    /* =========================================================
     * INVENTORY (INU U05)
     * ========================================================= */

    fun buildInventoryMessage(
        txn: PillCountTxnEntity,
        txnDetails: List<PillCountTxnDetailsEntity>,
        drugCode: String,
        drugName: String
    ): CompleteHL7Message {

        val now = now()
        val totalCount = txnDetails.sumOf { it.pillCount ?: 0 }

        val imageObx = buildImageObx(
            txnDetails,
            observationId = "INV_IMG",
            label = "Inventory Image"
        )

        val inventory = InventoryData(
            equipmentId = "ROBOT1",
            eventDateTime = now,
            bins = listOf(
                InventoryBinData(
                    substanceId = drugCode,
                    substanceName = drugName,
                    quantityOnHand = totalCount.toString(),
                    availableQuantity = totalCount.toString(),
                    quantityUnitCode = "TAB",
                    quantityUnitText = "Tablets",
                    expirationDate = txn.expiry,
                    lotNumber = txn.lotNo
                )
            )
        )

        return CompleteHL7Message(
            messageId = System.currentTimeMillis().toString(),
            messageType = "INU",
            triggerEvent = "U05",
            timestamp = now,
            sendingFacility = "PillCounter-${Build.MODEL}",

            header = buildHeader("INU", "U05", now),

            inventory = inventory,
            obxSegments = imageObx,

            notes = buildCommonNotes(txn, totalCount)
        )
    }

    /* =========================================================
     * SHARED HELPERS
     * ========================================================= */

    private fun buildImageObx(
        details: List<PillCountTxnDetailsEntity>,
        observationId: String,
        label: String
    ): List<ObservationData> {

        val localIp = NetworkUtils.getLocalIpAddress()

        return details.mapIndexedNotNull { index, detail ->
            detail.imagePath?.let { path ->
                ObservationData(
                    setId = (index + 1).toString(),
                    valueType = "RP",
                    observationId = observationId,
                    observationText = "$label ${index + 1}",
                    observationValue = buildRoomImageUrl(
                        localIp?:"",
                        path.substringAfterLast("/"),
                        IMAGE_PORT.toString()
                    ),
                    resultStatus = "F"
                )
            }
        }
    }

    private fun buildHeader(type: String, trigger: String, time: String) =
        MessageHeaderData(
            fieldSeparator = "|",
            encodingCharacters = "^~\\&",
            sendingApplication = "PillCounter",
            sendingFacility = "ROBOT",
            receivingApplication = "PMS",
            receivingFacility = "PHARMACY",
            messageType = type,
            triggerEvent = trigger,
            messageControlId = System.currentTimeMillis().toString(),
            processingId = "P",
            versionId = "2.5",
            messageDateTime = time
        )

    private fun buildCommonNotes(
        txn: PillCountTxnEntity,
        totalCount: Int
    ) = listOf(
        NoteData("1", "L", "Transaction completed"),
        NoteData("2", "L", "Total Count: $totalCount"),
        NoteData("3", "L", "Transaction Id: ${txn.txnId}")
    )

    private fun now(): String =
        SimpleDateFormat("yyyyMMddHHmmss", Locale.US).format(Date())

    private fun buildRoomImageUrl(deviceIp: String, fileName: String, port: String): String {
        return "https://$deviceIp:$port/images/$fileName"
    }
}