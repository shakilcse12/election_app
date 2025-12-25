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
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Vote Center ${center.centerNumber}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = {
                    val dialIntent = Intent(
                        Intent.ACTION_DIAL,
                        Uri.parse("tel:${center.presidingOfficerPhone}")
                    )
                    context.startActivity(dialIntent)
                }) {
                    Icon(Icons.Default.Call, contentDescription = "Call Presiding Officer")
                }

                IconButton(onClick = {
                    val mapUri = Uri.parse(
                        "geo:${center.latitude},${center.longitude}?q=${center.latitude},${center.longitude}(Vote Center ${center.centerNumber})"
                    )
                    context.startActivity(Intent(Intent.ACTION_VIEW, mapUri))
                }) {
                    Icon(Icons.Default.LocationOn, contentDescription = "Open Map")
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Presiding Officer: ${center.presidingOfficerName}",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = center.address,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
