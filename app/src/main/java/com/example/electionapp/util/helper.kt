
package com.example.electionapp.util
import com.example.electionapp.data.local.entity.VoteCenterDto
import com.example.electionapp.data.local.entity.VoteCenterEntity

fun VoteCenterDto.toEntity(): VoteCenterEntity {
    return VoteCenterEntity(
        centerNumber = centerNumber,
        centerName = centerName,
        presidingOfficerName = presidingOfficerName,
        presidingOfficerPhone = presidingOfficerPhone,
        otherOfficers = otherOfficers,
        address = address,
        booths = booths,
        voterAreas = voterAreas,
        maleVoters = maleVoters,
        femaleVoters = femaleVoters,
        hijraVoters = hijraVoters,
        totalVoters = totalVoters,
        latitude = latitude,
        longitude = longitude,
        remarks = remarks
    )
}
