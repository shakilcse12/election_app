package com.example.electionapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vote_centers")
data class VoteCenterEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val centerNumber: Int,
    val centerName: String,
    val presidingOfficerName: String,
    val presidingOfficerPhone: String,
    val otherOfficers: String,
    val address: String,

    val latitude: Double,
    val longitude: Double
)