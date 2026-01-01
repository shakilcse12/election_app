package com.example.electionapp.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.electionapp.ui.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onAddClick: () -> Unit,
    onEditClick: (Int) -> Unit,
    onLogout: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val voteCenters by viewModel
        .getAllCenters()
        .collectAsState(initial = emptyList())

    AdminScaffold(
        selectedIndex = 0,
        onDashboard = {},
        onAdd = onAddClick
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {

            /* ---------- TOP BAR ---------- */
            TopAppBar(
                title = { Text("Admin Dashboard") },
                actions = {
                    IconButton(
                        onClick = {
                            authViewModel.logout()
                            onLogout()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )

            /* ---------- CONTENT ---------- */
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxSize()
            ) {

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onAddClick
                ) {
                    Text("Add Vote Center")
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (voteCenters.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No vote centers added yet",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(voteCenters) { center ->
                            VoteCenterCardWithEdit(
                                center = center,
                                onEditClick = onEditClick
                            )
                        }
                    }
                }
            }
        }
    }
}
