package com.example.pillcountingnewmodels.core.utils.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Handles secure generation, encryption, and decryption of keys using Android's **Keystore system**.
 * It manages a Key Encryption Key (KEK) used to safely wrap/unwrap Data Encryption Keys (DEK),
 * ensuring sensitive data remains protected even if the app storage is compromised.
 * @property context Application context (required for key operations).
 */
class KeyManager(private val context: Context) {

    /** Alias used to store the Key Encryption Key (KEK) inside Android Keystore. */
    private val alias = "KEK_ALIAS"

    /** Loads or initializes the Android KeyStore instance. */
    private val keyStore: KeyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

    /**
     * Generates a **Key Encryption Key (KEK)** inside the secure hardware-backed keystore.
     *
     * This key is used to encrypt/decrypt other symmetric keys (DEKs) safely.
     * If the KEK already exists, it is not regenerated to preserve data integrity.
     */
    fun generateKEK() {
        if (!keyStore.containsAlias(alias)) {
            val keyGen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            keyGen.init(
                KeyGenParameterSpec.Builder(
                    alias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setUserAuthenticationRequired(false)
                    .build()
            )
            keyGen.generateKey()
        }
    }

    /**
     * Encrypts the provided **Data Encryption Key (DEK)** using the stored KEK.
     *
     * @param dek Raw DEK bytes to encrypt.
     * @return A [Pair] containing:
     *  - Initialization Vector (IV) used in encryption.
     *  - Encrypted DEK bytes.
     */
    fun encryptDEK(dek: ByteArray): Pair<ByteArray, ByteArray> {
        val kek = keyStore.getKey(alias, null) as? SecretKey
            ?: throw IllegalStateException("KEK not found. Call generateKEK() first.")

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, kek)
        val encryptedDEK = cipher.doFinal(dek)

        return cipher.iv to encryptedDEK
    }

    /**
     * Decrypts the previously encrypted **Data Encryption Key (DEK)** using KEK.
     *
     * @param iv Initialization Vector from encryption.
     * @param encryptedDEK Encrypted DEK bytes.
     * @return Decrypted DEK as a [ByteArray].
     */
    fun decryptDEK(iv: ByteArray, encryptedDEK: ByteArray): ByteArray {
        val kek = keyStore.getKey(alias, null) as? SecretKey
            ?: throw IllegalStateException("KEK not found. Unable to decrypt.")

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, kek, GCMParameterSpec(128, iv))
        return cipher.doFinal(encryptedDEK)
    }
}