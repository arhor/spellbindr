package com.github.arhor.spellbindr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.data.model.Spell
import com.github.arhor.spellbindr.data.repository.FavoriteSpellsRepository
import com.github.arhor.spellbindr.data.repository.SpellRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FavoriteSpellsState(
    val favoriteSpellNames: List<String> = emptyList(),
    val favoriteSpells: List<Spell> = emptyList(),
)

@HiltViewModel
class FavoriteSpellsViewModel @Inject constructor(
    private val favoriteSpellsRepository: FavoriteSpellsRepository,
    private val spellRepository: SpellRepository,
) : ViewModel() {

    val stateFlow: StateFlow<FavoriteSpellsState> = favoriteSpellsRepository.favoriteSpellNamesFlow
        .map(::toFavoriteSpellsState)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FavoriteSpellsState())

    fun toggleFavorite(spellName: String) {
        val current = stateFlow.value.favoriteSpellNames
        val currentNames = current.toMutableSet()
        val updatedNames = if (spellName in currentNames) {
            currentNames - spellName
        } else {
            currentNames + spellName
        }
        viewModelScope.launch {
            favoriteSpellsRepository.updateFavoriteSpells(updatedNames.toList())
        }
    }

    private fun toFavoriteSpellsState(favoriteSpellNames: List<String>): FavoriteSpellsState =
        spellRepository
            .findSpells(favoriteSpellNames)
            .let { FavoriteSpellsState(favoriteSpellNames = favoriteSpellNames, favoriteSpells = it) }
}

