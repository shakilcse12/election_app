package com.example.electionapp.ui.map

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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

    // Search state
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    // Filter results for the search dropdown
    val searchResults = remember(searchQuery, voteCenters) {
        if (searchQuery.isBlank()) emptyList()
        else voteCenters.filter {
            it.entity.centerName.contains(searchQuery, ignoreCase = true)
        }.take(5)
    }

    // We keep a reference to the MapView to move the camera programmatically
    val mapView = remember { MapView(context) }

    Box(modifier = Modifier.fillMaxSize()) {

        // 1. OSM Map Integration
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                mapView.apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(7.5)
                    controller.setCenter(GeoPoint(23.6850, 90.3563)) // Center of Bangladesh

                    // Add My Location Overlay
                    val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), this)
                    locationOverlay.enableMyLocation()
                    overlays.add(locationOverlay)
                }
            },
            update = { view ->
                // Clear and re-add markers when voteCenters change
                view.overlays.filterIsInstance<Marker>().forEach { view.overlays.remove(it) }

                voteCenters.forEach { center ->
                    val marker = Marker(view)
                    marker.position = GeoPoint(center.entity.latitude, center.entity.longitude)
                    marker.title = center.entity.centerName
                    marker.snippet = center.entity.address

                    // Handle marker info window click for directions
                    marker.setOnMarkerClickListener { m, _ ->
                        m.showInfoWindow()
                        true
                    }

                    // Simple logic: clicking info window triggers navigation
                    marker.infoWindow = CustomInfoWindow(view) {
                        openDirections(context, center.entity.latitude, center.entity.longitude)
                    }

                    view.overlays.add(marker)
                }
                view.invalidate()
            }
        )

        // 2. Search Bar Overlay
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 40.dp, start = 16.dp, end = 16.dp)
        ) {
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
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
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

                                // Animate map to the selected center
                                val target = GeoPoint(item.entity.latitude, item.entity.longitude)
                                mapView.controller.animateTo(target, 16.0, 1000L)
                            }
                        )
                    }
                }
            }
        }
    }
}

// Custom Info Window for OSM Markers
class CustomInfoWindow(
    mapView: MapView,
    private val onDirectionsClick: () -> Unit
) : org.osmdroid.views.overlay.infowindow.MarkerInfoWindow(
    org.osmdroid.library.R.layout.bonuspack_bubble,
    mapView
) {
    override fun onOpen(item: Any?) {
        super.onOpen(item)
        mView.setOnClickListener { onDirectionsClick() }
    }
}

private fun openDirections(context: Context, lat: Double, lon: Double) {
    val uri = Uri.parse("google.navigation:q=$lat,$lon")
    val intent = Intent(Intent.ACTION_VIEW, uri)
    intent.setPackage("com.google.android.apps.maps")
    context.startActivity(intent)
}