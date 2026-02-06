// File: VoteCenterListScreen.kt
package com.example.electionapp.ui.centers

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
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

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val showButton by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }

    // ✅ Real-time Calculations for the Summary Bar
    // ✅ FIX: Calculate stats based STRICTLY on what is visible in the list
    val filteredCenterCount = voteCenters.size

    // This dynamically counts how many unique unions are in the CURRENT filtered list
    val activeUnionsCount = remember(voteCenters) {
        voteCenters.map { it.entity.union }.distinct().size
    }

    val selectedUnionCount = if (selectedUnions.isEmpty()) "11" else selectedUnions.size.toString()

    val totalVotersCount = remember(voteCenters) {
        voteCenters.sumOf {
            // Robust parsing: handles "1,200", " 500 ", or empty strings safely
            it.entity.totalVoters.replace(",", "").trim().toIntOrNull() ?: 0
        }
    }

    // Formatting the number nicely (e.g. 12,345)
    val formattedTotalVoters = remember(totalVotersCount) {
        NumberFormat.getNumberInstance(Locale.US).format(totalVotersCount)
    }

    var mDisplayMenu by remember { mutableStateOf(false) }

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
                                            onAdminLoginClick?.invoke() // Triggers navigation
                                        },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.AdminPanelSettings, // Or any relevant icon
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    )
                                }
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

                    // 2. Chip Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 4.dp), // Reduced vertical padding slightly
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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

                    // 3. ✅ NEW STUNNING STATS BAR (Compact & Elegant)
                    // This bar dynamically updates based on the filtered list (Search + Union)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .height(36.dp), // Fixed small height for compactness
                        shape = RoundedCornerShape(50), // Pill shape
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        tonalElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Center Count
                            CompactStatItem(
                                icon = Icons.Default.LocationOn,
                                label = "$filteredCenterCount Centers",
                                color = MaterialTheme.colorScheme.primary
                            )

                            // Divider
                            VerticalDivider(modifier = Modifier.height(16.dp))

                            // Union Count
                            CompactStatItem(
                                icon = Icons.Default.Map,
                                label = "$selectedUnionCount Unions",
                                color = MaterialTheme.colorScheme.tertiary
                            )

                            // Divider
                            VerticalDivider(modifier = Modifier.height(16.dp))

                            // Total Voters
                            CompactStatItem(
                                icon = Icons.Default.Groups,
                                label = formattedTotalVoters,
                                color = Color(0xFF2E7D32) // Green for people
                            )
                        }
                    }
                    // 4. List with "No Results" Empty State
                    if (voteCenters.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No centers found",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )
                                if (searchQuery.isNotEmpty()) {
                                    Text(
                                        text = "Matched nothing for \"$searchQuery\"",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(24.dp))

                                // ✅ The Reset Button
                                Button(
                                    onClick = { viewModel.clearFilters() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        contentColor = MaterialTheme.colorScheme.primary
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    elevation = ButtonDefaults.buttonElevation(0.dp)
                                ) {
                                    Text("Clear search & filters")
                                }
                            }
                        }
                    } else {
                        // 4. List
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

// ✅ HELPER COMPONENT FOR STATS (Clean Code)
@Composable
fun CompactStatItem(icon: ImageVector, label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}