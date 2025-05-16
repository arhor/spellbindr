package com.github.arhor.spellbindr.ui.screens.spells.search

import android.util.Log
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class SpellSearchViewModel @Inject constructor(
    private val spellRepository: SpellRepository,
) : ViewModel() {

    data class State(
        val query: String = "",
        val spells: List<Spell> = emptyList(),
        val showFilterDialog: Boolean = false,
        val selectedClasses: Set<SpellcastingClass> = emptySet(),
        val isLoading: Boolean = false,
        val error: String? = null,
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        observeQueryAndFilter()
    }

    fun onQueryChanged(query: String) {
        if (query != _state.value.query) {
            _state.update { it.copy(query = query) }
        }
    }

    fun onFilterChanged(classes: Set<SpellcastingClass>) {
        if (classes != _state.value.selectedClasses) {
            _state.update {
                it.copy(
                    selectedClasses = classes,
                    showFilterDialog = false,
                )
            }
        }
    }

    fun displayFilterDialog() {
        _state.update { it.copy(showFilterDialog = true) }
    }

    @OptIn(FlowPreview::class)
    private fun observeQueryAndFilter() {
        viewModelScope.launch {
            _state.debounce(300.milliseconds)
                .map { it.query to it.selectedClasses }
                .distinctUntilChanged()
                .collect { (query, classes) -> searchSpells(query, classes) }
        }
    }

    private fun searchSpells(query: String, classes: Set<SpellcastingClass>) {
        try {
            _state.update { it.copy(isLoading = true, error = null) }
            val spells = spellRepository.findSpells(query = query, classes = classes)
            _state.update { it.copy(spells = spells, isLoading = false) }
        } catch (e: Exception) {
            Log.d(TAG, e.message.toString(), e)
            _state.update { it.copy(error = "Oops, something went wrong...", isLoading = false) }
        }
    }

    companion object {
        private val TAG = SpellSearchViewModel::class.simpleName
    }
}
