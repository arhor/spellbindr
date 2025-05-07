package com.github.arhor.spellbindr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.data.model.SpellList
import com.github.arhor.spellbindr.data.repository.SpellListRepository
import com.github.arhor.spellbindr.data.repository.SpellRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SpellListViewModel @Inject constructor(
    private val repository: SpellListRepository,
    private val spellRepository: SpellRepository
) : ViewModel() {
    val spellLists: StateFlow<List<SpellList>> = repository.spellListsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getAllSpells() = spellRepository.getAllSpells()

    fun addSpellList(list: SpellList) {
        viewModelScope.launch {
            repository.addSpellList(list)
        }
    }

    fun updateSpellList(list: SpellList) {
        viewModelScope.launch {
            repository.updateSpellList(list)
        }
    }

    fun deleteSpellList(name: String) {
        viewModelScope.launch {
            repository.deleteSpellList(name)
        }
    }
} 