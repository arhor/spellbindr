package io.github.arhor.dnd.companion.ui.feature.character.list

import io.github.arhor.dnd.companion.ui.feature.character.list.model.CharacterListItem
import io.github.arhor.dnd.companion.ui.feature.character.list.model.CreateCharacterMode

sealed interface CharactersListEffect {
    data class CharacterSelected(val character: CharacterListItem) : CharactersListEffect
    data class CreateCharacterSelected(val mode: CreateCharacterMode) : CharactersListEffect
}
