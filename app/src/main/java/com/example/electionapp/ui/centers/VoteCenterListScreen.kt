package com.example.electionapp.ui.centers

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.electionapp.ui.centers.VoteCenterCard
import com.example.electionapp.ui.centers.VoteCenterViewModel
import com.example.electionapp.ui.components.SearchBar
import com.example.electionapp.util.DummyData

@Composable
fun VoteCenterListScreen(
    viewModel: VoteCenterViewModel = hiltViewModel(),
    onCenterClick: (Int) -> Unit
) {
    // Insert dummy data once
    LaunchedEffect(Unit) {
        viewModel.insertDummyData(DummyData.voteCenters())
    }

    val voteCenters by viewModel.voteCenters.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            SearchBar(
                query = searchQuery,
                onQueryChange = viewModel::onSearchChange
            )
        }

        items(voteCenters) { center ->
            VoteCenterCard(
                center = center,
                onClick = { onCenterClick(center.id) }
            )
        }
    }
}
