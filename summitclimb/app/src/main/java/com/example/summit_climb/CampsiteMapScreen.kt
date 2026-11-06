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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.navigation.compose.rememberNavController
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.MapTileIndex
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.ui.text.TextStyle

// Summit Climb colors
private val black = Color.Black
private val charcoal = Color(0xFF36454F)
private val lightBlue = Color(0xFFA6C8EB)
private val white = Color.White


private val mapTilerSource = object : OnlineTileSourceBase(
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
    val converter = remember { LocationConverter(context) }
    var address by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("Search an address or tap the map") }
    var chosen by remember { mutableStateOf<GeoPoint?>(null) }

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
            controller.setCenter(GeoPoint(50.423, -100.423))
        }
    }

    // https://stackoverflow.com/questions/79250427/marker-in-osmdroid-map
    val pin = remember {
        Marker(map).apply {
            isDraggable = true
            // https://stackoverflow.com/questions/3897409/how-does-one-implement-drag-and-drop-for-android-marker
            setOnMarkerDragListener(object : Marker.OnMarkerDragListener {
                override fun onMarkerDrag(marker: Marker?) {}
                override fun onMarkerDragStart(marker: Marker?) {}
                override fun onMarkerDragEnd(marker: Marker?) {
                    chosen = marker?.position
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
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(top = 8.dp , bottom = 16.dp)
    ) {

        Row(
            modifier = Modifier
                .clickable { navController.popBackStack() }
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                modifier = Modifier.size(22.dp),
                tint = charcoal
            )
            Text(
                text = "Back",
                color = charcoal,
                fontSize = 17.sp,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Text(
            text = "Campsite location",
            color = black,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp
        )

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Type an address", fontSize = 15.sp) },
                singleLine = true,
                shape = RoundedCornerShape(17.dp),
                modifier = Modifier.weight(1f)
            )

            Button(
                onClick = {
                    if (address.isBlank()) {
                        message = "Please enter an address"
                    } else {
                        converter.resolveAddressToLocation(address.trim()) { result ->
                                map.controller.setCenter(result)
                                placePin(result)
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
        Text(
            message, color = charcoal,
            fontSize = 13.sp
        )

        chosen?.let {
            Text(
                text = "Selected: %.5f, %.5f".format(it.latitude, it.longitude),
                color = charcoal,
                fontSize = 13.sp)
        }

        Spacer(Modifier.height(8.dp))

        AndroidView(
            modifier = Modifier
                .height(300.dp)
                .fillMaxWidth()
                .padding(top = 16.dp)
                .clip(RoundedCornerShape(16.dp)),
            factory = {
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
            Text(
                text = "Confirm",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        }
    }
}



