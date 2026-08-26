package io.github.arhor.dnd.companion.ui.feature.character.spellpicker

import io.github.arhor.dnd.companion.domain.model.CharacterSpellAssignment
import io.github.arhor.dnd.companion.domain.model.EntityRef

/**
 * Represents user intents for the Character Spell Picker screen.
 */
sealed interface CharacterSpellPickerIntent {
    /**
     * Intent emitted when spellcasting class option is selected.
     */
    data class SpellcastingClassSelected(val value: EntityRef) : CharacterSpellPickerIntent

    /**
     * Intent emitted when source class text input changes.
     */
    data class SourceClassChanged(val value: String) : CharacterSpellPickerIntent

    /**
     * Intent emitted when search query changes.
     */
    data class QueryChanged(val query: String) : CharacterSpellPickerIntent

    /**
     * Intent emitted when favorites-only filter is toggled.
     */
    data object FavoritesToggled : CharacterSpellPickerIntent

    /**
     * Intent emitted when a spell assignment is selected.
     */
    data class SpellClicked(val assignment: CharacterSpellAssignment) : CharacterSpellPickerIntent
}

/**
 * Dispatch function for [CharacterSpellPickerIntent] events.
 */
typealias CharacterSpellPickerDispatch = (CharacterSpellPickerIntent) -> Unit
