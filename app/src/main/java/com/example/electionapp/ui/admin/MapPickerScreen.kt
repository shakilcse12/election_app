package com.example.electionapp.ui.admin

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import kotlin.coroutines.resume

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapPickerScreen(
    initialLat: Double,
    initialLng: Double,
    onLocationPicked: (Double, Double) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    // Initialize OSMDroid once
    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
    }
    // State management
    var selectedLat by rememberSaveable { mutableStateOf(if (initialLat != 0.0) initialLat else 0.0) }
    var selectedLng by rememberSaveable { mutableStateOf(if (initialLng != 0.0) initialLng else 0.0) }
    val selectedPoint: GeoPoint? = if (selectedLat != 0.0 && selectedLng != 0.0) GeoPoint(selectedLat, selectedLng) else null
    var selectedAddress by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var hasLocationPermission by remember { mutableStateOf(false) }
    var isSearching by remember { mutableStateOf(false) }

    // Track current marker to properly manage InfoWindows
    var currentMarker by remember { mutableStateOf<Marker?>(null) }
    // Check current permissions
    val currentFinePermission = remember {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    val currentCoarsePermission = remember {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    // Permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        hasLocationPermission = fineGranted || coarseGranted
    }

    // FIXED: Changed rememberSaveable to remember for SnapshotStateMap
    // SnapshotStateMap cannot be natively saved in a Bundle without a custom Saver.
    val addressCache = remember { mutableStateMapOf<String, String>() }

    // Helper function to update location
    fun updateLocation(point: GeoPoint) {
        selectedLat = point.latitude
        selectedLng = point.longitude
        scope.launch {
            val key = "${"%.6f".format(point.latitude)},${"%.6f".format(point.longitude)}"
            val cachedAddress = addressCache[key]
            if (cachedAddress != null) {
                selectedAddress = cachedAddress
            } else {
                val address = fetchAddress(context, point.latitude, point.longitude)
                selectedAddress = address ?: "Coordinates: ${"%.6f".format(point.latitude)}, ${"%.6f".format(point.longitude)}"
                if (address != null) {
                    addressCache[key] = address
                }
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val mapView = remember {
            MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                minZoomLevel = 3.0
                maxZoomLevel = 19.0
            }
        }

        val locationOverlay = remember {
            MyLocationNewOverlay(GpsMyLocationProvider(context), mapView).apply {
                setPersonHotspot(0.5f, 1.0f)
            }
        }

        LaunchedEffect(Unit) {
            if (!currentFinePermission && !currentCoarsePermission) {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            } else {
                hasLocationPermission = true
            }
        }

        LaunchedEffect(selectedPoint, hasLocationPermission) {
            val startPoint = selectedPoint ?: GeoPoint(23.6850, 90.3563)
            mapView.controller.setCenter(startPoint)
            mapView.controller.setZoom(15.0)
        }

        LaunchedEffect(hasLocationPermission) {
            if (hasLocationPermission && !mapView.overlays.contains(locationOverlay)) {
                locationOverlay.enableMyLocation()
                mapView.overlays.add(locationOverlay)
                mapView.invalidate()
            }
        }

        LaunchedEffect(selectedPoint) {
            if (selectedPoint != null && selectedAddress.isEmpty()) {
                updateLocation(selectedPoint)
            }
        }

        Scaffold(
            topBar = {
                Surface(shadowElevation = 8.dp) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Search place...") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                trailingIcon = {
                                    Row {
                                        if (searchQuery.isNotEmpty()) {
                                            IconButton(
                                                onClick = {
                                                    searchQuery = ""
                                                    isSearching = false
                                                }
                                            ) {
                                                Icon(Icons.Default.Close, contentDescription = "Clear")
                                            }
                                        }
                                        IconButton(
                                            onClick = {
                                                if (searchQuery.isNotBlank() && !isSearching) {
                                                    isSearching = true
                                                    scope.launch {
                                                        delay(300L)
                                                        val result = searchPlace(context, searchQuery)
                                                        if (result != null) {
                                                            currentMarker?.closeInfoWindow()
                                                            updateLocation(result)
                                                            mapView.controller.animateTo(result, 17.0, 1000L)
                                                        } else {
                                                            Toast.makeText(
                                                                context,
                                                                "Location not found",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                        isSearching = false
                                                    }
                                                }
                                            },
                                            enabled = searchQuery.isNotBlank() && !isSearching
                                        ) {
                                            if (isSearching) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(20.dp),
                                                    strokeWidth = 2.dp
                                                )
                                            } else {
                                                Icon(Icons.Default.Search, contentDescription = "Search")
                                            }
                                        }
                                    }
                                },
                                singleLine = true
                            )
                        }
                        if (isSearching) {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                            )
                        }
                    }
                }
            },
            bottomBar = {
                Column {
                    AnimatedVisibility(visible = selectedAddress.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = selectedAddress,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                    BottomAppBar(containerColor = Color.Transparent) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    currentMarker?.closeInfoWindow()
                                    mapView.overlays.clear()
                                    onDismiss()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                )
                            ) { Text("Cancel") }
                            Button(
                                onClick = {
                                    selectedPoint?.let {
                                        onLocationPicked(it.latitude, it.longitude)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                enabled = selectedPoint != null
                            ) { Text("Set Location") }
                        }
                    }
                }
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                AndroidView(
                    factory = { ctx ->
                        mapView.apply {
                            val eventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
                                override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                                    currentMarker?.closeInfoWindow()
                                    updateLocation(p)
                                    return true
                                }
                                override fun longPressHelper(p: GeoPoint): Boolean = false
                            })
                            overlays.add(eventsOverlay)

                            if (hasLocationPermission) {
                                locationOverlay.enableMyLocation()
                                overlays.add(locationOverlay)
                            }
                        }
                    },
                    update = { view ->
                        val oldMarkers = view.overlays.filterIsInstance<Marker>()
                        oldMarkers.forEach {
                            it.closeInfoWindow()
                            view.overlays.remove(it)
                        }

                        selectedPoint?.let { point ->
                            val marker = Marker(view)
                            marker.position = point
                            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            marker.title = "Selected Location"
                            marker.snippet = selectedAddress.takeIf { it.isNotBlank() }
                                ?: "Coordinates: ${"%.6f".format(point.latitude)}, ${"%.6f".format(point.longitude)}"

                            marker.setOnMarkerClickListener { m, _ ->
                                m.showInfoWindow()
                                true
                            }

                            view.overlays.add(marker)
                            currentMarker = marker
                        }
                        view.invalidate()
                    },
                    modifier = Modifier.fillMaxSize()
                )
                FloatingActionButton(
                    onClick = {
                        if (hasLocationPermission) {
                            val myLoc = locationOverlay.myLocation
                            if (myLoc != null) {
                                mapView.controller.animateTo(myLoc, 17.0, 1000L)
                            } else {
                                if (!mapView.overlays.contains(locationOverlay)) {
                                    locationOverlay.enableMyLocation()
                                    mapView.overlays.add(locationOverlay)
                                    mapView.invalidate()
                                }
                                locationOverlay.runOnFirstFix {
                                    val loc = locationOverlay.myLocation
                                    if (loc != null) {
                                        mapView.post {
                                            mapView.controller.animateTo(loc, 17.0, 1000L)
                                        }
                                    }
                                    true
                                }
                            }
                        } else {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .padding(bottom = 140.dp),
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = "Pan to My Location")
                }
            }
        }

        DisposableEffect(Unit) {
            onDispose {
                currentMarker?.closeInfoWindow()
                mapView.overlays.clear()
                mapView.onDetach()
            }
        }
    }
}

