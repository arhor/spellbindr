package com.github.arhor.spellbindr.ui.feature.compendium.spells.details

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.domain.repository.SpellsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@Stable
@HiltViewModel
class SpellDetailsViewModel @Inject constructor(
    private val spellRepository: SpellsRepository,
) : ViewModel() {

    @Immutable
    data class State(
        val spell: Spell? = null,
        val isFavorite: Boolean = false,
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            spellRepository.favoriteSpellIds
                .distinctUntilChanged()
                .collect {
                _state.update { state ->
                    state.copy(
                        isFavorite = spellRepository.isFavorite(
                            spellId = state.spell?.id,
                        )
                    )
                }
            }
        }
    }

    fun loadSpell(spellId: String?) {
        if (spellId == null || spellId == _state.value.spell?.id) {
            return
        }
        viewModelScope.launch {
            val spell = spellRepository.getSpellById(spellId) ?: return@launch
            val isFavorite = spellRepository.isFavorite(spell.id)

            _state.update { it.copy(spell = spell, isFavorite = isFavorite) }
        }
    }

    fun toggleFavorite() {
        val spellId = _state.value.spell?.id ?: return
        viewModelScope.launch {
            spellRepository.toggleFavorite(spellId)
        }
    }
}
