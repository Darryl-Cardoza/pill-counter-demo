package com.rite.pillcounting.core.hl7.hl7MessageHandler.builder.inventory

import org.rite.hl7.hl7.domain.model.InventoryData
import com.rite.pillcounting.core.hl7.hl7MessageHandler.domain.utils.HL7Utils
import com.rite.pillcounting.core.hl7.hl7MessageHandler.domain.utils.HL7Utils.buildComponent



fun buildEQU(
    inventory: InventoryData,
    hl7Version: String = "2.5"
): String {

    // EQU-1: Equipment Instance Identifier
    val equipmentId = buildComponent(
        inventory.equipmentId,
        inventory.equipmentIdNamespace ?: "",
        inventory.equipmentType ?: ""
    )

    /**
     * Canonical EQU fields (EQU-1 → EQU-6)
     */
    val allFields = listOf(
        equipmentId,                    // EQU-1
        inventory.eventDateTime ?: "",  // EQU-2
        inventory.equipmentState ?: "", // EQU-3
        "",                             // EQU-4
        "",                             // EQU-5
        inventory.equipmentName ?: ""   // EQU-6
    )

    val maxField = EquVersionCapabilities.maxField(hl7Version)

    return HL7Utils.buildSegment(
        "EQU",
        *allFields.take(maxField).toTypedArray()
    )
}


object EquVersionCapabilities {
    fun maxField(version: String): Int = 6
}
