package com.example.electionapp.ui.map

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.electionapp.ui.centers.VoteCenterViewModel
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoteCenterMapScreen(
    viewModel: VoteCenterViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val voteCenters by viewModel.voteCenters.collectAsState()

    val mapView = remember { MapView(context) }
    val locationOverlay = remember {
        MyLocationNewOverlay(GpsMyLocationProvider(context), mapView)
    }

    // Permission handling
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            locationOverlay.enableMyLocation()
        }
    }

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        )
    }

    // Fix 1: Zoom to all markers with extra padding for the search bar
    LaunchedEffect(voteCenters) {
        if (voteCenters.isNotEmpty()) {
            val points = voteCenters.map { GeoPoint(it.entity.latitude, it.entity.longitude) }
            val boundingBox = BoundingBox.fromGeoPoints(points)

            mapView.post {
                // increaseByScale(1.5f) adds extra space around the markers
                mapView.zoomToBoundingBox(boundingBox.increaseByScale(1.5f), true)
                // Slight zoom out to ensure search bar doesn't overlap top markers
                mapView.controller.zoomOut()
            }
        }
    }

    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    val searchResults = remember(searchQuery, voteCenters) {
        if (searchQuery.isBlank()) emptyList()
        else voteCenters.filter { it.entity.centerName.contains(searchQuery, ignoreCase = true) }.take(5)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                mapView.apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    locationOverlay.enableMyLocation()
                    overlays.add(locationOverlay)
                }
            },
            update = { view ->
                view.overlays.filterIsInstance<Marker>().forEach { view.overlays.remove(it) }

                voteCenters.forEach { center ->
                    val marker = Marker(view)
                    marker.position = GeoPoint(center.entity.latitude, center.entity.longitude)
                    marker.title = center.entity.centerName
                    marker.snippet = "${center.entity.address}\n\nTap bubble for Directions"

                    marker.setOnMarkerClickListener { m, _ ->
                        m.showInfoWindow()
                        true
                    }

                    // Fix 2: Reliable InfoWindow click for navigation
                    marker.infoWindow = CustomInfoWindow(view) {
                        openDirections(context, center.entity.latitude, center.entity.longitude)
                    }

                    view.overlays.add(marker)
                }
                view.invalidate()
            }
        )

        // UI: Search Bar
        Column(modifier = Modifier.align(Alignment.TopCenter).padding(top = 40.dp, start = 16.dp, end = 16.dp)) {
            DockedSearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onSearch = { isSearchActive = false; focusManager.clearFocus() },
                active = isSearchActive,
                onActiveChange = { isSearchActive = it },
                placeholder = { Text("Search Centers...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Close, contentDescription = null) }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                LazyColumn {
                    items(searchResults) { item ->
                        ListItem(
                            headlineContent = { Text(item.entity.centerName) },
                            supportingContent = { Text(item.entity.address) },
                            modifier = Modifier.clickable {
                                searchQuery = item.entity.centerName
                                isSearchActive = false
                                focusManager.clearFocus()
                                mapView.controller.animateTo(GeoPoint(item.entity.latitude, item.entity.longitude), 17.0, 1000L)
                            }
                        )
                    }
                }
            }
        }

        // UI: Locate Me Button
        FloatingActionButton(
            onClick = {
                locationOverlay.myLocation?.let {
                    mapView.controller.animateTo(it, 17.0, 1000L)
                } ?: run {
                    locationOverlay.enableMyLocation()
                }
            },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ) {
            Icon(Icons.Default.LocationOn, contentDescription = "My Location")
        }
    }
}

// Fixed Custom Info Window for guaranteed click detection
class CustomInfoWindow(mapView: MapView, private val onDirectionsClick: () -> Unit) :
    org.osmdroid.views.overlay.infowindow.MarkerInfoWindow(org.osmdroid.library.R.layout.bonuspack_bubble, mapView) {

    override fun onOpen(item: Any?) {
        super.onOpen(item)
        // Find the root layout of the OSM bonuspack bubble
        val layout = mView.findViewById<View>(org.osmdroid.library.R.id.bubble_description)
        layout?.setOnClickListener {
            onDirectionsClick()
            close()
        }

        // Also set on the main view for redundancy
        mView.setOnClickListener {
            onDirectionsClick()
            close()
        }
    }
}
private fun openDirections(context: Context, lat: Double, lon: Double) {
    // OLD: "google.navigation:q=$lat,$lon" -> Starts navigation immediately
    // NEW: Use the directions URL format -> Shows the route options/preview
    val uri = "https://www.google.com/maps/dir/?api=1&destination=$lat,$lon"

    val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
    mapIntent.setPackage("com.google.android.apps.maps")
    mapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    try {
        context.startActivity(mapIntent)
    } catch (e: Exception) {
        // Fallback: If Google Maps app isn't found, open in browser
        val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        context.startActivity(fallbackIntent)
    }
}
private fun openDirections2(context: Context, lat: Double, lon: Double) {
    // Explicit navigation URI
    val gmmIntentUri = Uri.parse("google.navigation:q=$lat,$lon")
    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
    mapIntent.setPackage("com.google.android.apps.maps")
    mapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    try {
        context.startActivity(mapIntent)
    } catch (e: Exception) {
        // Fallback for when Google Maps app isn't found
        val fallbackUri = Uri.parse("geo:0,0?q=$lat,$lon")
        val fallbackIntent = Intent(Intent.ACTION_VIEW, fallbackUri)
        context.startActivity(fallbackIntent)
    }
}