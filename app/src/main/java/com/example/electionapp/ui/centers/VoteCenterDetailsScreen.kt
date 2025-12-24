package com.example.electionapp.ui.centers

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun VoteCenterDetailsScreen(
    viewModel: VoteCenterViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val backStackEntry = rememberNavController().currentBackStackEntryAsState()
    val id = backStackEntry.value?.arguments?.getInt("id")

    LaunchedEffect(id) {
        id?.let { viewModel.loadCenterById(it) }
    }

    val center by viewModel.selectedCenter.collectAsState()

    center?.let {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            Text(
                "Vote Center ${it.centerNumber}",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(12.dp))
            Text("Presiding Officer: ${it.presidingOfficerName}")
            Text("Other Officers: ${it.otherOfficers}")
            Spacer(modifier = Modifier.height(8.dp))
            Text("Address:")
            Text(it.address)

            Spacer(modifier = Modifier.height(16.dp))

            Row {
                Button(onClick = {
                    context.startActivity(
                        Intent(Intent.ACTION_DIAL, Uri.parse("tel:${it.presidingOfficerPhone}"))
                    )
                }) {
                    Icon(Icons.Default.Call, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Call Officer")
                }

                Spacer(modifier = Modifier.width(12.dp))

                Button(onClick = {
                    val uri = Uri.parse("geo:${it.latitude},${it.longitude}")
                    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                }) {
                    Icon(Icons.Default.LocationOn, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Open Map")
                }
            }
        }
    }
}
