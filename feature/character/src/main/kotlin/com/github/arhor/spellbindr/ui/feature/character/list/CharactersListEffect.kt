package com.github.arhor.spellbindr.ui.feature.character.list

import com.github.arhor.spellbindr.ui.feature.character.list.model.CharacterListItem
import com.github.arhor.spellbindr.ui.feature.character.list.model.CreateCharacterMode

sealed interface CharactersListEffect {
    data class CharacterSelected(val character: CharacterListItem) : CharactersListEffect
    data class CreateCharacterSelected(val mode: CreateCharacterMode) : CharactersListEffect
}
