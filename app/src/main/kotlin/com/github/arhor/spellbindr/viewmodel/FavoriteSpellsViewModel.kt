package com.github.arhor.spellbindr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.data.model.Spell
import com.github.arhor.spellbindr.data.model.SpellList
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
    val spellList: SpellList = SpellList.EMPTY,
    val favoriteSpells: List<Spell> = emptyList()
)

@HiltViewModel
class FavoriteSpellsViewModel @Inject constructor(
    private val favoriteSpellsRepository: FavoriteSpellsRepository,
    private val spellRepository: SpellRepository,
) : ViewModel() {

    val stateFlow: StateFlow<FavoriteSpellsState> = favoriteSpellsRepository.spellListsFlow
        .map { lists -> lists.find { it.name == "Favorites" } ?: SpellList.EMPTY }
        .map { spellList ->
            val allSpells = spellRepository.getAllSpells()
            val favoriteNames = spellList.spellNames.toSet()
            val favoriteSpells = allSpells.filter { it.name in favoriteNames }
            FavoriteSpellsState(spellList = spellList, favoriteSpells = favoriteSpells)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FavoriteSpellsState())

    fun toggleFavorite(spellName: String) {
        val current = stateFlow.value.spellList
        val currentNames = current.spellNames.toMutableSet()
        val updatedNames = if (spellName in currentNames) {
            currentNames - spellName
        } else {
            currentNames + spellName
        }
        val updatedList = SpellList(name = "Favorites", spellNames = updatedNames.toList())
        viewModelScope.launch {
            favoriteSpellsRepository.updateSpellList(updatedList)
        }
    }
}

