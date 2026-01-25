package com.example.electionapp.ui.map

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.view.View
import android.widget.TextView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.electionapp.ui.centers.VoteCenterViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.InfoWindow
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoteCenterMapScreen(
    viewModel: VoteCenterViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
    }

    val allVoteCenters by viewModel.voteCenters.collectAsState()
    var selectedAddress by remember { mutableStateOf<String?>(null) }
    // Keeping track of coordinates for the bottom bar button
    var selectedCoords by remember { mutableStateOf<GeoPoint?>(null) }

    val customMarkerIcon = remember {
        ContextCompat.getDrawable(context, android.R.drawable.ic_menu_mylocation)?.apply {
            setColorFilter(Color.parseColor("#3F51B5"), PorterDuff.Mode.SRC_IN)
        }
    }

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
        }
    }

    val mapEventsOverlay = remember {
        MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                InfoWindow.closeAllInfoWindowsOn(mapView)
                selectedAddress = null
                selectedCoords = null
                focusManager.clearFocus()
                return true
            }
            override fun longPressHelper(p: GeoPoint?): Boolean = false
        })
    }

    val locationOverlay = remember {
        MyLocationNewOverlay(GpsMyLocationProvider(context), mapView)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            locationOverlay.enableMyLocation()
        }
    }

    val fabBottomPadding = if (selectedAddress != null) 100.dp else 24.dp

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        )
    }

    LaunchedEffect(allVoteCenters) {
        if (allVoteCenters.isNotEmpty()) {
            mapView.overlays.filterIsInstance<Marker>().forEach { mapView.overlays.remove(it) }

            allVoteCenters.forEach { center ->
                val marker = Marker(mapView).apply {
                    position = GeoPoint(center.entity.latitude, center.entity.longitude)
                    title = center.entity.centerName
                    snippet = center.entity.address
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

                    infoWindow = CustomInfoWindow(mapView) {
                        openDirections(context, center.entity.latitude, center.entity.longitude)
                    }

                    setOnMarkerClickListener { m, _ ->
                        selectedAddress = center.entity.address
                        selectedCoords = m.position
                        InfoWindow.closeAllInfoWindowsOn(mapView)
                        m.showInfoWindow()
                        mapView.controller.animateTo(m.position)
                        true
                    }
                }
                mapView.overlays.add(marker)
            }

            val points = allVoteCenters.map { GeoPoint(it.entity.latitude, it.entity.longitude) }
            val boundingBox = BoundingBox.fromGeoPoints(points)
            mapView.post {
                mapView.zoomToBoundingBox(boundingBox.increaseByScale(1.3f), true)
            }
            mapView.invalidate()
        }
    }

    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    val searchResults = remember(searchQuery, allVoteCenters) {
        if (searchQuery.isBlank()) emptyList()
        else allVoteCenters.filter {
            it.entity.centerNumber.toString().contains(searchQuery) ||
                    it.entity.centerName.contains(searchQuery, ignoreCase = true)
        }.take(5)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                mapView.apply {
                    locationOverlay.enableMyLocation()
                    if (!overlays.contains(mapEventsOverlay)) {
                        overlays.add(0, mapEventsOverlay)
                    }
                    if (!overlays.contains(locationOverlay)) {
                        overlays.add(locationOverlay)
                    }
                }
            },
            update = { }
        )

        Column(modifier = Modifier.align(Alignment.TopCenter).padding(top = 40.dp, start = 16.dp, end = 16.dp)) {
            DockedSearchBar(
                query = searchQuery,
                onQueryChange = {
                    searchQuery = it
                    isSearchActive = it.isNotBlank()
                },
                onSearch = { isSearchActive = false; focusManager.clearFocus() },
                active = isSearchActive,
                onActiveChange = { active -> isSearchActive = active },
                placeholder = { Text("Search Centers...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = ""; isSearchActive = false }) {
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
                                selectedAddress = item.entity.address
                                selectedCoords = GeoPoint(item.entity.latitude, item.entity.longitude)

                                val point = GeoPoint(item.entity.latitude, item.entity.longitude)
                                mapView.controller.animateTo(point, 18.0, 1000L)

                                InfoWindow.closeAllInfoWindowsOn(mapView)

                                mapView.overlays.filterIsInstance<Marker>().find {
                                    it.position.latitude == item.entity.latitude &&
                                            it.position.longitude == item.entity.longitude
                                }?.showInfoWindow()
                            }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                locationOverlay.myLocation?.let {
                    selectedAddress = null
                    selectedCoords = null
                    InfoWindow.closeAllInfoWindowsOn(mapView)
                    mapView.controller.animateTo(it, 17.0, 1000L)
                }
            },
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = fabBottomPadding),
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ) {
            Icon(Icons.Default.LocationOn, contentDescription = "My Location")
        }

        AnimatedVisibility(
            visible = selectedAddress != null,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = selectedAddress ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // --- ADDED DIRECTION BUTTON ---
                    IconButton(onClick = {
                        selectedCoords?.let {
                            openDirections(context, it.latitude, it.longitude)
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Directions,
                            contentDescription = "Directions",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(onClick = {
                        selectedAddress = null
                        selectedCoords = null
                        InfoWindow.closeAllInfoWindowsOn(mapView)
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Dismiss")
                    }
                }
            }
        }
    }
}

class CustomInfoWindow(mapView: MapView, private val onDirectionsClick: () -> Unit) :
    org.osmdroid.views.overlay.infowindow.MarkerInfoWindow(org.osmdroid.library.R.layout.bonuspack_bubble, mapView) {

    override fun onOpen(item: Any?) {
        super.onOpen(item)
        mView.isClickable = true
        val background = GradientDrawable().apply {
            setColor(Color.WHITE)
            cornerRadius = 32f
            setStroke(2, Color.LTGRAY)
        }
        mView.background = background

        val titleView = mView.findViewById<TextView>(org.osmdroid.library.R.id.bubble_title)
        val descriptionView = mView.findViewById<TextView>(org.osmdroid.library.R.id.bubble_description)

        titleView?.apply {
            setTextColor(Color.DKGRAY)
            setPadding(20, 10, 20, 0)
            text = "${(item as Marker).title}"
        }

        descriptionView?.apply {
            setTextColor(Color.GRAY)
            setPadding(20, 5, 20, 20)
            text = "${(item as Marker).snippet}\n\n📍 Tap below Address Bar for Directions"
        }

        val listener = View.OnClickListener {
            onDirectionsClick()
            close()
        }

        mView.setOnClickListener(listener)
        titleView?.setOnClickListener(listener)
        descriptionView?.setOnClickListener(listener)
    }
}

private fun openDirections(context: Context, lat: Double, lon: Double) {
    val directionsUri = "http://maps.google.com/maps?daddr=$lat,$lon"
    val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(directionsUri)).apply {
        setPackage("com.google.android.apps.maps")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(mapIntent)
    } catch (e: Exception) {
        val fallbackUri = Uri.parse("geo:0,0?q=$lat,$lon")
        context.startActivity(Intent(Intent.ACTION_VIEW, fallbackUri))
    }
}