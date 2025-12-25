package com.example.electionapp.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.electionapp.data.local.entity.VoteCenterEntity

@Composable
fun VoteCenterItem(
    center: VoteCenterEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.padding(8.dp),
        onClick = onClick
    ) {
        Text(
            text = "Vote Center ${center.centerNumber}",
            modifier = Modifier.padding(16.dp)
        )
    }
}
