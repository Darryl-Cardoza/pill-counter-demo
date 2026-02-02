package com.rite.pillcounting.core.hl7.hl7MessageHandler.parser.inventory

import org.rite.hl7.hl7.domain.model.InventoryData

/**
 * Parses inventory equipment and associated inventory bins.
 * Combines EQU (equipment) with related INV (bin) segments.
 */
fun parseInventory(
    segments: Map<String, List<List<String>>>,
    compSep: String
): InventoryData? {

    /** Retrieve the first EQU segment; inventory requires equipment context **/
    val equ = segments["EQU"]?.firstOrNull() ?: return null

    /** Parsed equipment identifier components from EQU-1 **/
    val equipParts = equ.getOrElse(1) { "" }.split(compSep)

    /** Build and return parsed inventory data **/
    return InventoryData(
        equipmentId = equipParts.getOrNull(0) ?: "",
        equipmentIdNamespace = equipParts.getOrNull(1)?.takeIf { it.isNotBlank() },
        eventDateTime = equ.getOrNull(2)?.takeIf { it.isNotBlank() },
        equipmentState = equ.getOrNull(3)?.takeIf { it.isNotBlank() },
        equipmentName = equ.getOrNull(3)?.takeIf { it.isNotBlank() },
        equipmentType = equipParts.getOrNull(2)?.takeIf { it.isNotBlank() },

        /** Parse all INV segments belonging to this equipment **/
        bins = (segments["INV"] ?: emptyList()).map { inv ->
            parseInventoryBin(inv, compSep)
        }
    )
}
