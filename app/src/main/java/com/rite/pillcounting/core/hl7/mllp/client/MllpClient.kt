package com.rite.pillcounting.core.hl7.mllp.client

import com.rite.pillcounting.core.hl7.mllp.tls.TlsSocketFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException
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
        private const val READ_TIMEOUT_MS = 0        // 0 = infinite (server pushes to us)
    }

    private var socket: SSLSocket? = null
    private var input: InputStream? = null
    private var output: OutputStream? = null
    private val mutex = Mutex()

    suspend fun connect(ip: String, port: Int) = withContext(Dispatchers.IO) {
        mutex.withLock {
            closeInternal()
            val sock = socketFactory.createSocket(ip, port)

            //  TCP-level keepalive — OS sends probe packets automatically
            // Detects dead connections without any application-level ping
            sock.keepAlive = true

            //  No read timeout — we wait for server to push messages
            // Disconnect is detected when read() returns -1 or throws
            sock.soTimeout = READ_TIMEOUT_MS

            socket = sock
            input = sock.inputStream
            output = sock.outputStream
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

    /**
     * Passive liveness check — no I/O, just inspects socket state.
     * Real disconnect detection happens in startPassiveReader().
     */
    fun isConnected(): Boolean =
        socket?.let { !it.isClosed && it.isConnected } ?: false

    /**
     * Starts a coroutine that blocks on read().
     * When PMS closes connection, read() returns -1 immediately.
     * This is the ONLY reliable way to detect disconnect in MLLP.
     *
     * Returns the job so caller can cancel it on shutdown.
     */
    fun startPassiveReader(
        scope: CoroutineScope,
        onMessageReceived: (String) -> Unit,
        onDisconnected: () -> Unit
    ): Job = scope.launch(Dispatchers.IO) {
        try {
            val stream = input ?: run { onDisconnected(); return@launch }
            val buffer = ByteArrayOutputStream()
            var started = false

            while (true) {
                val b = stream.read()  // blocks until data or disconnect

                if (b == -1) {
                    // Clean TCP close from PMS
                    onDisconnected()
                    return@launch
                }

                when (b.toByte()) {
                    SB -> { started = true; buffer.reset() }
                    EB -> {
                        stream.read() // CR
                        if (started) onMessageReceived(buffer.toString(Charsets.UTF_8.name()))
                        started = false
                    }
                    else -> if (started) buffer.write(b)
                }
            }
        } catch (_: Exception) {
            // Socket closed, timeout, or reset — all mean disconnected
            onDisconnected()
        }
    }

    suspend fun close() = withContext(Dispatchers.IO) {
        mutex.withLock { closeInternal() }
    }

    private fun wrap(msg: String): ByteArray =
        byteArrayOf(SB) + msg.toByteArray() + byteArrayOf(EB, CR)

    // Keep this for one-shot sends that need a response (dispense/inventory)
    private fun readResponse(): String {
        val buffer = ByteArrayOutputStream()
        var started = false
        while (true) {
            val b = input!!.read()
            if (b == -1) throw IOException("Connection closed by server")
            when (b.toByte()) {
                SB -> { started = true; buffer.reset() }
                EB -> {
                    input!!.read()
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
        input = null; output = null; socket = null
    }
}