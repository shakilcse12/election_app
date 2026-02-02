package com.example.electionapp.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "official_contacts",
    indices = [
        Index(value = ["department"]),
        Index(value = ["isActive"])
    ]
)
data class OfficialContactEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val department: String,          // Bangladesh Army / Police / BGB
    val name: String,
    val designation: String,
    val phoneNumber: String,

    val displayOrder: Int = 0,
    val isActive: Boolean = true
)
