package com.example.electionapp.ui.centers

import android.content.Intent
import android.net.Uri
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
import com.example.electionapp.data.local.VoteCenterEntity

@Composable
fun VoteCenterCard(center: VoteCenterEntity) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
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
                    val intent = Intent(
                        Intent.ACTION_DIAL,
                        Uri.parse("tel:${center.presidingOfficerPhone}")
                    )
                    context.startActivity(intent)
                }) {
                    Icon(Icons.Default.Call, contentDescription = "Call")
                }

                IconButton(onClick = {
                    val uri = Uri.parse(
                        "geo:${center.latitude},${center.longitude}?q=${center.latitude},${center.longitude}"
                    )
                    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                }) {
                    Icon(Icons.Default.LocationOn, contentDescription = "Map")
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text("Presiding Officer: ${center.presidingOfficerName}")
            Spacer(modifier = Modifier.height(4.dp))
            Text(center.address, style = MaterialTheme.typography.bodySmall)
        }
    }
}
