package com.example.electionapp.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ElevatedSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    elevated: Boolean
) {
    // 1. Animate the Elevation (Shadow)
    val elevation by animateDpAsState(
        targetValue = if (elevated) 6.dp else 0.dp,
        label = "SearchBarElevation"
    )

    // 2. Animate the Border Thickness (Adds a subtle "bold" effect when elevated)
    val borderThickness by animateDpAsState(
        targetValue = if (elevated) 1.5.dp else 1.dp,
        label = "SearchBarBorderThickness"
    )

    Surface(
        tonalElevation = elevation,
        shadowElevation = elevation,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        // ✅ Fixed Black Border (with 1.5.dp logic for better visibility when elevated)
        border = BorderStroke(
            width = borderThickness,
            color = Color.Black
        )
    ) {
        SearchBar(
            query = query,
            onQueryChange = onQueryChange,
            placeholder = placeholder
        )
    }
}


