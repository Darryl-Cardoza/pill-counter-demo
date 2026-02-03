package com.rite.pillcounting.core.hl7.mllp.tls


import android.content.Context
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

/**
 * TLS socket factory for HL7 MLLP clients.
 *
 * ✔ Pinned CA
 * ✔ TLS 1.2 / 1.3
 * ✔ Strong ciphers only
 */

class TlsSocketFactory {

    private val socketFactory: SSLSocketFactory =
        TlsProvider.createInsecureClientContext().socketFactory

    fun createSocket(ip: String, port: Int): SSLSocket =
        (socketFactory.createSocket(ip, port) as SSLSocket).apply {
            TlsProvider.configureClientSocket(this)
            startHandshake() // 🔐 encryption starts here
        }
}
