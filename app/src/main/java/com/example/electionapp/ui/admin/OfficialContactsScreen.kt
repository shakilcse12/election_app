package com.example.electionapp.ui.admin

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.electionapp.ui.admin.components.ContactEditDialog
import com.example.electionapp.ui.admin.components.DeleteConfirmationDialog
import com.example.electionapp.ui.admin.model.ContactPerson
import com.example.electionapp.ui.admin.model.Department
import com.example.electionapp.ui.components.ElevatedSearchBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfficialContactsScreen(
    isAdmin: Boolean,
    scaffoldPadding: PaddingValues,
    viewModel: AdminContactsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    /* -------------------- STATE -------------------- */

    val departments by viewModel.departments.collectAsState()
    val history by viewModel.searchHistory.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedContact by remember { mutableStateOf<ContactPerson?>(null) }

    /* -------------------- FILTER -------------------- */

    val filteredDepartments = remember(searchQuery, departments) {
        if (searchQuery.isBlank()) {
            departments
        } else {
            departments
                .map { dept ->
                    dept.copy(
                        contacts = dept.contacts.filter {
                            it.name.contains(searchQuery, true) ||
                                    it.designation.contains(searchQuery, true)
                        }
                    )
                }
                .filter { it.contacts.isNotEmpty() }
        }
    }

    /* -------------------- DIALOGS -------------------- */

    if (showEditDialog) {
        ContactEditDialog(
            contact = selectedContact,
            onDismiss = { showEditDialog = false },
            onConfirm = {
                viewModel.save(it)
                showEditDialog = false
            }
        )
    }

    if (showDeleteDialog && selectedContact != null) {
        DeleteConfirmationDialog(
            contact = selectedContact!!,
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                viewModel.delete(selectedContact!!.id)
                showDeleteDialog = false
            }
        )
    }

    /* -------------------- LAYOUT -------------------- */

    val bottomInset = scaffoldPadding.calculateBottomPadding()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures {
                    focusManager.clearFocus()
                    isSearchActive = false
                }
            }
    ) {
        val horizontalPadding = maxWidth * 0.04f
        val verticalPadding = maxHeight * 0.015f

        Scaffold(
            contentWindowInsets = WindowInsets.systemBars,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Official Contacts",
                            fontWeight = FontWeight.Medium
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            floatingActionButton = {
                if (isAdmin) {
                    FloatingActionButton(
                        onClick = {
                            selectedContact = null
                            showEditDialog = true
                        },
                        modifier = Modifier.padding(bottom = bottomInset + 16.dp),
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Contact")
                    }
                }
            },
            floatingActionButtonPosition = FabPosition.End
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            )
                        )
                    )
            ) {

                /* ---------------- SEARCH ---------------- */

                Box(
                    modifier = Modifier.padding(
                        horizontal = horizontalPadding,
                        vertical = 8.dp
                    )
                ) {
                    ElevatedSearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        placeholder = "Search name or rank...",
                        history = history,
                        elevated = false,
                        active = isSearchActive,
                        onActiveChange = { isSearchActive = it },
                        onSearchExecuted = {
                            viewModel.addToHistory(it)
                            focusManager.clearFocus()
                        },
                        onDeleteHistoryItem = viewModel::removeFromHistory,
                        onHistoryItemClick = {
                            searchQuery = it
                            focusManager.clearFocus()
                        }
                    )
                }

                /* ---------------- LIST ---------------- */

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(
                        start = horizontalPadding,
                        end = horizontalPadding,
                        top = verticalPadding,
                        bottom = bottomInset + 80.dp // FAB safety
                    )
                ) {
                    items(filteredDepartments, key = { it.title }) { dept ->
                        DepartmentCard(
                            dept = dept,
                            isAdmin = isAdmin,
                            onCall = { phone ->
                                context.startActivity(
                                    Intent(
                                        Intent.ACTION_DIAL,
                                        Uri.parse("tel:$phone")
                                    )
                                )
                            },
                            onEdit = {
                                selectedContact = it
                                showEditDialog = true
                            },
                            onDelete = {
                                selectedContact = it
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DepartmentCard(
    dept: Department,
    isAdmin: Boolean,
    onCall: (String) -> Unit,
    onEdit: (ContactPerson) -> Unit,
    onDelete: (ContactPerson) -> Unit
) {
    var expanded by remember { mutableStateOf(true) }

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val density = LocalDensity.current
        val maxWidthDp = maxWidth.coerceAtMost(600.dp)

        val iconSize = (maxWidthDp * 0.055f).coerceAtLeast(20.dp)
        val circleSize = maxWidthDp * 0.11f
        val buttonSize = (maxWidthDp * 0.09f).coerceAtLeast(38.dp)

        val titleFont = with(density) {
            (maxWidthDp * 0.042f).toSp().coerceAtMost(18.sp)
        }
        val nameFont = with(density) {
            (maxWidthDp * 0.038f).toSp().coerceAtMost(16.sp)
        }
        val designationFont = with(density) {
            (maxWidthDp * 0.032f).toSp().coerceAtMost(13.sp)
        }

        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
        ) {
            Column {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(dept.accentColor)
                        .clickable { expanded = !expanded }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(dept.icon, null, tint = Color.White, modifier = Modifier.size(iconSize))
                    Spacer(Modifier.width(12.dp))
                    Text(
                        dept.title,
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        fontSize = titleFont,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = Color.White
                    )
                }

                if (expanded) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp) // 👈 prevents infinite expansion
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        userScrollEnabled = dept.contacts.size > 4
                    ) {
                        items(
                            items = dept.contacts,
                            key = { it.id } // 🔑 stable key
                        ) { person ->
                            ContactItemRow(
                                person = person,
                                accentColor = dept.accentColor,
                                isAdmin = isAdmin,
                                onCall = onCall,
                                onEdit = onEdit,
                                onDelete = onDelete,
                                circleSize = circleSize,
                                fontSizeName = nameFont,
                                fontSizeDesignation = designationFont,
                                buttonSize = buttonSize
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun ContactItemRow(
    person: ContactPerson,
    accentColor: Color,
    isAdmin: Boolean,
    onCall: (String) -> Unit,
    onEdit: (ContactPerson) -> Unit,
    onDelete: (ContactPerson) -> Unit,
    circleSize: Dp,
    fontSizeName: TextUnit,
    fontSizeDesignation: TextUnit,
    buttonSize: Dp
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(modifier = Modifier.size(circleSize), shape = CircleShape, color = accentColor.copy(alpha = 0.1f)) {
            Box(contentAlignment = Alignment.Center) {
                Text(person.name.first().toString(), color = accentColor, fontWeight = FontWeight.Medium, fontSize = fontSizeName)
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(person.name, fontWeight = FontWeight.Medium, color = Color(0xFF2D3436), fontSize = fontSizeName)
            Text(person.designation, color = Color(0xFF636E72), fontSize = fontSizeDesignation, fontWeight = FontWeight.Normal)
        }

        if (isAdmin) {
            IconButton(onClick = { onEdit(person) }, modifier = Modifier.size(buttonSize)) {
                Icon(Icons.Default.Edit, null, tint = Color.Gray.copy(0.6f), modifier = Modifier.size(buttonSize * 0.5f))
            }
            IconButton(onClick = { onDelete(person) }, modifier = Modifier.size(buttonSize)) {
                Icon(Icons.Default.Delete, null, tint = Color.Red.copy(0.4f), modifier = Modifier.size(buttonSize * 0.5f))
            }
        }

        FilledIconButton(
            onClick = { onCall(person.phoneNumber) },
            colors = IconButtonDefaults.filledIconButtonColors(containerColor = accentColor),
            modifier = Modifier.size(buttonSize)
        ) {
            Icon(Icons.Default.Call, null, tint = Color.White, modifier = Modifier.size(buttonSize * 0.5f))
        }
    }
}

// Inline helper for TextUnit comparison
private fun TextUnit.coerceAtMost(max: TextUnit): TextUnit = if (this.value > max.value) max else this