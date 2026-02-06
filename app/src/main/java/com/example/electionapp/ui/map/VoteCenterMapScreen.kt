package com.example.electionapp.ui.map

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
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
import androidx.compose.runtime.saveable.Saver
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
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
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

// ---------- MAP STATE ----------
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
    val coroutineScope = rememberCoroutineScope() // Required for safe search selection

    val markerCache = remember { mutableMapOf<Int, Marker>() }

    var mapViewState by rememberSaveable(stateSaver = MapViewStateSaver) {
        mutableStateOf(MapViewState())
    }

    var hasPerformedInitialZoom by remember {
        mutableStateOf(mapViewState.hasInitialZoom)
    }

    var searchQuery by remember { mutableStateOf("") }
    var searchActive by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
    }

    val voteCenters by viewModel.voteCenters.collectAsState()
    var selectedCenter by remember { mutableStateOf<GeoPoint?>(null) }
    var selectedAddress by remember { mutableStateOf<String?>(null) }

    val fabOffset by animateDpAsState(
        targetValue = if (selectedAddress != null) 136.dp else 16.dp,
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

    val mapEventsOverlay = remember {
        MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                InfoWindow.closeAllInfoWindowsOn(mapView)
                selectedCenter = null
                selectedAddress = null
                searchActive = false
                focusManager.clearFocus()
                return true
            }
            override fun longPressHelper(p: GeoPoint?) = false
        })
    }

    // ---------- LIFECYCLE ----------
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    mapView.onResume()
                    if (mapViewState.zoomLevel > 0) {
                        mapView.controller.setZoom(mapViewState.zoomLevel)
                        mapView.controller.setCenter(
                            GeoPoint(mapViewState.centerLat, mapViewState.centerLon)
                        )
                    }
                }
                Lifecycle.Event.ON_PAUSE -> {
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
            mapView.onDetach()
        }
    }

    // ---------- PERMISSIONS ----------
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        if (perms[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            locationOverlay.enableMyLocation()
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
        } else {
            locationOverlay.enableMyLocation()
        }
    }

    LaunchedEffect(searchQuery) {
        viewModel.onSearchChange(searchQuery)
    }

    val searchResults = remember(searchQuery, voteCenters) {
        if (searchQuery.isBlank()) emptyList()
        else voteCenters.filter {
            it.entity.centerName.contains(searchQuery, true) ||
                    it.entity.centerNumber.toString().contains(searchQuery)
        }.take(5)
    }

    // ---------- MARKERS ----------
    LaunchedEffect(voteCenters) {
        voteCenters.forEach { center ->
            if (center.entity.id !in markerCache) {
                val marker = Marker(mapView).apply {
                    position = GeoPoint(center.entity.latitude, center.entity.longitude)
                    title = "${center.entity.centerNumber}. ${center.entity.centerName}"
                    snippet = center.entity.address
                    icon = createCustomMarker(context, center.entity.centerNumber.toString())
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    infoWindow = CustomInfoWindow(mapView) {
                        openDirections(context, position.latitude, position.longitude)
                    }
                    setOnMarkerClickListener { m, _ ->
                        searchActive = false
                        focusManager.clearFocus()
                        selectedCenter = m.position
                        selectedAddress = snippet
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

        if (!mapView.overlays.contains(locationOverlay)) {
            mapView.overlays.add(locationOverlay)
        }

        if (!hasPerformedInitialZoom && mapViewState.zoomLevel == 0.0 && voteCenters.isNotEmpty()) {
            val bbox = BoundingBox.fromGeoPoints(
                voteCenters.map { GeoPoint(it.entity.latitude, it.entity.longitude) }
            )
            mapView.zoomToBoundingBox(bbox.increaseByScale(1.3f), true)
            hasPerformedInitialZoom = true
        }
    }

    // ---------- SAFE STATE SYNC ----------
    LaunchedEffect(Unit) {
        while (isActive) {
            delay(500)
            if (mapView.zoomLevelDouble > 0) {
                val center = mapView.mapCenter
                mapViewState = MapViewState(
                    centerLat = center.latitude,
                    centerLon = center.longitude,
                    zoomLevel = mapView.zoomLevelDouble,
                    hasInitialZoom = hasPerformedInitialZoom
                )
            }
        }
    }

    // ---------- UI ----------
    Box(Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                if (!mapView.overlays.contains(mapEventsOverlay)) {
                    mapView.overlays.add(0, mapEventsOverlay)
                }
                mapView
            }
        )
        // UI content unchanged (FABs, SearchBar, BottomCard)
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
                                searchQuery = ""
                                searchActive = false
                                focusManager.clearFocus()
                                InfoWindow.closeAllInfoWindowsOn(mapView)
                                // Start animation first
                                mapView.controller.animateTo(point, 18.0, 800L)
                                // FIX: Use coroutine to wait for the map to stabilize
                                coroutineScope.launch {
                                    delay(500) // Wait for animation and clustering to settle
                                    markerCache[item.entity.id]?.let {
                                        mapView.invalidate()
                                        it.showInfoWindow()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = fabOffset),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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
val MapViewStateSaver = Saver<MapViewState, List<Any>>(
    save = { listOf(it.centerLat, it.centerLon, it.zoomLevel, it.hasInitialZoom) },
    restore = {
        MapViewState(
            centerLat = it[0] as Double,
            centerLon = it[1] as Double,
            zoomLevel = it[2] as Double,
            hasInitialZoom = it[3] as Boolean
        )
    }
)

// CustomInfoWindow (Blue number fix)
class CustomInfoWindow(private val mapView: MapView, private val onDirectionsClick: () -> Unit) :
    MarkerInfoWindow(org.osmdroid.library.R.layout.bonuspack_bubble, mapView) {

    override fun onOpen(item: Any?) {
        // 1. Manually inflate mView if it's null BEFORE calling super.onOpen
        if (mView == null) {
            val inflater = mapView.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as android.view.LayoutInflater
            mView = inflater.inflate(org.osmdroid.library.R.layout.bonuspack_bubble, mapView, false)
        }

        // 2. Now it is safe to call super
        super.onOpen(item)

        val marker = item as? Marker ?: return

        // 3. Use a safe let block to configure the view
        mView?.let { bubbleView ->
            bubbleView.isClickable = true
            bubbleView.background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = 32f
                setStroke(2, Color.LTGRAY)
            }

            val titleView = bubbleView.findViewById<TextView>(org.osmdroid.library.R.id.bubble_title)
            val descView = bubbleView.findViewById<TextView>(org.osmdroid.library.R.id.bubble_description)

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

            bubbleView.setOnClickListener {
                onDirectionsClick()
                close()
            }
        }
    }

    override fun onClose() {
        super.onClose()
        // Optional: Clean up if necessary
    }
}

private fun openDirections(context: Context, lat: Double, lon: Double) {
    val uri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$lat,$lon")
    val intent = Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}

// --- ADDED: Function to generate custom marker with number ---
// 1. Add this cache outside the function (at the top level of the file or in the ViewModel)
private val markerIconCache = mutableMapOf<String, Drawable>()

private fun createCustomMarker(
    context: Context,
    number: String
): Drawable {

    // Cache check (unchanged)
    markerIconCache[number]?.let { return it }

    val density = context.resources.displayMetrics.density

    // ---------- RESPONSIVE SIZES ----------
    val sizeDp = 44f              // logical size (dp)
    val tailHeightDp = 8f
    val strokeDp = 2f
    val textSp = 14f

    val sizePx = (sizeDp * density).toInt()
    val tailHeightPx = (tailHeightDp * density).toInt()
    val strokePx = strokeDp * density

    val bitmap = Bitmap.createBitmap(
        sizePx,
        sizePx + tailHeightPx,
        Bitmap.Config.ARGB_8888
    )

    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    val centerX = sizePx / 2f
    val centerY = sizePx / 2f
    val radius = sizePx / 2.2f

    // ---------- COLORS ----------
    val blueDark = Color.parseColor("#1565C0")
    val blueLight = Color.parseColor("#1E88E5")

    // ---------- PIN PATH ----------
    val pinPath = Path().apply {
        addCircle(centerX, centerY, radius, Path.Direction.CW)

        moveTo(centerX - radius / 3f, centerY + radius - strokePx)
        lineTo(centerX, sizePx + tailHeightPx.toFloat())
        lineTo(centerX + radius / 3f, centerY + radius - strokePx)
        close()
    }

    // ---------- GRADIENT FILL ----------
    paint.style = Paint.Style.FILL
    paint.shader = LinearGradient(
        centerX,
        centerY - radius,
        centerX,
        centerY + radius,
        blueLight,
        blueDark,
        Shader.TileMode.CLAMP
    )
    canvas.drawPath(pinPath, paint)

    // ---------- WHITE BORDER ----------
    paint.shader = null
    paint.style = Paint.Style.STROKE
    paint.color = Color.WHITE
    paint.strokeWidth = strokePx
    canvas.drawPath(pinPath, paint)

    // ---------- TEXT ----------
    paint.style = Paint.Style.FILL
    paint.color = Color.WHITE
    paint.textAlign = Paint.Align.CENTER
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    paint.textSize = textSp * density

    val textBounds = Rect()
    paint.getTextBounds(number, 0, number.length, textBounds)
    val textY = centerY + (textBounds.height() / 2f)

    canvas.drawText(number, centerX, textY, paint)

    val drawable = BitmapDrawable(context.resources, bitmap)

    // Cache (unchanged)
    markerIconCache[number] = drawable
    return drawable
}
