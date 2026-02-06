// File: VoteCenterListScreen.kt
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
import androidx.compose.ui.unit.sp
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
    val selectedUnions by viewModel.selectedUnions.collectAsState()
    val unionCounts by viewModel.unionCounts.collectAsState()

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val showButton by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }

    val backgroundGradient = Brush.verticalGradient(
        listOf(Color(0xFFF8F9FA), Color(0xFFE9ECEF))
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
                            IconButton(onClick = { /* menu logic */ }) {
                                Icon(Icons.Default.MoreVert, "Menu")
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
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Top")
                    }
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().background(backgroundGradient)) {
                Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

                    // 1. Search Bar
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        SearchBar(
                            query = searchQuery,
                            onQueryChange = { viewModel.onSearchChange(it) },
                            placeholder = "Search centers..."
                        )
                    }

                    // 2. ✅ STUNNING MULTI-SELECT CHIP ROW
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // "All" Reset Chip
                        FilterChip(
                            selected = selectedUnions.isEmpty(),
                            onClick = { viewModel.clearFilters() },
                            label = { Text("All") },
                            enabled = true
                        )

                        unionCounts.forEach { (union, count) ->
                            val isSelected = selectedUnions.contains(union)
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.toggleUnion(union) },
                                label = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(union)
                                        Spacer(Modifier.width(6.dp))
                                        // ✅ Count Badge
                                        Surface(
                                            color = if (isSelected) Color.White.copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.1f),
                                            shape = androidx.compose.foundation.shape.CircleShape
                                        ) {
                                            Text(
                                                text = count.toString(),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                color = if (isSelected) Color.White else Color.Gray
                                            )
                                        }
                                    }
                                },
                                enabled = true,
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Done, null, modifier = Modifier.size(16.dp)) }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = Color.White,
                                    selectedLeadingIconColor = Color.White
                                )
                            )
                        }
                    }

                    // 3. List
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(items = voteCenters, key = { it.entity.id }) { item ->
                            VoteCenterCard(
                                center = item.entity,
                                isAdmin = isAdmin,
                                onClick = { onCenterClick(item.entity.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}