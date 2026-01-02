package com.example.electionapp.ui.admin

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.electionapp.data.local.entity.VoteCenterEntity
import com.example.electionapp.data.repository.VoteCenterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val repository: VoteCenterRepository
) : ViewModel() {

    var uiState = mutableStateOf(
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

    fun getAllCenters(): Flow<List<VoteCenterEntity>> =
        repository.getAllCenters()

    fun load(id: Int?) {
        if (id == null) return
        viewModelScope.launch {
            uiState.value = repository.getById(id)
        }
    }

    fun update(block: (VoteCenterEntity) -> VoteCenterEntity) {
        uiState.value = block(uiState.value)
    }

    fun save(onDone: () -> Unit) {
        viewModelScope.launch {
            repository.save(uiState.value)
            onDone()
        }
    }
}
