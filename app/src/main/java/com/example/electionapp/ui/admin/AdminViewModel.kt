package com.example.electionapp.ui.admin

import androidx.compose.runtime.*
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
            centerNumber = 0,
            centerName = "",
            presidingOfficerName = "",
            presidingOfficerPhone = "",
            otherOfficers = "",
            address = "",
            latitude = 0.0,
            longitude = 0.0
        )
    )
        private set

    private var isSaving by mutableStateOf(false)
        private set

    fun load(id: Int?) {
        if (id == null) return

        viewModelScope.launch {
            val center = repository.getById(id)
            if (center != null) {
                uiState = center
            }
            // else: keep default empty uiState
        }
    }

    fun update(block: (VoteCenterEntity) -> VoteCenterEntity) {
        uiState = block(uiState)
    }

    private fun isValid(): Boolean {
        return uiState.centerNumber > 0 &&
                uiState.centerName.isNotBlank() &&
                uiState.address.isNotBlank()
    }

    fun save(
        onSuccess: () -> Unit,
        onError: () -> Unit
    ) {
        if (!isValid()) {
            onError()
            return
        }

        viewModelScope.launch {
            isSaving = true
            try {
                repository.save(uiState)
                onSuccess()
            } catch (e: Exception) {
                onError()
            } finally {
                isSaving = false
            }
            isSaving = false
        }
    }

    fun getAllCenters() = repository.getAllCenters()
}
