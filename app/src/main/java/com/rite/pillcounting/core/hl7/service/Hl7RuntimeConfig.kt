package com.rite.pillcounting.core.hl7.service

import android.os.Build

/**
 * Configuration data class for HL7 service initialization
 */
data class HL7Config(
    val serverPort: Int = 2575,
    val autoResponseDelayMs: Long = 10_000L,
    val nsdBroadcastServiceName: String = "PillCounter-${Build.MODEL}" ,
    val nsdBroadcastType: String = "_pillcounting._tcp",
    val nsdDiscoveryType: String ="_ritepmsserver._tcp",
    val imageServicePort: Int = 8080,
    val imageServiceSecurePort: Int = 8443
)