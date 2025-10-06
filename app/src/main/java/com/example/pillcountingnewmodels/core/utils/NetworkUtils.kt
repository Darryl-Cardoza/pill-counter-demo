package com.example.pillcountingnewmodels.core.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.annotation.RequiresApi

/**
 * Utility object providing helper methods to check network connectivity status.
 *
 * ---
 * ### Features:
 * - Detects active internet connectivity (Wi-Fi, Cellular, or Ethernet).
 * - Backward-compatible down to Android M (API 23).
 * - Lightweight, static usage — no context leaks.
 *
 * ---
 * ### Usage Example:
 * ```kotlin
 * if (!NetworkUtils.isNetworkAvailable(context)) {
 *     _uiState.value = LoginUiState.Error(context.getString(R.string.error_no_internet))
 * }
 */
object NetworkUtils {

    /**
     * Checks whether the device currently has an active internet connection.
     *
     * @param context Application or activity context.
     * @return `true` if connected to Wi-Fi, mobile data, or ethernet; `false` otherwise.
     */
    fun isNetworkAvailable(context: Context): Boolean {
        return try {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                checkNetworkCapabilities(connectivityManager)
            } else {
                // Legacy support for API < 23
                @Suppress("DEPRECATION")
                val networkInfo = connectivityManager.activeNetworkInfo
                networkInfo != null && networkInfo.isConnected
            }
        } catch (e: Exception) {
            AppLogger.create<NetworkUtils>().e("Network check failed", e)
            false
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkNetworkCapabilities(connectivityManager: ConnectivityManager): Boolean {
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false

        return when {
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }
}
