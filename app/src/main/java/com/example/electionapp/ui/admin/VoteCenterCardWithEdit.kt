package com.example.electionapp.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${center.centerName} (Center ${center.centerNumber})",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = center.address,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            IconButton(onClick = { onEditClick(center.id) }) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Vote Center"
                )
            }
        }
    }
}
