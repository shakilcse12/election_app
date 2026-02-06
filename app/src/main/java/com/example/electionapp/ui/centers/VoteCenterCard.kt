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
import androidx.compose.runtime.remember
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
    isAdmin: Boolean = false,
    onClick: () -> Unit,
    onEditClick: (() -> Unit)? = null
) {
    val context = LocalContext.current

    /* ---------- Gradients ---------- */
    val blueBadgeGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF4FC3F7), Color(0xFF0288D1))
    )
    val greenAvatarGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF81C784), Color(0xFF388E3C))
    )

    val banglaFont = FontFamily.Default

    val presidingOfficerDisplayName = remember(center.presidingOfficerName) {
        center.presidingOfficerName
            .split(",")
            .map { it.trim() }
            .take(2)
            .joinToString(", ")
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            /* ---------------- TOP ROW ---------------- */
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    /* --- Center Number Badge --- */
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(blueBadgeGradient, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = center.centerNumber.toString(),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = banglaFont,
                                color = Color.White
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    /* --- Center Name --- */
                    Text(
                        text = center.centerName,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            fontFamily = banglaFont,
                            color = Color(0xFF1F2937)
                        )
                    )
                }

                /* --- Action Icons --- */
                Row(verticalAlignment = Alignment.CenterVertically) {

                    IconButton(
                        modifier = Modifier
                            .background(Color(0xFFFFEBEE), CircleShape)
                            .size(38.dp),
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
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    if (isAdmin) {
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            modifier = Modifier
                                .background(Color(0xFFF3E5F5), CircleShape)
                                .size(38.dp),
                            onClick = { onEditClick?.invoke() }
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = Color(0xFF7B1FA2),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                thickness = 1.dp,
                color = Color(0xFFE5E7EB)
            )

            /* ---------------- PRESIDING OFFICER ---------------- */
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(greenAvatarGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Officer",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = presidingOfficerDisplayName,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            fontFamily = banglaFont,
                            color = Color(0xFF374151)
                        )
                    )
                }

                IconButton(
                    modifier = Modifier
                        .background(Color(0xFFE3F2FD), CircleShape)
                        .size(38.dp),
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
                        Icons.Default.Call,
                        contentDescription = "Call Presiding Officer",
                        tint = Color(0xFF1976D2),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            /* ---------------- ADDRESS ---------------- */
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF9FAFB), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = "\uD83D\uDCCD ${center.address}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = banglaFont,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = Color(0xFF4B5563)
                )
            }
        }
    }
}
