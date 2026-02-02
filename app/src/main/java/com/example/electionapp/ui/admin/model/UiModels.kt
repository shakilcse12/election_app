package com.example.electionapp.ui.admin.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class ContactPerson(
    val name: String,
    val designation: String,
    val phoneNumber: String
)

data class Department(
    val title: String,
    val icon: ImageVector,
    val accentColor: Color,
    val contacts: List<ContactPerson>
)
