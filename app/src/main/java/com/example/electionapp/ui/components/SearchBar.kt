package com.example.electionapp.ui.components

import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    TextField(
        value = query,              // ✅ controlled
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        placeholder = {
            Text("Search vote center, officer, address")
        },
        singleLine = true
    )
}
