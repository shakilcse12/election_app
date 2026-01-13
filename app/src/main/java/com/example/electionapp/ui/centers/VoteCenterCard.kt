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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.electionapp.data.local.entity.VoteCenterEntity

@Composable
fun VoteCenterCard(
    center: VoteCenterEntity,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    // Define Gradients
    // 1. Blue Gradient for Center Number (Top-start light blue to bottom-end deep blue)
    val blueBadgeGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF4FC3F7), // Light Blue (Start)
            Color(0xFF0288D1)  // Darker Blue (End)
        )
    )

    // 2. Green Gradient for Officer Avatar (Top-start light green to bottom-end deep green)
    val greenAvatarGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF81C784), // Light Green (Start)
            Color(0xFF388E3C)  // Darker Green (End)
        )
    )


    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {

            /* ---------------- TOP ROW ---------------- */
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Number + Name
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // 1. Vote Center Number Badge with Blue Gradient
                    Box(
                        modifier = Modifier
                            .size(width = 48.dp, height = 48.dp)
                            // Apply gradient background with the shape
                            .background(brush = blueBadgeGradient, shape = RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = center.centerNumber.toString(),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White // Text must be white on dark gradient
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // 2. Vote Center Name
                    Text(
                        text = center.centerName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // 3. Map Icon
                IconButton(
                    modifier = Modifier
                        .background(Color(0xFFFFEBEE), CircleShape)
                        .size(42.dp),
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

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            /* ---------------- PRESIDING OFFICER ROW ---------------- */
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Officer Info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Officer Avatar with Green Gradient
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            // Apply gradient background
                            .background(brush = greenAvatarGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Officer",
                            tint = Color.White // Icon must be white on dark gradient
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Officer Name
                    Text(
                        text = center.presidingOfficerName,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Call Icon
                IconButton(
                    modifier = Modifier
                        .background(Color(0xFFE3F2FD), CircleShape)
                        .size(42.dp),
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

            Spacer(modifier = Modifier.height(12.dp))

            /* ---------------- ADDRESS & DETAILS ---------------- */
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Others: ",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.DarkGray
                    )
                    Text(
                        text = center.otherOfficers,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = center.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF616161),
                    lineHeight = 16.sp
                )
            }
        }
    }
}