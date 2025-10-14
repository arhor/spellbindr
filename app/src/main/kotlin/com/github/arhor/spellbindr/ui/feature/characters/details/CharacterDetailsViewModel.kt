package com.github.arhor.spellbindr.ui.feature.characters.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.data.model.Character
import com.github.arhor.spellbindr.data.model.EntityRef
import com.github.arhor.spellbindr.data.repository.CharacterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class CharacterDetailsViewModel @Inject constructor(
    characterRepository: CharacterRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val character: StateFlow<Character> =
        characterRepository.getCharacter(savedStateHandle.get<String>("characterId")!!)
            .filterNotNull()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = Character(
                    id = "",
                    name = "Loading...",
                    race = EntityRef(""),
                    subrace = null,
                    classes = emptyMap(),
                    background = EntityRef(""),
                    abilityScores = emptyMap(),
                    proficiencies = emptySet()
                )
            )
} 
