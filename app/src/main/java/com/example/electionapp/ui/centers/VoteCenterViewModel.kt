package com.example.electionapp.ui.centers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.electionapp.data.local.entity.VoteCenterEntity
import com.example.electionapp.data.repository.VoteCenterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VoteCenterViewModel @Inject constructor(
    private val repository: VoteCenterRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    /**
     * Production Ready Flow:
     * 1. Debounce (wait for typing to stop)
     * 2. Distinct (don't search same thing twice)
     * 3. Fetch Data
     * 4. Map to UI Items using .toUiItem()
     */
    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val voteCenters: StateFlow<List<VoteCenterItem>> = _searchQuery
        .debounce(300L)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            repository.getVoteCenters(query)
        }
        .map { entities ->
            entities.map { it.toUiItem() } // Using the Mapper here!
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val _selectedCenter = MutableStateFlow<VoteCenterEntity?>(null)
    val selectedCenter: StateFlow<VoteCenterEntity?> = _selectedCenter

    fun onSearchChange(query: String) {
        _searchQuery.value = query
    }

    fun insertDummyData(centers: List<VoteCenterEntity>) {
        viewModelScope.launch {
            repository.clearAll()
            repository.insertCenters(centers)
        }
    }

    fun loadCenterById(id: Int) {
        viewModelScope.launch {
            _selectedCenter.value = repository.getById(id)
        }
    }
}