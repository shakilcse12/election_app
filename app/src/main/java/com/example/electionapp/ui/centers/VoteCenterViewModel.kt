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
    internal val repository: VoteCenterRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val voteCenters: StateFlow<List<VoteCenterItem>> = _searchQuery
        .debounce(300L)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            // No changes to logic: Repository handles the decision between getAll() and searchCenters()
            android.util.Log.d("SEARCH", "Querying for: $query") // Diagnostic Log
            val sanitizedQuery = query.trim()
            repository.getVoteCenters(sanitizedQuery)
                .map { centers ->
                    android.util.Log.d("SEARCH_DEBUG", "Found ${centers.size} centers")
                    // Log first few centers to see their numbers
                    centers.take(3).forEach { center ->
                        android.util.Log.d("SEARCH_DEBUG", "Center #${center.centerNumber}: ${center.centerName}")
                    }
                    centers
                }
        }
        .map { entities ->
            entities.map { it.toUiItem() }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val _selectedCenter = MutableStateFlow<VoteCenterEntity?>(null)
    val selectedCenter: StateFlow<VoteCenterEntity?> = _selectedCenter

    init {
        viewModelScope.launch {
            repository.seedVoteCentersIfNeeded()
        }
    }

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