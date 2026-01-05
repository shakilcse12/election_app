package com.example.electionapp.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.electionapp.data.local.entity.VoteCenterEntity

@Composable
fun AdminDashboardScreen(
    modifier: Modifier = Modifier,
    onAddClick: () -> Unit,
    onEditClick: (Int) -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val centers by viewModel.getAllCenters().collectAsState(initial = emptyList())

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Button(
            onClick = onAddClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text("Add Vote Center")
        }

        Spacer(Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(centers) { center ->
                VoteCenterCard(center, onEditClick)
            }
        }
    }
}

@Composable
private fun VoteCenterCard(
    center: VoteCenterEntity,
    onEditClick: (Int) -> Unit
) {
    Card {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("${center.centerNumber}. ${center.centerName}")
                Text(center.address, style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = { onEditClick(center.id) }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
        }
    }
}
