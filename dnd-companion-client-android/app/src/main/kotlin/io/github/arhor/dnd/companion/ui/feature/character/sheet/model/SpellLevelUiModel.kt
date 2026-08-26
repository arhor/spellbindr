package io.github.arhor.dnd.companion.ui.feature.character.sheet.model

import androidx.compose.runtime.Immutable

@Immutable
data class SpellLevelUiModel(
    val level: Int,
    val spells: List<CharacterSpellUiModel>,
)
