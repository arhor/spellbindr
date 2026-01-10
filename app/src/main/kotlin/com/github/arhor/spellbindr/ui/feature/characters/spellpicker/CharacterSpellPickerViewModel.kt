package com.github.arhor.spellbindr.ui.feature.characters.spellpicker

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.domain.usecase.ObserveCharacterSheetUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveSpellsUseCase
import com.github.arhor.spellbindr.ui.navigation.AppDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@Stable
@HiltViewModel
class CharacterSpellPickerViewModel @Inject constructor(
    private val observeCharacterSheet: ObserveCharacterSheetUseCase,
    private val observeSpells: ObserveSpellsUseCase,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    @Immutable
    private data class State(
        val query: String = "",
        val sourceClass: String = "",
        val defaultSourceClass: String = "",
        val showFavoriteOnly: Boolean = false,
    )

    @Immutable
    private data class Filters(
        val query: String,
        val classes: Set<EntityRef>,
        val favoritesOnly: Boolean,
    )

    private val _state = MutableStateFlow(State())

    val uiState: StateFlow<CharacterSpellPickerUiState> = combine(
        _state,
        observeSpellsUsingFilters(),
        observeCharacterSheet(savedStateHandle.toRoute<AppDestination.CharacterSpellPicker>().characterId)
    ) { state, spellsState, characterSheetState ->
        when {
            spellsState is Loadable.Content && characterSheetState is Loadable.Content ->
                CharacterSpellPickerUiState.Content(
                    query = state.query,
                    spells = spellsState.data,
                    showFavoriteOnly = state.showFavoriteOnly,
                    castingClasses = emptyList(),
                    currentClasses = emptySet(),
                    sourceClass = state.sourceClass,
                    defaultSourceClass = state.defaultSourceClass,
                )

            spellsState is Loadable.Failure -> CharacterSpellPickerUiState.Failure("Failed to load spells.")
            characterSheetState is Loadable.Failure -> CharacterSpellPickerUiState.Failure("Failed to load character.")

            else -> CharacterSpellPickerUiState.Loading
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CharacterSpellPickerUiState.Loading)

    fun onSourceClassChanged(value: String) {
        _state.update { it.copy(sourceClass = value) }
    }

    fun onQueryChanged(query: String) {
        _state.update { it.copy(query = query) }
    }

    fun onFavoritesToggled() {
        _state.update { it.copy(showFavoriteOnly = !it.showFavoriteOnly) }
    }

    private fun observeSpellsUsingFilters(): Flow<Loadable<List<Spell>>> {
        return combine(
            _state.map { it.query.trim() }.distinctUntilChanged().debounce { if (it.isBlank()) 0L else 350L },
            _state.map { resolveClassFilter(it.sourceClass, it.defaultSourceClass) }.distinctUntilChanged(),
            _state.map { it.showFavoriteOnly }.distinctUntilChanged(),
        ) { query, classes, favoriteOnly -> Filters(query, classes, favoriteOnly) }
            .distinctUntilChanged()
            .flatMapLatest { observeSpells(it.query, it.classes, it.favoritesOnly) }
    }

    private fun resolveClassFilter(sourceClass: String, defaultSourceClass: String): Set<EntityRef> =
        sourceClass.ifBlank { defaultSourceClass }
            .trim()
            .lowercase()
            .takeIf { it.isNotBlank() }
            ?.let { setOf(EntityRef(it)) }
            ?: emptySet()
}
