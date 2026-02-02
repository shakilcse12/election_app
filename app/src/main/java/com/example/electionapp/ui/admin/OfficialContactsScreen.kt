package com.example.electionapp.ui.admin

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.electionapp.data.local.entity.OfficialContactEntity
import com.example.electionapp.ui.admin.components.ContactEditDialog
import com.example.electionapp.ui.admin.model.ContactPerson
import com.example.electionapp.ui.admin.model.Department
import com.example.electionapp.ui.components.SearchBar

// --- MAIN SCREEN ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfficialContactsScreen(
    isAdmin: Boolean,
    viewModel: AdminContactsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val departments by viewModel.departments.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    // State for Dialog
    var showDialog by remember { mutableStateOf(false) }
    var selectedContact by remember { mutableStateOf<ContactPerson?>(null) }

    val filtered = remember(searchQuery, departments) {
        if (searchQuery.isBlank()) departments
        else departments.map { d ->
            d.copy(contacts = d.contacts.filter {
                it.name.contains(searchQuery, true) || it.designation.contains(searchQuery, true)
            })
        }.filter { it.contacts.isNotEmpty() }
    }

    if (showDialog) {
        ContactEditDialog(
            contact = selectedContact,
            onDismiss = { showDialog = false },
            onConfirm = {
                viewModel.save(it)
                showDialog = false
            }
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Official Contacts", fontWeight = FontWeight.Bold) }) },
        // ✅ ADMIN ONLY: FAB appears only if isAdmin is true
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(
                    onClick = {
                        selectedContact = null
                        showDialog = true
                    },
                    modifier = Modifier.padding(bottom = 72.dp), // 👈 Adjust this value (usually 56dp-80dp) to sit above your BottomBar
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Contact")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // ✅ Updated hint: "Search by name or rank"
            Box(modifier = Modifier.padding(16.dp)) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search by name or rank"
                )
            }

            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filtered) { dept ->
                    DepartmentCard(
                        dept = dept,
                        isAdmin = isAdmin,
                        onCall = { /* Intent logic */ },
                        onEdit = { selectedContact = it; showDialog = true },
                        onDelete = { id -> viewModel.delete(id.toLong()) }
                    )
                }
            }
        }
    }
}
// ✅ MOVED OUTSIDE: Standalone Composable for DepartmentCard
@Composable
fun DepartmentCard(
    dept: Department,
    isAdmin: Boolean,
    onCall: (String) -> Unit,
    onEdit: (ContactPerson) -> Unit,
    onDelete: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(true) }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                dept.accentColor.copy(0.8f),
                                dept.accentColor
                            )
                        )
                    )
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(dept.icon, null, tint = Color.White)
                Spacer(Modifier.width(16.dp))
                Text(dept.title, color = Color.White, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
                Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null, tint = Color.White)
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(8.dp)) {
                    dept.contacts.forEach { person ->
                        ContactItemRow(person, dept.accentColor, isAdmin, onCall, onEdit, onDelete)
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
    onDelete: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(Modifier.size(44.dp), CircleShape, accentColor.copy(0.1f)) {
            Box(contentAlignment = Alignment.Center) {
                Text(person.name.first().toString(), color = accentColor, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(person.name, fontWeight = FontWeight.Bold, color = Color(0xFF2D3436))
            Text(person.designation, style = MaterialTheme.typography.bodySmall, color = Color(0xFF636E72))
        }

        if (isAdmin) {
            IconButton(onClick = { onEdit(person) }) {
                Icon(Icons.Default.Edit, "Edit", tint = Color.Gray, modifier = Modifier.size(20.dp))
            }
            IconButton(onClick = { onDelete(person.id.toInt()) }) {
                Icon(Icons.Default.Delete, "Delete", tint = Color.Red.copy(0.7f), modifier = Modifier.size(20.dp))
            }
        }

        FilledIconButton(
            onClick = { onCall(person.phoneNumber) },
            colors = IconButtonDefaults.filledIconButtonColors(containerColor = accentColor),
            modifier = Modifier.size(40.dp)
        ) {
            Icon(Icons.Default.Call, null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
    }
}