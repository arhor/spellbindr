package com.github.arhor.spellbindr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.data.model.Spell
import com.github.arhor.spellbindr.repository.SpellRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SpellSearchUiState(
    val searchQuery: String = "",
    val spells: List<Spell> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SpellSearchViewModel @Inject constructor(
    private val spellRepository: SpellRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SpellSearchUiState())
    val uiState: StateFlow<SpellSearchUiState> = _uiState.asStateFlow()

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchSpells(query)
    }

    private fun searchSpells(query: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                val spells = spellRepository.searchSpells(query)
                _uiState.update { it.copy(spells = spells, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }
}
