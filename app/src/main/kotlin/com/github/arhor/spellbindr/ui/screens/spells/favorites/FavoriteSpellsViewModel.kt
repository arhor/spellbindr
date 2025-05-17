package com.github.arhor.spellbindr.ui.screens.spells.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.data.model.Spell
import com.github.arhor.spellbindr.data.repository.SpellRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class FavoriteSpellsState(
    val favoriteSpellNames: List<String> = emptyList(),
    val favoriteSpells: List<Spell> = emptyList(),
)

@HiltViewModel
class FavoriteSpellsViewModel @Inject constructor(
    private val spellRepository: SpellRepository,
) : ViewModel() {

    val state: StateFlow<FavoriteSpellsState> = spellRepository.favoriteSpellsFlow
        .map(::toFavoriteSpellsState)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FavoriteSpellsState())

    private fun toFavoriteSpellsState(favoriteSpellNames: List<String>): FavoriteSpellsState =
        spellRepository
            .findSpells(favoriteSpellNames)
            .let { FavoriteSpellsState(favoriteSpellNames = favoriteSpellNames, favoriteSpells = it) }
}
