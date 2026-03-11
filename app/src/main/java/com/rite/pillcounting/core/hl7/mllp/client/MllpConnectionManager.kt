package com.rite.pillcounting.core.hl7.mllp.client

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.IOException

/**
 * High-level MLLP connection manager.
 *
 * Adds:
 * - Retry logic
 * - Auto reconnect
 * - Safe shutdown
 * - Connection state tracking
 */
class MllpConnectionManager(
    private val client: MllpClient
) {

    private var ip: String = ""
    private var port: Int = 0
    private val mutex = Mutex()
    private var isShutdown = false

    companion object {
        private const val CONNECT_RETRIES = 3
        private const val SEND_RETRIES = 2
        private const val RETRY_DELAY_MS = 2000L
    }

    suspend fun connect(ip: String, port: Int) {
        mutex.withLock {
            this.ip = ip
            this.port = port
            isShutdown = false
        }
        retryConnect()
    }

    suspend fun send(message: String): String {
        if (isShutdown) {
            throw IOException("Connection manager is shutdown")
        }

        repeat(SEND_RETRIES) { attempt ->
            try {
                if (!client.isConnected()) {
                    retryConnect()
                }

                val ack = client.send(message)

                return ack

            } catch (e: Exception) {
                if (attempt == SEND_RETRIES - 1) throw e
                retryConnect()
            }
        }
        error("Unreachable")
    }


    fun isConnected(): Boolean = !isShutdown && client.isConnected()

    fun shutdown() {
        CoroutineScope(Dispatchers.IO).launch {
            mutex.withLock {
                isShutdown = true
                client.close()
            }
        }
    }

    private suspend fun retryConnect() {
        if (isShutdown) {
            throw IOException("Connection manager is shutdown")
        }

        repeat(CONNECT_RETRIES) { attempt ->
            try {
                client.connect(ip, port)
                return
            } catch (e: Exception) {
                if (attempt < CONNECT_RETRIES - 1) {
                    delay(RETRY_DELAY_MS * (attempt + 1))
                } else {
                    throw IOException("Unable to connect to $ip:$port after $CONNECT_RETRIES attempts", e)
                }
            }
        }
    }


    private fun validateAck(ackRaw: String) {
        // Fast safety check
        require(ackRaw.contains("|ACK|")) {
            "Invalid ACK message"
        }

        val msaLine = ackRaw
            .lines()
            .firstOrNull { it.startsWith("MSA|") }
            ?: error("ACK missing MSA segment")

        val fields = msaLine.split("|")
        val ackCode = fields.getOrNull(1)
            ?: error("ACK missing code")

        val originalMessageId = fields.getOrNull(2)

        when (ackCode) {
            "AA" -> {
            }
            "AE", "AR" -> {
                throw IOException("HL7 NACK received ($ackCode) for messageId=$originalMessageId")
            }
            else -> {
                throw IOException("Unknown ACK code: $ackCode")
            }
        }
    }

}