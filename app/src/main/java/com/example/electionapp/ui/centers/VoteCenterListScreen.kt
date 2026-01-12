package com.example.electionapp.ui.centers

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.electionapp.ui.components.SearchBar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoteCenterListScreen(
    onCenterClick: (Int) -> Unit,
    onAdminLoginClick: () -> Unit,
    viewModel: VoteCenterViewModel = hiltViewModel()
) {
    val voteCenters by viewModel.voteCenters.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var showMenu by remember { mutableStateOf(false) }

    /* ✅ EXTENDED search – NOTHING removed */
    val filteredCenters = remember(voteCenters, searchQuery) {
        if (searchQuery.isBlank()) {
            voteCenters
        } else {
            val query = searchQuery.lowercase(Locale.getDefault())

            voteCenters.filter { center ->
                center.centerName.lowercase(Locale.getDefault()).contains(query) ||   // ✅ NEW
                        center.centerNumber.toString().contains(query) ||                      // existing
                        center.presidingOfficerName.lowercase(Locale.getDefault()).contains(query) ||
                        center.address.lowercase(Locale.getDefault()).contains(query)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vote Centers") },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Admin Login") },
                            onClick = {
                                showMenu = false
                                onAdminLoginClick()
                            }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            /* ---------- SEARCH ---------- */
            item {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = viewModel::onSearchChange
                )
            }

            /* ---------- RESULTS ---------- */
            if (filteredCenters.isEmpty()) {
                item {
                    Text(
                        text = "No vote centers found",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                items(
                    items = filteredCenters,
                    key = { it.id }
                ) { center ->
                    VoteCenterCard(
                        center = center,
                        onClick = {
                            onCenterClick(center.id)
                        }
                    )
                }
            }
        }
    }
}
