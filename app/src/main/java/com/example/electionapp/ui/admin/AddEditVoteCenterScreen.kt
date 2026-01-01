package com.example.electionapp.ui.admin

import com.example.electionapp.ui.admin.AdminViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun AddEditVoteCenterScreen(
    centerId: Int?,
    onDone: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    LaunchedEffect(centerId) {
        viewModel.load(centerId)
    }

    val state = viewModel.uiState

    AdminScaffold(
        selectedIndex = 1,
        onDashboard = onDone,
        onAdd = {}
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(24.dp)
                .fillMaxSize()
        ) {

            Text(
                text = if (centerId == null) "Add Vote Center" else "Edit Vote Center",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = state.centerNumber.toString(),
                onValueChange = { input ->
                    viewModel.update { current ->
                        current.copy(
                            centerNumber = input.toIntOrNull() ?: 0
                        )
                    }
                },
                label = { Text("Center Number") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.presidingOfficerName,
                onValueChange = { input ->
                    viewModel.update { current ->
                        current.copy(presidingOfficerName = input)
                    }
                },
                label = { Text("Presiding Officer Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.presidingOfficerPhone,
                onValueChange = { input ->
                    viewModel.update { current ->
                        current.copy(presidingOfficerPhone = input)
                    }
                },
                label = { Text("Phone Number") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.address,
                onValueChange = { input ->
                    viewModel.update { current ->
                        current.copy(address = input)
                    }
                },
                label = { Text("Address") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(20.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { viewModel.save(onDone) }
            ) {
                Text("Save Center")
            }
        }
    }
}
