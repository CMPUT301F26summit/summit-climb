package com.example.summit_climb

import android.content.Context
import android.location.Geocoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Converts between address text and map coordinates using the device's [Geocoder].
 * Used by the campsite map screen so an owner can search for an address or drag a pin to find address
 * @param context: used to create the Geocoder
 * @constructor provides location services
 */
class LocationService(context: Context) {
    private val appContext = context.applicationContext

    /**
     * Finds the map coordinates for a text address
     * @param address: address of campsite
     * @return pair of (latitude, longitude) or null
     */
    // The following function was edited by Claude, Anthropic, 2026-11-02
    suspend fun addressToPoint(address: String): Pair<Double, Double>? =
        withContext(Dispatchers.IO) {
            try {
                @Suppress("DEPRECATION")
                Geocoder(appContext, Locale.getDefault())
                    .getFromLocationName(address, 1)
                    ?.firstOrNull()
                    ?.let { Pair(it.latitude, it.longitude) }
            } catch (e: Exception) {
                null
            }
        }

    /**
     * Finds the address at a position on the map, such as where the pin was dropped or dragged to
     * @param latitude: latitude of the pin.
     * @param longitude: longitude of the pin.
     * @return the first line of the address or null
     */
    // The following function was edited by Claude, Anthropic, 2026-11-02
    suspend fun pointToAddress(latitude: Double, longitude: Double): String? =
        withContext(Dispatchers.IO) {
            try {
                @Suppress("DEPRECATION")
                Geocoder(appContext, Locale.getDefault())
                    .getFromLocation(latitude, longitude, 1)
                    ?.firstOrNull()
                    ?.getAddressLine(0)
            } catch (e: Exception) {
                null
            }
        }
}