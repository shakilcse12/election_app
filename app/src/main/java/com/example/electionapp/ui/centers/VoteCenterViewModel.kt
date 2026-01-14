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

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    private val _voteCenters = _searchQuery
        .debounce(300L)
        .distinctUntilChanged()
        .flatMapLatest { query -> repository.getVoteCenters(query) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val voteCenters: StateFlow<List<VoteCenterEntity>> = _voteCenters

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
            val center = repository.getById(id)
            _selectedCenter.value = center
        }
    }


    fun loadCenterById2(id: Int) {
        viewModelScope.launch {
            repository.getVoteCenters("").first().let { list ->
                _selectedCenter.value = list.find { it.id == id }
            }
        }
    }
}
