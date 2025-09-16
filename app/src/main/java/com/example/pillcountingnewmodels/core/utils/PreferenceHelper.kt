package com.example.pillcountingnewmodels.core.utils

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper class to manage application preferences related to authentication and session state.
 *
 * This class provides methods for storing, retrieving, and clearing access tokens,
 * refresh tokens, and user login status. It uses Android's [SharedPreferences] for
 * persistent storage.
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
    }

    /** SharedPreferences instance for persistent key-value storage. */
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    /**
     * Saves both access and refresh tokens in persistent storage.
     *
     * @param accessToken The access token to save.
     * @param refreshToken The refresh token to save.
     */
    fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .apply()
    }

    /**
     * Retrieves the stored access token.
     *
     * @return The access token, or `null` if not found.
     */
    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)

    /**
     * Retrieves the stored refresh token.
     *
     * @return The refresh token, or `null` if not found.
     */
    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)

    /**
     * Clears both the access and refresh tokens from storage.
     * This is typically called on user logout.
     */
    fun clearTokens() {
        prefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .apply()
    }

    /**
     * Sets the login status flag indicating whether the user is logged in.
     *
     * @param loggedIn `true` if the user is logged in, `false` otherwise.
     */
    fun setUserLoggedIn(loggedIn: Boolean) {
        prefs.edit().putBoolean(KEY_USER_LOGGED_IN, loggedIn).apply()
    }

    /**
     * Returns the current login status of the user.
     *
     * @return `true` if the user is logged in, `false` otherwise.
     */
    fun isUserLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_USER_LOGGED_IN, false)
    }
}
