package com.example.summit_climb

import android.content.Context
import android.location.Geocoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.io.IOException
import java.util.Locale

class LocationService(private val context: Context) {
    private fun Campsite.point(): GeoPoint? =
        if (latitude == 0.0 && longitude == 0.0) null else GeoPoint(latitude, longitude)

    fun showOnMap(map: MapView, campsite: Campsite) {
        val point = campsite.point() ?: return
        map.overlays.add(Marker(map).apply {
            position = point
            title = campsite.name
        })
        map.controller.setZoom(15.0)
        map.controller.setCenter(point)
        map.invalidate()
    }

    fun distanceKm(campsite: Campsite, point: GeoPoint): Double? =
        campsite.point()?.distanceToAsDouble(point)?.div(1000)

    fun isWithinRadius(campsite: Campsite, center: GeoPoint, radiusKm: Double): Boolean =
        distanceKm(campsite, center)?.let { it <= radiusKm } ?: false

}