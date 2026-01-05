package com.github.arhor.spellbindr.ui.feature.compendium.spells.details

import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.usecase.ObserveSpellDetailsUseCase
import com.github.arhor.spellbindr.domain.usecase.ToggleFavoriteSpellUseCase
import com.github.arhor.spellbindr.ui.navigation.AppDestination
import com.github.arhor.spellbindr.utils.Logger.Companion.createLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Spell Details screen.
 *
 * Manages loading spell data by ID and handling favorite toggling.
 */
@Stable
@HiltViewModel
class SpellDetailsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val observeSpellDetails: ObserveSpellDetailsUseCase,
    private val toggleFavoriteSpell: ToggleFavoriteSpellUseCase,
) : ViewModel() {

    val uiState: StateFlow<SpellDetailsUiState> =
        savedStateHandle
            .toRoute<AppDestination.SpellDetails>()
            .let { observeSpellDetails(it.spellId) }
            .map {
                when (it) {
                    is Loadable.Loading -> SpellDetailsUiState.Loading
                    is Loadable.Ready -> SpellDetailsUiState.Content(it.data.spell, it.data.isFavorite)
                    is Loadable.Error -> SpellDetailsUiState.Error(it.errorMessage ?: "Could not load spell.")
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SpellDetailsUiState.Loading)

    fun toggleFavorite() {
        when (val currState = uiState.value) {
            is SpellDetailsUiState.Content -> {
                viewModelScope.launch {
                    toggleFavoriteSpell(currState.spell.id)
                }
            }

            else -> {
                logger.debug { "An attempt to toggle favorite spell in $currState state was made." }
            }
        }
    }

    companion object {
        private val logger = createLogger()
    }
}
