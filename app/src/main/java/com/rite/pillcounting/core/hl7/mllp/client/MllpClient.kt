package com.rite.pillcounting.core.hl7.mllp.client

import com.rite.pillcounting.core.hl7.mllp.tls.TlsSocketFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import javax.net.ssl.SSLSocket

class MllpClient(
    private val socketFactory: TlsSocketFactory
) {

    companion object {
        private const val SB: Byte = 0x0B
        private const val EB: Byte = 0x1C
        private const val CR: Byte = 0x0D
    }

    private var socket: SSLSocket? = null
    private var input: InputStream? = null
    private var output: OutputStream? = null
    private val mutex = Mutex()

    suspend fun connect(ip: String, port: Int) = withContext(Dispatchers.IO) {
        mutex.withLock {
            closeInternal()
            socket = socketFactory.createSocket(ip, port)
            input = socket!!.inputStream
            output = socket!!.outputStream
        }
    }

    suspend fun send(message: String): String = withContext(Dispatchers.IO) {
        mutex.withLock {
            require(isConnected()) { "Not connected" }

            output!!.write(wrap(message))
            output!!.flush()

            readResponse()
        }
    }

    fun isConnected(): Boolean =
        socket?.isConnected == true && socket?.isClosed == false

    suspend fun close() = withContext(Dispatchers.IO) {
        mutex.withLock { closeInternal() }
    }

    private fun wrap(msg: String): ByteArray =
        byteArrayOf(SB) + msg.toByteArray() + byteArrayOf(EB, CR)

    private fun readResponse(): String {
        val buffer = ByteArrayOutputStream()
        var started = false

        while (true) {
            val b = input!!.read()
            if (b == -1) error("Connection closed")

            when (b.toByte()) {
                SB -> {
                    started = true
                    buffer.reset()
                }
                EB -> {
                    input!!.read() // CR
                    return buffer.toString(Charsets.UTF_8.name())
                }
                else -> if (started) buffer.write(b)
            }
        }
    }

    private fun closeInternal() {
        try { input?.close() } catch (_: Exception) {}
        try { output?.close() } catch (_: Exception) {}
        try { socket?.close() } catch (_: Exception) {}
        input = null
        output = null
        socket = null
    }
}
