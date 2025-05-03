package com.github.arhor.spellbindr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.data.model.Character
import com.github.arhor.spellbindr.repository.CharacterRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CharacterViewModel(
    private val repo: CharacterRepository,
) : ViewModel() {
    val characters =
        repo.getCharacters()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun addSampleCharacter() {
        viewModelScope.launch {
            repo.addCharacter(
                Character(
                    name = "Aragorn",
                    classType = "Ranger",
                    level = 5,
                )
            )
        }
    }
}
