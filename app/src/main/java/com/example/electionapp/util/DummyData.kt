package com.example.electionapp.util

import com.example.electionapp.data.local.entity.VoteCenterEntity

object DummyData {
    fun voteCenters(): List<VoteCenterEntity> = listOf(
        VoteCenterEntity(
            id = 1,
            centerNumber = 56,
            centerName = "Kali hati center",
            presidingOfficerName = "Md. Rahman",
            presidingOfficerPhone = "01700000000",
            otherOfficers = "Officer A, Officer B",
            address = "Govt. Primary School, Ward 3",
            latitude = 23.8103,
            longitude = 90.4125
        ),
        VoteCenterEntity(
            id = 2,
            centerNumber = 57,
            centerName = "South bagan Center",
            presidingOfficerName = "Ms. Sultana",
            presidingOfficerPhone = "01800000000",
            otherOfficers = "Officer C, Officer D",
            address = "High School Playground",
            latitude = 23.8120,
            longitude = 90.4150
        )
    )
}
