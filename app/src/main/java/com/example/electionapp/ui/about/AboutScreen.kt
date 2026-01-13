package com.example.electionapp.ui.about

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen() {
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFF8F9FA), Color(0xFFE9ECEF))
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("App Credits", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().background(backgroundGradient).padding(padding)) {
            LazyColumn(
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    AboutMemberCard(
                        role = "App Concept",
                        name = "Md. Abdullah Al Roni",
                        designation = "Upazila Nirbahi Officer",
                        location = "Sakhipur, Tangail, Bangladesh",
                        icon = Icons.Default.Info,
                        gradient = Brush.linearGradient(listOf(Color(0xFF6A11CB), Color(0xFF2575FC)))
                    )
                }
                item {
                    AboutMemberCard(
                        role = "App Developer",
                        name = "Md. Tahmidul Hossain",
                        designation = "Upazila ICT Officer (Android Engineer)",
                        location = "Chauddagram, Cumilla, Bangladesh",
                        icon = Icons.Default.Build,
                        gradient = Brush.linearGradient(listOf(Color(0xFF00B09B), Color(0xFF96C93D)))
                    )
                }
                item {
                    AboutMemberCard(
                        role = "App Designer",
                        name = "Hashib Mahmud",
                        designation = "Upazila ICT Officer (UI/UX Specialist)",
                        location = "Sakhipur, Tangail- Bangladesh",
                        icon = Icons.Default.Face,
                        gradient = Brush.linearGradient(listOf(Color(0xFFFF5F6D), Color(0xFFFFC371)))
                    )
                }
                item {
                    AboutMemberCard(
                        role = "Special Thanks",
                        name = "Sharifa Hoque",
                        designation = "District Commissioner",
                        location = "Tangail, Bangladesh",
                        icon = Icons.Default.Favorite,
                        gradient = Brush.linearGradient(listOf(Color(0xFFEB3349), Color(0xFFF45C43)))
                    )
                }
            }
        }
    }
}

@Composable
fun AboutMemberCard(
    role: String,
    name: String,
    designation: String,
    location: String,
    icon: ImageVector,
    gradient: Brush
) {
    ElevatedCard(
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Badge with Gradient
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(gradient, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            Column {
                Text(
                    text = role.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                )
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp
                    ),
                    color = Color(0xFF2D3436)
                )
                Text(
                    text = designation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF636E72)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = location,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}