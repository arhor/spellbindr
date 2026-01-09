package com.github.arhor.spellbindr.ui.feature.compendium.spells

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.usecase.ObserveAllSpellsStateUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveFavoriteSpellIdsUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveSpellcastingClassesUseCase
import com.github.arhor.spellbindr.domain.usecase.SearchAndGroupSpellsUseCase
import com.github.arhor.spellbindr.utils.mapWhenReady
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@Stable
@HiltViewModel
class SpellsViewModel @Inject constructor(
    private val observeSpellcastingClasses: ObserveSpellcastingClassesUseCase,
    private val observeAllSpells: ObserveAllSpellsStateUseCase,
    private val observeFavoriteSpellIds: ObserveFavoriteSpellIdsUseCase,
    private val searchAndGroupSpells: SearchAndGroupSpellsUseCase,
) : ViewModel() {

    @Immutable
    data class State(
        val query: String = "",
        val showFavoriteOnly: Boolean = false,
        val showFilterDialog: Boolean = false,
        val castingClasses: List<EntityRef> = emptyList(),
        val currentClasses: List<EntityRef> = emptyList(),
    )

    private val _state = MutableStateFlow(State())

    val uiState: StateFlow<SpellsUiState> = combine(
        _state,
        observeAllSpells(),
        observeFavoriteSpellIds(),
        observeSpellcastingClasses().mapWhenReady { classes -> classes.map { EntityRef(it.id) } },
    ) { state, allSpells, favoriteSpellIds, castingClasses ->
        when {
            allSpells is Loadable.Ready && castingClasses is Loadable.Ready -> {
                val result = searchAndGroupSpells(
                    query = state.query,
                    classes = state.currentClasses,
                    favoriteOnly = state.showFavoriteOnly,
                    allSpells = allSpells.data,
                    favoriteSpellIds = favoriteSpellIds.toSet(),
                )
                SpellsUiState.Content(
                    query = state.query,
                    spells = result.spells,
                    showFavoriteOnly = state.showFavoriteOnly,
                    showFilterDialog = state.showFilterDialog,
                    castingClasses = state.currentClasses,
                    currentClasses = castingClasses.data,
                )
            }

            allSpells is Loadable.Error -> {
                SpellsUiState.Failure("Failed to load spells.")
            }

            castingClasses is Loadable.Error -> {
                SpellsUiState.Failure("Failed to load classes.")
            }

            else -> {
                SpellsUiState.Loading
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SpellsUiState.Loading)

    fun onQueryChanged(query: String) {
        val sanitizedQuery = query.trim()
        _state.update { it.copy(query = sanitizedQuery) }
    }

    fun onFiltersClick() {
        _state.update { it.copy(showFilterDialog = true) }
    }

    fun onFavoritesToggled() {
        _state.update { it.copy(showFavoriteOnly = !it.showFavoriteOnly) }
    }

    fun onFiltersSubmitted(classes: List<EntityRef>) {
        _state.update {
            it.copy(
                showFilterDialog = false,
                currentClasses = classes,
            )
        }
    }

    fun onFiltersCanceled(classes: List<EntityRef>) {
        _state.update {
            it.copy(
                showFilterDialog = false,
                currentClasses = classes,
            )
        }
    }
}
