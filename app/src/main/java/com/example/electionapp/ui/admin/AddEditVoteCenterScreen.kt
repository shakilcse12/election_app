package com.example.electionapp.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditVoteCenterScreen(
    centerId: Int?,
    onDone: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    var showMap by remember { mutableStateOf(false) }
    val state = viewModel.uiState.value
    val scrollState = rememberScrollState()

    LaunchedEffect(centerId) {
        viewModel.load(centerId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (centerId == null) "Add Vote Center" else "Edit Vote Center")
                },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            // Keyboard-safe FAB
            Box(
                modifier = Modifier
                    .padding(WindowInsets.ime.asPaddingValues()) // moves FAB above keyboard
                    .navigationBarsPadding() // avoids nav bar overlap
            ) {
                FloatingActionButton(onClick = { viewModel.save(onDone) }) {
                    Icon(Icons.Default.Check, contentDescription = "Save Center")
                }
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(scrollState)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            /* ---------- CENTER NUMBER ---------- */
            OutlinedTextField(
                value = state.centerNumber.toString(),
                onValueChange = { input ->
                    viewModel.update { current ->
                        current.copy(centerNumber = input.toIntOrNull() ?: 0)
                    }
                },
                label = { Text("Center Number") },
                modifier = Modifier.fillMaxWidth()
            )

            /* ---------- CENTER NAME ---------- */
            OutlinedTextField(
                value = state.centerName,
                onValueChange = { input ->
                    viewModel.update { current ->
                        current.copy(centerName = input)
                    }
                },
                label = { Text("Center Name") },
                modifier = Modifier.fillMaxWidth()
            )

            /* ---------- PRESIDING OFFICER ---------- */
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

            OutlinedTextField(
                value = state.presidingOfficerPhone,
                onValueChange = { input ->
                    viewModel.update { current ->
                        current.copy(presidingOfficerPhone = input)
                    }
                },
                label = { Text("Presiding Officer Phone") },
                modifier = Modifier.fillMaxWidth()
            )

            /* ---------- ADDRESS ---------- */
            OutlinedTextField(
                value = state.address,
                onValueChange = { input ->
                    viewModel.update { current ->
                        current.copy(address = input)
                    }
                },
                label = { Text("Vote Center Address") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            /* ---------- MAP PICKER ---------- */
            Button(
                onClick = { showMap = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Pick Location on Map")
            }

            Text("Latitude: ${state.latitude}")
            Text("Longitude: ${state.longitude}")

            //Spacer(modifier = Modifier.height(80.dp)) // extra space for FAB
        }
    }

    // Map Picker dialog/screen
    if (showMap) {
        MapPickerScreen { lat, lng ->
            viewModel.update { it.copy(latitude = lat, longitude = lng) }
            showMap = false
        }
    }
}
