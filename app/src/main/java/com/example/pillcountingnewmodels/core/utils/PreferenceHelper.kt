package com.example.pillcountingnewmodels.core.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper class to manage application preferences related to authentication and session state.
 *
 * This class provides methods for storing, retrieving, and clearing access tokens,
 * refresh tokens, user login status, and the logged-in user's ID.
 * It uses Android's [SharedPreferences] for persistent storage.
 *
 * @constructor Injects the application context using Hilt to access [SharedPreferences].
 * @param context Application-level context for accessing shared preferences.
 */
@Singleton
class PreferenceHelper @Inject constructor(
    @ApplicationContext context: Context
) {

    companion object {
        private const val PREF_NAME = "pillcounting_prefs"

        /** Key for storing the access token. */
        private const val KEY_ACCESS_TOKEN = "access_token"

        /** Key for storing the refresh token. */
        private const val KEY_REFRESH_TOKEN = "refresh_token"

        /** Key for tracking whether the user is currently logged in. */
        private const val KEY_USER_LOGGED_IN = "user_logged_in"

        /** Key for storing the currently logged-in user's ID. */
        private const val KEY_USER_ID = "user_id"

        /** Key for storing the currently logged-in user's ID. */
        private const val KEY_LOCAL_ID = "local_id"

        private const val KEY_TXN_ID = "txn_id"
    }

    /** SharedPreferences instance for persistent key-value storage. */
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    // ─────────────────────────── Tokens ───────────────────────────

    fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.edit {
            putString(KEY_ACCESS_TOKEN, accessToken)
                .putString(KEY_REFRESH_TOKEN, refreshToken)
        }
    }

    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)

    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)

    fun clearTokens() {
        prefs.edit {
            remove(KEY_ACCESS_TOKEN)
                .remove(KEY_REFRESH_TOKEN)
        }
    }

    // ─────────────────────────── User Session ───────────────────────────

    fun setUserLoggedIn(loggedIn: Boolean) {
        prefs.edit { putBoolean(KEY_USER_LOGGED_IN, loggedIn) }
    }

    fun isUserLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_USER_LOGGED_IN, false)
    }

    // ─────────────────────────── User ID ───────────────────────────

    /**
     * Saves the current user's ID into preferences.
     *
     * @param userId Unique identifier of the logged-in user.
     */
    fun saveUserId(userId: String) {
        prefs.edit { putString(KEY_USER_ID, userId) }
    }

    /**
     * Retrieves the stored user ID.
     *
     * @return The stored user ID, or `null` if none is set.
     */
    fun getUserId(): String? = prefs.getString(KEY_USER_ID, null)

    /**
     * Clears the stored user ID (e.g., on logout).
     */
    fun clearUserId() {
        prefs.edit { remove(KEY_USER_ID) }
    }

    /**
     * Saves the current user's ID into preferences.
     *
     * @param localId Unique identifier of the logged-in user in rooms db.
     */
    fun saveLocalId(localId: Long) {
        prefs.edit { putLong(KEY_LOCAL_ID, localId) }
    }

    /**
     * Retrieves the stored local ID.
     *
     * @return The stored local ID, or `null` if none is set.
     */
    fun getLocalId(): Long? = prefs.getLong(KEY_LOCAL_ID, 0)


    fun saveTxnId(txnId: Long) {
        prefs.edit { putLong(KEY_TXN_ID, txnId) }
    }

    fun getTxnId(): Long? = prefs.getLong(KEY_TXN_ID, 0)
}
