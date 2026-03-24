package com.rite.pillcounting.core.hl7.mllp.client

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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
    private val client: MllpClient,
    private val scope: CoroutineScope,
    private val onFirstConnected: (() -> Unit)? = null,
    private val onConnected: (() -> Unit)? = null,
    private val onDisconnected: (() -> Unit)? = null
) {
    companion object {
        private const val SEND_RETRIES = 2
        private const val RETRY_DELAY_MS = 3_000L
        private const val MAX_RETRY_DELAY_MS = 30_000L  // cap backoff at 30s
        private const val RECONNECT_CHECK_MS = 15_000L
    }

    private val mutex = Mutex()
    private var ip: String = ""
    private var port: Int = 0
    private var isShutdown = false
    private var hasEverConnected = false

    private var readerJob: Job? = null       // passive reader — detects disconnect
    private var reconnectJob: Job? = null

    @Volatile
    private var state: ConnectionState = ConnectionState.Disconnected

    fun getState(): ConnectionState = state
    fun isConnected(): Boolean = state == ConnectionState.Connected

    suspend fun connect(ip: String, port: Int) {
        mutex.withLock {
            this.ip = ip
            this.port = port
            isShutdown = false
        }
        retryConnect()
    }

    suspend fun send(message: String): String {
        if (isShutdown) throw IOException("Shutdown")
        repeat(SEND_RETRIES) { attempt ->
            try {
                if (!isConnected()) retryConnect()
                return client.send(message)
            } catch (e: Exception) {
                handleSendFailure()
                if (attempt == SEND_RETRIES - 1) throw e
                retryConnect()
            }
        }
        error("Unreachable")
    }

    fun startContinuousReconnect() {
        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            while (!isShutdown) {
                if (state == ConnectionState.Disconnected && ip.isNotEmpty()) {
                    try { retryConnect() } catch (_: Exception) {}
                }
                delay(RECONNECT_CHECK_MS)
            }
        }
    }

    fun shutdown() {
        isShutdown = true
        readerJob?.cancel()
        reconnectJob?.cancel()
        scope.launch { client.close() }
        updateState(ConnectionState.Disconnected)
    }

    // ── Private ──────────────────────────────────────────────────────────────

    private fun updateState(newState: ConnectionState) {
        if (state == newState) return
        state = newState
        when (newState) {
            ConnectionState.Connected -> onConnected?.invoke()
            ConnectionState.Disconnected -> onDisconnected?.invoke()
            else -> {}
        }
    }

    private suspend fun retryConnect() {
        if (isShutdown || ip.isEmpty()) return
        updateState(ConnectionState.Connecting)

        var attempt = 0
        while (!isShutdown) {
            try {
                client.connect(ip, port)
                onConnectionEstablished()
                return  // success
            } catch (_: Exception) {
                attempt++
                // Exponential backoff capped at 30s
                val delay = minOf(RETRY_DELAY_MS * attempt, MAX_RETRY_DELAY_MS)
                updateState(ConnectionState.Disconnected)
                delay(delay)
            }
        }
    }

    private fun onConnectionEstablished() {
        updateState(ConnectionState.Connected)

        if (!hasEverConnected) {
            hasEverConnected = true
            onFirstConnected?.invoke()
        }

        // Cancel previous reader if any
        readerJob?.cancel()

        // ✅ Start passive reader — this is what detects disconnect reliably
        readerJob = client.startPassiveReader(
            scope = scope,
            onMessageReceived = { /*
                Server-pushed messages land here if PMS sends unsolicited.
                For request-response (dispense/inventory), send() handles it.
                You can leave this empty or log it.
            */ },
            onDisconnected = {
                if (!isShutdown) {
                    // Fires immediately when PMS drops connection
                    updateState(ConnectionState.Disconnected)
                    scope.launch { client.close() }
                    // reconnectJob loop will pick this up within RECONNECT_CHECK_MS
                }
            }
        )
    }

    private fun handleSendFailure() {
        updateState(ConnectionState.Disconnected)
        readerJob?.cancel()
        scope.launch { client.close() }
    }
}
