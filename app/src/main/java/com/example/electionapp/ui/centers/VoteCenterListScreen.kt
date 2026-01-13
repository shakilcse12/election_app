package com.example.electionapp.ui.centers

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.electionapp.ui.components.SearchBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoteCenterListScreen(
    modifier: Modifier = Modifier, // ADD THIS PARAMETER
    isAdmin: Boolean = false,
    onCenterClick: (Int) -> Unit,
    onEditClick: ((Int) -> Unit)? = null,
    onAdminLoginClick: (() -> Unit)? = null,
    viewModel: VoteCenterViewModel = hiltViewModel()
) {
    val voteCenters by viewModel.voteCenters.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var showMenu by remember { mutableStateOf(false) }

    val filteredCenters = remember(voteCenters, searchQuery) {
        val query = searchQuery.trim().lowercase()
        if (query.isEmpty()) voteCenters
        else {
            voteCenters.filter { center ->
                center.centerName.lowercase().contains(query) ||
                        center.centerNumber.toString().contains(query) ||
                        center.address.lowercase().contains(query)
            }
        }
    }

    // We only show the Scaffold's TopBar here if NOT in Admin mode
    // (AdminNavGraph usually provides its own Scaffold/TopBar)
    // Apply the incoming modifier to the root container
    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                if (!isAdmin) {
                    TopAppBar(
                        title = { Text("Vote Centers") },
                        actions = {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }) {
                                DropdownMenuItem(
                                    text = { Text("Admin Login") },
                                    onClick = {
                                        showMenu = false
                                        onAdminLoginClick?.invoke()
                                    }
                                )
                            }
                        }
                    )
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    SearchBar(
                        query = searchQuery,
                        onQueryChange = { viewModel.onSearchChange(it) }
                    )
                }

                if (filteredCenters.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (searchQuery.isEmpty()) "No vote centers available"
                                else "No results found for \"$searchQuery\"",
                                style = MaterialTheme.typography.bodyMedium, color = Color.Gray
                            )
                        }
                    }
                } else {
                    items(items = filteredCenters, key = { it.id }) { center ->
                        VoteCenterCard(
                            center = center,
                            isAdmin = isAdmin,
                            onClick = { onCenterClick(center.id) },
                            onEditClick = { onEditClick?.invoke(center.id) }
                        )
                    }
                }
            }
        }
    }
}