package com.example.electionapp.ui.centers

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.electionapp.ui.components.ElevatedSearchBar
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

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
    val history by viewModel.searchHistory.collectAsState()
    val formattedTotalVoters by viewModel.totalVotersFormatted.collectAsState()

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var isSearchActive by remember { mutableStateOf(false) }
    var mDisplayMenu by remember { mutableStateOf(false) }

    val showScrollToTop by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 }
    }

    val searchBarElevated by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 }
    }

    val headerElevation by animateDpAsState(
        targetValue = if (showScrollToTop) 3.dp else 0.dp,
        label = "HeaderElevation"
    )

    val backgroundGradient = Brush.verticalGradient(
        listOf(Color(0xFFF8F9FA), Color(0xFFE9ECEF))
    )

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = modifier
            .background(backgroundGradient)
            .then(
                if (!isAdmin) Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
                else Modifier
            )
            .pointerInput(Unit) {
                detectTapGestures {
                    focusManager.clearFocus()
                    isSearchActive = false
                }
            },
        containerColor = Color.Transparent, // Correct: Background is handled by modifier
        topBar = {
            Surface(
                tonalElevation = headerElevation,
                color = Color.Transparent
            ) {
                Column {
                    if (!isAdmin) {
                        TopAppBar(
                            title = { Text("Vote Centers", fontWeight = FontWeight.ExtraBold) },
                            scrollBehavior = scrollBehavior,
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.Transparent,
                                scrolledContainerColor = Color.Transparent
                            ),
                            actions = {
                                Box {
                                    IconButton(onClick = { mDisplayMenu = true }) {
                                        Icon(Icons.Default.MoreVert, null)
                                    }
                                    DropdownMenu(
                                        expanded = mDisplayMenu,
                                        onDismissRequest = { mDisplayMenu = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Admin Login") },
                                            onClick = {
                                                mDisplayMenu = false
                                                onAdminLoginClick?.invoke()
                                            },
                                            leadingIcon = {
                                                Icon(Icons.Default.AdminPanelSettings, null, modifier = Modifier.size(18.dp))
                                            }
                                        )
                                    }
                                }
                            }
                        )
                    }

                    VoteCenterHeader(
                        searchQuery = searchQuery,
                        history = history,
                        selectedUnions = selectedUnions,
                        unionCounts = unionCounts,
                        searchBarElevated = searchBarElevated,
                        isSearchActive = isSearchActive,
                        onSearchActiveChange = { isSearchActive = it },
                        onSearchChange = viewModel::onSearchChange,
                        onSearchExecuted = viewModel::addToHistory,
                        onDeleteHistory = viewModel::removeFromHistory,
                        onClearFilters = viewModel::clearFilters,
                        onToggleUnion = viewModel::toggleUnion
                    )

                    StatsBar(
                        centers = voteCenters.size,
                        unions = if (selectedUnions.isEmpty()) unionCounts.size.toString() else selectedUnions.size.toString(),
                        voters = formattedTotalVoters,
                        elevated = searchBarElevated
                    )
                }
            }
        },
        floatingActionButton = {
            if (showScrollToTop) {
                FloatingActionButton(
                    onClick = { scope.launch { listState.animateScrollToItem(0) } }
                ) {
                    Icon(Icons.Default.KeyboardArrowUp, null)
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            if (voteCenters.isEmpty() && searchQuery.isNotEmpty()) {
                EmptyState(onClear = {
                    viewModel.clearFilters()
                    isSearchActive = false
                    focusManager.clearFocus()
                })
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = voteCenters,
                        key = { it.entity.id },
                        contentType = { "VoteCenter" }
                    ) { item ->
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

/* ---------------- Header ---------------- */

@Composable
private fun VoteCenterHeader(
    searchQuery: String,
    history: List<String>,
    selectedUnions: Set<String>,
    unionCounts: Map<String, Int>,
    searchBarElevated: Boolean,
    isSearchActive: Boolean,
    onSearchActiveChange: (Boolean) -> Unit,
    onSearchChange: (String) -> Unit,
    onSearchExecuted: (String) -> Unit,
    onDeleteHistory: (String) -> Unit,
    onClearFilters: () -> Unit,
    onToggleUnion: (String) -> Unit
) {
    Column {
        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
            ElevatedSearchBar(
                query = searchQuery,
                onQueryChange = onSearchChange,
                placeholder = "Search centers...",
                elevated = searchBarElevated,
                history = history,
                active = isSearchActive,
                onActiveChange = onSearchActiveChange,
                onSearchExecuted = onSearchExecuted,
                onDeleteHistoryItem = onDeleteHistory,
                onHistoryItemClick = onSearchChange
            )
        }

        UnionFilterRow(
            selectedUnions = selectedUnions,
            unionCounts = unionCounts,
            onClearFilters = onClearFilters,
            onToggleUnion = onToggleUnion
        )
    }
}

@Composable
private fun UnionFilterRow(
    selectedUnions: Set<String>,
    unionCounts: Map<String, Int>,
    onClearFilters: () -> Unit,
    onToggleUnion: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // "All" Filter Chip
        FilterChip(
            selected = selectedUnions.isEmpty(),
            onClick = onClearFilters,
            label = { Text("All") },
            colors = FilterChipDefaults.filterChipColors(
                containerColor = Color.White,
                selectedContainerColor = MaterialTheme.colorScheme.primary,
                selectedLabelColor = Color.White
            )
        )

        // Union Count Chips
        unionCounts.forEach { (union, count) ->
            val isSelected = selectedUnions.contains(union)
            FilterChip(
                selected = isSelected,
                onClick = { onToggleUnion(union) },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(union)
                        Spacer(Modifier.width(6.dp))
                        Surface(
                            color = if (isSelected) Color.White.copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.1f),
                            shape = CircleShape
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
                leadingIcon = if (isSelected) {
                    { Icon(Icons.Default.Done, null, modifier = Modifier.size(16.dp)) }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color.White,
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = Color.White,
                    selectedLeadingIconColor = Color.White
                )
            )
        }
    }
}

@Composable
private fun StatsBar(
    centers: Int,
    unions: String,
    voters: String,
    elevated: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.9f),
        tonalElevation = if (elevated) 2.dp else 0.dp,
        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SlimStatItem(
                icon = Icons.Default.LocationOn,
                value = centers.toString(),
                label = "CENTERS",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            Box(modifier = Modifier.width(1.dp).height(16.dp).background(Color(0xFFE5E7EB)))
            SlimStatItem(
                icon = Icons.Default.Map,
                value = unions,
                label = "UNIONS",
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
            Box(modifier = Modifier.width(1.dp).height(16.dp).background(Color(0xFFE5E7EB)))
            SlimStatItem(
                icon = Icons.Default.Groups,
                value = voters,
                label = "VOTERS",
                color = Color(0xFF2E7D32),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun SlimStatItem(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )

        Spacer(modifier = Modifier.width(6.dp))

        Column {
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp,
                    lineHeight = 14.sp
                ),
                color = Color(0xFF1F2937)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 8.sp,
                    color = Color.Gray,
                    lineHeight = 10.sp
                )
            )
        }
    }
}

@Composable
fun EmptyState(onClear: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Search,
                null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "No centers found",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onClear,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(8.dp)
            ) { Text("Clear search & filters") }
        }
    }
}
