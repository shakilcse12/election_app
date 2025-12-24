package com.example.electionapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vote_centers")
data class VoteCenterEntity(
    @PrimaryKey val id: Int,
    val centerNumber: Int,
    val presidingOfficerName: String,
    val presidingOfficerPhone: String,
    val otherOfficers: String,
    val address: String,
    val latitude: Double,
    val longitude: Double
)
