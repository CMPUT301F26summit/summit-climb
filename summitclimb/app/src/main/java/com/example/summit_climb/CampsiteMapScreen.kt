package com.example.summit_climb

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.MapTileIndex
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

// Summit Climb colors
private val charcoal = Color(0xFF36454F)
private val lightBlue = Color(0xFFA6C8EB)


// Shows osmdroid where to get MapTiler tiles and adds the API key to each request
// The following variable and function are from Claude, Anthropic, "How do you initialize a map with Map Tiler", 2026-11-01
internal val mapTilerSource = object : OnlineTileSourceBase(
    "MapTiler", 0, 18, 256, ".png",
    arrayOf("https://api.maptiler.com/maps/streets-v2/256/"),
    "© MapTiler © OpenStreetMap contributors"
) {
    override fun getTileURLString(pMapTileIndex: Long): String {
        return baseUrl +
                MapTileIndex.getZoom(pMapTileIndex) + "/" +
                MapTileIndex.getX(pMapTileIndex) + "/" +
                MapTileIndex.getY(pMapTileIndex) +
                mImageFilenameEnding + "?key=" + BuildConfig.MAPTILER_KEY
    }
}

@Composable
fun CampsiteMapScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var address by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("Search an address or tap the map") }
    var chosen by remember { mutableStateOf<GeoPoint?>(null) }
    val locationService = remember { LocationService(context) }
    var pinAddress by remember { mutableStateOf("") }


    // Configures osmdroid and sets up map details like zoom limits and starting place
    val map = remember {
        val config = Configuration.getInstance()
        config.load(context, context.getSharedPreferences("OSMPrefs", Context.MODE_PRIVATE))
        config.userAgentValue = "SummitClimbApp/1.0 (trivia.trivona@gmail.com)"

        MapView(context).apply {
            setTileSource(mapTilerSource)
            setMultiTouchControls(true)
            maxZoomLevel = 25.0
            minZoomLevel = 5.0
            controller.setZoom(15.0)
            controller.setCenter(GeoPoint(50.12, -100.51))
        }
    }

    fun updateAddress(point: GeoPoint) {
        scope.launch {
            pinAddress = locationService.pointToAddress(point.latitude, point.longitude) ?: "Address not found"
        }
    }

    /*
       Author: Tino hhttps://stackoverflow.com/users/28528725/tino
       Title: "Marker in osmdroid map"
       Answer: https://stackoverflow.com/questions/79250427/marker-in-osmdroid-map
       Date: 2024-12-04
       License: CC-BY-SA 4.0 (International)
    */
    // campsite marker to drag to choose a location
    val pin = remember {
        Marker(map).apply {
            isDraggable = true
            /*
               Author: UrMi https://stackoverflow.com/users/2978205/urmi
               Title: "Marker in osmdroid map"
               Answer: How does one implement drag and drop for Android marker?
               Date: 2013-11-11
               License: CC-BY-SA 4.0 (International)
            */
            setOnMarkerDragListener(object : Marker.OnMarkerDragListener {
                override fun onMarkerDrag(marker: Marker?) {}
                override fun onMarkerDragStart(marker: Marker?) {}
                override fun onMarkerDragEnd(marker: Marker?) {
                    chosen = marker?.position
                    marker?.position?.let { updateAddress(it) }
                }
            })
        }
    }

    fun placePin(point: GeoPoint) {
        pin.position = point
        if (pin !in map.overlays) {
            map.overlays.add(pin)
        }
        map.invalidate()
        chosen = point
        updateAddress(point)
    }

    Spacer(Modifier.height(30.dp))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // back button
        Text(
            text = "Back",
            color = charcoal,
            fontSize = 15.sp,
            modifier = Modifier
                .clickable { navController.popBackStack() }
                .padding(vertical = 8.dp)
        )

        Text(
            text = "Campsite location",
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp
        )

        Spacer(Modifier.height(8.dp))

        // Search bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Type an address") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f)
            )

            Button(
                onClick = {
                    if (address.isBlank()) {
                        message = "Please enter an address"
                    } else {
                        scope.launch {
                            val result = locationService.addressToPoint(address.trim())
                            if (result == null) {
                                message = "Location not found"
                            } else {
                                message = "Search an address or tap the map"
                                val point = GeoPoint(result.first, result.second)
                                map.controller.setCenter(point)
                                placePin(point)
                            }
                        }
                    }
                },
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = lightBlue, contentColor = charcoal)
            ) {
                Text("Search", fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(Modifier.height(8.dp))

        // Shows current coordinates
        Text(message, color = charcoal, fontSize = 13.sp)
        chosen?.let {
            Text("Selected: ${it.latitude}, ${it.longitude}", color = charcoal, fontSize = 13.sp)
        }
        if (pinAddress.isNotBlank()) {
            Text(pinAddress, color = charcoal, fontSize = 13.sp)
        }

        Spacer(Modifier.height(8.dp))

        AndroidView(
            modifier = Modifier
                .height(300.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)),
            factory = {
                // The following overlay functions are from Claude, Anthropic, "How to properly receive touch input with maptiler in jetpack compose", 2026-10-30
                map.overlays.add(0, MapEventsOverlay(object : MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                        p?.let { placePin(it) }
                        return true
                    }

                    override fun longPressHelper(p: GeoPoint?): Boolean = false
                }))
                map
            }
        )

        Text(
            text = "© MapTiler © OpenStreetMap contributors",
            color = charcoal,
            fontSize = 10.sp
        )

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = {
                chosen?.let { point ->
                    val handle = navController.previousBackStackEntry?.savedStateHandle
                    handle?.set("latitude", point.latitude)
                    handle?.set("longitude", point.longitude)
                    handle?.set("address", pinAddress)
                    navController.popBackStack()
                }
            },
            enabled = chosen != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = lightBlue, contentColor = charcoal)
        ) {
            // Confirm button
            Text(
                text = "Confirm",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        }
    }
}