package com.jorge.mysound.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jorge.mysound.data.repository.MusicRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

class SearchViewModel(private val repository: MusicRepository) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    @OptIn(FlowPreview::class)
    val searchResults = _query
        .debounce(500) // Esperamos medio segundo. No seas ansias, gordo.
        .filter { it.length >= 2 } // Por una letra no buscamos, que es de ser newbie.
        .distinctUntilChanged()
        .flatMapLatest { q ->
            flow {
                _isSearching.value = true
                val result = repository.searchSongs(q)
                emit(result.getOrDefault(emptyList()))
                _isSearching.value = false
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }
}