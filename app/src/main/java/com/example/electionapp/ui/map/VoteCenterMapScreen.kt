package com.example.electionapp.ui.map

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.electionapp.ui.centers.VoteCenterViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoteCenterMapScreen(
    viewModel: VoteCenterViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    // Coroutine scope for handling map animations
    val scope = rememberCoroutineScope()

    val voteCenters by viewModel.voteCenters.collectAsState()

    // --- UI State ---
    var searchQuery by remember { mutableStateOf("") }
    var searchActive by remember { mutableStateOf(false) }

    var selectedLatLng by remember { mutableStateOf<LatLng?>(null) }
    var selectedAddress by remember { mutableStateOf<String?>(null) }

    val bottomSheetHeight = if (selectedAddress != null) 120.dp else 0.dp
    val fabOffset by animateDpAsState(
        targetValue = bottomSheetHeight + 16.dp,
        label = "fab-offset"
    )

    // --- Camera ---
    val cameraPositionState = rememberCameraPositionState()

    // --- Permissions ---
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> /* GoogleMap handles location automatically */ }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // --- Search Results ---
    val searchResults = remember(searchQuery, voteCenters) {
        if (searchQuery.isBlank()) emptyList()
        else voteCenters.filter {
            it.entity.centerName.contains(searchQuery, true) ||
                    it.entity.centerNumber.toString().contains(searchQuery)
        }.take(5)
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // ===================== MAP =====================
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = true),
            uiSettings = MapUiSettings(
                myLocationButtonEnabled = false,
                zoomControlsEnabled = false
            ),
            onMapClick = {
                selectedLatLng = null
                selectedAddress = null
                searchActive = false
                focusManager.clearFocus()
            }
        ) {

            // Initial bounding zoom
            LaunchedEffect(voteCenters) {
                if (voteCenters.isNotEmpty()) {
                    val bounds = LatLngBounds.builder().apply {
                        voteCenters.forEach {
                            include(LatLng(it.entity.latitude, it.entity.longitude))
                        }
                    }.build()
                    cameraPositionState.move(
                        CameraUpdateFactory.newLatLngBounds(bounds, 120)
                    )
                }
            }

            // Markers
            voteCenters.forEach { center ->
                val position = LatLng(center.entity.latitude, center.entity.longitude)
                Marker(
                    state = MarkerState(position),
                    title = "${center.entity.centerNumber}. ${center.entity.centerName}",
                    snippet = center.entity.address,
                    onClick = {
                        selectedLatLng = position
                        selectedAddress = center.entity.address

                        // FIX: Wrapped animate in scope.launch
                        scope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(position, 17f)
                            )
                        }
                        false
                    }
                )
            }
        }

        // ===================== SEARCH =====================
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 40.dp, start = 16.dp, end = 16.dp)
        ) {
            DockedSearchBar(
                query = searchQuery,
                onQueryChange = {
                    searchQuery = it
                    searchActive = it.isNotBlank()
                    viewModel.onSearchChange(it)
                },
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
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, null)
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
                                val latLng = LatLng(item.entity.latitude, item.entity.longitude)
                                selectedLatLng = latLng
                                selectedAddress = item.entity.address
                                searchQuery = ""
                                searchActive = false
                                focusManager.clearFocus()

                                // FIX: Wrapped animate in scope.launch
                                scope.launch {
                                    cameraPositionState.animate(
                                        CameraUpdateFactory.newLatLngZoom(latLng, 18f)
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }

        // ===================== FABs =====================
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = fabOffset),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FloatingActionButton(
                onClick = {
                    val bounds = LatLngBounds.builder().apply {
                        voteCenters.forEach {
                            include(LatLng(it.entity.latitude, it.entity.longitude))
                        }
                    }.build()

                    // FIX: Wrapped animate in scope.launch
                    scope.launch {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngBounds(bounds, 120)
                        )
                    }
                },
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Icon(Icons.Default.ZoomOutMap, null)
            }

            FloatingActionButton(
                onClick = {
                    cameraPositionState.position.target.let {

                        // FIX: Wrapped animate in scope.launch
                        scope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(it, 17f)
                            )
                        }
                    }
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(Icons.Default.MyLocation, null)
            }
        }

        // ===================== DETAILS CARD =====================
        AnimatedVisibility(
            visible = selectedAddress != null,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            selectedAddress ?: "",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        selectedLatLng?.let {
                            Text(
                                text = "Tap directions to navigate",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                    IconButton(
                        onClick = {
                            selectedLatLng?.let {
                                openDirections(context, it.latitude, it.longitude)
                            }
                        }
                    ) {
                        Icon(Icons.Default.Directions, null)
                    }
                    IconButton(
                        onClick = {
                            selectedLatLng = null
                            selectedAddress = null
                        }
                    ) {
                        Icon(Icons.Default.Close, null)
                    }
                }
            }
        }
    }
}

// ===================== DIRECTIONS =====================
private fun openDirections(context: Context, lat: Double, lon: Double) {
    // Standard Google Maps Navigation Intent
    val uri = Uri.parse("google.navigation:q=$lat,$lon")
    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
        setPackage("com.google.android.apps.maps")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        // Fallback: If Google Maps app isn't found, open in browser
        val browserUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$lat,$lon")
        context.startActivity(Intent(Intent.ACTION_VIEW, browserUri))
    }
}