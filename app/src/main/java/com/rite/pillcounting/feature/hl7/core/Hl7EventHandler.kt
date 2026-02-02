package com.rite.pillcounting.feature.hl7.core

import com.rite.pillcounting.core.hl7.core.Hl7EventListener
import com.rite.pillcounting.core.utils.logger.AppLogger
import com.rite.pillcounting.feature.hl7.data.repository.Hl7Repository
import com.rite.pillcounting.feature.hl7.notification.Hl7Notifier
import org.rite.hl7.hl7.domain.model.CompleteHL7Message
import javax.inject.Inject
import javax.inject.Singleton

/**
 * HL7EventHandler
 *
 * Acts as the SINGLE adapter between the HL7 core layer and
 * the PillCounting business layer.
 *
 * Responsibilities:
 * - Receive callbacks from HL7 runtime
 * - Log lifecycle & protocol events
 * - Delegate business-relevant events directly to Hl7Repository
 */
@Singleton
class Hl7EventHandler @Inject constructor(
    private val hl7Repository: Hl7Repository,
    private val notifier: Hl7Notifier
) : Hl7EventListener {


    private val logger = AppLogger("HL7EventHandler")


    /**
     * Called when a new HL7 message is received from PMS.
     *
     * Business meaning:
     * - Incoming dispense request
     * - Incoming inventory count request
     *
     * Action:
     * - Delegate to repository for parsing, mapping, and persistence
     */
    override fun onMessageReceived(
        parsed: CompleteHL7Message,
        idempotencyKey: String
    ) {
        logger.i("HL7 message received | msgId=${parsed.messageId} | key=$idempotencyKey ")
        hl7Repository.handleReceivedMessage(parsed)

        notifier.show(
            title = "Transaction received!",
            message = "${parsed.messageType}^${parsed.triggerEvent} from ${parsed.sendingFacility} "
        )
    }

    /**
     * Called when an outbound HL7 message is successfully sent.
     *
     * Business meaning:
     * - Message left device successfully
     *
     * Action:
     * - Currently informational only
     * - ACK is the real sync signal
     */
    override fun onMessageSent(raw: String, messageId: String) {
        logger.i("HL7 message sent | msgId=$messageId")
    }

    /**
     * Called when an ACK is received for a previously sent HL7 message.
     *
     * Business meaning:
     * - PMS has accepted the message
     * - Transaction can be marked as synced
     *
     * Action:
     * - Update transaction sync status
     */
    override fun onAckReceived(ackRaw: String, messageId: String) {
        logger.i("HL7 ACK received | msgId=$messageId")
        hl7Repository.markTransactionSynced()
    }


    /**
     * Called when HL7 background service starts.
     *
     * Business meaning:
     * - HL7 runtime is ready
     *
     * Action:
     * - Logging only
     */
    override fun onServiceStarted() {
        logger.i("HL7 service started")
    }

    /**
     * Called when HL7 background service stops.
     *
     * Business meaning:
     * - HL7 runtime unavailable
     *
     * Action:
     * - Logging only
     */
    override fun onServiceStopped() {
        logger.w("HL7 service stopped")
    }

    /**
     * Called when HL7 server socket starts listening.
     *
     * Business meaning:
     * - PMS can now connect to device
     */
    override fun onServerStarted(port: Int) {
        logger.i("HL7 server started on port $port")
    }

    /**
     * Called when HL7 server socket is stopped.
     */
    override fun onServerStopped() {
        logger.w("HL7 server stopped")
    }

    /**
     * Called when client connection to PMS is established.
     *
     * Business meaning:
     * - Safe to resend queued HL7 messages
     *
     * Action:
     * - Trigger resend of pending transactions
     */
    override fun onClientConnected(host: String, port: Int) {
        logger.i("HL7 client connected | $host:$port")
        hl7Repository.resendPendingHl7Transactions()
        notifier.show(
            title = "Device Connected",
            message = "Connected to $host Successfully"
        )
    }

    /**
     * Called when client disconnects from PMS.
     *
     * Business meaning:
     * - Temporary connectivity loss
     */
    override fun onClientDisconnected() {
        logger.w("HL7 client disconnected")
    }

    /**
     * Called when image HTTP service starts.
     */
    override fun onImageServiceStarted(baseUrl: String) {
        logger.i("HL7 image service started | baseUrl=$baseUrl")
    }

    /**
     * Called when image HTTP service stops.
     */
    override fun onImageServiceStopped() {
        logger.w("HL7 image service stopped")
    }

    /**
     * Called when NSD service is registered.
     *
     * Business meaning:
     * - PMS can discover device automatically
     */
    override fun onNsdRegistered(serviceName: String) {
        logger.i("HL7 NSD registered | service=$serviceName")
    }

    /**
     * Called when NSD discovery starts.
     */
    override fun onNsdDiscoveryStarted() {
        logger.i("HL7 NSD discovery started")
    }

    /**
     * Called when a PMS service is found via NSD.
     */
    override fun onNsdServiceFound(serviceName: String, host: String, port: Int) {
        logger.i("HL7 NSD service found | $serviceName @ $host:$port")
    }

    /**
     * Called for any error inside HL7 runtime.
     */
    override fun onError(source: String, throwable: Throwable) {
        logger.e("HL7 error | source=$source | message=${throwable.message}")
    }



}
