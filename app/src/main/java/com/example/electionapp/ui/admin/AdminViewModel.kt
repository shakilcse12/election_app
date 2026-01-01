package com.example.electionapp.ui.admin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.electionapp.data.local.entity.VoteCenterEntity
import com.example.electionapp.data.repository.VoteCenterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val repository: VoteCenterRepository
) : ViewModel() {

    var uiState by mutableStateOf(
        VoteCenterEntity(
            id = 0,
            centerNumber = 0,
            presidingOfficerName = "",
            presidingOfficerPhone = "",
            otherOfficers = "",
            address = "",
            latitude = 0.0,
            longitude = 0.0
        )
    )
        private set

    // New function to fetch all centers as Flow
    fun getAllCenters() = repository.getVoteCenters(query = "")

    fun load(id: Int?) {
        if (id == null) return
        viewModelScope.launch {
            repository.getCenter(id)?.let {
                uiState = it
            }
        }
    }

    fun update(field: (VoteCenterEntity) -> VoteCenterEntity) {
        uiState = field(uiState)
    }

    fun save(onDone: () -> Unit) {
        viewModelScope.launch {
            repository.save(uiState)
            onDone()
        }
    }
}

