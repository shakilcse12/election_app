package com.example.electionapp.ui.centers

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.electionapp.data.local.entity.VoteCenterEntity
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoteCenterDetailsScreen(
    centerId: Int,
    viewModel: VoteCenterViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    LaunchedEffect(centerId) {
        viewModel.loadCenterById(centerId)
    }

    val center by viewModel.selectedCenter.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Center Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { context.findActivity()?.onBackPressed() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // ✅ STUNNING SHARE BUTTON
                    center?.let {
                        IconButton(onClick = { shareCenterDetails(context, it) }) {
                            Icon(Icons.Default.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        bottomBar = {
            if (center != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .padding(bottom = 8.dp), // Original padding
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilledTonalButton(
                        onClick = {
                            context.startActivity(
                                Intent(
                                    Intent.ACTION_DIAL,
                                    Uri.parse("tel:${center!!.presidingOfficerPhone}")
                                )
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Call Officer")
                    }

                    Button(
                        onClick = {
                            // Restored original universal Google Maps link
                            val uri = "http://maps.google.com/maps?daddr=${center!!.latitude},${center!!.longitude}"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                            intent.setPackage("com.google.android.apps.maps")
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Open Map")
                    }
                }
            }
        }
    ) { paddingValues ->
        center?.let { it ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
            ) {
                // 1. HERO HEADER (Visual UX)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                listOf(MaterialTheme.colorScheme.primaryContainer, Color.White)
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Surface(color = MaterialTheme.colorScheme.primary, shape = MaterialTheme.shapes.small) {
                            Text(
                                "CENTER NO: ${it.centerNumber}",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = it.centerName,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Union: ${it.union}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {

                    // 2. PERSONNEL SECTION
                    InformationCard("Election Officers", Icons.Default.Badge, MaterialTheme.colorScheme.primary) {
                        InfoRow(Icons.Default.Person, "Presiding Officer", it.presidingOfficerName, MaterialTheme.colorScheme.primary)
                        Divider(Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)
                        InfoRow(Icons.Default.Phone, "Contact Number", it.presidingOfficerPhone, MaterialTheme.colorScheme.primary)
                        if (it.otherOfficers.isNotBlank()) {
                            Divider(Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)
                            InfoRow(Icons.Default.Groups, "Support Staff", it.otherOfficers, MaterialTheme.colorScheme.primary)
                        }
                    }

                    // 3. VOTER STATISTICS (Visual Grid)
                    InformationCard("Voter Statistics", Icons.Default.Analytics, Color(0xFF2E7D32)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            VoterStatBox("Male", it.maleVoters, Icons.Default.Male, Color(0xFF1976D2))
                            VoterStatBox("Female", it.femaleVoters, Icons.Default.Female, Color(0xFFC2185B))
                            VoterStatBox("Hijra", it.hijraVoters, Icons.Default.Transgender, Color(0xFF7B1FA2))
                        }
                        Spacer(Modifier.height(16.dp))
                        Surface(modifier = Modifier.fillMaxWidth(), color = Color(0xFFE8F5E9), shape = MaterialTheme.shapes.small) {
                            Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Voters", fontWeight = FontWeight.Bold)
                                Text(it.totalVoters, fontWeight = FontWeight.Black, color = Color(0xFF2E7D32))
                            }
                        }
                    }

                    // 4. COVERAGE & INFRASTRUCTURE
                    InformationCard("Coverage & Infrastructure", Icons.Default.MeetingRoom, MaterialTheme.colorScheme.tertiary) {
                        InfoRow(Icons.Default.Place, "Voter Areas", it.voterAreas, MaterialTheme.colorScheme.tertiary)
                        Divider(Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)
                        InfoRow(Icons.Default.TableChart, "Booth Count", it.booths, MaterialTheme.colorScheme.tertiary)
                        Divider(Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)
                        InfoRow(Icons.Default.HomeWork, "Address", it.address, MaterialTheme.colorScheme.tertiary)
                    }

                    // 5. TECHNICAL & REMARKS
                    InformationCard("Additional Info", Icons.Default.Info, Color.Gray) {
                        InfoRow(Icons.Default.GpsFixed, "Coordinates", "${it.latitude}, ${it.longitude}", Color.Gray)
                        if (it.remarks.isNotBlank()) {
                            Divider(Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)
                            Text("Remarks:", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(it.remarks, style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    // 6. MAP PREVIEW
                    Surface(shape = MaterialTheme.shapes.medium, border = CardDefaults.outlinedCardBorder(), modifier = Modifier.fillMaxWidth().height(200.dp)) {
                        AndroidView(factory = { ctx ->
                            MapView(ctx).apply {
                                setTileSource(TileSourceFactory.MAPNIK)
                                val point = GeoPoint(it.latitude, it.longitude)
                                controller.setZoom(17.0)
                                controller.setCenter(point)
                                overlays.add(Marker(this).apply { position = point; title = it.centerName })
                            }
                        }, modifier = Modifier.fillMaxSize())
                    }

                    Spacer(Modifier.height(100.dp))
                }
            }
        } ?: LoadingState()
    }
}

// ✅ HELPER: SHARE LOGIC
private fun shareCenterDetails(context: Context, center: VoteCenterEntity) {
    val text = """
        🗳️ *Vote Center:* ${center.centerName}
        🆔 *Center No:* ${center.centerNumber}
        📍 *Union:* ${center.union}
        🏠 *Address:* ${center.address}
        
        👤 *Officer:* ${center.presidingOfficerName}
        📞 *Phone:* ${center.presidingOfficerPhone}
        
        📊 *Total Voters:* ${center.totalVoters}
        🗺️ *Map:* https://www.google.com/maps/search/?api=1&query=${center.latitude},${center.longitude}
    """.trimIndent()

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Share Center Info"))
}

@Composable
private fun VoterStatBox(label: String, value: String, icon: ImageVector, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
        Text(value, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
    }
}

@Composable
private fun InformationCard(title: String, icon: ImageVector, iconColor: Color, content: @Composable ColumnScope.() -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, colors = CardDefaults.elevatedCardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String, iconColor: Color) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, null, tint = iconColor.copy(alpha = 0.6f), modifier = Modifier.size(18.dp).padding(top = 2.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun LoadingState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

// ✅ THE MISSING FUNCTION (Fixes the "Unresolved reference" error)
fun Context.findActivity(): android.app.Activity? = when (this) {
    is android.app.Activity -> this
    is android.content.ContextWrapper -> baseContext.findActivity()
    else -> null
}