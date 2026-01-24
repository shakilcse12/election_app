package com.example.electionapp.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    object Centers : BottomNavItem("centers", "Centers", Icons.Default.List)
    object Map : BottomNavItem("map", "Map", Icons.Default.LocationOn)
    object Law : BottomNavItem("law", "Contacts", Icons.Default.CheckCircle)
    // New About Tab
    object About : BottomNavItem("about", "About", Icons.Default.Info)
}
