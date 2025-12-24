package com.example.electionapp.ui.centers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.electionapp.data.local.VoteCenterEntity
import com.example.electionapp.data.repository.VoteCenterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VoteCenterViewModel @Inject constructor(
    private val repository: VoteCenterRepository
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val _selectedCenter = MutableStateFlow<VoteCenterEntity?>(null)

    val selectedCenter: StateFlow<VoteCenterEntity?> = _selectedCenter

    val centers: StateFlow<List<VoteCenterEntity>> =
        repository.centers
            .combine(searchQuery) { list, query ->
                if (query.isBlank()) list
                else list.filter {
                    it.centerNumber.toString().contains(query) ||
                            it.address.contains(query, ignoreCase = true) ||
                            it.presidingOfficerName.contains(query, ignoreCase = true)
                }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                emptyList()
            )

    init {
        viewModelScope.launch {
            repository.insertDummyDataIfEmpty()
        }
    }

    fun updateSearch(query: String) {
        searchQuery.value = query
    }

    fun loadCenterById(id: Int) {
        viewModelScope.launch {
            _selectedCenter.value = repository.getCenterById(id)
        }
    }
}
