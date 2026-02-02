package com.example.electionapp.ui.admin.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class ContactPerson(
    val id: Long, // Added ID for targeting specific records
    val name: String,
    val designation: String,
    val phoneNumber: String,
    val department: String // Added to fix the "Unresolved reference"
)

data class Department(
    val title: String,
    val icon: ImageVector,
    val accentColor: Color,
    val contacts: List<ContactPerson>
)
