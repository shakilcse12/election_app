package com.example.electionapp.ui.map

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.view.View
import android.widget.TextView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.electionapp.ui.centers.VoteCenterViewModel
import kotlinx.coroutines.delay
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.InfoWindow
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.Locale

// --- ADD THIS DATA CLASS FOR MAP STATE PERSISTENCE ---
data class MapViewState(
    val centerLat: Double = 0.0,
    val centerLon: Double = 0.0,
    val zoomLevel: Double = 0.0,
    val hasInitialZoom: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoteCenterMapScreen(
    viewModel: VoteCenterViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val markerCache = remember { mutableMapOf<Int, Marker>() }

    // --- MODIFIED: Use rememberSaveable to persist map state ---
    var mapViewState by rememberSaveable(stateSaver = MapViewStateSaver) {
        mutableStateOf(MapViewState())
    }

    // Derive hasPerformedInitialZoom from saved state
    var hasPerformedInitialZoom by remember {
        mutableStateOf(mapViewState.hasInitialZoom)
    }

    // Search States
    var searchQuery by remember { mutableStateOf("") }
    var searchActive by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
    }

    val voteCenters by viewModel.voteCenters.collectAsState()
    var selectedCenter by remember { mutableStateOf<GeoPoint?>(null) }
    var selectedAddress by remember { mutableStateOf<String?>(null) }

    val bottomSheetHeight = if (selectedAddress != null) 120.dp else 0.dp
    val fabOffset by animateDpAsState(
        targetValue = bottomSheetHeight + 16.dp,
        label = "fab-offset"
    )

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            isHorizontalMapRepetitionEnabled = false
            isVerticalMapRepetitionEnabled = false
        }
    }

    val locationOverlay = remember {
        MyLocationNewOverlay(GpsMyLocationProvider(context), mapView)
    }

    // --- MODIFIED: Track map state changes ---
    var isMapIdle by remember { mutableStateOf(false) }
    var lastMapState by remember { mutableStateOf<MapViewState?>(null) }

    // Handles map interaction to close search
    val mapEventsOverlay = remember {
        MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                InfoWindow.closeAllInfoWindowsOn(mapView)
                selectedAddress = null
                selectedCenter = null

                // Standard behavior: Close search on map tap
                searchActive = false
                focusManager.clearFocus()
                return true
            }
            override fun longPressHelper(p: GeoPoint?) = false
        })
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    mapView.onResume()
                    // --- ADDED: Restore map state when returning to screen ---
                    if (mapViewState.zoomLevel > 0) {
                        mapView.postDelayed({
                            mapView.controller.setZoom(mapViewState.zoomLevel)
                            mapView.controller.setCenter(
                                GeoPoint(mapViewState.centerLat, mapViewState.centerLon)
                            )
                        }, 100)
                    }
                }
                Lifecycle.Event.ON_PAUSE -> {
                    // --- ADDED: Save map state before pausing ---
                    if (mapView.zoomLevelDouble > 0) {
                        val center = mapView.mapCenter
                        mapViewState = MapViewState(
                            centerLat = center.latitude,
                            centerLon = center.longitude,
                            zoomLevel = mapView.zoomLevelDouble,
                            hasInitialZoom = hasPerformedInitialZoom
                        )
                    }
                    mapView.onPause()
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            InfoWindow.closeAllInfoWindowsOn(mapView)
            locationOverlay.disableMyLocation()

            // --- ADDED: Save final map state when leaving screen ---
            if (mapView.zoomLevelDouble > 0) {
                val center = mapView.mapCenter
                mapViewState = MapViewState(
                    centerLat = center.latitude,
                    centerLon = center.longitude,
                    zoomLevel = mapView.zoomLevelDouble,
                    hasInitialZoom = hasPerformedInitialZoom
                )
            }

            mapView.onDetach()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        if (perms[Manifest.permission.ACCESS_FINE_LOCATION] == true) locationOverlay.enableMyLocation()
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
        } else {
            locationOverlay.enableMyLocation()
        }
    }

    LaunchedEffect(searchQuery) { viewModel.onSearchChange(searchQuery) }

    val searchResults = remember(searchQuery, voteCenters) {
        if (searchQuery.isBlank()) emptyList()
        else voteCenters.filter {
            it.entity.centerName.contains(searchQuery, true) || it.entity.centerNumber.toString().contains(searchQuery)
        }.take(5)
    }

    LaunchedEffect(voteCenters) {
        if (voteCenters.isNotEmpty()) {
            delay(50)
            voteCenters.forEach { center ->
                if (center.entity.id !in markerCache) {
                    val marker = Marker(mapView).apply {
                        position = GeoPoint(center.entity.latitude, center.entity.longitude)
                        title = "${center.entity.centerNumber}. ${center.entity.centerName}"
                        snippet = center.entity.address
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        infoWindow = CustomInfoWindow(mapView) {
                            openDirections(context, center.entity.latitude, center.entity.longitude)
                        }
                        setOnMarkerClickListener { m, _ ->
                            // Close search when interacting with markers
                            searchActive = false
                            focusManager.clearFocus()

                            selectedAddress = center.entity.address
                            selectedCenter = m.position
                            InfoWindow.closeAllInfoWindowsOn(mapView)
                            m.showInfoWindow()
                            mapView.controller.animateTo(m.position)
                            true
                        }
                    }
                    mapView.overlays.add(marker)
                    markerCache[center.entity.id] = marker
                }
            }
            if (!mapView.overlays.contains(locationOverlay)) mapView.overlays.add(locationOverlay)

            // --- MODIFIED: Check saved state before doing initial zoom ---
            if (!hasPerformedInitialZoom && mapViewState.zoomLevel == 0.0) {
                val points = voteCenters.map { GeoPoint(it.entity.latitude, it.entity.longitude) }
                if (points.isNotEmpty()) {
                    val bbox = BoundingBox.fromGeoPoints(points)
                    mapView.post {
                        mapView.zoomToBoundingBox(bbox.increaseByScale(1.3f), true)
                        hasPerformedInitialZoom = true
                        mapViewState = mapViewState.copy(hasInitialZoom = true)
                    }
                }
            }
        }
    }

    // --- ADDED: Save map state when user interacts with map ---
    LaunchedEffect(Unit) {
        while (true) {
            delay(500) // Check every 500ms
            if (mapView.zoomLevelDouble > 0 && !isMapIdle) {
                val currentCenter = mapView.mapCenter
                val currentState = MapViewState(
                    centerLat = currentCenter.latitude,
                    centerLon = currentCenter.longitude,
                    zoomLevel = mapView.zoomLevelDouble,
                    hasInitialZoom = hasPerformedInitialZoom
                )

                // Only update if state has changed
                if (lastMapState != currentState) {
                    lastMapState = currentState
                    mapViewState = currentState
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                if (!mapView.overlays.contains(mapEventsOverlay)) mapView.overlays.add(0, mapEventsOverlay)
                mapView
            }
        )

        // Standard Material 3 Search Bar
        Column(modifier = Modifier.align(Alignment.TopCenter).padding(top = 40.dp, start = 16.dp, end = 16.dp)) {
            DockedSearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it; searchActive = it.isNotBlank() },
                onSearch = {
                    searchActive = false
                    focusManager.clearFocus()
                },
                active = searchActive,
                onActiveChange = { searchActive = it },
                placeholder = { Text("Search centers") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Close, null) }
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
                                val point = GeoPoint(item.entity.latitude, item.entity.longitude)
                                selectedCenter = point
                                selectedAddress = item.entity.address

                                // Reset search state
                                searchQuery = ""
                                searchActive = false
                                focusManager.clearFocus()

                                InfoWindow.closeAllInfoWindowsOn(mapView)
                                markerCache[item.entity.id]?.let {
                                    it.showInfoWindow()
                                    mapView.controller.animateTo(point, 18.0, 800L)
                                }
                            }
                        )
                    }
                }
            }
        }

        // Action Buttons
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = fabOffset),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Reset Zoom FAB
            AnimatedVisibility(visible = hasPerformedInitialZoom) {
                FloatingActionButton(
                    onClick = {
                        searchActive = false
                        focusManager.clearFocus()
                        selectedAddress = null
                        selectedCenter = null
                        InfoWindow.closeAllInfoWindowsOn(mapView)
                        val points = voteCenters.map { GeoPoint(it.entity.latitude, it.entity.longitude) }
                        if (points.isNotEmpty()) {
                            val bbox = BoundingBox.fromGeoPoints(points)
                            mapView.zoomToBoundingBox(bbox.increaseByScale(1.3f), true)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Icon(Icons.Default.ZoomOutMap, "Reset Zoom")
                }
            }

            // My Location FAB
            FloatingActionButton(
                onClick = {
                    searchActive = false
                    focusManager.clearFocus()
                    locationOverlay.myLocation?.let {
                        selectedAddress = null
                        selectedCenter = null
                        InfoWindow.closeAllInfoWindowsOn(mapView)
                        mapView.controller.animateTo(it, 17.0, 800L)
                    }
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(Icons.Default.MyLocation, "My Location")
            }
        }

        // Details Card
        AnimatedVisibility(
            visible = selectedAddress != null,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(selectedAddress ?: "", style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)

                        val myLoc = locationOverlay.myLocation
                        if (myLoc != null && selectedCenter != null) {
                            val distKm = myLoc.distanceToAsDouble(selectedCenter) / 1000.0
                            val timeMins = (distKm / 40.0 * 60.0).toInt().coerceAtLeast(1)

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = String.format(Locale.getDefault(), "%.1f km", distKm),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(text = "• Est. $timeMins min drive", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                    IconButton(onClick = { selectedCenter?.let { openDirections(context, it.latitude, it.longitude) } }) {
                        Icon(Icons.Default.Directions, null, tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { selectedAddress = null; selectedCenter = null; InfoWindow.closeAllInfoWindowsOn(mapView) }) {
                        Icon(Icons.Default.Close, null)
                    }
                }
            }
        }
    }
}

// --- ADD THIS SAVER FOR MapViewState ---
val MapViewStateSaver = androidx.compose.runtime.saveable.Saver<MapViewState, List<Any>>(
    save = { state ->
        listOf(
            state.centerLat,
            state.centerLon,
            state.zoomLevel,
            state.hasInitialZoom
        )
    },
    restore = { saved ->
        MapViewState(
            centerLat = saved[0] as Double,
            centerLon = saved[1] as Double,
            zoomLevel = saved[2] as Double,
            hasInitialZoom = saved[3] as Boolean
        )
    }
)

// CustomInfoWindow (Blue number fix)
class CustomInfoWindow(mapView: MapView, private val onDirectionsClick: () -> Unit) :
    MarkerInfoWindow(org.osmdroid.library.R.layout.bonuspack_bubble, mapView) {

    override fun onOpen(item: Any?) {
        super.onOpen(item)
        val marker = item as? Marker ?: return
        mView.isClickable = true
        mView.background = GradientDrawable().apply {
            setColor(Color.WHITE)
            cornerRadius = 32f
            setStroke(2, Color.LTGRAY)
        }

        val titleView = mView.findViewById<TextView>(org.osmdroid.library.R.id.bubble_title)
        val descView = mView.findViewById<TextView>(org.osmdroid.library.R.id.bubble_description)

        titleView?.apply {
            setTextColor(Color.parseColor("#1976D2")) // Highlight Number/Title in Blue
            text = marker.title
            setPadding(20, 10, 20, 0)
        }

        descView?.apply {
            setTextColor(Color.GRAY)
            text = "${marker.snippet}\n\n📍 Tap for Directions"
            setPadding(20, 5, 20, 20)
        }

        mView.setOnClickListener { onDirectionsClick(); close() }
    }
}

private fun openDirections(context: Context, lat: Double, lon: Double) {
    // This URI opens the "Directions" screen, letting the user choose the mode (Walk, Bike, Drive, etc.)
    val directionsUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$lat,$lon")

    val mapIntent = Intent(Intent.ACTION_VIEW, directionsUri).apply {
        // Attempt to launch the Google Maps app specifically
        setPackage("com.google.android.apps.maps")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    try {
        // If Google Maps is installed, it will open the directions selection screen
        context.startActivity(mapIntent)
    } catch (e: Exception) {
        // Fallback: If Google Maps app is missing, use a generic geo intent
        // which will offer a browser or any other installed map app
        val fallbackUri = Uri.parse("geo:$lat,$lon?q=$lat,$lon")
        val fallbackIntent = Intent(Intent.ACTION_VIEW, fallbackUri)
        context.startActivity(fallbackIntent)
    }
}