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
    viewModel: AdminContactsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val departments by viewModel.departments.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val history by viewModel.searchHistory.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedContact by remember { mutableStateOf<ContactPerson?>(null) }

    val filtered = remember(searchQuery, departments) {
        if (searchQuery.isBlank()) departments
        else departments.map { d ->
            d.copy(contacts = d.contacts.filter {
                it.name.contains(searchQuery, true) || it.designation.contains(searchQuery, true)
            })
        }.filter { it.contacts.isNotEmpty() }
    }

    val totalContacts = remember(filtered) { filtered.sumOf { it.contacts.size } }

    if (showEditDialog) {
        ContactEditDialog(
            contact = selectedContact,
            onDismiss = { showEditDialog = false },
            onConfirm = { viewModel.save(it); showEditDialog = false }
        )
    }

    if (showDeleteDialog && selectedContact != null) {
        DeleteConfirmationDialog(
            contact = selectedContact!!,
            onDismiss = { showDeleteDialog = false },
            onConfirm = { viewModel.delete(selectedContact!!.id); showDeleteDialog = false }
        )
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) }
    ) {
        // Responsiveness factors
        val horizontalPadding = maxWidth * 0.04f
        val verticalPadding = maxHeight * 0.015f
        val fabPadding = maxHeight * 0.02f

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Official Contacts", fontWeight = FontWeight.Medium) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
            },
            floatingActionButton = {
                if (isAdmin) {
                    FloatingActionButton(
                        onClick = { selectedContact = null; showEditDialog = true },
                        modifier = Modifier.padding(bottom = fabPadding),
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) { Icon(Icons.Default.Add, contentDescription = "Add") }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(
                        Brush.verticalGradient(
                            listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        )
                    )
            ) {
                // Search Section
                Box(modifier = Modifier.padding(horizontal = horizontalPadding, vertical = 8.dp)) {
                    ElevatedSearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        placeholder = "Search name or rank...",
                        history = history,
                        onSearchExecuted = { viewModel.addToHistory(it); focusManager.clearFocus() },
                        onDeleteHistoryItem = { viewModel.removeFromHistory(it) },
                        onHistoryItemClick = { searchQuery = it; focusManager.clearFocus() },
                        elevated = false
                    )
                }

                // List
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = horizontalPadding, vertical = verticalPadding),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filtered, key = { it.title }) { dept ->
                        DepartmentCard(
                            dept = dept,
                            isAdmin = isAdmin,
                            onCall = { phone -> context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))) },
                            onEdit = { person -> selectedContact = person; showEditDialog = true },
                            onDelete = { person -> selectedContact = person; showDeleteDialog = true }
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
        val resWidth = maxWidth.coerceAtMost(600.dp) // Cap width logic

        // Responsive Calculations
        val iconSize = (resWidth * 0.055f).coerceAtLeast(20.dp)
        val circleSize = resWidth * 0.11f
        val fontSizeTitle = with(density) { (resWidth * 0.042f).toSp().coerceAtMost(18.sp) }
        val fontSizeName = with(density) { (resWidth * 0.038f).toSp().coerceAtMost(16.sp) }
        val fontSizeDesignation = with(density) { (resWidth * 0.032f).toSp().coerceAtMost(13.sp) }
        val buttonSize = (resWidth * 0.09f).coerceAtLeast(38.dp)

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
        ) {
            Column {
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
                    Text(dept.title, color = Color.White, fontWeight = FontWeight.Medium, fontSize = fontSizeTitle, modifier = Modifier.weight(1f))
                    Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null, tint = Color.White)
                }

                AnimatedVisibility(visible = expanded) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                        dept.contacts.forEach { person ->
                            ContactItemRow(
                                person = person,
                                accentColor = dept.accentColor,
                                isAdmin = isAdmin,
                                onCall = onCall,
                                onEdit = onEdit,
                                onDelete = onDelete,
                                circleSize = circleSize,
                                fontSizeName = fontSizeName,
                                fontSizeDesignation = fontSizeDesignation,
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