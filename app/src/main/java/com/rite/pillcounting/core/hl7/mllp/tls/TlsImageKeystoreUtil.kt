package com.rite.pillcounting.core.hl7.imageWebService

import android.content.Context
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.MessageDigest
import java.security.cert.X509Certificate
import java.util.*
import javax.security.auth.x500.X500Principal
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Security

object TlsImageKeystoreUtil {

    private const val FILE_NAME = "image_https.p12"
    private const val PASSWORD = "changeit"
    private const val ALIAS = "image_https"
    private const val VALID_YEARS = 10

    init {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(BouncyCastleProvider())
        }
    }

    fun ensureKeystore(context: Context): KeyStore {
        val file = File(context.filesDir, FILE_NAME)
        val ks = KeyStore.getInstance("PKCS12")

        if (file.exists()) {
            FileInputStream(file).use {
                ks.load(it, PASSWORD.toCharArray())
            }
            return ks
        }

        ks.load(null, null)

        val keyGen = KeyPairGenerator.getInstance("RSA")
        keyGen.initialize(2048)
        val keyPair = keyGen.generateKeyPair()

        val now = Date()
        val until = Calendar.getInstance().apply {
            time = now
            add(Calendar.YEAR, VALID_YEARS)
        }.time

        val cert = JcaX509v3CertificateBuilder(
            X500Principal("CN=AndroidImageServer"),
            BigInteger.valueOf(System.currentTimeMillis()),
            now,
            until,
            X500Principal("CN=AndroidImageServer"),
            keyPair.public
        ).build(
            JcaContentSignerBuilder("SHA256withRSA")
                .build(keyPair.private)
        ).let {
            JcaX509CertificateConverter().getCertificate(it)
        }

        ks.setKeyEntry(
            ALIAS,
            keyPair.private,
            PASSWORD.toCharArray(),
            arrayOf(cert)
        )

        FileOutputStream(file).use {
            ks.store(it, PASSWORD.toCharArray())
        }

        return ks
    }

    fun password(): CharArray = PASSWORD.toCharArray()
    fun alias(): String = ALIAS

    fun fingerprint(context: Context): String {
        val ks = ensureKeystore(context)
        val cert = ks.getCertificate(ALIAS) as X509Certificate
        val md = MessageDigest.getInstance("SHA-256")
        return md.digest(cert.encoded)
            .joinToString(":") { "%02X".format(it) }
    }
}
