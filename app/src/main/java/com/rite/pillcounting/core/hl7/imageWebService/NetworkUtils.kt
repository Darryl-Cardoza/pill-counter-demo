package com.rite.pillcounting.core.hl7.imageWebService

import java.net.Inet4Address
import java.net.NetworkInterface

object NetworkUtils {
    fun getLocalIpAddress(): String {
        NetworkInterface.getNetworkInterfaces().toList().forEach { iface ->
            iface.inetAddresses.toList().forEach { addr ->
                if (!addr.isLoopbackAddress && addr is Inet4Address) {
                    return addr.hostAddress
                }
            }
        }
        error("No local IP address found")
    }
}

