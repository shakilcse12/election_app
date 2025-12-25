package com.example.electionapp.ui.centers

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.electionapp.data.local.entity.VoteCenterEntity

@Composable
fun VoteCenterCard(
    center: VoteCenterEntity,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Top row: Vote Center # + Call Icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Vote Center ${center.centerNumber}",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF2E7D32), // Green for center number
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = {
                    val dialIntent = Intent(
                        Intent.ACTION_DIAL,
                        Uri.parse("tel:${center.presidingOfficerPhone}")
                    )
                    context.startActivity(dialIntent)
                }) {
                    Icon(
                        Icons.Default.Call,
                        contentDescription = "Call Presiding Officer",
                        tint = Color(0xFF1976D2) // Blue for call icon
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom row: Text details on left, Map icon bottom-right
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom
            ) {
                // Left Column: Presiding Officer, Other Officers, Address
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Presiding Officer: ${center.presidingOfficerName}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Other Officers: ${center.otherOfficers}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = center.address,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Right Column: Map icon
                IconButton(
                    onClick = {
                        val mapUri = Uri.parse(
                            "geo:${center.latitude},${center.longitude}?q=${center.latitude},${center.longitude}(Vote Center ${center.centerNumber})"
                        )
                        context.startActivity(Intent(Intent.ACTION_VIEW, mapUri))
                    },
                    modifier = Modifier.align(Alignment.Bottom)
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = "Open Map",
                        tint = Color(0xFFD32F2F) // Red for map icon
                    )
                }
            }
        }
    }
}
