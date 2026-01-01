package com.example.electionapp.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.electionapp.data.local.entity.VoteCenterEntity

@Composable
fun VoteCenterCardWithEdit(
    center: VoteCenterEntity,
    onEditClick: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Center ${center.centerNumber}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(4.dp))
                Text("Officer: ${center.presidingOfficerName}")
                Text("Address: ${center.address}")
            }

            IconButton(onClick = { onEditClick(center.id) }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
        }
    }
}
