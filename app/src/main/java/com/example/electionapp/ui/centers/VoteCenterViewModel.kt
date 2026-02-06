package com.example.electionapp.ui.centers

import android.util.Log
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

    // ✅ Union Filter State
    private val _selectedUnion = MutableStateFlow("All")
    val selectedUnion = _selectedUnion.asStateFlow()

    // ✅ Derive unique unions for the Filter UI
    val availableUnions: StateFlow<List<String>> = repository.getVoteCenters("")
        .map { centers ->
            listOf("All") + centers.map { it.union }.distinct().sorted()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("All"))

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val voteCenters: StateFlow<List<VoteCenterItem>> = combine(
        _searchQuery.debounce(300L).distinctUntilChanged(),
        _selectedUnion
    ) { query, union ->
        query to union
    }.flatMapLatest { (query, union) ->
        val sanitizedQuery = query.trim()

        repository.getVoteCenters(sanitizedQuery).map { entities ->
            // Apply Union Filter
            val filtered = if (union == "All") {
                entities
            } else {
                entities.filter { it.union == union }
            }

            // ✅ Mapping to your existing UI Wrapper
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

    fun onUnionSelect(union: String) {
        _selectedUnion.value = union
    }

    fun loadCenterById(id: Int) {
        viewModelScope.launch {
            _selectedCenter.value = repository.getById(id)
        }
    }
}