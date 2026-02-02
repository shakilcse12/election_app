package com.example.electionapp.ui.admin.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.electionapp.data.local.entity.OfficialContactEntity
import com.example.electionapp.ui.admin.model.ContactPerson

@Composable
fun ContactEditDialog(
    contact: ContactPerson? = null,
    onDismiss: () -> Unit,
    onConfirm: (OfficialContactEntity) -> Unit
) {
    var name by remember { mutableStateOf(contact?.name ?: "") }
    var rank by remember { mutableStateOf(contact?.designation ?: "") }
    var phone by remember { mutableStateOf(contact?.phoneNumber ?: "") }
    var dept by remember { mutableStateOf(contact?.department ?: "Bangladesh Army") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (contact == null) "Add Official" else "Edit Details") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                OutlinedTextField(value = rank, onValueChange = { rank = it }, label = { Text("Rank / Designation") })
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") })

                Text("Department", style = MaterialTheme.typography.labelMedium)
                listOf("Bangladesh Army", "Bangladesh Police", "Border Guard (BGB)").forEach { item ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = dept == item, onClick = { dept = item })
                        Text(item, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(OfficialContactEntity(contact?.id ?: 0, dept, name, rank, phone))
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}