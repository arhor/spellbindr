package com.github.arhor.spellbindr.ui.screens.spells.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.data.model.Spell
import com.github.arhor.spellbindr.data.repository.SpellRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SpellDetailsViewModel @Inject constructor(
    private val spellRepository: SpellRepository,
) : ViewModel() {

    data class State(
        val spell: Spell? = null,
        val isFavorite: Boolean = false,
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            spellRepository.favoriteSpellsFlow.collect { favoriteSpells ->
                _state.update { state ->
                    state.copy(
                        isFavorite = spellRepository.isFavorite(
                            name = state.spell?.name,
                            favoriteSpells = favoriteSpells,
                        )
                    )
                }
            }
        }
    }

    fun loadSpellByName(name: String?) {
        if (name == null) {
            return
        }
        viewModelScope.launch {
            val spell = spellRepository.findSpellByName(name)
            val isFavorite = spellRepository.isFavorite(name)

            _state.update { it.copy(spell = spell, isFavorite = isFavorite) }
        }
    }

    fun toggleFavorite() {
        val spellName = _state.value.spell?.name ?: return
        viewModelScope.launch {
            spellRepository.toggleFavorite(spellName)
        }
    }
}
