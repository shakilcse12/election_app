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
import java.text.Normalizer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoteCenterListScreen(
    modifier: Modifier = Modifier,
    isAdmin: Boolean = false,
    onCenterClick: (Int) -> Unit,
    onEditClick: ((Int) -> Unit)? = null,
    onAdminLoginClick: (() -> Unit)? = null,
    viewModel: VoteCenterViewModel = hiltViewModel()
) {
    val voteCenters by viewModel.voteCenters.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var showMenu by remember { mutableStateOf(false) }

    // Normalize function for Unicode-safe search (works for Bengali & English)
    fun normalizeText(input: String): String {
        return Normalizer.normalize(input, Normalizer.Form.NFKC)
            .trim()
            .lowercase()
    }

    // Filter vote centers based on normalized search query
    val filteredCenters = remember(voteCenters, searchQuery) {
        val query = normalizeText(searchQuery)
        if (query.isEmpty()) voteCenters
        else voteCenters.filter { center ->
            normalizeText(center.centerName).contains(query) ||
                    center.centerNumber.toString().contains(query) ||
                    normalizeText(center.presidingOfficerName).contains(query) ||
                    normalizeText(center.address).contains(query)
        }
    }

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
                                onDismissRequest = { showMenu = false }
                            ) {
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (searchQuery.isEmpty()) "No vote centers available"
                                else "No results found for \"$searchQuery\"",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    }
                } else {
                    items(
                        items = filteredCenters,
                        key = { it.id }
                    ) { center ->
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
