package com.github.arhor.spellbindr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.data.model.Spell
import com.github.arhor.spellbindr.data.repository.SpellRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

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

    val state: StateFlow<SpellSearchUiState> by lazy { uiState.asStateFlow() }

    private val uiState = MutableStateFlow(SpellSearchUiState())
    private val searchQuery = MutableStateFlow("")

    init {
        observeSearchQuery()
        loadAllSpells()
    }

    fun onSearchQueryChanged(query: String) {
        if (query == uiState.value.searchQuery) {
            return
        }
        uiState.update { it.copy(searchQuery = query) }
        searchQuery.value = query
    }

    private fun loadAllSpells() {
        viewModelScope.launch {
            try {
                uiState.update { it.copy(isLoading = true, error = null) }
                val spells = spellRepository.getAllSpells()
                    .sortedWith(LEVEL_AND_NAME_COMPARATOR)
                uiState.update { it.copy(spells = spells, isLoading = false) }
            } catch (e: Exception) {
                uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    @OptIn(FlowPreview::class)
    private fun observeSearchQuery() {
        viewModelScope.launch {
            searchQuery
                .debounce(500.milliseconds)
                .distinctUntilChanged()
                .collect { query ->
                    if (query.isBlank()) {
                        loadAllSpells()
                    } else {
                        searchSpells(query)
                    }
                }
        }
    }

    private fun searchSpells(query: String) {
        viewModelScope.launch {
            try {
                uiState.update { it.copy(isLoading = true, error = null) }
                val spells = spellRepository.searchSpells(query)
                    .sortedWith(LEVEL_AND_NAME_COMPARATOR)
                uiState.update { it.copy(spells = spells, isLoading = false) }
            } catch (e: Exception) {
                uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    companion object {
        private val LEVEL_AND_NAME_COMPARATOR = compareBy<Spell>({ it.level }, { it.name })
    }
}
