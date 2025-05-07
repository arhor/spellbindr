package com.github.arhor.spellbindr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.data.model.Spell
import com.github.arhor.spellbindr.data.repository.SpellRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SpellDetailViewModel @Inject constructor(
    private val spellRepository: SpellRepository
) : ViewModel() {

    private val spell = MutableStateFlow<Spell?>(null)
    val state: StateFlow<Spell?> = spell.asStateFlow()

    fun loadSpellByName(name: String) {
        viewModelScope.launch {
            spell.value = spellRepository.getAllSpells().find { it.name == name }
        }
    }
} 