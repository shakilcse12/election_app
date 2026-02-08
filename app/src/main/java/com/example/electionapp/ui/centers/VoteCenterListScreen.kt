// File: VoteCenterListScreen.kt
package com.example.electionapp.ui.centers

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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

    //val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
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

    // for search history
    val history by viewModel.searchHistory.collectAsState()

    // 3. Stats Bar
    // We use the searchBarElevated boolean to drive the stats bar animation too
    val statsBorderThickness by animateDpAsState(
        targetValue = if (searchBarElevated) 1.dp else 0.dp,
        label = "StatsBarBorderThickness"
    )

    val statsBackgroundColor by animateColorAsState(
        targetValue = if (searchBarElevated) Color.White else Color(0xFFFCFCFC),
        label = "StatsBarBackgroundColor"
    )

    val focusManager = LocalFocusManager.current // Ensure this is available

    // 1. Only create scroll behavior if NOT admin
    val scrollBehavior = if (!isAdmin) {
        TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    } else {
        null
    }

    // 2. Conditional modifier for the Scaffold
    val scaffoldModifier = if (!isAdmin && scrollBehavior != null) {
        modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    } else {
        modifier // In admin mode, we just use the modifier passed from NavGraph
    }

    //val voteCenters by viewModel.voteCenters.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState() // 👈 Add this
    var isSearchActive by remember { mutableStateOf(false) }

    Scaffold(
        modifier = scaffoldModifier
            .background(backgroundGradient),
        containerColor = Color.Transparent,
        topBar = {
            Surface(
                tonalElevation = headerElevation,
                color = Color.Transparent,
            ) {
                Column {
                    if (!isAdmin && scrollBehavior != null) {
                        TopAppBar(
                            scrollBehavior = scrollBehavior,
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.Transparent,
                                scrolledContainerColor = Color.Transparent
                            ),
                            title = {
                                Text(
                                    "Vote Centers",
                                    fontWeight = FontWeight.ExtraBold
                                )
                            },
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
                                                Icon(
                                                    Icons.Default.AdminPanelSettings,
                                                    null,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        )
                    }

                    // 1. Search Bar
                    Box(
                        modifier = Modifier.padding(
                            start = 16.dp,
                            end = 16.dp,
                            top = 8.dp,
                            bottom = 4.dp
                        )
                    ) {
                        ElevatedSearchBar(
                            query = searchQuery,
                            onQueryChange = { viewModel.onSearchChange(it) },
                            placeholder = "Search centers...",
                            elevated = searchBarElevated,
                            history = history,
                            onSearchExecuted = { viewModel.addToHistory(it) },
                            onDeleteHistoryItem = { viewModel.removeFromHistory(it) },
                            onHistoryItemClick = { viewModel.onSearchChange(it) },
                            active = isSearchActive, // Pass the state
                            onActiveChange = { isSearchActive = it }
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

                    // 3. Stats Bar
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.9f),
                        tonalElevation = if (searchBarElevated) 2.dp else 0.dp,
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
                                value = filteredCenterCount.toString(),
                                label = "CENTERS",
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            Box(modifier = Modifier.width(1.dp).height(16.dp).background(Color(0xFFE5E7EB)))
                            SlimStatItem(
                                icon = Icons.Default.Map,
                                value = selectedUnionCount,
                                label = "UNIONS",
                                color = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.weight(1f)
                            )
                            Box(modifier = Modifier.width(1.dp).height(16.dp).background(Color(0xFFE5E7EB)))
                            SlimStatItem(
                                icon = Icons.Default.Groups,
                                value = formattedTotalVoters,
                                label = "VOTERS",
                                color = Color(0xFF2E7D32),
                                modifier = Modifier.weight(1f)
                            )
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
        Box(modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
            .padding(paddingValues)) {

            // --- LOGIC FIX: Check for Loading vs Empty ---
            val isActuallyEmpty = voteCenters.isEmpty() && searchQuery.isNotEmpty()
            val isInitialLoad = voteCenters.isEmpty() && searchQuery.isEmpty()

            when {
                // 1. Initial Loading State: Just show the background, no "Not Found" icon
                isInitialLoad -> {
                    // Optionally add a CircularProgressIndicator() here
                }

                // 2. Real Empty State: User searched for something that doesn't exist
                isActuallyEmpty -> {
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
                                onClick = {
                                    // 1. Clear the text
                                    viewModel.onSearchChange("")
                                    viewModel.clearFilters()

                                    // 2. FORCE clear focus to hide keyboard and dropdown
                                    isSearchActive = false // Force collapse the dropdown
                                    focusManager.clearFocus() // hide the keyboard
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    contentColor = MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) { Text("Clear search & filters") }
                        }
                    }
                }

                // 3. The List
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 12.dp,
                            end = 12.dp,
                            top = 8.dp, // Small top padding for first card
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
            modifier = Modifier.size(16.dp) // Small, sharp icon
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