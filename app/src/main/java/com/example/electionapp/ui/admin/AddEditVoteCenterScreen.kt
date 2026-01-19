package com.example.electionapp.ui.admin

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.electionapp.data.local.entity.VoteCenterEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditVoteCenterScreen(
    centerId: Int?,
    onDone: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val state = viewModel.uiState
    val isSaving = viewModel.isSaving

    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showMap by remember { mutableStateOf(false) }
    var attemptedSave by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    var initialState by remember { mutableStateOf<VoteCenterEntity?>(null) }

    LaunchedEffect(centerId) {
        viewModel.load(centerId)
    }

    LaunchedEffect(state) {
        if (initialState == null && (centerId == null || state.id != 0)) {
            initialState = state
        }
    }

    val isDirty = initialState != null && state != initialState

    BackHandler(enabled = isDirty) {
        showDiscardDialog = true
    }

    /* ---------------- Validation Logic ---------------- */
    val centerNumberError = attemptedSave && state.centerNumber <= 0
    val centerNameError = attemptedSave && state.centerName.isBlank()
    val officerNameError = attemptedSave && state.presidingOfficerName.isBlank()
    val phoneError = attemptedSave && state.presidingOfficerPhone.length < 6
    val addressError = attemptedSave && state.address.isBlank()
    val hasErrors = centerNumberError || centerNameError || officerNameError || phoneError || addressError

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Discard Changes?") },
            text = { Text("You have unsaved changes. Are you sure you want to go back?") },
            confirmButton = {
                TextButton(onClick = onDone) {
                    Text("Discard", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text("Keep Editing")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (centerId == null) "Add Vote Center" else "Edit Vote Center") },
                navigationIcon = {
                    IconButton(onClick = { if (isDirty) showDiscardDialog = true else onDone() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            Box(modifier = Modifier.padding(WindowInsets.ime.asPaddingValues()).navigationBarsPadding()) {
                FloatingActionButton(
                    onClick = {
                        if (isSaving) return@FloatingActionButton
                        attemptedSave = true

                        if (hasErrors) {
                            scope.launch { snackbarHostState.showSnackbar("Please fix errors") }
                            return@FloatingActionButton
                        }

                        viewModel.save(
                            onSuccess = {
                                // FIXED LOGIC:
                                // We launch the snackbar in its own coroutine so it DOES NOT block navigation
                                scope.launch {
                                    snackbarHostState.showSnackbar("Saved successfully")
                                }

                                // Now we delay for a very short time and navigate immediately
                                scope.launch {
                                    delay(150) // Shortest time for the user to see the button change
                                    onDone()
                                }
                            },
                            onError = {
                                scope.launch { snackbarHostState.showSnackbar("Failed to save") }
                            }
                        )
                    }
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                    } else {
                        Icon(Icons.Default.Check, contentDescription = "Save Center")
                    }
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
            OutlinedTextField(
                value = if (state.centerNumber == 0) "" else state.centerNumber.toString(),
                onValueChange = { input -> viewModel.update { it.copy(centerNumber = input.toIntOrNull() ?: 0) } },
                label = { Text("Center Number") },
                isError = centerNumberError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.centerName,
                onValueChange = { input -> viewModel.update { it.copy(centerName = input) } },
                label = { Text("Center Name") },
                isError = centerNameError,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.presidingOfficerName,
                onValueChange = { input -> viewModel.update { it.copy(presidingOfficerName = input) } },
                label = { Text("Presiding Officer Name") },
                isError = officerNameError,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.presidingOfficerPhone,
                onValueChange = { input -> viewModel.update { it.copy(presidingOfficerPhone = input) } },
                label = { Text("Presiding Officer Phone") },
                isError = phoneError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.address,
                onValueChange = { input -> viewModel.update { it.copy(address = input) } },
                label = { Text("Vote Center Address") },
                isError = addressError,
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )

            Button(onClick = { showMap = true }, modifier = Modifier.fillMaxWidth()) {
                Text("Pick Location on Map")
            }

            Text("Lat: ${state.latitude}, Lng: ${state.longitude}", style = MaterialTheme.typography.bodySmall)
        }
    }

    if (showMap) {
        /*MapPickerScreen { lat, lng, address ->
            viewModel.update { it.copy(latitude = lat, longitude = lng, address = address) }
            showMap = false
        }*/
    }
}