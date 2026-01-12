package com.example.electionapp.ui.centers

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
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
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            /* ---------------- TOP ROW ---------------- */
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Vote Center ${center.centerNumber}",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF2E7D32),
                    modifier = Modifier.weight(1f)
                )

                /* MAP ICON — right of center name */
                IconButton(
                    onClick = {
                        val mapUri = Uri.parse(
                            "geo:${center.latitude},${center.longitude}?q=${center.latitude},${center.longitude}(Vote Center ${center.centerNumber})"
                        )
                        context.startActivity(Intent(Intent.ACTION_VIEW, mapUri))
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Open Map",
                        tint = Color(0xFFD32F2F)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            /* ---------------- PRESIDING OFFICER ROW ---------------- */
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                color = Color(0xFFE0E0E0),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Officer",
                            tint = Color(0xFF616161)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = center.presidingOfficerName,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                /* CALL ICON — right of officer name */
                IconButton(
                    onClick = {
                        context.startActivity(
                            Intent(
                                Intent.ACTION_DIAL,
                                Uri.parse("tel:${center.presidingOfficerPhone}")
                            )
                        )
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call Presiding Officer",
                        tint = Color(0xFF1976D2)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Other Officers: ${center.otherOfficers}",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = center.address,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF424242)
            )
        }
    }
}
