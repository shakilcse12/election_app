package com.example.electionapp.ui.admin

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.electionapp.ui.admin.components.ContactEditDialog
import com.example.electionapp.ui.admin.components.DeleteConfirmationDialog
import com.example.electionapp.ui.admin.model.ContactPerson
import com.example.electionapp.ui.admin.model.Department
import com.example.electionapp.ui.components.ElevatedSearchBar
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

    // ✅ ADD THIS LINE to search history
    val history by viewModel.searchHistory.collectAsState()

    // ✅ Use BoxWithConstraints for responsiveness
    BoxWithConstraints(modifier = Modifier.fillMaxSize()// ✅ ADD THIS: Detects taps on the background to clear focus
        .pointerInput(Unit) {
            detectTapGestures(onTap = {
                focusManager.clearFocus()
            })
        }) {

        // Assign to local variables explicitly
        val horizontalPadding = this.maxWidth * 0.04f
        val verticalPadding = this.maxHeight * 0.01f
        val fabPadding = this.maxHeight * 0.09f

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
                    title = { Text("Official Contacts", fontWeight = FontWeight.SemiBold) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            },
            floatingActionButton = {
                if (isAdmin) {
                    FloatingActionButton(
                        onClick = { selectedContact = null; showEditDialog = true },
                        modifier = Modifier.padding(bottom = fabPadding),
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
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.padding(horizontal = horizontalPadding, vertical = verticalPadding)) {
                        // Updated SearchBar call:
                            ElevatedSearchBar(
                                query = searchQuery,
                                onQueryChange = { searchQuery = it }, // ✅ Correct for local 'var' state
                                placeholder = "Search by name or rank",
                                elevated = false, // ✅ Fixed: searchBarElevated doesn't exist here
                                history = history,
                                onSearchExecuted = {
                                    viewModel.addToHistory(it)
                                    focusManager.clearFocus()
                                },
                                onDeleteHistoryItem = { viewModel.removeFromHistory(it) },
                                onHistoryItemClick = {
                                    searchQuery = it
                                    focusManager.clearFocus()
                                }
                            )
                    }


                    androidx.compose.animation.AnimatedVisibility(
                        visible = totalContacts > 0,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Text(
                            text = "Showing $totalContacts officials",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            modifier = Modifier.padding(horizontal = horizontalPadding, vertical = verticalPadding)
                        )
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            contentPadding = PaddingValues(
                                start = horizontalPadding,
                                end = horizontalPadding,
                                top = verticalPadding,
                                bottom = fabPadding
                            ),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
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

                        androidx.compose.animation.AnimatedVisibility(
                            visible = totalContacts == 0,
                            enter = fadeIn() + scaleIn(),
                            exit = fadeOut() + scaleOut(),
                            modifier = Modifier.align(Alignment.Center)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = horizontalPadding * 2)) {
                                Icon(
                                    imageVector = Icons.Default.SearchOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(this@BoxWithConstraints.maxWidth * 0.15f),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                )
                                Spacer(Modifier.height(verticalPadding * 3))
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
        val iconSize = maxWidth * 0.06f
        val circleSize = maxWidth * 0.11f
        val spacing = maxWidth * 0.03f
        val fontSizeTitle = with(LocalDensity.current) { (maxWidth * 0.045f).toSp() }
        val fontSizeContactName = with(LocalDensity.current) { (maxWidth * 0.04f).toSp() }
        val fontSizeDesignation = with(LocalDensity.current) { (maxWidth * 0.035f).toSp() }
        val buttonSize = maxWidth * 0.08f

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // HEADER
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(dept.accentColor)
                        .clickable { expanded = !expanded }
                        .padding(horizontal = spacing, vertical = spacing),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = dept.icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(iconSize)
                    )
                    Spacer(Modifier.width(spacing))
                    Text(
                        text = dept.title,
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        fontSize = fontSizeTitle,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(iconSize)
                    )
                }

                if (expanded) {
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = Color.LightGray.copy(alpha = 0.3f)
                    )
                }

                AnimatedVisibility(
                    visible = expanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(horizontal = spacing, vertical = spacing / 2)
                    ) {
                        dept.contacts.forEach { person ->
                            ContactItemRow(
                                person = person,
                                accentColor = dept.accentColor,
                                isAdmin = isAdmin,
                                onCall = onCall,
                                onEdit = onEdit,
                                onDelete = onDelete,
                                circleSize = circleSize,
                                fontSizeName = fontSizeContactName,
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
    fontSizeName: androidx.compose.ui.unit.TextUnit,
    fontSizeDesignation: androidx.compose.ui.unit.TextUnit,
    buttonSize: Dp
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(circleSize),
            shape = CircleShape,
            color = accentColor.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = person.name.first().toString(),
                    color = accentColor,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = fontSizeName
                )
            }
        }

        Spacer(Modifier.width(circleSize * 0.25f))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = person.name,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2D3436),
                fontSize = fontSizeName
            )
            Text(
                text = person.designation,
                color = Color(0xFF636E72),
                fontSize = fontSizeDesignation
            )
        }

        if (isAdmin) {
            IconButton(onClick = { onEdit(person) }, modifier = Modifier.size(buttonSize)) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(buttonSize * 0.5f)
                )
            }
            IconButton(onClick = { onDelete(person) }, modifier = Modifier.size(buttonSize)) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = Color.Red.copy(alpha = 0.7f),
                    modifier = Modifier.size(buttonSize * 0.5f)
                )
            }
        }

        FilledIconButton(
            onClick = { onCall(person.phoneNumber) },
            colors = IconButtonDefaults.filledIconButtonColors(containerColor = accentColor),
            modifier = Modifier.size(buttonSize)
        ) {
            Icon(
                imageVector = Icons.Default.Call,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(buttonSize * 0.55f)
            )
        }
    }
}

