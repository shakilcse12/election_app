package com.example.electionapp.ui.admin.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.material3.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.example.electionapp.data.local.entity.OfficialContactEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactEditDialog(
    initial: OfficialContactEntity?,
    onSave: (OfficialContactEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var designation by remember { mutableStateOf(initial?.designation ?: "") }
    var phone by remember { mutableStateOf(initial?.phoneNumber ?: "") }
    var department by remember { mutableStateOf(initial?.department ?: "Bangladesh Army") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                onSave(
                    OfficialContactEntity(
                        id = initial?.id ?: 0,
                        department = department,
                        name = name,
                        designation = designation,
                        phoneNumber = phone
                    )
                )
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text(if (initial == null) "Add Contact" else "Edit Contact") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Name") })
                OutlinedTextField(designation, { designation = it }, label = { Text("Designation") })
                OutlinedTextField(phone, { phone = it }, label = { Text("Phone") })
                OutlinedTextField(department, { department = it }, label = { Text("Department") })
            }
        }
    )
}
