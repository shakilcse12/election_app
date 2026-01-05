package com.example.electionapp.ui.admin

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Call
import androidx.compose.ui.graphics.vector.ImageVector

sealed class AdminNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    object Dashboard : AdminNavItem(
        route = "admin/dashboard",
        icon = Icons.Default.AddCircle,
        label = "Dashboard"
    )

    object AddCenter : AdminNavItem(
        route = "admin/add",
        icon = Icons.Default.Add,
        label = "Add Center"
    )

    object Law : AdminNavItem(
        route = "admin/law",
        icon = Icons.Default.Call,
        label = "Law"
    )
}
