package com.rite.pillcounting.core.hl7.mllp.tls

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.util.Calendar
import java.util.Date
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.security.auth.x500.X500Principal

object TlsKeystoreUtil {

    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "hl7_tls_key_v1"
    fun ensureKeyExists() {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        if (ks.containsAlias(KEY_ALIAS)) return

        val generator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_RSA,
            "AndroidKeyStore"
        )

        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_SIGN
        )
            .setKeySize(2048)
            .setDigests(
                KeyProperties.DIGEST_NONE,
                KeyProperties.DIGEST_SHA1,
                KeyProperties.DIGEST_SHA256,
                KeyProperties.DIGEST_SHA384,
                KeyProperties.DIGEST_SHA512
            )
            .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
            .setCertificateSubject(
                X500Principal("CN=HL7 Android TLS Server")
            )
            .setCertificateSerialNumber(BigInteger.ONE)
            .setCertificateNotBefore(Date())
            .setCertificateNotAfter(
                Calendar.getInstance().apply { add(Calendar.YEAR, 10) }.time
            )
            .build()

        generator.initialize(spec)
        generator.generateKeyPair()
    }

    fun getKeyStore(): KeyStore =
        KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

    fun getKeyAlias(): String = KEY_ALIAS


    fun createServerSslContext(): SSLContext {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

        val kmf = KeyManagerFactory.getInstance(
            KeyManagerFactory.getDefaultAlgorithm()
        )
        kmf.init(keyStore, null)

        return SSLContext.getInstance("TLS").apply {
            init(kmf.keyManagers, null, null)
        }
    }
}