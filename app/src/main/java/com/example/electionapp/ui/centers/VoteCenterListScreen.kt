package com.example.electionapp.ui.centers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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

// Collects List<VoteCenterItem> (already filtered & mapped)
    val voteCenters by viewModel.voteCenters.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var showMenu by remember { mutableStateOf(false) }

    // 1. Create a scroll state to track the list position
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope() // Required to trigger the scroll animation

    // 2. Logic to decide when to show the button (e.g., after scrolling past the first item)
    val showButton by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 }
    }

    // 1. Define the background gradient brush (same as AboutScreen)
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFF8F9FA), Color(0xFFE9ECEF))
    )

    Box(modifier = modifier.fillMaxSize()) {

        Scaffold(
            // 2. Set containerColor to Transparent
            containerColor = Color.Transparent,
            topBar = {
                if (!isAdmin) {

                    TopAppBar(
                        // 3. Make the TopAppBar transparent as well
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent
                        ),

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
            },

            floatingActionButton = {
                if (showButton) {
                    FloatingActionButton(
                        onClick = {
                            // Launch a coroutine to scroll back to the top
                            scope.launch {
                                listState.animateScrollToItem(index = 0)
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Scroll to top")
                    }
                }
            }
        ) { paddingValues ->
            // 4. Wrap the content in a Box with the gradient background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundGradient) // Apply the brush here
            ) {            // Use a Column to stack the SearchBar and the List vertically
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // 1. Fixed SearchBar (outside the LazyColumn)
                Box(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 0.dp)) {
                    SearchBar(
                        query = searchQuery,
                        onQueryChange = { viewModel.onSearchChange(it) }
                    )
                }

                LazyColumn(
                    state = listState, // 4. Attach the scroll state here
                    modifier = Modifier
                        .fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)

                ) {
                    if (voteCenters.isEmpty()) {
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
                            items = voteCenters,
                            key = { it.entity.id } // Use the entity ID as key
                        ) { item ->
                            VoteCenterCard(
                                center = item.entity, // Pass the inner entity to Card
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