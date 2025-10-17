package com.rite.pillcounting.core.security

import androidx.room.TypeConverter
import com.rite.pillcounting.core.security.models.SecureString

/**
 * Handles conversion between [SecureString] and its encrypted (or hashed) string form
 * for Room database persistence.
 *
 * - Encrypts or hashes sensitive fields before storing in the database.
 * - Decrypts (or passes through) data when reading from the database.
 */
object SecureStringConverter {

    /** Converts a [SecureString] to its encrypted/hashed representation for Room storage. */
    @JvmStatic
    @TypeConverter
    fun fromSecure(value: SecureString?): String? {
        return value?.value?.let { CryptoHelper.encryptField(it) }
    }

    /** Converts a stored string back to a [SecureString] for use in the app layer. */
    @JvmStatic
    @TypeConverter
    fun toSecure(value: String?): SecureString? {
        return value?.let { SecureString(CryptoHelper.decryptField(it)) }
    }
}
