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

/*// --- DATA MODELS ---
data class ContactPerson(
    val name: String,
    val designation: String,
    val phoneNumber: String
)

data class Department(
    val title: String,
    val icon: ImageVector,
    val accentColor: Color,
    val contacts: List<ContactPerson>
)*/

/*// --- MOCK DATA ---
val officialContacts = listOf(
    Department(
        title = "Bangladesh Army",
        icon = Icons.Default.Security,
        accentColor = Color(0xFF2E7D32),
        contacts = listOf(
            ContactPerson("Col. Kamal Ahmed", "Sector Commander", "01711000000"),
            ContactPerson("Maj. Rafiqul Islam", "Operations Officer", "01711111111")
        )
    ),
    Department(
        title = "Bangladesh Police",
        icon = Icons.Default.LocalPolice,
        accentColor = Color(0xFF1565C0),
        contacts = listOf(
            ContactPerson("SP Mahmudullah", "District Superintendent", "01811000000"),
            ContactPerson("ASP Nusrat Jahan", "HQ Coordinator", "01811333333")
        )
    ),
    Department(
        title = "Border Guard (BGB)",
        icon = Icons.Default.Business,
        accentColor = Color(0xFFC62828),
        contacts = listOf(
            ContactPerson("Lt. Col. Tariqul", "Battalion Commander", "01911000000"),
            ContactPerson("Maj. S.M. Nazmul", "Intelligence Lead", "01911555555")
        )
    )
)*/

// --- MAIN SCREEN ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfficialContactsScreen(
    isAdmin: Boolean,
    viewModel: AdminContactsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val departments by viewModel.departments.collectAsState(initial = emptyList())
    var searchQuery by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Filtering logic preserved from previous version
    val filteredDepartments = remember(searchQuery, departments) {
        if (searchQuery.isBlank()) departments
        else departments.map { dept ->
            dept.copy(
                contacts = dept.contacts.filter {
                    it.name.contains(searchQuery, true) ||
                            it.designation.contains(searchQuery, true)
                }
            )
        }.filter { it.contacts.isNotEmpty() }
    }

    // 1. Define the background gradient brush (matched to VoteCenterListScreen)
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFF8F9FA), Color(0xFFE9ECEF))
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent, // 2. Transparent Scaffold
            topBar = {
                TopAppBar(
                    title = { Text("Official Contacts", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { paddingValues ->
            // 3. Wrap content in Box with Gradient Background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundGradient)
                    .padding(paddingValues)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {

                    // 4. Integrated SearchBar (Matches VoteCenterListScreen Layout)
                    Box(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 0.dp)) {
                        SearchBar(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            placeholder = "Search by name or rank"
                        )
                    }

                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (filteredDepartments.isEmpty() && searchQuery.isNotEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No results found for \"$searchQuery\"",
                                        color = Color.Gray
                                    )
                                }
                            }
                        } else {
                            items(filteredDepartments) { dept ->
                                DepartmentCard(dept) { phone ->
                                    context.startActivity(
                                        Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ✅ MOVED OUTSIDE: Standalone Composable for DepartmentCard
@Composable
fun DepartmentCard(dept: Department, onCall: (String) -> Unit) {
    var expanded by remember { mutableStateOf(true) }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp), // Matched to AboutScreen
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(dept.accentColor.copy(alpha = 0.8f), dept.accentColor)
                        )
                    )
                    .clickable { expanded = !expanded }
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(dept.icon, contentDescription = null, tint = Color.White)
                Spacer(Modifier.width(16.dp))
                Text(
                    text = dept.title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    ),
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = Color.White
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    dept.contacts.forEach { person ->
                        ContactItemRow(person, dept.accentColor, onCall)
                    }
                }
            }
        }
    }
}

// ✅ MOVED OUTSIDE: Standalone Composable for ContactItemRow
@Composable
fun ContactItemRow(person: ContactPerson, accentColor: Color, onCall: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(50.dp),
            shape = CircleShape,
            color = accentColor.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = person.name.first().toString(),
                    color = accentColor,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        Spacer(Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = person.name,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D3436) // Matched to AboutScreen
                )
            )
            Text(
                text = person.designation,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF636E72) // Matched to AboutScreen
            )
        }

        FilledIconButton(
            onClick = { onCall(person.phoneNumber) },
            colors = IconButtonDefaults.filledIconButtonColors(containerColor = accentColor),
            modifier = Modifier.size(44.dp)
        ) {
            Icon(Icons.Default.Call, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
        }
    }
}