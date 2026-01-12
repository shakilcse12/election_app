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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditVoteCenterScreen(
    centerId: Int?,
    onDone: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val state = viewModel.uiState
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showMap by remember { mutableStateOf(false) }
    var attemptedSave by remember { mutableStateOf(false) }

    LaunchedEffect(centerId) {
        viewModel.load(centerId)
    }

    /* ---------------- Validation ---------------- */

    val centerNumberError =
        attemptedSave && state.centerNumber <= 0

    val centerNameError =
        attemptedSave && state.centerName.isBlank()

    val officerNameError =
        attemptedSave && state.presidingOfficerName.isBlank()

    val phoneError =
        attemptedSave && state.presidingOfficerPhone.length < 6

    val addressError =
        attemptedSave && state.address.isBlank()

    val hasErrors =
        centerNumberError ||
                centerNameError ||
                officerNameError ||
                phoneError ||
                addressError

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
            Box(
                modifier = Modifier
                    .padding(WindowInsets.ime.asPaddingValues())
                    .navigationBarsPadding()
            ) {
                FloatingActionButton(
                    onClick = {
                        if (state.isSaving) return@FloatingActionButton

                        attemptedSave = true

                        if (hasErrors) {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    "Please fix the highlighted errors"
                                )
                            }
                            return@FloatingActionButton
                        }

                        viewModel.save(
                            onSuccess = {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        "Vote center saved successfully"
                                    )
                                }
                                onDone()
                            },
                            onError = {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        "Failed to save vote center"
                                    )
                                }
                            }
                        )
                    }
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(22.dp)
                        )
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

            /* ---------- CENTER NUMBER ---------- */
            OutlinedTextField(
                value = if (state.centerNumber == 0) "" else state.centerNumber.toString(),
                onValueChange = { input ->
                    viewModel.update {
                        it.copy(centerNumber = input.toIntOrNull() ?: 0)
                    }
                },
                label = { Text("Center Number") },
                isError = centerNumberError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                supportingText = {
                    if (centerNumberError) {
                        Text("Center number must be greater than 0")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            /* ---------- CENTER NAME ---------- */
            OutlinedTextField(
                value = state.centerName,
                onValueChange = {
                    viewModel.update { current ->
                        current.copy(centerName = it)
                    }
                },
                label = { Text("Center Name") },
                isError = centerNameError,
                supportingText = {
                    if (centerNameError) {
                        Text("Center name is required")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            /* ---------- PRESIDING OFFICER ---------- */
            OutlinedTextField(
                value = state.presidingOfficerName,
                onValueChange = {
                    viewModel.update { current ->
                        current.copy(presidingOfficerName = it)
                    }
                },
                label = { Text("Presiding Officer Name") },
                isError = officerNameError,
                supportingText = {
                    if (officerNameError) {
                        Text("Officer name is required")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.presidingOfficerPhone,
                onValueChange = {
                    viewModel.update { current ->
                        current.copy(presidingOfficerPhone = it)
                    }
                },
                label = { Text("Presiding Officer Phone") },
                isError = phoneError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                supportingText = {
                    if (phoneError) {
                        Text("Enter a valid phone number")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            /* ---------- ADDRESS ---------- */
            OutlinedTextField(
                value = state.address,
                onValueChange = {
                    viewModel.update { current ->
                        current.copy(address = it)
                    }
                },
                label = { Text("Vote Center Address") },
                isError = addressError,
                supportingText = {
                    if (addressError) {
                        Text("Address is required")
                    }
                },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
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
        }
    }

    /* ---------- MAP PICKER ---------- */
    if (showMap) {
        MapPickerScreen { lat, lng ->
            viewModel.update {
                it.copy(latitude = lat, longitude = lng)
            }
            showMap = false
        }
    }
}
