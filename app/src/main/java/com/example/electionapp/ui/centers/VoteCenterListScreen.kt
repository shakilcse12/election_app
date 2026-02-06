package com.example.electionapp.ui.centers

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.electionapp.ui.components.SearchBar
import kotlinx.coroutines.launch

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
    val selectedUnion by viewModel.selectedUnion.collectAsState()
    val unions by viewModel.availableUnions.collectAsState()

    var showMenu by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val showButton by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFF8F9FA), Color(0xFFE9ECEF))
    )

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                if (!isAdmin) {
                    TopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                        title = { Text("Vote Centers", fontWeight = FontWeight.ExtraBold) },
                        actions = {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                            }
                            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                DropdownMenuItem(
                                    text = { Text("Admin Login") },
                                    onClick = { showMenu = false; onAdminLoginClick?.invoke() }
                                )
                            }
                        }
                    )
                }
            },
            floatingActionButton = {
                if (showButton) {
                    FloatingActionButton(
                        onClick = { scope.launch { listState.animateScrollToItem(0) } },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Scroll to top")
                    }
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().background(backgroundGradient)) {
                Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

                    // SEARCH SECTION
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        SearchBar(
                            query = searchQuery,
                            onQueryChange = { viewModel.onSearchChange(it) },
                            placeholder = "Search by name, number or address"
                        )
                    }

                    // ✅ UNION FILTER CHIPS (Fixed parameters)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        unions.forEach { union ->
                            val isSelected = (union == selectedUnion)
                            FilterChip(
                                selected = isSelected, // Explicitly passed
                                onClick = { viewModel.onUnionSelect(union) },
                                label = { Text(union) },
                                enabled = true, // Explicitly passed to avoid "no value passed"
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Done, null, modifier = Modifier.size(18.dp)) }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = Color.White,
                                    selectedLeadingIconColor = Color.White,
                                    containerColor = Color.White.copy(alpha = 0.6f)
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    selectedBorderColor = Color.Transparent,
                                    borderWidth = 1.dp,
                                    enabled = true,
                                    selected = isSelected
                                )
                            )
                        }
                    }

                    // LIST SECTION
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (voteCenters.isEmpty()) {
                            item {
                                EmptySearchResults(searchQuery, selectedUnion)
                            }
                        } else {
                            items(items = voteCenters, key = { it.entity.id }) { item ->
                                VoteCenterCard(
                                    center = item.entity,
                                    isAdmin = isAdmin,
                                    onClick = { onCenterClick(item.entity.id) },
                                    onEditClick = { onEditClick?.invoke(item.entity.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptySearchResults(query: String, union: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No centers found",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = if (query.isEmpty()) "There are no centers in $union."
            else "Nothing matches \"$query\" in $union.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}