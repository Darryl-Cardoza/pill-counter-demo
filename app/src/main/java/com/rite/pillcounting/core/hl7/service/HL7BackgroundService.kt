package com.rite.pillcounting.core.hl7.service

import ImageWebServer
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.rite.pillcounting.core.hl7.core.Hl7EventListener
import com.rite.pillcounting.core.hl7.hl7MessageHandler.builder.HL7MessageBuilder
import com.rite.pillcounting.core.hl7.hl7MessageHandler.builder.toTypedHL7String
import com.rite.pillcounting.core.hl7.hl7MessageHandler.parser.Hl7Parser
import com.rite.pillcounting.core.hl7.hl7MessageHandler.parser.generateMessageIdempotencyKey
import com.rite.pillcounting.core.hl7.imageWebService.NetworkUtils
import com.rite.pillcounting.core.hl7.mllp.client.MllpClient
import com.rite.pillcounting.core.hl7.mllp.client.MllpConnectionManager
import com.rite.pillcounting.core.hl7.mllp.nsd.NsdHelper
import com.rite.pillcounting.core.hl7.mllp.nsd.NetworkIpMonitor
import com.rite.pillcounting.core.hl7.mllp.server.MllpServer
import com.rite.pillcounting.core.hl7.mllp.tls.TlsSocketFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.rite.hl7.hl7.AckDecision
import org.rite.hl7.hl7.domain.model.CompleteHL7Message


/**
 * Foreground Android Service responsible for running the complete HL7 runtime.
 *
 * Responsibilities:
 * - Load HL7 configuration from Intent extras at startup
 * - Start and manage MLLP server and client connections
 * - Register and broadcast HL7 service via NSD
 * - Parse incoming HL7 messages and emit callbacks
 * - Send automated responses (ACK / RDS)
 * - Maintain foreground notification to prevent background termination
 *
 * This service is designed to survive process death and OS restarts.
 */
class HL7Service : Service() {
    companion object {
        private const val TAG = "HL7Service"
        private const val CHANNEL_ID = "hl7_bg"
        private const val NOTIFICATION_ID = 7001
    }

    /** HL7 runtime configuration.
     * Initialized from Intent extras during service startup.
     */
    private var config: HL7Config = HL7Config()

    /** Coroutine scope bound to service lifecycle **/
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** Core components **/
    private lateinit var server: MllpServer
    private lateinit var clientManager: MllpConnectionManager
    private lateinit var nsdHelper: NsdHelper

    private lateinit var networkIpMonitor: NetworkIpMonitor

    private lateinit var parser: Hl7Parser
    private lateinit var builder: HL7MessageBuilder

    private var listener: Hl7EventListener? = null

    private lateinit var imageServer: ImageWebServer



