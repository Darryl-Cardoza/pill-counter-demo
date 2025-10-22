package com.rite.pillcounting.core.utils.common

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.os.Build
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.util.Locale
import kotlin.coroutines.resume

class LocationProvider(private val context: Context) {

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocationAsString(): String {
        return try {
            val client = LocationServices.getFusedLocationProviderClient(context)
            val location: Location? = client.lastLocation.await()

            if (location != null) {
                val geocoder = Geocoder(context, Locale.getDefault())

                // Use the new async API when available
                val addresses = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    suspendCancellableCoroutine { cont ->
                        geocoder.getFromLocation(
                            location.latitude,
                            location.longitude,
                            1
                        ) { result ->
                            cont.resume(result)
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocation(location.latitude, location.longitude, 1)
                }

                val address = addresses?.firstOrNull()
                if (address != null) {
                    "${address.locality ?: "Unknown City"}, ${address.countryName ?: "Unknown Country"}"
                } else {
                    "%.5f, %.5f".format(location.latitude, location.longitude)
                }
            } else {
                "Unknown Location"
            }
        } catch (_: Exception) {
            "Unknown Location"
        }
    }
}
