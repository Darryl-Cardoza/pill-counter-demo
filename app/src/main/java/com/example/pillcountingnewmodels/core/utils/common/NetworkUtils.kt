package com.example.pillcountingnewmodels.core.utils.common

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.example.pillcountingnewmodels.core.utils.logger.AppLogger

/**
 * Utility object for checking **network connectivity status** across Android versions.
 * Supports Wi-Fi, cellular, and Ethernet checks with backward compatibility.
 */
object NetworkUtils {

    /**
     * Determines if the device currently has an active internet connection.
     *
     * @param context Application or activity context.
     * @return `true` if connected to Wi-Fi, mobile data, or Ethernet; otherwise `false`.
     */
    @SuppressLint("ObsoleteSdkInt")
    fun isNetworkAvailable(context: Context): Boolean {
        return try {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                checkNetworkCapabilities(connectivityManager)
            } else {
                @Suppress("DEPRECATION")
                val networkInfo = connectivityManager.activeNetworkInfo
                networkInfo != null && networkInfo.isConnected
            }
        } catch (e: Exception) {
            AppLogger.create<NetworkUtils>().e("Network check failed", e)
            false
        }
    }

    /**
     * Checks active network capabilities for valid internet transports.
     *
     * @param connectivityManager System connectivity service.
     * @return `true` if connected through Wi-Fi, cellular, or Ethernet.
     */
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
