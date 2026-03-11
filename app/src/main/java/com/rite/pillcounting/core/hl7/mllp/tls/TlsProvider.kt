package com.rite.pillcounting.core.hl7.mllp.tls

import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.*

object TlsProvider {

    private val TLS_PROTOCOLS = arrayOf("TLSv1.2", "TLSv1.3")

    /**
     * TLS context that encrypts but trusts ANY server certificate.
     */
    fun createInsecureClientContext(): SSLContext {

        val trustAllManager = object : X509TrustManager {
            override fun checkClientTrusted(
                chain: Array<X509Certificate>,
                authType: String
            ) = Unit

            override fun checkServerTrusted(
                chain: Array<X509Certificate>,
                authType: String
            ) = Unit

            override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
        }

        return SSLContext.getInstance("TLS").apply {
            init(
                null, // no client cert
                arrayOf<TrustManager>(trustAllManager),
                SecureRandom()
            )
        }
    }

    fun configureClientSocket(socket: SSLSocket) {
        socket.enabledProtocols = TLS_PROTOCOLS

        socket.enabledCipherSuites =
            socket.supportedCipherSuites.filter {
                it.contains("AES") && it.contains("GCM")
            }.toTypedArray()

        // Disable hostname verification (IP-based HL7)
        socket.sslParameters = socket.sslParameters.apply {
            endpointIdentificationAlgorithm = null
        }

        socket.soTimeout = 30_000
    }
}
