// File: VoteCenterListScreen.kt
package com.example.electionapp.ui.centers

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.electionapp.ui.components.ElevatedSearchBar
import com.example.electionapp.ui.components.SearchBar
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

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val showButton by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }

    val filteredCenterCount = voteCenters.size
    val selectedUnionCount = if (selectedUnions.isEmpty()) "11" else selectedUnions.size.toString()

    val totalVotersCount = remember(voteCenters) {
        voteCenters.sumOf {
            it.entity.totalVoters.replace(",", "").trim().toIntOrNull() ?: 0
        }
    }

    val formattedTotalVoters = remember(totalVotersCount) {
        NumberFormat.getNumberInstance(Locale.US).format(totalVotersCount)
    }

    var mDisplayMenu by remember { mutableStateOf(false) }

    // Unified background gradient used for both header and list
    val backgroundGradient = Brush.verticalGradient(
        listOf(Color(0xFFF8F9FA), Color(0xFFE9ECEF))
    )

    val headerElevation by animateDpAsState(
        targetValue = if (showButton) 3.dp else 0.dp,
        label = "HeaderElevation"
    )

    val searchBarElevated by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 ||
                    listState.firstVisibleItemScrollOffset > 8
        }
    }

    Box(modifier = modifier.fillMaxSize().background(backgroundGradient)) {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            containerColor = Color.Transparent,
            topBar = {
                // ✅ UPDATED: Transparent Surface with Gradient Column
                Surface(
                    tonalElevation = headerElevation,
                    color = Color.Transparent, // Makes the surface invisible
                ) {
                    Column() {
                        if (!isAdmin) {
                            TopAppBar(
                                scrollBehavior = scrollBehavior,
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = Color.Transparent,
                                    scrolledContainerColor = Color.Transparent
                                ),
                                title = { Text("Vote Centers", fontWeight = FontWeight.ExtraBold) },
                                actions = {
                                    Box {
                                        IconButton(onClick = { mDisplayMenu = true }) {
                                            Icon(Icons.Default.MoreVert, "Menu")
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
                        // 1. Search Bar
                        Box(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp)) {
                            ElevatedSearchBar(
                                query = searchQuery,
                                onQueryChange = { viewModel.onSearchChange(it) },
                                placeholder = "Search centers...",
                                elevated = searchBarElevated // Logic controlled by your scroll state
                            )
                        }
                        // 2. Chip Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                                FilterChip(
                                    selected = selectedUnions.isEmpty(),
                                    onClick = { viewModel.clearFilters() },
                                    label = { Text("All") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = Color.White,
                                        labelColor = MaterialTheme.colorScheme.onSurface,
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = Color.White
                                    )
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
                                    leadingIcon = if (isSelected) {
                                        { Icon(Icons.Default.Done, null, modifier = Modifier.size(16.dp)) }
                                    } else null,
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = Color.White,
                                        labelColor = MaterialTheme.colorScheme.onSurface,
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = Color.White,
                                        selectedLeadingIconColor = Color.White
                                    )

                                )
                            }
                        }

                        // 3. Stats Bar
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 8.dp)
                                .height(38.dp),
                            shape = RoundedCornerShape(50),
                            color = Color(0xFFFCFCFC),
                            tonalElevation = 2.dp
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                CompactStatItem(Icons.Default.LocationOn, "$filteredCenterCount Centers", MaterialTheme.colorScheme.primary)
                                VerticalDivider(modifier = Modifier.height(16.dp))
                                CompactStatItem(Icons.Default.Map, "$selectedUnionCount Unions", MaterialTheme.colorScheme.tertiary)
                                VerticalDivider(modifier = Modifier.height(16.dp))
                                CompactStatItem(Icons.Default.Groups, formattedTotalVoters, Color(0xFF2E7D32))
                            }
                        }
                    }
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
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                if (voteCenters.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Search, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No centers found", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { viewModel.clearFilters() },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), contentColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(8.dp)
                            ) { Text("Clear search & filters") }
                        }
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 12.dp,
                            end = 12.dp,
                            top = 0.dp,      // reduced
                            bottom = 12.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
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

@Composable
fun CompactStatItem(icon: ImageVector, label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = color, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp))
    }
}