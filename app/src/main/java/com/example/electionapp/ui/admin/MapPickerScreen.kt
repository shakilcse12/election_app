package com.example.electionapp.ui.admin

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapPickerScreen(
    initialLat: Double,
    initialLng: Double,
    onLocationPicked: (Double, Double) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    // State for the currently selected point
    var selectedPoint by remember {
        mutableStateOf(if (initialLat != 0.0) GeoPoint(initialLat, initialLng) else null)
    }

    var searchQuery by remember { mutableStateOf("") }
    val mapView = remember { MapView(context) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                Surface(shadowElevation = 8.dp) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search place (e.g. Dhaka College)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = {
                                IconButton(onClick = {
                                    searchPlace(context, searchQuery) { result ->
                                        result?.let {
                                            selectedPoint = it
                                            mapView.controller.animateTo(it, 17.0, 1000L)
                                        }
                                    }
                                }) {
                                    Icon(Icons.Default.Search, contentDescription = "Search")
                                }
                            },
                            singleLine = true
                        )
                    }
                }
            },
            bottomBar = {
                BottomAppBar(containerColor = Color.Transparent) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) { Text("Cancel") }

                        Button(
                            onClick = { selectedPoint?.let { onLocationPicked(it.latitude, it.longitude) } },
                            modifier = Modifier.weight(1f),
                            enabled = selectedPoint != null
                        ) { Text("Set Location") }
                    }
                }
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                // --- OpenStreetMap ---
                AndroidView(
                    factory = { ctx ->
                        mapView.apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(true)
                            controller.setZoom(15.0)

                            // Set initial center
                            val startPoint = selectedPoint ?: GeoPoint(23.6850, 90.3563)
                            controller.setCenter(startPoint)

                            // 1. "My Location" Blue Dot Overlay
                            val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(ctx), this)
                            locationOverlay.enableMyLocation()
                            overlays.add(locationOverlay)

                            // 2. Tap to Pick Location Overlay
                            val eventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
                                override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                                    selectedPoint = p
                                    return true
                                }
                                override fun longPressHelper(p: GeoPoint): Boolean = false
                            })
                            overlays.add(eventsOverlay)
                        }
                    },
                    update = { view ->
                        // 3. Handle Marker updates
                        view.overlays.filterIsInstance<Marker>().forEach { view.overlays.remove(it) }
                        selectedPoint?.let { point ->
                            val marker = Marker(view)
                            marker.position = point
                            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            marker.title = "Selected Location"
                            view.overlays.add(marker)
                        }
                        view.invalidate()
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // --- Floating My Location Button ---
                FloatingActionButton(
                    onClick = {
                        val provider = GpsMyLocationProvider(context)
                        val lastKnown = provider.lastKnownLocation
                        if (lastKnown != null) {
                            val myPoint = GeoPoint(lastKnown.latitude, lastKnown.longitude)
                            selectedPoint = myPoint
                            mapView.controller.animateTo(myPoint, 17.0, 1000L)
                        }
                    },
                    modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp).padding(bottom = 80.dp)
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = "My Location")
                }
            }
        }
    }
}

/**
 * Geocoding Helper: Converts text search to GeoPoint
 */
private fun searchPlace(context: Context, query: String, onResult: (GeoPoint?) -> Unit) {
    if (query.isBlank()) return
    val geocoder = Geocoder(context, Locale.getDefault())

    // Modern Android (Tiramisu+) uses an async listener
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        geocoder.getFromLocationName(query, 1, object : Geocoder.GeocodeListener {
            override fun onGeocode(addresses: MutableList<Address>) {
                if (addresses.isNotEmpty()) {
                    val addr = addresses[0]
                    onResult(GeoPoint(addr.latitude, addr.longitude))
                }
            }
            override fun onError(errorMessage: String?) { onResult(null) }
        })
    } else {
        // Legacy support
        try {
            val addresses = geocoder.getFromLocationName(query, 1)
            if (!addresses.isNullOrEmpty()) {
                val addr = addresses[0]
                onResult(GeoPoint(addr.latitude, addr.longitude))
            }
        } catch (e: Exception) {
            onResult(null)
        }
    }
}