package com.example.electionapp.ui.admin

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable
fun AdminScaffold(
    selectedIndex: Int,
    onDashboard: () -> Unit,
    onAdd: () -> Unit,
    onLaw: () -> Unit,
    topBar: @Composable (() -> Unit)? = null, // ✅ MUST EXIST
    content: @Composable (PaddingValues) -> Unit
) {
    val items = listOf(
        AdminNavItem.Dashboard,
        AdminNavItem.AddCenter,
        AdminNavItem.Law
    )

    Scaffold(
        topBar = { topBar?.invoke() }, // ✅ SAFE
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = {
                            when (item) {
                                AdminNavItem.Dashboard -> onDashboard()
                                AdminNavItem.AddCenter -> onAdd()
                                AdminNavItem.Law -> onLaw()
                            }
                        },
                        icon = {
                            Icon(item.icon, contentDescription = item.label)
                        },
                        label = { Text(item.label) }
                    )
                }
            }
        },
        content = content
    )
}
