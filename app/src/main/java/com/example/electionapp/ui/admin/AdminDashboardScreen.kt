package com.example.electionapp.ui.admin

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun AdminDashboardScreen(
    onAddClick: () -> Unit
) {
    AdminScaffold(
        selectedIndex = 0,
        onDashboard = {},
        onAdd = onAddClick
    ) { padding ->
        Text(
            text = "Admin Dashboard",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(padding)
        )
    }
}
