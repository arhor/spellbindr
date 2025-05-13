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
        val selectedClasses: Set<SpellcastingClass> = emptySet()
    )

    val state: StateFlow<SpellSearchViewState> by lazy { uiState.asStateFlow() }

    private val uiState = MutableStateFlow(SpellSearchViewState())
    private val searchQuery = MutableStateFlow("")
    private val selectedClasses = MutableStateFlow<Set<SpellcastingClass>>(emptySet())

    init {
        observeQueryAndClasses()
    }

    fun onSearchQueryChanged(query: String) {
        if (query == uiState.value.searchQuery) {
            return
        }
        uiState.update { it.copy(searchQuery = query) }
        searchQuery.value = query
    }

    fun onClassFilterChanged(classes: Set<SpellcastingClass>) {
        if (classes == uiState.value.selectedClasses) {
            return
        }
        uiState.update { it.copy(selectedClasses = classes) }
        selectedClasses.value = classes
    }

    @OptIn(FlowPreview::class)
    private fun observeQueryAndClasses() {
        viewModelScope.launch {
            combine(searchQuery, selectedClasses) { query, classes -> query to classes }
                .debounce(500.milliseconds)
                .distinctUntilChanged()
                .collect { (query, classes) ->
                    if (query.isBlank()) {
                        loadAllSpells(classes)
                    } else {
                        searchSpells(query, classes)
                    }
                }
        }
    }

    private fun loadAllSpells(classes: Set<SpellcastingClass>) {
        viewModelScope.launch {
            try {
                uiState.update { it.copy(isLoading = true, error = null) }
                val spells = spellRepository.getAllSpells()
                    .filterByClasses(classes)
                    .sortedWith(LEVEL_AND_NAME_COMPARATOR)
                uiState.update { it.copy(spells = spells, isLoading = false) }
            } catch (e: Exception) {
                uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    private fun searchSpells(query: String, classes: Set<SpellcastingClass>) {
        viewModelScope.launch {
            try {
                uiState.update { it.copy(isLoading = true, error = null) }

                val spells =
                    spellRepository.searchSpells(query)
                        .filterByClasses(classes)
                        .sortedWith(LEVEL_AND_NAME_COMPARATOR)

                uiState.update { it.copy(spells = spells, isLoading = false) }
            } catch (e: Exception) {
                uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    private fun List<Spell>.filterByClasses(classes: Set<SpellcastingClass>): List<Spell> {
        return if (classes.isEmpty()) this
        else filter { spell -> classes.all { it in spell.classes } }
    }

    companion object {
        private val LEVEL_AND_NAME_COMPARATOR = compareBy<Spell>({ it.level }, { it.name })
    }
}
