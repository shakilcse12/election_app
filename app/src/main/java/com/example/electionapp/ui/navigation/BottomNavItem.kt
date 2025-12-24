package com.example.electionapp.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.List
//import androidx.compose.material.icons.filled.Map
//import androidx.compose.material.icons.filled.Security
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    object Centers : BottomNavItem("centers", "Centers", Icons.Default.List)
    object Map : BottomNavItem("map", "Map", Icons.Default.Call)
    object Law : BottomNavItem("law", "Law", Icons.Default.CheckCircle)
}
