package com.example.electionapp.ui.centers

import com.example.electionapp.data.local.entity.VoteCenterEntity

// Converts Database Entity -> UI Model
fun VoteCenterEntity.toUiItem(): VoteCenterItem {
    return VoteCenterItem(entity = this)
}

// Converts UI Model -> Database Entity (if needed later)
fun VoteCenterItem.toEntity(): VoteCenterEntity {
    return this.entity
}