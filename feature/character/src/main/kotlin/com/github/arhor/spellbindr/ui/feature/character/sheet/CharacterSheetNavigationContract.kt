package com.github.arhor.spellbindr.ui.feature.character.sheet

data class CharacterSheetRouteArgs(
    val characterId: String,
    val initialName: String? = null,
    val initialSubtitle: String? = null,
)

const val CHARACTER_SPELL_SELECTION_RESULT_KEY = "character_spell_selection_result"
