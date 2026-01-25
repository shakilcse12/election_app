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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.electionapp.data.local.entity.VoteCenterEntity

@Composable
fun VoteCenterCard(
    center: VoteCenterEntity,
    isAdmin: Boolean = false, // Show edit button only for admin
    onClick: () -> Unit,
    onEditClick: (() -> Unit)? = null
) {
    val context = LocalContext.current

    // Gradients for badges
    val blueBadgeGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF4FC3F7), Color(0xFF0288D1))
    )
    val greenAvatarGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF81C784), Color(0xFF388E3C))
    )

    // Bengali-capable font (system default is fine, or replace with Noto Sans Bengali)
    val banglaFont = FontFamily.Default

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            /* ---------------- TOP ROW ---------------- */
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Vote Center Number Badge
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(brush = blueBadgeGradient, shape = RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = center.centerNumber.toString(),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                fontFamily = banglaFont
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Vote Center Name (Bengali supported)
                    Text(
                        text = center.centerName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            fontFamily = banglaFont
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Map Icon
                    IconButton(
                        modifier = Modifier
                            .background(Color(0xFFFFEBEE), CircleShape)
                            .size(40.dp),
                        onClick = {
                            val mapUri = Uri.parse(
                                "geo:${center.latitude},${center.longitude}?q=${center.latitude},${center.longitude}(${center.centerName})"
                            )
                            context.startActivity(Intent(Intent.ACTION_VIEW, mapUri))
                        }
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = "Open Map",
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Edit Icon (Admin only)
                    if (isAdmin) {
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            modifier = Modifier
                                .background(Color(0xFFF3E5F5), CircleShape)
                                .size(40.dp),
                            onClick = { onEditClick?.invoke() }
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = Color(0xFF7B1FA2),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                thickness = 1.dp,
                color = Color(0xFFEEEEEE)
            )

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
                            .clip(CircleShape)
                            .background(brush = greenAvatarGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Officer",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = center.presidingOfficerName,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = banglaFont
                        )
                    )
                }

                // Call Button
                IconButton(
                    modifier = Modifier
                        .background(Color(0xFFE3F2FD), CircleShape)
                        .size(40.dp),
                    onClick = {
                        context.startActivity(
                            Intent(Intent.ACTION_DIAL, Uri.parse("tel:${center.presidingOfficerPhone}"))
                        )
                    }
                ) {
                    Icon(
                        Icons.Default.Call,
                        contentDescription = "Call Presiding Officer",
                        tint = Color(0xFF1976D2),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            /* ---------------- ADDRESS AREA ---------------- */
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF9F9F9), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                /*Text(
                    text = "Others: ${center.otherOfficers}",
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = banglaFont),
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))*/
                Text(
                    text = center.address,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = banglaFont),
                    color = Color.DarkGray
                )
            }
        }
    }
}