private suspend fun searchPlace(context: Context, query: String): GeoPoint? = withContext(Dispatchers.IO) {
    return@withContext try {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { cont ->
                geocoder.getFromLocationName(query, 1, object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: MutableList<Address>) {
                        cont.resume(addresses)
                    }
                    override fun onError(errorMessage: String?) {
                        cont.resume(emptyList())
                    }
                })
            }
        } else {
            @Suppress("DEPRECATION")
            geocoder.getFromLocationName(query, 1) ?: emptyList()
        }

        if (addresses.isNotEmpty()) {
            GeoPoint(addresses[0].latitude, addresses[0].longitude)
        } else {
            nominatimSearch(query)
        }
    } catch (e: Exception) {
        nominatimSearch(query)
    }
}

private suspend fun fetchAddress(context: Context, lat: Double, lon: Double): String? = withContext(Dispatchers.IO) {
    return@withContext try {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { cont ->
                geocoder.getFromLocation(lat, lon, 1, object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: MutableList<Address>) {
                        cont.resume(addresses)
                    }
                    override fun onError(errorMessage: String?) {
                        cont.resume(emptyList())
                    }
                })
            }
        } else {
            @Suppress("DEPRECATION")
            geocoder.getFromLocation(lat, lon, 1) ?: emptyList()
        }

        if (addresses.isNotEmpty()) {
            val address = addresses[0]
            val sb = StringBuilder()
            for (i in 0..address.maxAddressLineIndex.coerceAtMost(2)) {
                if (i > 0) sb.append(", ")
                sb.append(address.getAddressLine(i))
            }
            sb.toString()
        } else {
            nominatimReverse(lat, lon)
        }
    } catch (e: Exception) {
        nominatimReverse(lat, lon)
    }
}

private fun nominatimSearch(query: String): GeoPoint? {
    return try {
        val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")
        val urlString = "https://nominatim.openstreetmap.org/search?q=$encodedQuery&format=json&limit=1"
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("User-Agent", "ElectionApp/1.0")
        connection.connectTimeout = 10000
        connection.readTimeout = 10000

        if (connection.responseCode == 200) {
            val reader = java.io.BufferedReader(java.io.InputStreamReader(connection.inputStream))
            val response = reader.use { it.readText() }
            val jsonArray = JSONArray(response)
            if (jsonArray.length() > 0) {
                val jsonObject = jsonArray.getJSONObject(0)
                GeoPoint(jsonObject.getDouble("lat"), jsonObject.getDouble("lon"))
            } else null
        } else null
    } catch (e: Exception) {
        null
    }
}

private fun nominatimReverse(lat: Double, lon: Double): String? {
    return try {
        val urlString = "https://nominatim.openstreetmap.org/reverse?lat=$lat&lon=$lon&format=json"
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("User-Agent", "ElectionApp/1.0")
        connection.connectTimeout = 10000
        connection.readTimeout = 10000

        if (connection.responseCode == 200) {
            val reader = java.io.BufferedReader(java.io.InputStreamReader(connection.inputStream))
            val response = reader.use { it.readText() }
            val jsonObject = JSONObject(response)
            jsonObject.optString("display_name", null)
        } else null
    } catch (e: Exception) {
        null
    }
}