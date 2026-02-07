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
    elevated: Boolean,
    // ✅ Add these new parameters with defaults to match SearchBar
    history: List<String> = emptyList(),
    onHistoryItemClick: (String) -> Unit = {},
    onDeleteHistoryItem: (String) -> Unit = {},
    onSearchExecuted: (String) -> Unit = {}
) {
    val elevation by animateDpAsState(
        targetValue = if (elevated) 6.dp else 0.dp,
        label = "SearchBarElevation"
    )

    val borderThickness by animateDpAsState(
        targetValue = if (elevated) 1.5.dp else 1.dp,
        label = "SearchBarBorderThickness"
    )

    Surface(
        tonalElevation = elevation,
        shadowElevation = elevation,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = borderThickness,
            color = Color.Black
        )
    ) {
        // ✅ Pass the parameters down to the actual SearchBar
        SearchBar(
            query = query,
            onQueryChange = onQueryChange,
            placeholder = placeholder,
            history = history,
            onHistoryItemClick = onHistoryItemClick,
            onDeleteHistoryItem = onDeleteHistoryItem,
            onSearchExecuted = onSearchExecuted
        )
    }
}


