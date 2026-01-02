package com.example.electionapp.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun AddEditVoteCenterScreen(
    centerId: Int?,
    onDone: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    var showMap by remember { mutableStateOf(false) }
    val state = viewModel.uiState.value

    LaunchedEffect(centerId) {
        viewModel.load(centerId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = if (centerId == null) "Add Vote Center" else "Edit Vote Center",
            style = MaterialTheme.typography.headlineMedium
        )

        /* ---------- CENTER NUMBER ---------- */
        OutlinedTextField(
            value = state.centerNumber.toString(),
            onValueChange = { value ->
                viewModel.update { current ->
                    current.copy(centerNumber = value.toIntOrNull() ?: 0)
                }
            },
            label = { Text("Center Number") },
            //keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        /* ---------- CENTER NAME ---------- */
        OutlinedTextField(
            value = state.centerName,
            onValueChange = { value ->
                viewModel.update { current ->
                    current.copy(centerName = value)
                }
            },
            label = { Text("Center Name") },
            modifier = Modifier.fillMaxWidth()
        )

        /* ---------- PRESIDING OFFICER NAME ---------- */
        OutlinedTextField(
            value = state.presidingOfficerName,
            onValueChange = { value ->
                viewModel.update { current ->
                    current.copy(presidingOfficerName = value)
                }
            },
            label = { Text("Presiding Officer Name") },
            modifier = Modifier.fillMaxWidth()
        )

        /* ---------- PRESIDING OFFICER PHONE ---------- */
        OutlinedTextField(
            value = state.presidingOfficerPhone,
            onValueChange = { value ->
                viewModel.update { current ->
                    current.copy(presidingOfficerPhone = value)
                }
            },
            label = { Text("Presiding Officer Phone") },
            //keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )

        /* ---------- ADDRESS ---------- */
        OutlinedTextField(
            value = state.address,
            onValueChange = { value ->
                viewModel.update { current ->
                    current.copy(address = value)
                }
            },
            label = { Text("Vote Center Address") },
            modifier = Modifier.fillMaxWidth()
        )

        /* ---------- MAP PICKER ---------- */
        Button(
            onClick = { showMap = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Pick Location on Map")
        }

        Text(
            text = "Latitude: ${state.latitude}",
            style = MaterialTheme.typography.bodySmall
        )

        Text(
            text = "Longitude: ${state.longitude}",
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(Modifier.height(16.dp))

        /* ---------- SAVE ---------- */
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { viewModel.save(onDone) }
        ) {
            Text("Save Center")
        }
    }

    if (showMap) {
        MapPickerScreen { lat, lng ->
            viewModel.update { current ->
                current.copy(latitude = lat, longitude = lng)
            }
            showMap = false
        }
    }
}
