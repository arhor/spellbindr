package com.github.arhor.spellbindr.ui.feature.compendium.spells.details

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.domain.usecase.ObserveSpellDetailsUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveSpellDetailsUseCase.SpellDetailsState
import com.github.arhor.spellbindr.domain.usecase.ToggleFavoriteSpellUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@Stable
@HiltViewModel
class SpellDetailsViewModel @Inject constructor(
    private val observeSpellDetailsUseCase: ObserveSpellDetailsUseCase,
    private val toggleFavoriteSpellUseCase: ToggleFavoriteSpellUseCase,
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState

        @Immutable
        data class Loaded(
            val spell: Spell,
            val isFavorite: Boolean,
        ) : UiState

        @Immutable
        data class Error(val message: String) : UiState
    }

    private val spellId = MutableStateFlow<String?>(null)

    val state: StateFlow<UiState> = spellId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(UiState.Error("Spell not found."))
            } else {
                observeSpellDetailsUseCase(id).map { detailsState ->
                    when (detailsState) {
                        SpellDetailsState.Loading -> UiState.Loading
                        is SpellDetailsState.Error -> UiState.Error(detailsState.message)
                        is SpellDetailsState.Loaded -> UiState.Loaded(
                            spell = detailsState.spell,
                            isFavorite = detailsState.isFavorite,
                        )
                    }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    fun loadSpell(spellId: String?) {
        this.spellId.value = spellId
    }

    fun toggleFavorite() {
        val currentSpellId = (state.value as? UiState.Loaded)?.spell?.id ?: return
        viewModelScope.launch {
            toggleFavoriteSpellUseCase(currentSpellId)
        }
    }
}
