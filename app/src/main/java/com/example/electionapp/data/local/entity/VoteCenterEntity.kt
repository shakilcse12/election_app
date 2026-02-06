package com.example.electionapp.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "vote_centers",
    indices = [Index(value = ["centerNumber"], unique = true)]
)
data class VoteCenterEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val centerNumber: Int,
    val centerName: String,

    val presidingOfficerName: String,
    val presidingOfficerPhone: String,
    val otherOfficers: String,

    val address: String,
    val union: String,

    val booths: String,
    val voterAreas: String,

    val maleVoters: String,
    val femaleVoters: String,
    val hijraVoters: String,
    val totalVoters: String,

    val latitude: Double,
    val longitude: Double,

    val remarks: String
)

data class VoteCenterDto(
    val centerNumber: Int,
    val centerName: String,
    val presidingOfficerName: String,
    val presidingOfficerPhone: String,
    val otherOfficers: String,
    val address: String,
    val union: String,
    val booths: String,
    val voterAreas: String,
    val maleVoters: String,
    val femaleVoters: String,
    val hijraVoters: String,
    val totalVoters: String,
    val latitude: Double,
    val longitude: Double,
    val remarks: String
)

