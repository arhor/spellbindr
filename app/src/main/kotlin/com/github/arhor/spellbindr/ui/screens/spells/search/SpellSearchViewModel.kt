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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
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
        val showFavorite: Boolean = false,
        val showFilterDialog: Boolean = false,
        val selectedClasses: Set<SpellcastingClass> = emptySet(),
        val isLoading: Boolean = false,
        val error: String? = null,
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        observeStateChanges()
    }

    fun onFavoritesClicked() {
        _state.update { it.copy(showFavorite = !it.showFavorite) }
    }

    fun onFilterClicked() {
        _state.update { it.copy(showFilterDialog = true) }
    }

    fun onQueryChanged(query: String) {
        if (query != _state.value.query) {
            _state.update { it.copy(query = query) }
        }
    }

    fun onFilterChanged(classes: Set<SpellcastingClass>) {
        _state.update {
            if (classes != _state.value.selectedClasses) {
                it.copy(
                    showFilterDialog = false,
                    selectedClasses = classes,
                )
            } else {
                it.copy(
                    showFilterDialog = false,
                )
            }
        }
    }

    @OptIn(FlowPreview::class)
    private fun observeStateChanges() {
        viewModelScope.launch {
            combine(_state, spellRepository.favoriteSpells) { state, favoriteSpells ->
                Step(
                    state.query,
                    state.selectedClasses,
                    state.showFavorite,
                    favoriteSpells
                )
            }.debounce(350.milliseconds).distinctUntilChanged().collect { step ->
                try {
                    _state.update { it.copy(isLoading = true, error = null) }
                    val spells = spellRepository.findSpells(
                        query = step.query,
                        classes = step.selectedClasses,
                        favorite = step.showFavorite,
                    )
                    _state.update { it.copy(spells = spells, isLoading = false) }
                } catch (e: Exception) {
                    Log.d(TAG, e.message.toString(), e)
                    _state.update { it.copy(error = "Oops, something went wrong...", isLoading = false) }
                }
            }
        }
    }

    private data class Step(
        val query: String,
        val selectedClasses: Set<SpellcastingClass>,
        val showFavorite: Boolean,
        val favoriteSpells: List<String>
    )

    companion object {
        private val TAG = SpellSearchViewModel::class.simpleName
    }
}
