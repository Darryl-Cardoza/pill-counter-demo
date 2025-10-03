package com.example.pillcountingnewmodels.core.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.pillcountingnewmodels.core.models.ColorSettings
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Secure Preference Helper for managing application-level persistent state.
 *
 * This class wraps [EncryptedSharedPreferences] to provide:
 * - Encrypted storage of authentication tokens (access/refresh).
 * - User session state (logged in, IDs).
 * - Transaction identifiers.
 * - Cached theme configuration ([ColorSettings]) for instant UI rendering.
 *
 * ### Security
 * - Keys are encrypted with **AES-256 SIV**.
 * - Values are encrypted with **AES-256 GCM**.
 * - Master key is generated and stored in the Android Keystore (hardware-backed if available).
 *
 * ### Logging
 * - Operations are logged via [AppLogger], but sensitive values are never logged.
 * - Instead, logs include key names, existence, and string lengths.
 *
 * ### Usage
 * This helper is a singleton injected with Hilt:
 * ```
 * @Inject lateinit var preferenceHelper: PreferenceHelper
 * ```
 *
 * @constructor Creates an instance of [PreferenceHelper] with secure storage.
 * @param context Application context, required for encrypted preferences.
 */
@Singleton
class PreferenceHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val PREF_NAME = "pillcounting_secure_prefs"

        // Keys for Tokens
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"

        // Keys for Session
        private const val KEY_USER_LOGGED_IN = "user_logged_in"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_LOCAL_ID = "local_id"

        // Keys for Transactions
        private const val KEY_TXN_ID = "txn_id"

        // Keys for Theme Caching
        private const val KEY_THEME_COLORS = "theme_colors"

        private const val KEY_DO_NOT_ASK_AGAIN = "do_not_ask_again"

        private const val KEY_SHOW_NOTES_DIALOG = "key_show_notes_dialog"
    }

    /** Secure SharedPreferences instance used for all storage operations. */
    private val prefs: SharedPreferences

    /** JSON serializer/deserializer for objects like [ColorSettings]. */
    private val gson = Gson()

    /** Logger instance for consistent structured logging. */
    private val logger = AppLogger.create<PreferenceHelper>()

    init {
        // Generate or retrieve a secure AES256_GCM master key
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        // Create EncryptedSharedPreferences instance
        prefs = EncryptedSharedPreferences.create(
            context,
            PREF_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        logger.i("EncryptedSharedPreferences initialized (AES-256).")
    }

    // ─────────────────────────── Tokens ───────────────────────────

    /**
     * Saves access and refresh tokens securely.
     *
     * @param accessToken The access token for API authentication.
     * @param refreshToken The refresh token for renewing access tokens.
     */
    fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.edit {
            putString(KEY_ACCESS_TOKEN, accessToken)
            putString(KEY_REFRESH_TOKEN, refreshToken)
        }
        logger.i("Saved tokens securely (lengths: ${accessToken.length}, ${refreshToken.length})")
    }

    /**
     * Retrieves the stored access token.
     *
     * @return The decrypted access token, or `null` if not set.
     */
    fun getAccessToken(): String? {
        val token = prefs.getString(KEY_ACCESS_TOKEN, null)
        logger.d("Access token retrieved (exists=${token != null}, length=${token?.length ?: 0})")
        return token
    }

    /**
     * Retrieves the stored refresh token.
     *
     * @return The decrypted refresh token, or `null` if not set.
     */
    fun getRefreshToken(): String? {
        val token = prefs.getString(KEY_REFRESH_TOKEN, null)
        logger.d("Refresh token retrieved (exists=${token != null}, length=${token?.length ?: 0})")
        return token
    }

    /**
     * Clears all stored tokens from secure storage.
     */
    fun clearTokens() {
        prefs.edit {
            remove(KEY_ACCESS_TOKEN)
            remove(KEY_REFRESH_TOKEN)
        }
        logger.w("Cleared tokens from secure storage.")
    }

    // ─────────────────────────── User Session ───────────────────────────

    /**
     * Sets the user's login state.
     *
     * @param loggedIn `true` if the user is logged in, otherwise `false`.
     */
    fun setUserLoggedIn(loggedIn: Boolean) {
        prefs.edit { putBoolean(KEY_USER_LOGGED_IN, loggedIn) }
        logger.i("Updated login state: $loggedIn")
    }

    /**
     * Checks whether the user is currently logged in.
     *
     * @return `true` if logged in, otherwise `false`.
     */
    fun isUserLoggedIn(): Boolean {
        val state = prefs.getBoolean(KEY_USER_LOGGED_IN, false)
        logger.d("Login state checked: $state")
        return state
    }

    // ─────────────────────────── User Identifiers ───────────────────────────

    /**
     * Saves the backend-provided user ID securely.
     *
     * @param userId The unique identifier assigned by the backend.
     */
    fun saveUserId(userId: String) {
        prefs.edit { putString(KEY_USER_ID, userId) }
        logger.i("Saved userId securely (length=${userId.length})")
    }

    /**
     * Retrieves the stored backend user ID.
     *
     * @return The decrypted user ID, or `null` if not set.
     */
    fun getUserId(): String? {
        val id = prefs.getString(KEY_USER_ID, null)
        logger.d("UserId retrieved (exists=${id != null}, length=${id?.length ?: 0})")
        return id
    }

    /**
     * Clears the stored backend user ID.
     */
    fun clearUserId() {
        prefs.edit { remove(KEY_USER_ID) }
        logger.w("Cleared userId from secure storage.")
    }

    /**
     * Saves the Room database local user ID.
     *
     * @param localId Auto-generated primary key for the user in Room DB.
     */
    fun saveLocalId(localId: Long) {
        prefs.edit { putLong(KEY_LOCAL_ID, localId) }
        logger.i("Saved localId: $localId")
    }

    /**
     * Retrieves the stored Room DB local user ID.
     *
     * @return The Room DB ID, or `0` if not set.
     */
    fun getLocalId(): Long {
        val id = prefs.getLong(KEY_LOCAL_ID, 0)
        logger.d("LocalId retrieved: $id")
        return id
    }

    // ─────────────────────────── Transactions ───────────────────────────

    /**
     * Saves a transaction ID securely.
     *
     * @param txnId Identifier of the ongoing transaction.
     */
    fun saveTxnId(txnId: Long) {
        prefs.edit { putLong(KEY_TXN_ID, txnId) }
        logger.i("Saved txnId: $txnId")
    }

    /**
     * Retrieves the stored transaction ID.
     *
     * @return The transaction ID, or `0` if not set.
     */
    fun getTxnId(): Long {
        val id = prefs.getLong(KEY_TXN_ID, 0)
        logger.d("TxnId retrieved: $id")
        return id
    }

    // ─────────────────────────── Theme Caching ───────────────────────────

    /**
     * Saves theme [ColorSettings] as encrypted JSON.
     *
     * @param theme The [ColorSettings] object to persist securely.
     */
    fun saveThemeColors(theme: ColorSettings) {
        val json = gson.toJson(theme)
        prefs.edit { putString(KEY_THEME_COLORS, json) }
        logger.i("Saved theme colors (json length=${json.length})")
    }

    /**
     * Retrieves the cached theme [ColorSettings].
     *
     * @return Decrypted [ColorSettings], or `null` if not cached.
     */
    fun getThemeColors(): ColorSettings? {
        val json = prefs.getString(KEY_THEME_COLORS, null)
        return if (json != null) {
            logger.d("Theme colors retrieved (json length=${json.length})")
            gson.fromJson(json, ColorSettings::class.java)
        } else {
            logger.w("No cached theme colors found.")
            null
        }
    }

    /**
     * Clears cached theme colors.
     */
    fun clearThemeColors() {
        prefs.edit { remove(KEY_THEME_COLORS) }
        logger.w("Cleared theme colors from secure storage.")
    }

    // ─────────────────────────── Do Not Ask Again ───────────────────────────

    /**
     * Saves the "Do not ask again" preference.
     *
     * @param doNotAsk Whether the user chose not to be asked again.
     */
    fun saveDoNotAskAgain(doNotAsk: Boolean) {
        prefs.edit { putBoolean(KEY_DO_NOT_ASK_AGAIN, doNotAsk) }
        logger.i("Saved DoNotAskAgain preference: $doNotAsk")
    }

    /**
     * Retrieves the "Do not ask again" preference.
     *
     * @return true if the user has chosen not to be asked again, false otherwise.
     */
    fun isDoNotAskAgain(): Boolean {
        val value = prefs.getBoolean(KEY_DO_NOT_ASK_AGAIN, false)
        logger.d("Retrieved DoNotAskAgain preference: $value")
        return value
    }


    /** Provides application context when required (e.g., PackageManager checks). */
    fun getContext(): Context = context



    fun saveShowNotesDialogSetting(show: Boolean) {
        prefs.edit { putBoolean(KEY_SHOW_NOTES_DIALOG, show) }
        logger.i("Saved showNotesDialog: $show")
    }

    fun getShowNotesDialogSetting(): Boolean {
        val value = prefs.getBoolean(KEY_SHOW_NOTES_DIALOG, true)
        logger.d("ShowNotesDialog retrieved: $value")
        return value
    }

}
