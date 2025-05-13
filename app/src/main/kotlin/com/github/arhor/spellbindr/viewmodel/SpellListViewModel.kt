package com.github.arhor.spellbindr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.data.model.Spell
import com.github.arhor.spellbindr.data.model.SpellList
import com.github.arhor.spellbindr.data.repository.SpellListRepository
import com.github.arhor.spellbindr.data.repository.SpellRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SpellListViewModel @Inject constructor(
    private val repository: SpellListRepository,
    private val spellRepository: SpellRepository
) : ViewModel() {
    val state: StateFlow<SpellList> = repository.spellListsFlow
        .map { lists -> lists.find { it.name == "Favorites" } ?: SpellList.EMPTY }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SpellList.EMPTY)

    fun getAllSpells(): List<Spell> = spellRepository.getAllSpells()

    fun isFavorite(spellName: String?): Boolean = if (spellName.isNullOrBlank()) {
        false
    } else {
        spellName in state.value.spellNames
    }

    fun toggleFavorite(spellName: String) {
        val current = state.value
        val currentNames = current.spellNames.toMutableSet()
        val updatedNames = if (spellName in currentNames) {
            currentNames - spellName
        } else {
            currentNames + spellName
        }
        val updatedList = SpellList(name = "Favorites", spellNames = updatedNames.toList())
        viewModelScope.launch {
            repository.updateSpellList(updatedList)
        }
    }

    fun getFavoriteSpells(): List<Spell> {
        val allSpells = getAllSpells()
        val favoriteNames = state.value.spellNames.toSet()
        return allSpells.filter { it.name in favoriteNames }
    }
} 
