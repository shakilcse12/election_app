package com.example.electionapp.ui.admin

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.electionapp.ui.admin.components.ContactEditDialog
import com.example.electionapp.ui.admin.components.DeleteConfirmationDialog
import com.example.electionapp.ui.admin.model.ContactPerson
import com.example.electionapp.ui.admin.model.Department
import com.example.electionapp.ui.components.SearchBar

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

    val screenBackground = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Official Contacts", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(
                    onClick = { selectedContact = null; showEditDialog = true },
                    modifier = Modifier.padding(bottom = 72.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) { Icon(Icons.Default.Add, contentDescription = "Add Official") }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(screenBackground)
                .padding(padding)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { focusManager.clearFocus() }
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 2.dp)) {
                    SearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        placeholder = "Search by name or rank"
                    )
                }

                AnimatedVisibility(
                    visible = totalContacts > 0,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Text(
                        text = "Showing $totalContacts officials",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 2.dp)
                    )
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filtered, key = { it.title }) { dept ->
                            DepartmentCard(
                                dept = dept,
                                isAdmin = isAdmin,
                                onCall = { phone ->
                                    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
                                },
                                onEdit = { person ->
                                    selectedContact = person
                                    showEditDialog = true
                                },
                                onDelete = { person ->
                                    selectedContact = person
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }

                    androidx.compose.animation.AnimatedVisibility(
                        visible = totalContacts == 0,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut(),
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = "No officials found for \"$searchQuery\"",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(onClick = { searchQuery = "" }) {
                                Text("Clear Search")
                            }
                        }
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

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color.White
        )
    ) {
        // ✅ OUTER ROW: Contains the continuous Left Bar and the Main Column
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {

            // ✅ CONTINUOUS LEFT BAR: Runs from very top to very bottom
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(6.dp)
                    .background(
                        color = dept.accentColor,
                        shape = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)
                    )
            )

            // MAIN CONTENT COLUMN
            Column(modifier = Modifier.weight(1f)) {

                // ✅ HEADER: Uses MaterialTheme.colorScheme.surface (Off-White)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background( if (isSystemInDarkTheme())
                            MaterialTheme.colorScheme.surfaceVariant
                        else
                            Color(0xFFF1F6FB))
                        .clickable { expanded = !expanded }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = dept.icon,
                        contentDescription = null,
                        tint = dept.accentColor
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text = dept.title,
                        color = dept.accentColor,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = dept.accentColor
                    )
                }

                // SUBTLE DIVIDER
                if (expanded) {
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = dept.accentColor.copy(alpha = 0.1f)
                    )
                }

                // ✅ ANIMATED CONTENT: Absolute White
                AnimatedVisibility(
                    visible = expanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(8.dp)
                    ) {
                        dept.contacts.forEach { person ->
                            ContactItemRow(person, dept.accentColor, isAdmin, onCall, onEdit, onDelete)
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
    onDelete: (ContactPerson) -> Unit
) {
    // ✅ STYLE RESTORED: Row structure and padding back to original
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(44.dp),
            shape = CircleShape,
            color = accentColor.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = person.name.first().toString(),
                    color = accentColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = person.name,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2D3436)
            )
            Text(
                text = person.designation,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF636E72)
            )
        }
        if (isAdmin) {
            IconButton(onClick = { onEdit(person) }) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
            IconButton(onClick = { onDelete(person) }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = Color.Red.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        FilledIconButton(
            onClick = { onCall(person.phoneNumber) },
            colors = IconButtonDefaults.filledIconButtonColors(containerColor = accentColor)
        ) {
            Icon(
                imageVector = Icons.Default.Call,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}