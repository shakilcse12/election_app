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

    // ✅ Multi-select: Use a Set to store selected unions
    private val _selectedUnions = MutableStateFlow<Set<String>>(emptySet())
    val selectedUnions = _selectedUnions.asStateFlow()

    // ✅ Selection state for the Details Screen (Fixes your error)
    private val _selectedCenter = MutableStateFlow<VoteCenterEntity?>(null)
    val selectedCenter: StateFlow<VoteCenterEntity?> = _selectedCenter

    // ✅ Logic to extract unique unions and their counts
    val unionCounts: StateFlow<Map<String, Int>> = repository.getAllCenters()
        .map { centers ->
            centers.groupBy { it.union }
                .mapValues { it.value.size }
                .toSortedMap()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val voteCenters: StateFlow<List<VoteCenterItem>> = combine(
        _searchQuery.debounce(300L).distinctUntilChanged(),
        _selectedUnions
    ) { query, selectedSet ->
        query to selectedSet
    }.flatMapLatest { (query, selectedSet) ->
        repository.getVoteCenters(query).map { entities ->
            // Apply Multi-select Filter logic
            val filtered = if (selectedSet.isEmpty()) {
                entities
            } else {
                entities.filter { selectedSet.contains(it.union) }
            }

            // ✅ Zero breaking changes: mapping to your existing UI Wrapper
            filtered.map { entity ->
                VoteCenterItem(
                    entity = entity,
                    isSaving = false,
                    isExpanded = false,
                    distance = null
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            repository.seedVoteCentersIfNeeded()
        }
    }

    // --- Search & Filter Actions ---

    fun onSearchChange(query: String) {
        _searchQuery.value = query
    }

    fun toggleUnion(union: String) {
        _selectedUnions.update { current ->
            if (current.contains(union)) current - union else current + union
        }
    }

    fun clearFilters() {
        _selectedUnions.value = emptySet()
    }

    // --- Data Loading (Used by Details Screen) ---

    /**
     * ✅ This is the function your Details screen was missing!
     */
    fun loadCenterById(id: Int) {
        viewModelScope.launch {
            _selectedCenter.value = repository.getById(id)
        }
    }

    // --- Admin/Debug Actions ---

    fun insertDummyData(centers: List<VoteCenterEntity>) {
        viewModelScope.launch {
            repository.clearAll()
            repository.insertCenters(centers)
        }
    }
}