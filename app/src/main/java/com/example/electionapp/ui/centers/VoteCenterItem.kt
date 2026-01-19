package com.example.electionapp.ui.centers

import com.example.electionapp.data.local.entity.VoteCenterEntity

/**
 * UI Wrapper that holds the data + UI state.
 */
data class VoteCenterItem(
    val entity: VoteCenterEntity,
    val isSaving: Boolean = false,   // Example: Show spinner on specific card
    val isExpanded: Boolean = false, // Example: Expand card details
    val distance: String? = null     // Example: "2.5 km away"
)