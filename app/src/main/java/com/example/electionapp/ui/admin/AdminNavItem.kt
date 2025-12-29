package com.example.electionapp.ui.admin

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.ui.graphics.vector.ImageVector

sealed class AdminNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    object Dashboard : AdminNavItem(
        route = "admin_dashboard",
        icon = Icons.Default.AddCircle,
        label = "Dashboard"
    )

    object AddCenter : AdminNavItem(
        route = "admin_add_center",
        icon = Icons.Default.Add,
        label = "Add Center"
    )
}
