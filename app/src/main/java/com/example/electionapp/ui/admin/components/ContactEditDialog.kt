package com.example.electionapp.ui.admin.components

import android.Manifest
import android.content.pm.PackageManager
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContactPage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.electionapp.data.local.entity.OfficialContactEntity
import com.example.electionapp.ui.admin.model.ContactPerson

@Composable
fun ContactEditDialog(
    contact: ContactPerson?,
    onDismiss: () -> Unit,
    onConfirm: (OfficialContactEntity) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(contact?.name ?: "") }
    var designation by remember { mutableStateOf(contact?.designation ?: "") }
    var phoneNumber by remember { mutableStateOf(contact?.phoneNumber ?: "") }
    var department by remember { mutableStateOf(contact?.department ?: "Bangladesh Army") }

    val isFormValid = name.isNotBlank() && designation.isNotBlank() && phoneNumber.length >= 5

    // ✅ 1. Picker Launcher (Triggered after permission is granted)
    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri ->
        uri?.let { contactUri ->
            try {
                val projection = arrayOf(ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts._ID)
                context.contentResolver.query(contactUri, projection, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        name = cursor.getString(0)
                        val id = cursor.getString(1)

                        context.contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            arrayOf(id),
                            null
                        )?.use { phoneCursor ->
                            if (phoneCursor.moveToFirst()) {
                                phoneNumber = phoneCursor.getString(0).replace(Regex("[^0-9+]"), "")
                            }
                        }
                    }
                }
            } catch (e: SecurityException) {
                Toast.makeText(context, "Permission required to read phone number", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ✅ 2. Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            contactPickerLauncher.launch(null)
        } else {
            Toast.makeText(context, "Contacts permission is required to import data", Toast.LENGTH_LONG).show()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (contact == null) "Add Official" else "Edit Official") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = {
                            // ✅ 3. Permission Check Logic
                            val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
                            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                                contactPickerLauncher.launch(null)
                            } else {
                                permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                            }
                        }) {
                            Icon(Icons.Default.ContactPage, contentDescription = "Import")
                        }
                    }
                )
                OutlinedTextField(
                    value = designation,
                    onValueChange = { designation = it },
                    label = { Text("Designation / Rank") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Department", style = MaterialTheme.typography.labelMedium)
                val departments = listOf("Bangladesh Army", "Bangladesh Police", "Border Guard (BGB)")
                departments.forEach { dept ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = department == dept, onClick = { department = dept })
                        Text(dept, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(OfficialContactEntity(
                        id = contact?.id ?: 0L,
                        department = department,
                        name = name,
                        designation = designation,
                        phoneNumber = phoneNumber,
                        isActive = true
                    ))
                },
                enabled = isFormValid
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}