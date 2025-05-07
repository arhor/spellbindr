package com.github.arhor.spellbindr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.data.model.Spell
import com.github.arhor.spellbindr.data.model.SpellcastingClass
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

data class SpellSearchUiState(
    val searchQuery: String = "",
    val spells: List<Spell> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedClass: SpellcastingClass? = null
)

@HiltViewModel
class SpellSearchViewModel @Inject constructor(
    private val spellRepository: SpellRepository
) : ViewModel() {

    val state: StateFlow<SpellSearchUiState> by lazy { uiState.asStateFlow() }

    private val uiState = MutableStateFlow(SpellSearchUiState())
    private val searchQuery = MutableStateFlow("")
    private val selectedClass = MutableStateFlow<SpellcastingClass?>(null)

    init {
        observeSearchQuery()
        observeSelectedClass()
        loadAllSpells()
    }

    fun onSearchQueryChanged(query: String) {
        if (query == uiState.value.searchQuery) {
            return
        }
        uiState.update { it.copy(searchQuery = query) }
        searchQuery.value = query
    }

    fun onClassFilterChanged(spellClass: SpellcastingClass?) {
        if (spellClass == uiState.value.selectedClass) {
            return
        }
        uiState.update { it.copy(selectedClass = spellClass) }
        selectedClass.value = spellClass
    }

    private fun loadAllSpells() {
        viewModelScope.launch {
            try {
                uiState.update { it.copy(isLoading = true, error = null) }
                val spells = spellRepository.getAllSpells()
                    .let { filterByClass(it, selectedClass.value) }
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

    private fun observeSelectedClass() {
        viewModelScope.launch {
            selectedClass
                .collect {
                    if (uiState.value.searchQuery.isBlank()) {
                        loadAllSpells()
                    } else {
                        searchSpells(uiState.value.searchQuery)
                    }
                }
        }
    }

    private fun searchSpells(query: String) {
        viewModelScope.launch {
            try {
                uiState.update { it.copy(isLoading = true, error = null) }
                val spells = spellRepository.searchSpells(query)
                    .let { filterByClass(it, selectedClass.value) }
                    .sortedWith(LEVEL_AND_NAME_COMPARATOR)
                uiState.update { it.copy(spells = spells, isLoading = false) }
            } catch (e: Exception) {
                uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    private fun filterByClass(spells: List<Spell>, spellClass: SpellcastingClass?): List<Spell> {
        return spellClass?.let { spells.filter { it.classes.contains(spellClass) } } ?: spells
    }

    companion object {
        private val LEVEL_AND_NAME_COMPARATOR = compareBy<Spell>({ it.level }, { it.name })
    }
}
