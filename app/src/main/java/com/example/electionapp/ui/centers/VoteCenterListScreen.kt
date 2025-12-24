package com.example.electionapp.ui.centers

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun VoteCenterListScreen(
    onItemClick: (Int) -> Unit,
    viewModel: VoteCenterViewModel = hiltViewModel()
) {
    val centers by viewModel.centers.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {

        OutlinedTextField(
            value = "",
            onValueChange = { viewModel.updateSearch(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            label = { Text("Search vote centers") }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(centers, key = { it.id }) { center ->
                VoteCenterCard(
                    center = center,
                    onClick = { onItemClick(center.id) }
                )
            }
        }
    }
}