    /** Binder to expose service instance to clients */
    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): HL7Service = this@HL7Service
    }

    override fun onBind(intent: Intent?): IBinder = binder

    /* -------------------- SERVICE LIFECYCLE -------------------- */

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "Service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let { loadConfigFromIntent(it) }

        Log.i(TAG, "Service starting with config: $config")

        startForeground(NOTIFICATION_ID, buildNotification())
        startImageServer()
        initializeCoreComponents()
        startMllpServer()
        initNetworkMonitoring(this)

        return START_STICKY
    }

    override fun onDestroy() {
        Log.i(TAG, "Service destroying")

        runBlocking {
            try {
                server.stop()
                Log.d(TAG, "MLLP server stopped")
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping server", e)
            }
        }

        clientManager.shutdown()
        nsdHelper.shutdown()
        serviceScope.cancel()
        networkIpMonitor.stop()
        nsdHelper.shutdown()
        imageServer.stop()
        super.onDestroy()
        listener?.onServiceStopped()
    }

    /** -------------------- CONFIGURATION -------------------- **/

    private fun loadConfigFromIntent(intent: Intent) {
        config = HL7Config(
            serverPort = intent.getIntExtra(
                Hl7serviceHandler.EXTRA_SERVER_PORT,
                config.serverPort
            ),
            autoResponseDelayMs = intent.getLongExtra(
                Hl7serviceHandler.EXTRA_AUTO_RESPONSE_DELAY,
                config.autoResponseDelayMs
            ),
            nsdBroadcastServiceName = intent.getStringExtra(
                Hl7serviceHandler.EXTRA_NSD_BROADCAST_NAME
            ) ?: config.nsdBroadcastServiceName,
            nsdBroadcastType = intent.getStringExtra(
                Hl7serviceHandler.EXTRA_NSD_BROADCAST_TYPE
            ) ?: config.nsdBroadcastType,
            nsdDiscoveryType = intent.getStringExtra(
                Hl7serviceHandler.EXTRA_NSD_DISCOVERY_TYPE
            ) ?: config.nsdDiscoveryType,
            imageServicePort = intent.getIntExtra(
                Hl7serviceHandler.EXTRA_IMAGE_SERVICE_PORT,
                config.imageServicePort
            ),
            imageServiceSecurePort = intent.getIntExtra(
                Hl7serviceHandler.EXTRA_IMAGE_SERVICE_SECURE_PORT,
                config.imageServiceSecurePort
            )
        )
    }

    fun updateConfig(newConfig: HL7Config) {
        Log.i(TAG, "Updating config: $newConfig")
        this.config = newConfig
    }

    /* -------------------- INITIALIZATION -------------------- */



    private fun initializeCoreComponents() {
        parser = Hl7Parser()
        builder = HL7MessageBuilder()
        nsdHelper = NsdHelper(this)

        val tlsFactory = TlsSocketFactory()
        val client = MllpClient(tlsFactory)
        clientManager = MllpConnectionManager(client)

        Log.d(TAG, "Core components initialized")
    }


    private fun initNetworkMonitoring(context: Context) {

        networkIpMonitor = NetworkIpMonitor(
            context = context,

            onWifiAvailable = {
                Log.i(TAG, "Wi-Fi available → start NSD broadcast")
                startNsdBroadcast()
            },

            onWifiLost = {
                Log.w(TAG, "Wi-Fi lost → stop NSD broadcast")
                nsdHelper.stopRegistration()
            },

            onIpChanged = { newIp ->
                Log.w(TAG, "IP changed to $newIp → rebroadcast NSD")
                rebroadcastNsd()
            }
        )

        networkIpMonitor.start()
    }


    /* -------------------- SERVER -------------------- */

    private fun startMllpServer() {
        server = MllpServer(
            port = config.serverPort,
        ) { raw ->
            handleIncomingMessage(raw)
        }

        serviceScope.launch {
            server.start()
            Log.i(TAG, "MLLP server listening on ${config.serverPort}")
            listener?.onServerStarted(config.serverPort)
        }
    }

    /* -------------------- NSD -------------------- */

    private fun startNsdBroadcast() {
        nsdHelper.registerService(
            port = config.serverPort,
            serviceName = config.nsdBroadcastServiceName,
            serviceType = config.nsdBroadcastType,
            txtRecords = mapOf("protocol" to "MLLP/TLS")
        )

        listener?.onNsdRegistered(config.nsdBroadcastServiceName)
        Log.i(
            TAG,
            "NSD broadcast registered: ${config.nsdBroadcastServiceName} ${config.nsdBroadcastType}"
        )
    }

    /**
     * Rebroadcast NSD.
     *
     * Called when:
     * - Wi-Fi network changes
     * - IP/interface changes
     * - Router reboot
     */
    fun rebroadcastNsd() {
        Log.w(TAG, "Rebroadcasting NSD service")

        nsdHelper.stopRegistration()

        /** Small delay avoids NSD race conditions on Android */
        Handler(Looper.getMainLooper()).postDelayed({
            startNsdBroadcast()
        }, 500)
    }


    fun discoverPmsAndConnect() {
        listener?.onNsdDiscoveryStarted()

        nsdHelper.discover(config.nsdDiscoveryType) { info ->
            serviceScope.launch {
                try {
                    val host = info.host.hostAddress
                    val port = info.port
                    if (host != null) {
                        listener?.onNsdServiceFound(info.serviceName, host, port)
                        clientManager.connect(host, port)
                        listener?.onClientConnected(host, port)
                    }
                    Log.e(TAG, "PMS connection establish")
                } catch (e: Exception) {
                    Log.e(TAG, "PMS connection failed", e)
                    listener?.onError("CLIENT_CONNECT", e)
                }
            }
        }
    }

    /* -------------------- MESSAGE HANDLING -------------------- */

    private fun handleIncomingMessage(raw: String): AckDecision {
        return try {
            val message = parser.parse(raw)
            val key = message.generateMessageIdempotencyKey()

            listener?.onMessageReceived(
                parsed = message,
                idempotencyKey = key
            )

            AckDecision.Accept
        } catch (e: Exception) {
            listener?.onError("HL7_PARSE", e)
            Log.e(TAG, "HL7 processing failed", e)
            AckDecision.Error(e.message ?: "HL7 error")
        }
    }

    /* -------------------- RESPONSE -------------------- */


    fun sendHl7Message(original: CompleteHL7Message) {
        serviceScope.launch {
            try {
                val messageStr = original.toTypedHL7String()

                val ack = clientManager.send(messageStr)
                listener?.onMessageSent(messageStr, original.messageId)
                listener?.onAckReceived(ack, original.messageId)
            } catch (e: Exception) {
                listener?.onError("MESSAGE_SEND", e)
            }
        }
    }

    /* -------------------- NOTIFICATION -------------------- */
    private fun buildNotification(): Notification {
        createChannel()
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("HL7 Background Service")
            .setContentText("Listening & responding to HL7")
            .setOngoing(true)
            .build()
    }

    private fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "HL7 Background",
            NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }


    fun setListener(listener: Hl7EventListener) {
        this.listener = listener
        Log.d(TAG, "Listener set")
    }

    fun removeListener() {
        this.listener = null
        Log.d(TAG, "Listener removed")
    }


    private fun startImageServer() {
        imageServer = ImageWebServer(this)
        imageServer.start()

        val ip = NetworkUtils.getLocalIpAddress()
        Log.i(TAG, "Image server running at https://$ip:8443/images/{fileName}")
    }

}