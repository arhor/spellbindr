package com.github.arhor.spellbindr.ui.feature.compendium.spells

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.domain.usecase.ObserveSpellcastingClassesUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveSpellsUseCase
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
class SpellsViewModel @Inject constructor(
    private val observeSpells: ObserveSpellsUseCase,
    private val observeSpellcastingClasses: ObserveSpellcastingClassesUseCase,
) : ViewModel() {

    @Immutable
    private data class State(
        val query: String = "",
        val showFavoriteOnly: Boolean = false,
        val showFilterDialog: Boolean = false,
        val characterClasses: Set<EntityRef> = emptySet(),
    )

    @Immutable
    private data class Filters(
        val query: String,
        val classes: Set<EntityRef>,
        val favoritesOnly: Boolean,
    )

    private val _state = MutableStateFlow(State())

    val uiState: StateFlow<SpellsUiState> = combine(
        _state,
        observeSpellsUsingFilters(),
        observeSpellcastingClasses(),
    ) { state, spells, classes ->
        when {
            spells is Loadable.Success && classes is Loadable.Success ->
                SpellsUiState.Content(
                    query = state.query,
                    spells = spells.data,
                    showFavoriteOnly = state.showFavoriteOnly,
                    showFilterDialog = state.showFilterDialog,
                    castingClasses = classes.data.map { EntityRef(it.id) },
                    currentClasses = state.characterClasses,
                )

            spells is Loadable.Failure ->
                SpellsUiState.Failure("Failed to load spells.")

            classes is Loadable.Failure ->
                SpellsUiState.Failure("Failed to load spellcasting classes.")

            else ->
                SpellsUiState.Loading
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SpellsUiState.Loading)

    fun onQueryChanged(query: String) {
        _state.update { it.copy(query = query) }
    }

    fun onFiltersClick() {
        _state.update { it.copy(showFilterDialog = true) }
    }

    fun onFavoritesToggled() {
        _state.update { it.copy(showFavoriteOnly = !it.showFavoriteOnly) }
    }

    fun onFiltersSubmit(classes: Set<EntityRef>) {
        _state.update { it.copy(showFilterDialog = false, characterClasses = classes) }
    }

    fun onFiltersCancel() {
        _state.update { it.copy(showFilterDialog = false, characterClasses = emptySet()) }
    }

    /**
     * Observes spells using the current UI filters state.
     *
     * This flow derives a stable stream of filters (query + class filters + favorites-only flag)
     * and switches the underlying spells observation whenever any of those filters change.
     *
     * Behavior:
     * - Query is normalized via [String.trim] and deduplicated via [distinctUntilChanged].
     * - Query changes are debounced:
     *   - blank query emits immediately (0 ms delay), so clearing the search field instantly shows all spells.
     *   - non-blank query emits after 350 ms of inactivity, preventing a search on every keystroke.
     * - Class and favorites filters are *not* debounced; they apply immediately.
     * - [flatMapLatest] ensures that when filters change, the previous observation is cancelled and replaced,
     *   so only the latest filters drive the result stream.
     *
     * @return A flow emitting a [Loadable] list of [Spell] that reflects the current filters.
     */
    private fun observeSpellsUsingFilters(): Flow<Loadable<List<Spell>>> {
        return combine(
            _state.map { it.query.trim() }.distinctUntilChanged().debounce { if (it.isBlank()) 0L else 350L },
            _state.map { it.characterClasses }.distinctUntilChanged(),
            _state.map { it.showFavoriteOnly }.distinctUntilChanged(),
        ) { query, classes, favoriteOnly -> Filters(query, classes, favoriteOnly) }
            .distinctUntilChanged()
            .flatMapLatest { observeSpells(it.query, it.classes, it.favoritesOnly) }
    }
}
