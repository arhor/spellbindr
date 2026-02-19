package com.github.arhor.spellbindr.ui.feature.character.list

import com.github.arhor.spellbindr.ui.feature.character.list.model.CharacterListItem
import com.github.arhor.spellbindr.ui.feature.character.list.model.CreateCharacterMode

/**
 * Represents user intents for the Characters List screen.
 */
sealed interface CharactersListIntent {
    /**
     * Intent emitted when a character card is selected.
     */
    data class SelectCharacterClicked(val character: CharacterListItem) : CharactersListIntent

    /**
     * Intent emitted when a create character mode is chosen.
     */
    data class CreateCharacterClicked(val mode: CreateCharacterMode) : CharactersListIntent
}

/**
 * Dispatch function for [CharactersListIntent] events.
 */
typealias CharactersListDispatch = (CharactersListIntent) -> Unit
