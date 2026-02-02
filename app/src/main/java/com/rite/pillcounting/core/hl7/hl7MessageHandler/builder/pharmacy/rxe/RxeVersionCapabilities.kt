package com.rite.pillcounting.core.hl7.hl7MessageHandler.builder.pharmacy.rxe

object RxeVersionCapabilities {

    fun maxField(version: String): Int =
        when {
            version.startsWith("2.1") -> 14
            version.startsWith("2.2") -> 16
            version.startsWith("2.3") -> 18
            version.startsWith("2.4") -> 20
            else -> 21
        }
}
