package com.github.arhor.spellbindr.ui.screens.library.spells.search

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.data.model.EntityRef
import com.github.arhor.spellbindr.data.model.Spell
import com.github.arhor.spellbindr.data.repository.CharacterClassRepository
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

@Stable
@HiltViewModel
class SpellSearchViewModel @Inject constructor(
    private val spellRepository: SpellRepository,
    private val characterClassRepository: CharacterClassRepository,
) : ViewModel() {

    @Immutable
    data class State(
        val query: String = "",
        val spells: List<Spell> = emptyList(),
        val showFavorite: Boolean = false,
        val showFilterDialog: Boolean = false,
        val castingClasses: List<EntityRef> = emptyList(),
        val currentClasses: Set<EntityRef> = emptySet(),
        val isLoading: Boolean = false,
        val error: String? = null,
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            characterClassRepository
                .findSpellcastingClassesRefs()
                .let { refs -> _state.update { it.copy(castingClasses = refs) } }
        }
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

    fun onFilterChanged(classes: Set<EntityRef>) {
        _state.update {
            if (classes != _state.value.currentClasses) {
                it.copy(
                    showFilterDialog = false,
                    currentClasses = classes,
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
            combine(_state, spellRepository.allSpells, spellRepository.favSpells, ::toObservableData)
                .debounce(350.milliseconds)
                .distinctUntilChanged()
                .collect { data ->
                    try {
                        _state.update { it.copy(isLoading = true, error = null) }
                        val spells = spellRepository.findSpells(
                            query = data.query,
                            classes = data.currentClasses,
                            favorite = data.showFavorite,
                        )
                        _state.update { it.copy(spells = spells, isLoading = false) }
                    } catch (e: Exception) {
                        Log.d(TAG, e.message.toString(), e)
                        _state.update { it.copy(error = "Oops, something went wrong...", isLoading = false) }
                    }
                }
        }
    }

    private fun toObservableData(
        state: State, allSpells: List<Spell>, favSpells: List<String>
    ): ObservableData = ObservableData(
        state.query,
        state.castingClasses,
        state.currentClasses,
        state.showFavorite,
        allSpells,
        favSpells,
    )

    private data class ObservableData(
        val query: String,
        val castingClasses: List<EntityRef>,
        val currentClasses: Set<EntityRef>,
        val showFavorite: Boolean,
        val allSpells: List<Spell>,
        val favSpells: List<String>,
    )

    companion object {
        private val TAG = SpellSearchViewModel::class.simpleName
    }
}
