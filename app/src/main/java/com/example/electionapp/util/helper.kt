
package com.example.electionapp.util
import com.example.electionapp.data.local.entity.VoteCenterDto
import com.example.electionapp.data.local.entity.VoteCenterEntity
import java.text.Normalizer

fun String.normalizeBanglaSafe(): String {
    return Normalizer.normalize(this, Normalizer.Form.NFC)
}

fun VoteCenterDto.toEntity(): VoteCenterEntity {
    return VoteCenterEntity(
        centerNumber = centerNumber,
        centerName = centerName.normalizeBanglaSafe(),
        presidingOfficerName = presidingOfficerName.normalizeBanglaSafe(),
        presidingOfficerPhone = presidingOfficerPhone,
        otherOfficers = otherOfficers,
        address = address.normalizeBanglaSafe(),
        union = union.normalizeBanglaSafe(),
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
