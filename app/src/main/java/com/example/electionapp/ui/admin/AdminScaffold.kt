package com.example.electionapp.ui.admin

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable
fun AdminScaffold(
    selectedIndex: Int,
    onDashboard: () -> Unit,
    onAdd: () -> Unit,
    onLaw: () -> Unit,
    topBar: @Composable () -> Unit,
    content: @Composable (PaddingValues, SnackbarHostState) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = topBar,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedIndex == 0,
                    onClick = onDashboard,
                    icon = {
                        Icon(Icons.Default.Home, contentDescription = "Dashboard")
                    },
                    label = { Text("Dashboard") }
                )

                NavigationBarItem(
                    selected = selectedIndex == 1,
                    onClick = onAdd,
                    icon = {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                    },
                    label = { Text("Add") }
                )

                NavigationBarItem(
                    selected = selectedIndex == 2,
                    onClick = onLaw,
                    icon = {
                        Icon(Icons.Default.Done, contentDescription = "Law")
                    },
                    label = { Text("Law") }
                )
            }
        }
    ) { padding ->
        content(padding, snackbarHostState)
    }
}
