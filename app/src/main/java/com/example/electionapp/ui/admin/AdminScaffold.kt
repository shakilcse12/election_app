package com.example.electionapp.ui.admin

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun AdminScaffold(
    selectedIndex: Int,
    onDashboard: () -> Unit,
    onAdd: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    val items = listOf(
        AdminNavItem.Dashboard,
        AdminNavItem.AddCenter
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = {
                            when (item) {
                                AdminNavItem.Dashboard -> onDashboard()
                                AdminNavItem.AddCenter -> onAdd()
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        },
        content = content
    )
}
