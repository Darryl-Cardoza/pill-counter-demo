package com.rite.pillcounting.core.security.models

/**
 * Wrapper class for sensitive string values stored in the local database.
 *
 * Used with [com.rite.pillcounting.core.security.SecureStringConverter] to automatically encrypt or hash data
 * when persisted and decrypt or unwrap it when read.
 */
data class SecureString(val value: String?)
