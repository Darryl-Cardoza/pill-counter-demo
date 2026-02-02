package com.rite.pillcounting.feature.hl7.core

import org.rite.hl7.hl7.domain.model.CompleteHL7Message
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Hl7MessageSender @Inject constructor(
    private val hl7ServiceManager: Hl7ServiceManager
) {
    fun send(message: CompleteHL7Message) {
        hl7ServiceManager.sendMessage(message)
    }

    fun connect() {
        hl7ServiceManager.discoverAndConnect()
    }
}
