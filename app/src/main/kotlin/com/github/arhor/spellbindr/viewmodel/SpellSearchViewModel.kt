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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class SpellSearchViewModel @Inject constructor(
    private val spellRepository: SpellRepository
) : ViewModel() {

    data class SpellSearchViewState(
        val searchQuery: String = "",
        val spells: List<Spell> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val selectedClass: SpellcastingClass? = null
    )

    val state: StateFlow<SpellSearchViewState> by lazy { uiState.asStateFlow() }

    private val uiState = MutableStateFlow(SpellSearchViewState())
    private val searchQuery = MutableStateFlow("")
    private val selectedClass = MutableStateFlow<SpellcastingClass?>(null)

    init {
        observeQueryAndClass()
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

    @OptIn(FlowPreview::class)
    private fun observeQueryAndClass() {
        viewModelScope.launch {
            combine(searchQuery, selectedClass) { query, spellClass -> query to spellClass }
                .debounce(500.milliseconds)
                .distinctUntilChanged()
                .collect { (query, spellClass) ->
                    if (query.isBlank()) {
                        loadAllSpells(spellClass)
                    } else {
                        searchSpells(query, spellClass)
                    }
                }
        }
    }

    private fun loadAllSpells(spellClass: SpellcastingClass?) {
        viewModelScope.launch {
            try {
                uiState.update { it.copy(isLoading = true, error = null) }
                val spells = spellRepository.getAllSpells()
                    .filterByClass(spellClass)
                    .sortedWith(LEVEL_AND_NAME_COMPARATOR)
                uiState.update { it.copy(spells = spells, isLoading = false) }
            } catch (e: Exception) {
                uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    private fun searchSpells(query: String, spellClass: SpellcastingClass?) {
        viewModelScope.launch {
            try {
                uiState.update { it.copy(isLoading = true, error = null) }

                val spells =
                    spellRepository.searchSpells(query)
                        .filterByClass(spellClass)
                        .sortedWith(LEVEL_AND_NAME_COMPARATOR)

                uiState.update { it.copy(spells = spells, isLoading = false) }
            } catch (e: Exception) {
                uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    private fun List<Spell>.filterByClass(spellClass: SpellcastingClass?): List<Spell> {
        return spellClass?.let { filter { it.classes.contains(spellClass) } } ?: this
    }

    companion object {
        private val LEVEL_AND_NAME_COMPARATOR = compareBy<Spell>({ it.level }, { it.name })
    }
}
