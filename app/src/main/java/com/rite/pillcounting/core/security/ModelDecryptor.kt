package com.rite.pillcounting.core.security

import java.io.File
import java.io.FileInputStream
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object ModelDecryptor {
    private const val MAGIC = "RITE"
    private const val NONCE_LEN = 12
    private const val TAG_LEN_BITS = 128

    // ⚠️ Same 32-byte key as in your Python script (64 hex chars)
    private val CONSTANT_DEK: ByteArray = hexToBytes(
        "5e81e4694423a0bd3c4890a6aeecf233315418e3d939e1125948157b360d5600"
    )

    /**
     * Decrypts the binary content of an encrypted file and returns a ByteArray.
     * Compatible with model_cryptography.py output.
     */
    fun decryptToBytes(inFile: File): ByteArray {
        FileInputStream(inFile).use { fis ->
            val magicBytes = ByteArray(4)
            require(fis.read(magicBytes) == 4 && magicBytes.contentEquals(MAGIC.toByteArray())) {
                "Invalid file format: missing RITE header"
            }

            val nonce = ByteArray(NONCE_LEN)
            require(fis.read(nonce) == NONCE_LEN) { "Incomplete nonce" }

            val cipherText = fis.readBytes()

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val keySpec = SecretKeySpec(CONSTANT_DEK, "AES")
            val gcmSpec = GCMParameterSpec(TAG_LEN_BITS, nonce)
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec)

            return try {
                cipher.doFinal(cipherText)
            } catch (e: Exception) {
                throw IllegalStateException("Decryption failed: wrong key or corrupted file", e)
            }
        }
    }

    private fun hexToBytes(hex: String): ByteArray {
        val clean = hex.trim().replace(" ", "")
        require(clean.length % 2 == 0) { "Invalid hex length" }
        return ByteArray(clean.length / 2) { i ->
            clean.substring(i * 2, i * 2 + 2).toInt(16).toByte()
        }
    }
}
