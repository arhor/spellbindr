package com.github.arhor.spellbindr.ui.feature.compendium.spelldetails

import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.usecase.ObserveSpellDetailsUseCase
import com.github.arhor.spellbindr.domain.usecase.ToggleFavoriteSpellUseCase
import com.github.arhor.spellbindr.utils.Logger.Companion.createLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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

    private val _effects = MutableSharedFlow<SpellDetailsEffect>()
    val effects: SharedFlow<SpellDetailsEffect> = _effects.asSharedFlow()
    private val spellId: String? = savedStateHandle.get<String>("spellId")

    val uiState: StateFlow<SpellDetailsUiState> =
        spellId?.let { id ->
            observeSpellDetails(id)
                .map {
                    when (it) {
                        is Loadable.Loading -> {
                            SpellDetailsUiState.Loading
                        }

                        is Loadable.Content -> {
                            SpellDetailsUiState.Content(it.data.spell, it.data.isFavorite)
                        }

                        is Loadable.Failure -> {
                            SpellDetailsUiState.Failure(it.errorMessage ?: "Could not load spell.")
                        }
                    }
                }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SpellDetailsUiState.Loading)
        } ?: MutableStateFlow(SpellDetailsUiState.Failure("Missing spell id."))

    fun dispatch(intent: SpellDetailsIntent) {
        when (intent) {
            SpellDetailsIntent.ToggleFavorite -> toggleFavorite()
        }
    }

    private fun toggleFavorite() {
        when (val currState = uiState.value) {
            is SpellDetailsUiState.Content -> {
                viewModelScope.launch {
                    runCatching { toggleFavoriteSpell(currState.spell.id) }
                        .onFailure { throwable ->
                            _effects.emit(
                                SpellDetailsEffect.ShowMessage(
                                    throwable.message ?: "Unable to update favorite",
                                ),
                            )
                        }
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
